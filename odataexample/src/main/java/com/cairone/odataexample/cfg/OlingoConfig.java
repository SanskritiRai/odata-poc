package com.cairone.odataexample.cfg;

import org.apache.olingo.server.api.ODataApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.ctrls.ODataController;
import com.cairone.olingo.ext.jpa.processors.ActionProcessor;
import com.cairone.olingo.ext.jpa.processors.BatchRequestProcessor;
import com.cairone.olingo.ext.jpa.processors.MediaProcessor;
import com.cairone.olingo.ext.jpa.providers.EdmProvider;

@Configuration
public class OlingoConfig {

	private final static Logger LOG = LoggerFactory.getLogger(OlingoConfig.class);
	
    @Autowired private ApplicationContext context = null;
    @Autowired ODataController dispatcherServlet = null;
    
    @Bean
    public MediaProcessor getMediaProcessor() throws ODataApplicationException {
    	
    	LOG.info("Configurando MEDIAPROCESSOR ...");
    	
    	MediaProcessor mediaProcessor = new MediaProcessor()
	    	.setDefaultEdmPackage(OdataExample.DEFAULT_EDM_PACKAGE)
			.setServiceRoot(OdataExample.SERVICE_ROOT)
			.initialize(context);
    	
    	return mediaProcessor;
    }
    
    @Bean
    public ActionProcessor getActionProcessor() throws ODataApplicationException {

    	LOG.info("Configurando ACTIONPROCESSOR ...");
    	
    	ActionProcessor processor = new ActionProcessor()
    		.setDefaultEdmPackage(OdataExample.DEFAULT_EDM_PACKAGE)
    		.setServiceRoot(OdataExample.SERVICE_ROOT)
    		.initialize(context);
    	
    	return processor;
    }
    
    @Bean
    public BatchRequestProcessor getBatchRequestProcessor() {

    	LOG.info("Configurando BATCHPROCESSOR ...");
    	
    	BatchRequestProcessor processor = new BatchRequestProcessor();
    	return processor;
    }
    
    @Bean
    public EdmProvider getOdataexampleEdmProvider() throws ODataApplicationException {

    	LOG.info("Configurando PROVIDER ...");
    	
    	EdmProvider provider = new EdmProvider()
    		.setContainerName(OdataExample.CONTAINER_NAME)
    		.setDefaultEdmPackage(OdataExample.DEFAULT_EDM_PACKAGE)
    		.setNameSpace(OdataExample.NAME_SPACE)
    		.setServiceRoot(OdataExample.SERVICE_ROOT)
    		.initialize();
    	
    	return provider;
    }

    @Bean
    public ServletRegistrationBean dispatcherServletRegistration() {

    	LOG.info("Configurando DISPATCHER ...");
    	
    	ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet, "/odata/appexample.svc/*");
    	return registration;
    }
}
