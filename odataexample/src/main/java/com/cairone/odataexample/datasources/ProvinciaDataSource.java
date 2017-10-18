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

import com.cairone.odataexample.dtos.ProvinciaFrmDto;
import com.cairone.odataexample.dtos.validators.ProvinciaFrmDtoValidator;
import com.cairone.odataexample.edm.resources.ProvinciaEdm;
import com.cairone.odataexample.entities.ProvinciaEntity;
import com.cairone.odataexample.exceptions.ODataBadRequestException;
import com.cairone.odataexample.services.ProvinciaService;
import com.cairone.odataexample.utils.OdataExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;

@Component
public class ProvinciaDataSource extends AbstractDataSource {
	
	public static final String ENTITY_SET_NAME = "Provincias";
	private static final Logger LOG = LoggerFactory.getLogger(PaisDataSource.class);
	
	@Autowired private ProvinciaService provinciaService = null;
	@Autowired private ProvinciaFrmDtoValidator provinciaFrmDtoValidator = null;
	
	@Override
	public Object create(Object entity) throws ODataApplicationException {

		if(entity instanceof ProvinciaEdm) {
			
			ProvinciaEdm provinciaEdm = (ProvinciaEdm) entity;
			ProvinciaFrmDto provinciaFrmDto = new ProvinciaFrmDto(provinciaEdm);
			
			try {
				ValidatorUtil.validate(provinciaFrmDtoValidator, messageSource, provinciaFrmDto);
				ProvinciaEntity provinciaEntity = provinciaService.nuevo(provinciaFrmDto);
				return new ProvinciaEdm(provinciaEntity);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PROVINCIA");
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {

    	if(entity instanceof ProvinciaEdm) {

        	Integer provinciaID = Integer.valueOf( keyPredicateMap.get("id").getText() );
        	Integer paisID = Integer.valueOf( keyPredicateMap.get("paisId").getText() );
        	
    		ProvinciaEdm provincia = (ProvinciaEdm) entity;
    		ProvinciaFrmDto provinciaFrmDto = new ProvinciaFrmDto(provincia);
    		
    		provinciaFrmDto.setId(provinciaID);
    		provinciaFrmDto.setPaisID(paisID);

    		try
    		{
    			ProvinciaEntity provinciaEntity = provinciaService.buscarPorID(paisID, provinciaID);
	    		
    			if(!isPut) {
    				if(provinciaFrmDto.getNombre() == null && !propertiesInJSON.contains("nombre")) provinciaFrmDto.setNombre(provinciaEntity.getNombre());
    			}
    		
				ValidatorUtil.validate(provinciaFrmDtoValidator, messageSource, provinciaFrmDto);
				return new ProvinciaEdm( provinciaService.actualizar(provinciaFrmDto) );
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw OdataExceptionParser.parse(e);
			}
    	}
    	
    	throw new ODataBadRequestException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PROVINCIA");
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {

    	Integer provinciaID = Integer.valueOf( keyPredicateMap.get("id").getText() );
    	Integer paisID = Integer.valueOf( keyPredicateMap.get("paisId").getText() );
    	
    	try {
			provinciaService.borrar(paisID, provinciaID);
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
		
		Integer provinciaID = Integer.valueOf( keyPredicateMap.get("id").getText() );
    	Integer paisID = Integer.valueOf( keyPredicateMap.get("paisId").getText() );
    	
    	try
    	{
	    	ProvinciaEntity provinciaEntity = provinciaService.buscarPorID(paisID, provinciaID);
	    	ProvinciaEdm provinciaEdm = provinciaEntity == null ? null : new ProvinciaEdm(provinciaEntity);
	    	
	    	return provinciaEdm;
    	} catch (Exception e) {
    		LOG.warn(e.getMessage(), e);
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(true)
			.setClazz(ProvinciaEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
	
		List<ProvinciaEntity> provinciaEntities = JPQLQuery.execute(entityManager, query);
		List<ProvinciaEdm> provinciaEdms = provinciaEntities.stream().map(entity -> { return new ProvinciaEdm(entity); }).collect(Collectors.toList());
		
		Map<String, ProvinciaEntity> map = hazelcastInstance.getMap(ProvinciaService.CACHE_NAME);
		provinciaEntities.forEach(provinciaEntity -> {
			String key = String.format("%s-%s", provinciaEntity.getPais().getId(), provinciaEntity.getId());
			map.put(key, provinciaEntity);
		});
		
		return provinciaEdms;
	}
}
