package com.db.rds.config;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import liquibase.command.CommandScope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class Config {

	@Bean
	public SpringLiquibase liquibase() {
		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setDataSource(h2DataSource());
		liquibase.setDropFirst(false);
		liquibase.setShouldRun(false);
		return liquibase;
	}

	@Bean
	public String generateUpdateQueries() throws Exception {
		String outputPath = "generated.sql";
		String inputPath = "master.xml";
		generateSqlToFile(h2DataSource(), inputPath, outputPath);
		return outputPath;
	}

	public void generateSqlToFile(DataSource dataSource, String changeLogPath, String outputFilePath) throws Exception {

		try (Connection connection = dataSource.getConnection()) {
			Database database = DatabaseFactory.getInstance()
					.findCorrectDatabaseImplementation(new liquibase.database.jvm.JdbcConnection(connection));

			try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(outputFilePath))) {
				CommandScope commandScope = new CommandScope("updateSql");
				// CommandScope commandScope = new CommandScope("update");
				commandScope.addArgumentValue("database", database);
				commandScope.addArgumentValue("changelogFile", changeLogPath);
				commandScope.setOutput(writer);
				commandScope.execute();
			}
		}
	}

	@Bean
	public DataSource h2DataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setUrl("jdbc:h2:mem:test1db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
		dataSource.setUsername("h2");
		dataSource.setPassword("h2");
		return dataSource;
	}

}
