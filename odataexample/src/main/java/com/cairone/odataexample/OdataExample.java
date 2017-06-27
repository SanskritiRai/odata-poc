package com.cairone.odataexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

@SpringBootApplication
public class OdataExample extends SpringBootServletInitializer
{
	public static final String NAME_SPACE = "com.cairone.odataexample";
	public static final String CONTAINER_NAME = "ODataExample";
	public static final String SERVICE_ROOT = "http://localhost:8080/odata/appexample.svc/";
	public static final String DEFAULT_EDM_PACKAGE = "com.cairone.odataexample.edm";
	
	private static Class<OdataExample> applicationClass = OdataExample.class;
	
    public static void main( String[] args ) {
        SpringApplication.run(OdataExample.class, args);
    }
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    	return application.sources(applicationClass);
    }
}
