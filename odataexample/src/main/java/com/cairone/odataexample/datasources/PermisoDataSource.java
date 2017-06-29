package com.cairone.odataexample.datasources;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.edm.resources.PermisoEdm;
import com.cairone.odataexample.entities.PermisoEntity;
import com.cairone.odataexample.exceptions.ODataForbiddenException;
import com.cairone.odataexample.services.PermisoService;
import com.cairone.odataexample.utils.OdataExceptionParser;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;
import com.google.common.base.CharMatcher;

@Component
public class PermisoDataSource extends AbstractDataSource {

	public static final String ENTITY_SET_NAME = "Permisos";

	@Autowired private PermisoService permisoService = null;

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
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption) throws ODataApplicationException {
		
		String permisoID = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("id").getText() );
		
		try
		{
			PermisoEntity permisoEntity = permisoService.buscarPorNombre(permisoID);
			PermisoEdm permisoEdm = permisoEntity == null ? null : new PermisoEdm(permisoEntity);
			
			return permisoEdm;
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(true)
			.setClazz(PermisoEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();

		List<PermisoEntity> permisoEntities = JPQLQuery.execute(entityManager, query);
		List<PermisoEdm> permisoEdms = permisoEntities.stream().map(entity -> { return new PermisoEdm(entity); }).collect(Collectors.toList());

		Map<String, PermisoEntity> map = hazelcastInstance.getMap(PermisoService.CACHE_NAME);
		map.putAll(permisoEntities.stream().collect(Collectors.toMap(PermisoEntity::getNombre, e -> e)));
		
		return permisoEdms;
	}
}
