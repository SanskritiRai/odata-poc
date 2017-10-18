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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.dtos.PaisFrmDto;
import com.cairone.odataexample.dtos.validators.PaisFrmDtoValidator;
import com.cairone.odataexample.edm.resources.PaisEdm;
import com.cairone.odataexample.entities.PaisEntity;
import com.cairone.odataexample.exceptions.ODataBadRequestException;
import com.cairone.odataexample.services.PaisService;
import com.cairone.odataexample.utils.OdataExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;

@Component
public class PaisDataSource extends AbstractDataSource {
	
	public static final String ENTITY_SET_NAME = "Paises";
	private static final Logger LOG = LoggerFactory.getLogger(PaisDataSource.class);
	
	@Autowired private PaisService paisService = null;
	@Autowired private PaisFrmDtoValidator paisFrmDtoValidator = null;
	
	@Override
	public Object create(Object entity) throws ODataApplicationException {
		
		if(entity instanceof PaisEdm) {
			
			PaisEdm paisEdm = (PaisEdm) entity;
    		PaisFrmDto paisFrmDto = new PaisFrmDto(paisEdm);
    		
			try {
				ValidatorUtil.validate(paisFrmDtoValidator, messageSource, paisFrmDto);
				PaisEntity paisEntity = paisService.nuevo(paisFrmDto);
				return new PaisEdm(paisEntity);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PAIS");
	}
	
	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {
		
    	if(entity instanceof PaisEdm) {
    		
    		Integer paisID = Integer.valueOf( keyPredicateMap.get("id").getText() );
        	
    		PaisEdm pais = (PaisEdm) entity;
    		PaisFrmDto paisFrmDto = new PaisFrmDto(pais);
    		
    		paisFrmDto.setId(paisID);

    		try
    		{
    			PaisEntity paisEntity = paisService.buscarPorID(paisID);
	    		
    			if(!isPut) {
    				if(paisFrmDto.getNombre() == null && !propertiesInJSON.contains("nombre")) paisFrmDto.setNombre(paisEntity.getNombre());
    				if(paisFrmDto.getPrefijo() == null && !propertiesInJSON.contains("prefijo")) paisFrmDto.setPrefijo(paisEntity.getPrefijo());
    			}
    			
				ValidatorUtil.validate(paisFrmDtoValidator, messageSource, paisFrmDto);
				paisEntity = paisService.actualizar(paisFrmDto);
				
				return new PaisEdm(paisEntity);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw OdataExceptionParser.parse(e);
			}
    	}
    	
    	throw new ODataBadRequestException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PAIS");
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {

		Integer paisID = Integer.valueOf( keyPredicateMap.get("id").getText() );

    	try {
			paisService.borrar(paisID);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw OdataExceptionParser.parse(e);
		}
    	
    	return null;
	}
	
	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption) throws ODataApplicationException {
		
		Integer paisID = Integer.valueOf( keyPredicateMap.get("id").getText() );
		
		try
    	{
			PaisEntity paisEntity = paisService.buscarPorID(paisID);
			PaisEdm paisEdm = new PaisEdm(paisEntity);
		
			return paisEdm;
    	} catch (Exception e) {
    		LOG.warn(e.getMessage(), e);
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(true)
			.setClazz(PaisEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
		
		List<PaisEntity> paisEntities = JPQLQuery.execute(entityManager, query);
		List<PaisEdm> paisEdms = paisEntities.stream().map(entity -> { return new PaisEdm(entity); }).collect(Collectors.toList());

		LOG.debug("PAISES ENCONTRADOS: {}", paisEdms.size());
		
		Map<Integer, PaisEntity> map = hazelcastInstance.getMap(PaisService.CACHE_NAME);
		map.putAll(paisEntities.stream().collect(Collectors.toMap(PaisEntity::getId, e -> e)));
		
		return paisEdms;
	}
}
