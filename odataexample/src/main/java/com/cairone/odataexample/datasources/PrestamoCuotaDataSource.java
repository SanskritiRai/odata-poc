package com.cairone.odataexample.datasources;

import java.util.List;
import java.util.Map;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.exceptions.ODataForbiddenException;
import com.cairone.odataexample.exceptions.ODataNotImplementedException;

@Component
public class PrestamoCuotaDataSource extends AbstractDataSource {

	public static final String ENTITY_SET_NAME = "PrestamoCuotas";

	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public Object create(Object entity) throws ODataApplicationException {
		throw new ODataForbiddenException("OPERACION NO PERMITIDA");
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {
		throw new ODataForbiddenException("OPERACION NO PERMITIDA");
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {
		throw new ODataForbiddenException("OPERACION NO PERMITIDA");
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption) throws ODataApplicationException {
		//TODO
		throw new ODataNotImplementedException("AUN NO IMPLEMENTADO");
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException {
		//TODO
		throw new ODataNotImplementedException("AUN NO IMPLEMENTADO");
	}
}
