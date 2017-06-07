package com.cairone.odataexample.edm.operations;

import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.services.PrestamoService;
import com.cairone.olingo.ext.jpa.annotations.EdmAction;
import com.cairone.olingo.ext.jpa.interfaces.Operation;
import com.google.common.base.CharMatcher;

@Component
@EdmAction(namespace = OdataExample.NAME_SPACE, name = "PrestamoAprobacionAction", isBound = true, entitySetPath = "PrestamosPendientes")
public class PrestamoAprobacionAction implements Operation<Void> {

	@Autowired private PrestamoService prestamoService = null;
	
	@Override
	public Void doOperation(boolean isBound, Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		String clave = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("clave").getText() );
		
		try {
			prestamoService.aprobar(clave);
		} catch (Exception e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
		return null;
	}
}
