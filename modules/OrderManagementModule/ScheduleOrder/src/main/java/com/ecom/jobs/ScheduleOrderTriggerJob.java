package com.ecom.jobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ecom.component.ScheduleComponent;


@Component
public class ScheduleOrderTriggerJob implements Job {

	private static final Logger LOGGER = LogManager.getLogger(ScheduleOrderTriggerJob.class);

	@Autowired
	private ScheduleComponent service;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOGGER.debug("ScheduleOrderTriggerJob.debug");
		service.executeService();
	}

}
