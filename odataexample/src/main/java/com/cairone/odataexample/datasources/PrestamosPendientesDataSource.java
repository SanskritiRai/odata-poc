package com.cairone.odataexample.datasources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.edm.resources.PrestamoPendienteEdm;
import com.cairone.odataexample.exceptions.ODataForbiddenException;
import com.cairone.odataexample.exceptions.ODataResourceNotFoundException;
import com.cairone.odataexample.services.PrestamoService;
import com.google.common.base.CharMatcher;

@Component
public class PrestamosPendientesDataSource extends AbstractDataSource {

	private static final String ENTITY_SET_NAME = "PrestamosPendientes";
	
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
		
		Map<String, PrestamoPendienteEdm> map = hazelcastInstance.getMap(PrestamoService.CACHE_NAME_PENDIENTES);
		String clave = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("clave").getText() );
		
		return map.remove(clave);
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption) throws ODataApplicationException {
		
		Map<String, PrestamoPendienteEdm> map = hazelcastInstance.getMap(PrestamoService.CACHE_NAME_PENDIENTES);
		
		String clave = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("clave").getText() );
		PrestamoPendienteEdm prestamoPendienteEdm = map.get(clave);
		
		if(prestamoPendienteEdm == null) {
			throw new ODataResourceNotFoundException(String.format("NO HAY UN PRESTAMO PENDIENTE CON CLAVE %s", clave));
		}
		
		return prestamoPendienteEdm;
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException {
		
		List<PrestamoPendienteEdm> prestamoPendienteEdms = new ArrayList<PrestamoPendienteEdm>();
		Map<String, PrestamoPendienteEdm> map = hazelcastInstance.getMap(PrestamoService.CACHE_NAME_PENDIENTES);
		
		for(Map.Entry<String, PrestamoPendienteEdm> entry : map.entrySet()) {
			PrestamoPendienteEdm prestamoPendienteEdm = entry.getValue();
			prestamoPendienteEdms.add(prestamoPendienteEdm);
		}
		
		return prestamoPendienteEdms;
	}	
}
