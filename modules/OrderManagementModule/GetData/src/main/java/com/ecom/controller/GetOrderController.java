package com.ecom.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.dto.mapper.OrderDataMapper;
import com.ecom.entity.OrderData;
import com.ecom.services.OrderService;

@RestController
public class GetOrderController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderDataMapper orderDataMapper;

	@GetMapping("/getOrder")
	public com.ecom.dto.OrderData getOrder(@RequestParam(name = "orderNo") String orderNo) {
		OrderData data = orderService.getOrder(orderNo);
		return orderDataMapper.convertToDTO(data);
	}
}
