package com.ecom.services;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ecom.common.Util;
import com.ecom.dto.search.OrderSearch;
import com.ecom.entity.OrderData;
import com.ecom.entity.QOrderData;
import com.ecom.enums.OrderNoSearchMatchType;
import com.ecom.enums.QueryJoinType;
import com.ecom.exception.ExceptionCodes;
import com.ecom.exception.ServiceException;
import com.ecom.repo.OrderDataRepo;
import com.querydsl.core.types.dsl.BooleanExpression;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class OrderDataService {

	@Autowired
	private OrderDataRepo orderDataRepo;

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

	/**
	 * If no search params present, just do findAll to return list from db. Else
	 * search with passed parameters
	 * 
	 * @param searchParams
	 * @return
	 */
	public Iterable<OrderData> searchOrder(OrderSearch searchParams, int pageNumber, int pageSize) {
		String functionName = "OrderService.searchOrder";
		Iterable<OrderData> orderList = null;

		try {
			// record time
			Timer.Sample sample = Timer.start(meterRegistry);

			// use dsl if any filter criteria present
			if (searchParams.anyNonBlankSearchParam()) {
				orderList = searchOrderWithDSL(searchParams, pageNumber, pageSize);
			} else {
				// return all orders without filter in paginated manner
				orderList = orderDataRepo.findAll(PageRequest.of(pageNumber, pageSize,
						Sort.by(searchParams.getSortOrder(), searchParams.getSortByAttribute())));
			}

			// get time to execute
			long durationNanos = sample.stop(meterRegistry.timer(functionName));
			long durationMillis = durationNanos / 1_000_000;
			statService.logStat(functionName, durationMillis);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(searchParams, e.getMessage(), e.getStackTrace(), functionName);
		}

		return orderList;
	}

	/**
	 * Create a search query with dsl with various combinations of input params
	 * 
	 * @param searchParams
	 * @return
	 */
	private Iterable<OrderData> searchOrderWithDSL(OrderSearch searchParams, int pageNumber, int pageSize) {
		String functionName = "OrderService.searchOrderWithDSL";
		Iterable<OrderData> orderList = null;

		// record time
		Timer.Sample sample = Timer.start(meterRegistry);

		String orderNo = searchParams.getOrderNo();
		String matchType = searchParams.getMatchType();
		String entity = searchParams.getEntity();
		String fromDate = searchParams.getFromDate();
		String toDate = searchParams.getToDate();

		Timestamp fromTimeStamp = Util.getTimeStampFromDate(fromDate, null);
		Timestamp toTimeStamp = Util.getTimeStampFromDate(toDate, null);

		QOrderData qorderData = QOrderData.orderData;
		BooleanExpression queryExpression = null;
		if (Util.isValidString(orderNo)) {
			if (Util.isValidString(matchType)) {
				if (OrderNoSearchMatchType.Exact.equals(OrderNoSearchMatchType.valueOf(matchType))) {
					queryExpression = Util.appendDSLExpression(queryExpression, qorderData.orderNo.eq(orderNo),
							QueryJoinType.And);
				} else if (OrderNoSearchMatchType.Like.equals(OrderNoSearchMatchType.valueOf(matchType))) {
					queryExpression = Util.appendDSLExpression(queryExpression, qorderData.orderNo.like(orderNo),
							QueryJoinType.And);
				}
			} else {
				// if no match type, assume exact
				queryExpression = Util.appendDSLExpression(queryExpression, qorderData.orderNo.eq(orderNo),
						QueryJoinType.And);
			}
		}

		if (Util.isValidString(entity)) {
			queryExpression = Util.appendDSLExpression(queryExpression, qorderData.entity.eq(entity),
					QueryJoinType.And);
		}

		if (fromTimeStamp != null) {
			queryExpression = Util.appendDSLExpression(queryExpression, qorderData.orderDate.goe(fromTimeStamp),
					QueryJoinType.And);
		}

		if (toTimeStamp != null) {
			queryExpression = Util.appendDSLExpression(queryExpression, qorderData.orderDate.loe(toTimeStamp),
					QueryJoinType.And);
		}

		orderList = orderDataRepo.findAll(queryExpression, PageRequest.of(pageNumber, pageSize,
				Sort.by(searchParams.getSortOrder(), searchParams.getSortByAttribute())));

		// get time to execute
		long durationNanos = sample.stop(meterRegistry.timer(functionName));
		long durationMillis = durationNanos / 1_000_000;
		statService.logStat(functionName, durationMillis);

		return orderList;
	}
}
