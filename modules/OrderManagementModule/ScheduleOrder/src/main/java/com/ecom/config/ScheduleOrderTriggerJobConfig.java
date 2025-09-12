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

import com.ecom.component.ScheduleOrderTriggerJobListener;
import com.ecom.jobs.ScheduleOrderTriggerJob;

@Configuration
public class ScheduleOrderTriggerJobConfig {

	private JobKey jobKey = JobKey.jobKey("ScheduleOrderTriggerJob");

	@Bean("ScheduleOrderTriggerJobDetail")
	public JobDetail jobDetail() {
		return JobBuilder.newJob(ScheduleOrderTriggerJob.class).withIdentity(jobKey).storeDurably().build();
	}

	@Bean("ScheduleOrderJobTrigger")
	public Trigger trigger() throws SchedulerException {
		Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail()).withIdentity("ScheduleOrderJobTrigger")
				.startNow().build();
		return trigger;
	}

	@Bean("ScheduleOrderTriggerJobListener")
	public ScheduleOrderTriggerJobListener addJobListener(Scheduler scheduler) throws SchedulerException {
		ScheduleOrderTriggerJobListener jobListener = new ScheduleOrderTriggerJobListener();
		scheduler.getListenerManager().addJobListener(jobListener, KeyMatcher.keyEquals(jobKey));
		return jobListener;
	}
}
