package com.cairone.odataexample.utils;

import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.UnexpectedRollbackException;

import com.cairone.odataexample.exceptions.ODataBadRequestException;
import com.cairone.odataexample.exceptions.ODataInternalServerErrorException;
import com.cairone.odataexample.exceptions.ODataResourceNotFoundException;
import com.cairone.odataexample.exceptions.ServiceException;

public class OdataExceptionParser {
	
	public static final ODataApplicationException parse(Exception e) {
		if(e instanceof DataIntegrityViolationException || e instanceof UnexpectedRollbackException) {
			String message = SQLExceptionParser.parse(e);
			return new ODataInternalServerErrorException(message);
		} else if(e instanceof ServiceException) {
			ServiceException serviceException = (ServiceException) e;
			switch(serviceException.getCode()) {
			case ServiceException.ENTITY_NOT_FOUND:
				return new ODataResourceNotFoundException(e.getMessage());
			case ServiceException.TRANSACION_API_EXCEPTION:
				return new ODataInternalServerErrorException(e.getMessage());
			default:
				return new ODataBadRequestException(e.getMessage());
			}
		} else if(e instanceof ODataApplicationException) {
			return (ODataApplicationException) e;
		}
		return new ODataInternalServerErrorException(e.getMessage());
	}
}
