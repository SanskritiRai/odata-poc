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

@Component
public class PersonaSectorDataSource extends AbstractDataSource {

	private static final String ENTITY_SET_NAME = "PersonasSectores";

	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public Object create(Object entity) throws ODataApplicationException {
		throw new ODataForbiddenException("OPERACION NO PERMITIDA - AGREGAR PERSONAS A SECTORES A TRAVES DE LA ACCION CORRESPONDIENTE");
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {
		throw new ODataForbiddenException("OPERACION NO PERMITIDA - AGREGAR PERSONAS A SECTORES A TRAVES DE LA ACCION CORRESPONDIENTE");
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {
		throw new ODataForbiddenException("OPERACION NO PERMITIDA - QUITAR PERSONAS DE SECTORES A TRAVES DE LA ACCION CORRESPONDIENTE");
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption) throws ODataApplicationException {
		throw new ODataForbiddenException("OPERACION NO PERMITIDA - CONSULTE LOS SECTORES DE UNA PERSONA A TRAVES DE LA PROPIEDAD DE NAVEGACION CORRESPONDIENTE");
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException {
		throw new ODataForbiddenException("OPERACION NO PERMITIDA - CONSULTE LOS SECTORES DE UNA PERSONA A TRAVES DE LA PROPIEDAD DE NAVEGACION CORRESPONDIENTE");
	}
}
