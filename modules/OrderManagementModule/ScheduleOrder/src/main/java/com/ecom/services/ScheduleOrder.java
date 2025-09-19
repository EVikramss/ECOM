package com.ecom.services;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

@Service
public class ScheduleOrder {

	private static final Logger LOGGER = LogManager.getLogger(ScheduleOrder.class);

	@Autowired
	private OrderStatusRepo orderStatusRepo;

	@Autowired
	private OrderDataRepo orderDataRepo;

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

		// Create Lambda client
		LambdaClient lambdaClient = LambdaClient.create();

		boolean atLeastOneItemScheduled = false;
		if (entityNode != null && entityNode.trim().length() > 0) {
			Iterator<OrderItemData> iter = itemDataList.iterator();
			while (iter.hasNext()) {
				OrderItemData itemData = iter.next();
				String itemID = itemData.getSku();
				int qty = itemData.getQty();
				String demandId = orderNo;
				String node = entityNode;

				boolean isSuccess = reserveItem(itemID, qty, demandId, node, lambdaClient);
				if (isSuccess) {
					atLeastOneItemScheduled = true;
					itemData.setStatus(StatusEnum.SCHEDULED.getStatus());
				} else {
					itemData.setStatus(StatusEnum.CANCELLED.getStatus());
				}
			}
		}
		lambdaClient.close();
		
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

	/**
	 * Invoke Lambda function to reserve for single item
	 * 
	 * @param itemID
	 * @param qty
	 * @param demandId
	 * @param node
	 * @param lambdaClient 
	 * @return
	 */
	private boolean reserveItem(String itemID, int qty, String demandId, String node, LambdaClient lambdaClient) {
		String functionName = "ScheduleOrder.reserveItem";

		// record time
		Timer.Sample sample = Timer.start(meterRegistry);

		boolean isSuccess = false;

		// form input for a single item
		String requestJson = String.format(
				"{\"OP\":\"ReserveAvailability\",\"ItemID\":\"%s\",\"Node\":\"%s\",\"Qty\":%s,\"Id\": \"%s\"}", itemID,
				node, qty, demandId);
		LOGGER.debug(requestJson);

		// Invoke Lambda
		InvokeRequest request = InvokeRequest.builder().functionName("ECOMINVAvailabilityOp")
				.payload(SdkBytes.fromUtf8String(requestJson)).build();
		InvokeResponse response = lambdaClient.invoke(request);

		// extract response code and check if success
		String responsePayload = response.payload().asUtf8String();
		int statusCode = response.statusCode();
		if(statusCode == 200)
			isSuccess = true;
		
		LOGGER.debug(statusCode);
		LOGGER.debug(responsePayload);

		// get time to execute
		long durationNanos = sample.stop(meterRegistry.timer(functionName));
		long durationMillis = durationNanos / 1_000_000;
		statService.logStat(functionName, durationMillis);

		return isSuccess;
	}
}
