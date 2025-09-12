package com.ecom.services;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ecom.common.Util;
import com.ecom.entity.ServiceStatistic;
import com.ecom.repo.ServiceStatisticsRepo;

@Service
public class StatisticsService {

	private Map<String, List<Long>> statsMap = new ConcurrentHashMap<String, List<Long>>();
	private Timestamp from;
	private Timestamp to;
	private volatile boolean copyInProgress = false;

	@Value("${spring.application.name}")
	private String serviceName;
	
	@Autowired
	private ServiceStatisticsRepo serviceStatisticsRepo;

	@Scheduled(cron = "${statistic_cron}", zone = "UTC")
	public void persistData() {
		if (statsMap.keySet().size() > 0) {
			// calculate from and to times
			ZonedDateTime dateTime = ZonedDateTime.now(ZoneId.of("UTC"));
			to = Timestamp.from(dateTime.toInstant());
			dateTime = dateTime.minusMinutes(5);
			from = Timestamp.from(dateTime.toInstant());

			// copy the data and clear map for new data
			copyInProgress = true;
			Map<String, List<Long>> statsMapCopy = new ConcurrentHashMap<String, List<Long>>(statsMap);
			clearStats();
			copyInProgress = false;

			// compute stats and store in db
			Iterator<String> iter = statsMapCopy.keySet().iterator();
			while (iter.hasNext()) {
				String functionName = iter.next();
				List<Long> functionStats = statsMapCopy.get(functionName);
				LongSummaryStatistics longSummaryStatistics = functionStats.stream()
						.collect(Collectors.summarizingLong(l -> l));
				double average = longSummaryStatistics.getAverage();
				double max = longSummaryStatistics.getMax();
				double min = longSummaryStatistics.getMin();
				double count = longSummaryStatistics.getCount();

				ServiceStatistic serviceStatistic = new ServiceStatistic();
				serviceStatistic.setService(serviceName);
				serviceStatistic.setFrom(from);
				serviceStatistic.setTo(to);
				serviceStatistic.setFunctionName(functionName);
				serviceStatistic.setAverage(average);
				serviceStatistic.setMax(max);
				serviceStatistic.setMin(min);
				serviceStatistic.setCount(count);
				serviceStatistic.setStatsKey(Util.generateKey());
				
				serviceStatisticsRepo.save(serviceStatistic);
			}
		}
	}

	private void clearStats() {
		statsMap.clear();
	}

	public void logStat(String serviceName, long durationMillis) {
		// wait for copy to complete
		while (copyInProgress) {
		}
		statsMap.computeIfAbsent(serviceName, s -> new ArrayList<Long>()).add(durationMillis);
	}
}
