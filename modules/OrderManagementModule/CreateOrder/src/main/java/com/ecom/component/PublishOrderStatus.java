package com.ecom.component;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ecom.common.StatusEnum;
import com.ecom.entity.OrderData;
import com.ecom.services.StatisticsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
public class PublishOrderStatus {

	@Autowired
	private SnsClient snsClient;

	@Autowired
	private MeterRegistry meterRegistry;

	@Autowired
	private StatisticsService statService;

	@Value("${OrderStatusTopicARN}")
	private String topicArn;

	public void publish(OrderData orderData, StatusEnum status) throws JsonProcessingException {
		String functionName = "PublishOrderStatus.publish";

		// record time
		Timer.Sample sample = Timer.start(meterRegistry);

		// form status update
		List<ItemData> itemData = orderData.getItemData().stream().map(i -> {
			return new ItemData(i.getSku(), i.getQty(), status.getStatus());
		}).collect(Collectors.toList());

		OrderStatus orderStatus = new OrderStatus(orderData.getOrderNo(), itemData,
				new CustomerContact(orderData.getCustomerContact().getSub()));

		// convert to json
		ObjectMapper mapper = new ObjectMapper();
		String message = mapper.writer().writeValueAsString(orderStatus);
		
		// post to topic
		PublishRequest request = PublishRequest.builder().topicArn(topicArn).message(message).build();
		snsClient.publish(request);

		// get time to execute
		long durationNanos = sample.stop(meterRegistry.timer(functionName));
		long durationMillis = durationNanos / 1_000_000;
		statService.logStat(functionName, durationMillis);
	}
	
	record CustomerContact(String sub) {
	}

	record OrderStatus(String orderNo, List<ItemData> itemData, CustomerContact customerContact) {
	}

	record ItemData(String sku, int qty, int status) {
	}
}
