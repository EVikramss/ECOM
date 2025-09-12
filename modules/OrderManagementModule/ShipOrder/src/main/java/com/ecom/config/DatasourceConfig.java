package com.ecom.config;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class DatasourceConfig {

	@Value("${DBPRX_EP}")
	private String dbEndpoint;

	@Value("${SECRET}")
	private String dbSecretJson;

	@Bean
	public DataSource getDataSource() throws JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> credentials = mapper.readValue(dbSecretJson, Map.class);
		String endpointURL = String.format("jdbc:postgresql://%s:5432/", dbEndpoint);

		return DataSourceBuilder.create().url(endpointURL).username(credentials.get("username"))
				.password(credentials.get("password")).driverClassName("org.postgresql.Driver").build();
	}

}
