package com.ecom.services;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.ecom.common.EntityNode;
import com.ecom.common.StatusEnum;
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
public class ScheduleOrder {
	
	private static final Logger LOGGER = LogManager.getLogger(ScheduleOrder.class);

	@Autowired
	private OrderStatusRepo orderStatusRepo;

	@Autowired
	private OrderDataRepo orderDataRepo;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private MeterRegistry meterRegistry;

	@Autowired
	private StatisticsService statService;

	/**
	 * In a transaction, attempt to reserve all items one by one. 1. Move the items
	 * reserved to scheduled status 2. For items with no inventory, move to
	 * cancelled status
	 * 
	 * @param string
	 * @param w
	 * @throws Exception
	 */
	@Transactional
	public void executeOrder(OrderStatus w) throws Exception {
		String functionName = "ScheduleOrder.executeOrder";

		// record time
		Timer.Sample sample = Timer.start(meterRegistry);

		try {
			// lock order status record
			w = orderStatusRepo.lockRecord(w.getOrderStatusKey());
		} catch (PessimisticLockException | LockTimeoutException e) {
			throw new Exception("Couldnt lock order");
		}

		// check if status still created
		if (w.getStatus() != StatusEnum.CREATED.getStatus())
			return;

		BigInteger orderKey = w.getOrderKey();
		OrderData orderData = orderDataRepo.findById(orderKey).orElseThrow();

		String entity = orderData.getEntity();
		String orderNo = orderData.getOrderNo();
		Set<OrderItemData> itemDataList = orderData.getItemData();
		String entityNode = EntityNode.valueOf(entity).getNode();

		boolean atLeastOneItemScheduled = false;
		if (entityNode != null && entityNode.trim().length() > 0) {
			Iterator<OrderItemData> iter = itemDataList.iterator();
			while (iter.hasNext()) {
				OrderItemData itemData = iter.next();
				String itemID = itemData.getSku();
				int qty = itemData.getQty();
				String demandId = orderNo;
				String node = entityNode;

				boolean isSuccess = reserveItem(itemID, qty, demandId, node);
				if (isSuccess) {
					atLeastOneItemScheduled = true;
					itemData.setStatus(StatusEnum.SCHEDULED.getStatus());
				} else {
					itemData.setStatus(StatusEnum.CANCELLED.getStatus());
				}
			}
		}

		if (atLeastOneItemScheduled)
			orderData.getOrderStatus().setStatus(StatusEnum.SCHEDULED.getStatus());
		else
			orderData.getOrderStatus().setStatus(StatusEnum.CANCELLED.getStatus());
		orderDataRepo.save(orderData);

		// get time to execute
		long durationNanos = sample.stop(meterRegistry.timer(functionName));
		long durationMillis = durationNanos / 1_000_000;
		statService.logStat(functionName, durationMillis);
	}

	private boolean reserveItem(String itemID, int qty, String demandId, String node) {
		String functionName = "ScheduleOrder.reserveItem";

		// record time
		Timer.Sample sample = Timer.start(meterRegistry);

		String envUrl = System.getenv("INVAVLURL");
		boolean isSuccess = false;
		LOGGER.debug(envUrl);

		if (envUrl != null && envUrl.trim().length() > 0) {
			String requestJson = String.format(
					"{\"OP\":\"ReserveAvailability\",\"ItemID\":\"%s\",\"Node\":\"%s\",\"Qty\":%s,\"Id\": \"%s\"}",
					itemID, node, qty, demandId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
			
			LOGGER.debug(requestJson);
			
			ResponseEntity<String> response = restTemplate.postForEntity(envUrl, entity, String.class);

			if (response.getStatusCode().is2xxSuccessful())
				isSuccess = true;

			LOGGER.debug(response.getStatusCode());
			LOGGER.debug(response.getBody());
		}

		// get time to execute
		long durationNanos = sample.stop(meterRegistry.timer(functionName));
		long durationMillis = durationNanos / 1_000_000;
		statService.logStat(functionName, durationMillis);

		return isSuccess;
	}
}
