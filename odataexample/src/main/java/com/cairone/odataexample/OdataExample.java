package com.cairone.odataexample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import com.cairone.odataexample.ctrls.ODataController;

@SpringBootApplication
public class OdataExample extends SpringBootServletInitializer
{
    public static void main( String[] args ) {
        SpringApplication.run(OdataExample.class, args);
    }
    
    @Autowired ODataController dispatcherServlet = null;
    
    @Bean
    public ServletRegistrationBean dispatcherServletRegistration() {
    	ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet, "/odata/appexample.svc/*");
    	return registration;
    }
}
