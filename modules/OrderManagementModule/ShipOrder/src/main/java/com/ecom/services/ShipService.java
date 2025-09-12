package com.ecom.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.common.StatusEnum;
import com.ecom.entity.OrderStatus;
import com.ecom.exception.ServiceException;
import com.ecom.repo.OrderStatusRepo;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class ShipService {
	private static final Logger LOGGER = LogManager.getLogger(ShipService.class);

	@Autowired
	private OrderStatusRepo orderStatusRepo;

	@Autowired
	private ShipOrder shipOrder;

	@Autowired
	private MeterRegistry meterRegistry;

	@Autowired
	private StatisticsService statService;

	private ExecutorService executorService = Executors.newFixedThreadPool(2);

	public void executeService() {
		String functionName = "ShipService.executeService";

		// record time
		Timer.Sample sample = Timer.start(meterRegistry);

		// fetch all orders in scheduled status
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
			}
		});

		// get time to execute
		long durationNanos = sample.stop(meterRegistry.timer(functionName));
		long durationMillis = durationNanos / 1_000_000;
		statService.logStat(functionName, durationMillis);
	}

	private List<OrderStatus> getOrderList() {
		List<OrderStatus> workQueueList = orderStatusRepo.findByStatus(StatusEnum.SCHEDULED.getStatus());
		return workQueueList;
	}

	private OrderStatus executeSingleWorkItem(OrderStatus w) {
		try {
			shipOrder.executeOrder(w);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
			throw new ServiceException(w, e.getMessage(), e.getStackTrace(), "ShipService.executeSingleWorkItem");
		}
		return w;
	}
}
