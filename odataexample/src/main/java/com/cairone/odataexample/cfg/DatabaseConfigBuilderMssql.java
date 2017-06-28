package com.cairone.odataexample.cfg;

import javax.sql.DataSource;

import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.microsoft.sqlserver.jdbc.SQLServerXADataSource;

@Component @Profile("database-mssql")
public class DatabaseConfigBuilderMssql extends DatabaseConfigAbstractBuilder {
	
	public DataSource build() {

		SQLServerXADataSource xaDataSource = new SQLServerXADataSource();
		
		xaDataSource.setServerName(serverName);
		xaDataSource.setDatabaseName(databaseName);
		xaDataSource.setUser(user);
		xaDataSource.setPassword(password);
		
		if(!instanceName.isEmpty()) xaDataSource.setInstanceName(instanceName);
		
		AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
		
		ds.setXaDataSource(xaDataSource);
		ds.setUniqueResourceName(uniqueResourceName);
		ds.setPoolSize(poolSize);
		
		return ds;
	}	
}
