package com.ecom.services;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ecom.dto.mapper.OrderDataMapper;
import com.ecom.exception.ServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Service
public class QueueService {

	@Autowired
	private SqsClient sqsClient;

	@Autowired
	private OrderService orderService;

	@Autowired
	private Validator validator;

	@Autowired
	private OrderDataMapper orderDataMapper;

	@Autowired
	private ErrorService errorService;

	@Value("${CreateOrderQURL}")
	private String queueUrl;

	private final ExecutorService executorService = Executors.newFixedThreadPool(2);

	@PostConstruct
	public void startPolling() {
		for (int i = 0; i < 2; i++) {
			executorService.submit(() -> pollMessages());
		}
	}

	private void pollMessages() {
		LOGGER.info("Listening on queue " + queueUrl + " with thread " + Thread.currentThread().getName());
		
		while (true) {
			ReceiveMessageRequest request = ReceiveMessageRequest.builder().queueUrl(queueUrl).waitTimeSeconds(20)
					.maxNumberOfMessages(10).build();

			List<Message> messages = sqsClient.receiveMessage(request).messages();

			for (Message message : messages) {
				// in case of unhandled exception from processMessage message is not deleted q
				// let the exception stop the thread, instead of polling same message again and
				// going into a loop
				processMessage(message);
				deleteMessage(message);
			}
		}
	}

	private void processMessage(Message message) {
		String msgBody = message.body();
		processQMessage(msgBody);
	}

	public void processQMessage(String msgBody) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			com.ecom.dto.OrderData inputData = mapper.readValue(msgBody, com.ecom.dto.OrderData.class);
			Set<ConstraintViolation<com.ecom.dto.OrderData>> violations = validator.validate(inputData);
			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(violations);
			}
			orderService.createOrder(orderDataMapper.convertFromDTO(inputData));
		} catch (ServiceException e) {
			e.printStackTrace();
			errorService.persistData(e);
		} catch (Exception e) {
			e.printStackTrace();
			errorService.persistData(e, msgBody);
		}
	}

	private void deleteMessage(Message message) {
		DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder().queueUrl(queueUrl)
				.receiptHandle(message.receiptHandle()).build();
		sqsClient.deleteMessage(deleteRequest);
	}
}


