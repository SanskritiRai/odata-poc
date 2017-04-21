package com.cairone.odataexample.interfaces;

public interface DataSourceProvider {

	String isSuitableFor();
	DataSource getDataSource();
}
