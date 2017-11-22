package com.cairone.odataexample.cfg;

import javax.sql.DataSource;

import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;

@Component @Profile("database-mysql")
public class DatabaseConfigBuilderMysql extends DatabaseConfigAbstractBuilder {

	@Override
	public DataSource build() {
		
		String url = String.format("jdbc:mysql://%s/%s", serverName, databaseName);
		MysqlXADataSource xaDataSource = new MysqlXADataSource();
		
		xaDataSource.setUrl(url);
		xaDataSource.setPinGlobalTxToPhysicalConnection(true);
		xaDataSource.setUser(user);
		xaDataSource.setPassword(password);
		xaDataSource.setPinGlobalTxToPhysicalConnection(true);
		
		AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
		
		ds.setXaDataSource(xaDataSource);
		ds.setUniqueResourceName(uniqueResourceName);
		ds.setPoolSize(poolSize);
		ds.setTestQuery("select 1");
		
		return ds;
	}
}
