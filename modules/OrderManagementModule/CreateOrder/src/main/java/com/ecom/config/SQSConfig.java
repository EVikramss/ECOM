package com.ecom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class SQSConfig {

	@Bean
	public SqsClient sqsClient() {
		return SqsClient.builder().region(Region.of("us-east-1")).build();
	}
}
