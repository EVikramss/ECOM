package com.ecom.component;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.listeners.JobListenerSupport;
import org.springframework.beans.factory.annotation.Value;

public class ShipOrderTriggerJobListener extends JobListenerSupport {

	@Value(value = "${ShipOrderTriggerJob.fixedDelayInMS}")
	private int fixedDelayInterval;

	@Override
	public String getName() {
		return "ShipOrderTriggerJobListener";
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		try {
			Thread.sleep(fixedDelayInterval);
			JobKey key = context.getJobDetail().getKey();
			context.getScheduler().triggerJob(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
