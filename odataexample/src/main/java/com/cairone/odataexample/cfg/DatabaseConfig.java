package com.cairone.odataexample.cfg;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
	
	@Value("${database.serverName}") private String serverName = null;
	@Value("${database.instanceName}") private String instanceName = null;
	@Value("${database.databaseName}") private String databaseName = null;
	@Value("${database.user}") private String user = null;
	@Value("${database.password}") private String password = null;
	@Value("${database.uniqueResourceName}") private String uniqueResourceName = null;
	@Value("${database.poolSize}") private Integer poolSize = null;
	
	@Autowired
	DatabaseConfigBuilder databaseConfigBuilder = null;
	
	@Bean
	public DataSource databaseDataSource() {
		return databaseConfigBuilder
					.setServerName(serverName)
					.setInstanceName(instanceName)
					.setDatabaseName(databaseName)
					.setUser(user)
					.setPassword(password)
					.setUniqueResourceName(uniqueResourceName)
					.setPoolSize(poolSize)
					.build();
	}
}
