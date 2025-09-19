package com.ecom.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
	
	@Value("${connectTimeoutInS}")
	private int connectTimeout;
	
	@Value("${readTimeoutInS}")
	private int readTimeout;

	@Bean
	public RestTemplate getTemplate(RestTemplateBuilder builder) {
		return builder.connectTimeout(Duration.ofSeconds(5)).readTimeout(Duration.ofSeconds(5)).build();
	}
}
