package com.cairone.odataexample.cfg;

import javax.sql.DataSource;

public interface DatabaseConfigBuilder {

	public DataSource build();
	
	public DatabaseConfigBuilder setServerName(String serverName);
	public DatabaseConfigBuilder setInstanceName(String instanceName);
	public DatabaseConfigBuilder setDatabaseName(String databaseName);
	public DatabaseConfigBuilder setUser(String user);
	public DatabaseConfigBuilder setPassword(String password);
	public DatabaseConfigBuilder setUniqueResourceName(String uniqueResourceName);
	public DatabaseConfigBuilder setPoolSize(Integer poolSize);
	
}
