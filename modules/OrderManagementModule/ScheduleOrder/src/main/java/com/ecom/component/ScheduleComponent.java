package com.ecom.component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ecom.common.StatusEnum;
import com.ecom.entity.OrderStatus;
import com.ecom.exception.ServiceException;
import com.ecom.repo.OrderStatusRepo;
import com.ecom.services.ErrorService;
import com.ecom.services.ScheduleOrder;
import com.ecom.services.StatisticsService;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Component
public class ScheduleComponent {
	private static final Logger LOGGER = LogManager.getLogger(ScheduleComponent.class);

	@Autowired
	private OrderStatusRepo orderStatusRepo;

	@Autowired
	private ScheduleOrder scheduleOrder;

	@Autowired
	private MeterRegistry meterRegistry;

	@Autowired
	private StatisticsService statService;

	@Autowired
	private ErrorService errorService;

	private ExecutorService executorService = Executors.newFixedThreadPool(2);

	public void executeService() {
		String functionName = "ScheduleService.executeService";

		// record time
		Timer.Sample sample = Timer.start(meterRegistry);

		// fetch all orders in created status
		List<OrderStatus> workQueueList = getOrderList();

		// execute work items parallely
		List<CompletableFuture<OrderStatus>> resultList = workQueueList.stream()
				.map(w -> CompletableFuture.supplyAsync(() -> executeSingleWorkItem(w), executorService))
				.collect(Collectors.toList());

		// wait for all results
		resultList.stream().forEach(r -> {
			try {
				r.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			} catch (ServiceException e) {
				e.printStackTrace();
				errorService.persistData(e);
			} catch (Exception e) {
				e.printStackTrace();
				errorService.persistData(e);
			}
		});

		// get time to execute
		long durationNanos = sample.stop(meterRegistry.timer(functionName));
		long durationMillis = durationNanos / 1_000_000;
		statService.logStat(functionName, durationMillis);
	}

	private List<OrderStatus> getOrderList() {
		List<OrderStatus> workQueueList = orderStatusRepo.findByStatus(StatusEnum.CREATED.getStatus());
		return workQueueList;
	}

	private OrderStatus executeSingleWorkItem(OrderStatus w) {
		try {
			scheduleOrder.executeOrder(w);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new ServiceException(w, e.getMessage(), e.getStackTrace(), "ScheduleService.executeSingleWorkItem");
		}
		return w;
	}
}
