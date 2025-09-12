package com.ecom.services;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecom.common.StatusEnum;
import com.ecom.common.Util;
import com.ecom.entity.OrderData;
import com.ecom.entity.OrderItemData;
import com.ecom.entity.OrderStatus;
import com.ecom.exception.ExceptionCodes;
import com.ecom.exception.ServiceException;
import com.ecom.repo.OrderDataRepo;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class OrderService {

	@Autowired
	private OrderDataRepo orderDataRepo;

	@Autowired
	private MeterRegistry meterRegistry;

	@Autowired
	private StatisticsService statService;

	@Transactional
	public OrderData createOrder(OrderData orderData) {
		String functionName = "OrderService.createOrder";

		try {
			// record time
			Timer.Sample sample = Timer.start(meterRegistry);

			// check if order no blank
			String orderNo = orderData.getOrderNo();
			if (!Util.isValidString(orderNo))
				throw new Exception(ExceptionCodes.ERR001);

			// check if order no already exists - and if so throw error
			OrderData existingOrder = orderDataRepo.findByOrderNo(orderNo);
			if (existingOrder != null)
				throw new Exception(ExceptionCodes.ERR002);

			// set current date time in UTC
			ZonedDateTime dateTime = ZonedDateTime.now(ZoneId.of("UTC"));
			orderData.setOrderDate(Timestamp.from(dateTime.toInstant()));
			orderData.setOrderKey(Util.generateKey());

			// set address and customer contact keys
			orderData.getAddress().setAddresskey(Util.generateKey());
			orderData.getCustomerContact().setCustomerContactkey(Util.generateKey());

			// set item data keys & line no's
			Set<OrderItemData> itemDataSet = orderData.getItemData();
			Iterator<OrderItemData> iter = itemDataSet.iterator();
			int counter = 1;
			BigInteger orderItemKey = Util.generateKey();
			while (iter.hasNext()) {
				OrderItemData itemData = iter.next();
				itemData.setLineno(counter);
				itemData.setOrderData(orderData);
				itemData.setOrderItemKey(orderItemKey);
				orderItemKey = orderItemKey.add(BigInteger.ONE);
				counter++;
			}

			// store order status
			OrderStatus os = orderData.getOrderStatus();
			os.setOrderKey(orderData.getOrderKey());
			os.setStatus(StatusEnum.CREATED.getStatus());
			os.setOrderStatusKey(Util.generateKey());

			// save order
			orderData = orderDataRepo.save(orderData);

			// get time to execute
			long durationNanos = sample.stop(meterRegistry.timer(functionName));
			long durationMillis = durationNanos / 1_000_000;
			statService.logStat(functionName, durationMillis);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(orderData, e.getMessage(), e.getStackTrace(), functionName);
		}

		return orderData;
	}
}
