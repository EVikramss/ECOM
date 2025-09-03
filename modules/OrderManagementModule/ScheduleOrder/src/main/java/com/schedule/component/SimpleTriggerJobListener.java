package com.schedule.component;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;
import org.springframework.beans.factory.annotation.Value;

public class SimpleTriggerJobListener extends JobListenerSupport {

	@Value(value = "${SimpleTriggerJob.fixedDelayInMS}")
	private int fixedDelayInterval;

	@Override
	public String getName() {
		return "SimpleTriggerJobListener";
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		try {
			Thread.sleep(fixedDelayInterval);
			context.getScheduler().triggerJob(context.getJobDetail().getKey());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
