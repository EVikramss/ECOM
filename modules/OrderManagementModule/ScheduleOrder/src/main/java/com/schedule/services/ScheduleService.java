package com.schedule.services;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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

import com.schedule.common.EntityNode;
import com.schedule.common.StatusEnum;
import com.schedule.entity.OrderData;
import com.schedule.entity.OrderItemData;
import com.schedule.entity.OrderStatus;
import com.schedule.repo.OrderDataRepo;
import com.schedule.repo.OrderStatusRepo;

import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;

@Service
public class ScheduleService {
	private static final Logger LOGGER = LogManager.getLogger(ScheduleService.class);

	@Autowired
	private OrderStatusRepo orderStatusRepo;

	@Autowired
	private OrderDataRepo orderDataRepo;

	@Autowired
	private RestTemplate restTemplate;

	private ExecutorService executorService = Executors.newFixedThreadPool(2);

	public void executeService() {
		// fetch all orders in created status
		List<OrderStatus> workQueueList = orderStatusRepo.findByStatus(StatusEnum.CREATED.getStatus());

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
	}

	@Transactional
	private OrderStatus executeSingleWorkItem(OrderStatus w) {
		try {
			executeOrder(w.getOrderNo(), w);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return w;
	}

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
	public void executeOrder(String string, OrderStatus w) throws Exception {
		
		try {
			// lock order status record
			w = orderStatusRepo.lockRecord(w.getOrderStatusKey());	
		} catch(PessimisticLockException | LockTimeoutException e) {
			throw new Exception("Couldnt lock order");
		}
		
		
		String orderNo = w.getOrderNo();
		OrderData orderData = orderDataRepo.findById(orderNo).orElseThrow();

		String entity = orderData.getEntity();
		Set<OrderItemData> itemDataList = orderData.getItemData();
		String entityNode = EntityNode.valueOf(entity).getNodeForEntity();

		if (entityNode != null && entityNode.trim().length() > 0) {
			itemDataList.stream().forEach(id -> {
				String itemID = id.getItemid();
				int qty = id.getQty();
				String demandId = orderNo;
				String node = entityNode;

				boolean isSuccess = reserveItem(itemID, qty, demandId, node);
				if (isSuccess) {
					id.setStatus(StatusEnum.SCHEDULED.getStatus());
				} else {
					id.setStatus(StatusEnum.CANCELLED.getStatus());
				}
			});
		}
		
		orderDataRepo.save(orderData);
		w.setStatus(StatusEnum.SCHEDULED.getStatus());
		orderStatusRepo.save(w);
	}

	private boolean reserveItem(String itemID, int qty, String demandId, String node) {
		String envUrl = System.getenv("INVAVLURL");
		boolean isSuccess = false;

		if (envUrl != null && envUrl.trim().length() > 0) {
			String requestJson = String.format(
					"{\"OP\":\"ReserveAvailability\",\"ItemID\":\"%s\",\"Node\":\"%s\",\"Qty\":%s,\"Id\": \"%s\"}",
					itemID, node, qty, demandId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
			ResponseEntity<String> response = restTemplate.postForEntity(envUrl, entity, String.class);

			if (response.getStatusCode().is2xxSuccessful())
				isSuccess = true;
		}

		return isSuccess;
	}
}
