package com.cairone.odataexample.datasources;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.interfaces.DataSource;
import com.cairone.odataexample.interfaces.DataSourceProvider;
import com.cairone.odataexample.services.PermisoService;

@Component
public class PermisoDataSource implements DataSourceProvider, DataSource {

	private static final String ENTITY_SET_NAME = "Permisos";

	@Autowired private PermisoService permisoService = null;
	
	@Autowired
	private MessageSource messageSource = null;

	@Override
	public Object create(Object entity) throws ODataException {
		throw new ODataApplicationException("OPERACION NO PERMITIDA", HttpStatusCode.FORBIDDEN.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataException {
		throw new ODataApplicationException("OPERACION NO PERMITIDA", HttpStatusCode.FORBIDDEN.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataException {
		throw new ODataApplicationException("OPERACION NO PERMITIDA", HttpStatusCode.FORBIDDEN.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public DataSource getDataSource() {
		return this;
	}
}
