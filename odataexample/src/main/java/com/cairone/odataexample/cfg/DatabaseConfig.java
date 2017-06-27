package com.cairone.odataexample.cfg;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.microsoft.sqlserver.jdbc.SQLServerXADataSource;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
	
	private final static Logger LOG = LoggerFactory.getLogger(DatabaseConfig.class);
	
	@Value("${database.serverName}") private String serverName = null;
	@Value("${database.instanceName}") private String instanceName = null;
	@Value("${database.databaseName}") private String databaseName = null;
	@Value("${database.user}") private String user = null;
	@Value("${database.password}") private String password = null;
	
	@Bean
	public DataSource databaseDataSource() {

		LOG.info("Configurando DATASOURCE ...");
		
		SQLServerXADataSource xaDataSource = new SQLServerXADataSource();
		
		xaDataSource.setServerName(serverName);
		xaDataSource.setDatabaseName(databaseName);
		xaDataSource.setUser(user);
		xaDataSource.setPassword(password);
		
		if(!instanceName.isEmpty()) xaDataSource.setInstanceName(instanceName);
		
		AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
		
		ds.setXaDataSource(xaDataSource);
		ds.setUniqueResourceName("odataexample-mssql");
		ds.setPoolSize(5);
		
		return ds;
	}
}
