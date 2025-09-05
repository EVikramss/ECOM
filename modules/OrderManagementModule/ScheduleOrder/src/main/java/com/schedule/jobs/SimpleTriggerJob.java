package com.schedule.jobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.schedule.services.ScheduleService;

@Component
public class SimpleTriggerJob implements Job {

	private static final Logger LOGGER = LogManager.getLogger(SimpleTriggerJob.class);

	@Autowired
	private ScheduleService service;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOGGER.debug("SimpleTriggerJob.debug");
		service.executeService();
	}

}
