package com.cairone.odataexample.interfaces;

import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriParameter;

public interface DataSource {

	Object create(Object entity) throws ODataException;
	Object update(Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataException;
	Object delete(List<UriParameter> keyPredicates) throws ODataException;
}
