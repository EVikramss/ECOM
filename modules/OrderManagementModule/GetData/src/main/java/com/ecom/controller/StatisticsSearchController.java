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

import com.ecom.dto.mapper.ServiceStatisticsMapper;
import com.ecom.dto.search.StatsSearch;
import com.ecom.entity.ServiceStatistic;
import com.ecom.services.StatisticsService;

@RestController
public class StatisticsSearchController {

	@Autowired
	private StatisticsService statisticsService;

	@Autowired
	private ServiceStatisticsMapper serviceStatisticsMapper;

	@PostMapping("/searchStats")
	public List<com.ecom.dto.ServiceStatistic> searchOrder(@RequestBody StatsSearch searchParams,
			@RequestParam int pageNumber, @RequestParam int pageSize) {
		Iterable<ServiceStatistic> data = statisticsService.getStatsWithServiceName(searchParams, pageNumber, pageSize);
		return serviceStatisticsMapper.convertToDTO(data);
	}

	@GetMapping("/getStatServiceNames")
	public List<String> getStatServiceNames() {
		List<String> serviceList = statisticsService.getServiceNames();
		return serviceList;
	}
}
