package com.schedule.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
public class SimpleTriggerJob implements Job {

	private static int counter = 0;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("Hiii" + counter);
		counter++;
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
