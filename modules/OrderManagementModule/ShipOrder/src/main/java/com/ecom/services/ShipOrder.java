package com.ecom.services;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecom.common.StatusEnum;
import com.ecom.component.PublishOrderStatus;
import com.ecom.entity.OrderData;
import com.ecom.entity.OrderItemData;
import com.ecom.entity.OrderStatus;
import com.ecom.repo.OrderDataRepo;
import com.ecom.repo.OrderStatusRepo;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;

@Service
public class ShipOrder {

	@Autowired
	private OrderStatusRepo orderStatusRepo;

	@Autowired
	private OrderDataRepo orderDataRepo;

	@Autowired
	private MeterRegistry meterRegistry;

	@Autowired
	private StatisticsService statService;
	
	@Autowired
	private PublishOrderStatus publishOrderStatus;

	@Transactional
	public void executeOrder(OrderStatus w) throws Exception {
		String functionName = "ShipOrder.executeOrder";

		// record time
		Timer.Sample sample = Timer.start(meterRegistry);

		try {
			// lock order status record
			w = orderStatusRepo.lockRecord(w.getOrderStatusKey());
		} catch (PessimisticLockException | LockTimeoutException e) {
			throw new Exception("Couldnt lock order");
		}

		// check if status still created
		if (w.getStatus() != StatusEnum.SCHEDULED.getStatus())
			return;

		BigInteger orderKey = w.getOrderKey();
		OrderData orderData = orderDataRepo.findById(orderKey).orElseThrow();

		// move line and order status
		Set<OrderItemData> itemDataList = orderData.getItemData();
		Iterator<OrderItemData> iter = itemDataList.iterator();
		while (iter.hasNext()) {
			OrderItemData itemData = iter.next();
			int itemStatus = itemData.getStatus();
			if (itemStatus == StatusEnum.SCHEDULED.getStatus()) {
				itemData.setStatus(StatusEnum.SHIPPED.getStatus());
			}
		}
		
		orderData.getOrderStatus().setStatus(StatusEnum.SHIPPED.getStatus());
		orderDataRepo.save(orderData);
		
		// publish order status
		publishOrderStatus.publish(orderData);

		// get time to execute
		long durationNanos = sample.stop(meterRegistry.timer(functionName));
		long durationMillis = durationNanos / 1_000_000;
		statService.logStat(functionName, durationMillis);
	}
}
