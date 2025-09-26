package com.ecom.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.dto.search.OrderSearch;
import com.ecom.dto.mapper.OrderDataMapper;
import com.ecom.entity.OrderData;
import com.ecom.services.OrderDataService;

@RestController
public class OrderSearchController {

	@Autowired
	private OrderDataService orderService;

	@Autowired
	private OrderDataMapper orderDataMapper;

	@PostMapping("/searchOrder")
	public List<com.ecom.dto.OrderData> searchOrder(@RequestBody OrderSearch searchParams, @RequestParam int pageNumber,
			@RequestParam int pageSize) {
		Iterable<OrderData> data = orderService.searchOrder(searchParams, pageNumber, pageSize);
		return orderDataMapper.convertToDTO(data, false);
	}

	@GetMapping("/getOrder")
	public com.ecom.dto.OrderData getOrder(@RequestParam String orderNo) {
		OrderData data = orderService.getOrder(orderNo);
		return orderDataMapper.convertToDTO(data, true);
	}
}
