package com.ecom.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.dto.mapper.OrderDataMapper;
import com.ecom.entity.OrderData;
import com.ecom.services.OrderService;

import jakarta.validation.Valid;

@RestController
public class OrderController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderDataMapper orderDataMapper;

	@PostMapping("/createOrder")
	public String createOrder(@Valid @RequestBody com.ecom.dto.OrderData inputData) {
		OrderData orderData = orderService.createOrder(orderDataMapper.convertFromDTO(inputData));
		return orderData.getOrderNo();
	}
}
