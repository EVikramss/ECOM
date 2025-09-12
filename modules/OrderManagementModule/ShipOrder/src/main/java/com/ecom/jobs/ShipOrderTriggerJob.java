package com.ecom.jobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ecom.services.ShipService;

@Component
public class ShipOrderTriggerJob implements Job {

	private static final Logger LOGGER = LogManager.getLogger(ShipOrderTriggerJob.class);

	@Autowired
	private ShipService service;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOGGER.debug("ShipOrderTriggerJob.debug");
		service.executeService();
	}

}
