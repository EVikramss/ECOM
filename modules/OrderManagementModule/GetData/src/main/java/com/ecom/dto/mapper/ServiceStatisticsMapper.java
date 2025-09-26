package com.ecom.dto.mapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ecom.entity.ServiceStatistic;

@Component
public class ServiceStatisticsMapper {

	public com.ecom.dto.ServiceStatistic convertToDTO(ServiceStatistic inputData) {
		com.ecom.dto.ServiceStatistic serviceStatistic = new com.ecom.dto.ServiceStatistic();
		serviceStatistic.setStatsKey(inputData.getStatsKey());
		serviceStatistic.setAverage(inputData.getAverage());
		serviceStatistic.setCount(inputData.getCount());
		serviceStatistic.setFromTime(inputData.getFrom());
		serviceStatistic.setFunctionName(inputData.getFunctionName());
		serviceStatistic.setMax(inputData.getMax());
		serviceStatistic.setMin(inputData.getMin());
		serviceStatistic.setService(inputData.getService());
		serviceStatistic.setToTime(inputData.getTo());
		return serviceStatistic;
	}

	public List<com.ecom.dto.ServiceStatistic> convertToDTO(Iterable<ServiceStatistic> inputData) {

		List<com.ecom.dto.ServiceStatistic> output = new ArrayList<com.ecom.dto.ServiceStatistic>();

		Iterator<ServiceStatistic> iter = inputData.iterator();
		while (iter.hasNext()) {
			ServiceStatistic data = iter.next();
			com.ecom.dto.ServiceStatistic outputData = convertToDTO(data);
			output.add(outputData);
		}

		return output;
	}
}
