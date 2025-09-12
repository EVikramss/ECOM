package com.ecom.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.entity.OrderData;
import com.ecom.exception.ExceptionCodes;
import com.ecom.exception.ServiceException;
import com.ecom.repo.OrderDataRepo;
import com.ecom.repo.OrderStatusRepo;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class OrderService {

	@Autowired
	private OrderDataRepo orderDataRepo;
	
	@Autowired
	private OrderStatusRepo orderStatusRepo;

	@Autowired
	private MeterRegistry meterRegistry;

	@Autowired
	private StatisticsService statService;

	public OrderData getOrder(String orderNo) {
		String functionName = "OrderService.getOrder";
		OrderData data;

		try {
			// record time
			Timer.Sample sample = Timer.start(meterRegistry);

			// fetch order
			data = orderDataRepo.findByOrderNo(orderNo);

			// if order doesnt exist throw error
			if (data == null)
				throw new Exception(ExceptionCodes.ERR003);

			// get time to execute
			long durationNanos = sample.stop(meterRegistry.timer(functionName));
			long durationMillis = durationNanos / 1_000_000;
			statService.logStat(functionName, durationMillis);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(orderNo, e.getMessage(), e.getStackTrace(), functionName);
		}

		return data;
	}
}
