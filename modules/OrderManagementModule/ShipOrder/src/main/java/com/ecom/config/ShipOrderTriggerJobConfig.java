package com.ecom.config;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.KeyMatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecom.component.ShipOrderTriggerJobListener;
import com.ecom.jobs.ShipOrderTriggerJob;

@Configuration
public class ShipOrderTriggerJobConfig {

	private JobKey jobKey = JobKey.jobKey("ShipOrderTriggerJob");

	@Bean("ShipOrderTriggerJobDetail")
	public JobDetail jobDetail() {
		return JobBuilder.newJob(ShipOrderTriggerJob.class).withIdentity(jobKey).storeDurably().build();
	}

	@Bean("ShipOrderJobTrigger")
	public Trigger trigger() throws SchedulerException {
		Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail()).withIdentity("ShipOrderJobTrigger")
				.startNow().build();
		return trigger;
	}

	@Bean("ShipOrderTriggerJobListener")
	public ShipOrderTriggerJobListener addJobListener(Scheduler scheduler) throws SchedulerException {
		ShipOrderTriggerJobListener jobListener = new ShipOrderTriggerJobListener();
		scheduler.getListenerManager().addJobListener(jobListener, KeyMatcher.keyEquals(jobKey));
		return jobListener;
	}
}
