package com.schedule.config;

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

import com.schedule.component.SimpleTriggerJobListener;
import com.schedule.jobs.SimpleTriggerJob;

@Configuration
public class SimpleTriggerJobConfig {

	private JobKey jobKey = JobKey.jobKey("simpleTriggerJob");

	@Bean("SimpleTriggerJobDetail")
	public JobDetail jobDetail() {
		return JobBuilder.newJob(SimpleTriggerJob.class).withIdentity(jobKey).storeDurably().build();
	}

	@Bean("SimpleTriggerJobTrigger")
	public Trigger trigger() throws SchedulerException {
		Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail()).withIdentity("SimpleTriggerJobTrigger")
				.startNow().build();
		return trigger;
	}

	@Bean("SimpleTriggerJobListener")
	public SimpleTriggerJobListener addJobListener(Scheduler scheduler) throws SchedulerException {
		SimpleTriggerJobListener jobListener = new SimpleTriggerJobListener();
		scheduler.getListenerManager().addJobListener(jobListener, KeyMatcher.keyEquals(jobKey));
		return jobListener;
	}
}
