package com.cairone.odataexample.cfg;

import javax.sql.DataSource;

public abstract class DatabaseConfigAbstractBuilder implements DatabaseConfigBuilder {

	protected String serverName = null;
	protected String instanceName = null;
	protected String databaseName = null;
	protected String user = null;
	protected String password = null;
	protected String uniqueResourceName = null;
	protected Integer poolSize = null;
	
	@Override
	public abstract DataSource build();
	
	@Override
	public DatabaseConfigBuilder setServerName(String serverName) {
		this.serverName = serverName;
		return this;
	}
	
	@Override
	public DatabaseConfigBuilder setInstanceName(String instanceName) {
		this.instanceName = instanceName;
		return this;
	}
	
	@Override
	public DatabaseConfigBuilder setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
		return this;
	}
	
	@Override
	public DatabaseConfigBuilder setUser(String user) {
		this.user = user;
		return this;
	}
	
	@Override
	public DatabaseConfigBuilder setPassword(String password) {
		this.password = password;
		return this;
	}

	@Override
	public DatabaseConfigBuilder setUniqueResourceName(String uniqueResourceName) {
		this.uniqueResourceName = uniqueResourceName;
		return this;
	}

	@Override
	public DatabaseConfigBuilder setPoolSize(Integer poolSize) {
		this.poolSize = poolSize;
		return this;
	}	
	
}
