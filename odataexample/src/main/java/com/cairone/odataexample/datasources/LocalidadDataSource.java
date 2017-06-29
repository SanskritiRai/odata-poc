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

import com.cairone.odataexample.dtos.LocalidadFrmDto;
import com.cairone.odataexample.dtos.validators.LocalidadFrmDtoValidator;
import com.cairone.odataexample.edm.resources.LocalidadEdm;
import com.cairone.odataexample.entities.LocalidadEntity;
import com.cairone.odataexample.exceptions.ODataBadRequestException;
import com.cairone.odataexample.services.LocalidadService;
import com.cairone.odataexample.utils.OdataExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;

@Component
public class LocalidadDataSource extends AbstractDataSource {

	public static final String ENTITY_SET_NAME = "Localidades";

	@Autowired private LocalidadService localidadService = null;
	@Autowired private LocalidadFrmDtoValidator localidadFrmDtoValidator = null;
			
	@Override
	public Object create(Object entity) throws ODataApplicationException {
		
		if(entity instanceof LocalidadEdm) {
			
			LocalidadEdm localidadEdm = (LocalidadEdm) entity;
			LocalidadFrmDto localidadFrmDto = new LocalidadFrmDto(localidadEdm);
			
			try {
				ValidatorUtil.validate(localidadFrmDtoValidator, messageSource, localidadFrmDto);
				LocalidadEntity localidadEntity = localidadService.nuevo(localidadFrmDto);
				return new LocalidadEdm(localidadEntity);
			} catch (Exception e) {
				throw OdataExceptionParser.parse(e);
			}
		}

		throw new ODataBadRequestException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD LOCALIDAD");
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {
		
    	if(entity instanceof LocalidadEdm) {
    		
    		Integer paisID = Integer.valueOf( keyPredicateMap.get("paisId").getText() );
    		Integer provinciaID = Integer.valueOf( keyPredicateMap.get("provinciaId").getText() );
    		Integer localidadID = Integer.valueOf( keyPredicateMap.get("localidadId").getText() );
        	
    		LocalidadEdm localidad = (LocalidadEdm) entity;
    		LocalidadFrmDto localidadFrmDto = new LocalidadFrmDto(localidad);
    		
    		localidadFrmDto.setPaisId(paisID);
    		localidadFrmDto.setProvinciaId(provinciaID);
    		localidadFrmDto.setLocalidadId(localidadID);
    		
    		try
    		{
    			LocalidadEntity localidadEntity = localidadService.buscarPorID(paisID, provinciaID, localidadID);

    			if(!isPut) {
    				if(localidadFrmDto.getNombre() == null && !propertiesInJSON.contains("nombre")) localidadFrmDto.setNombre(localidadEntity.getNombre());
    			}
    			
    			ValidatorUtil.validate(localidadFrmDtoValidator, messageSource, localidadFrmDto);
    			localidadEntity = localidadService.actualizar(localidadFrmDto);
    			
    			return new LocalidadEdm( localidadEntity );
    			
    		} catch (Exception e) {
    			throw OdataExceptionParser.parse(e);
			}
    	}
    	
    	throw new ODataBadRequestException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD LOCALIDAD");
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {

    	Integer paisID = Integer.valueOf( keyPredicateMap.get("paisId").getText() );
    	Integer provinciaID = Integer.valueOf( keyPredicateMap.get("provinciaId").getText() );
    	Integer localidadID = Integer.valueOf( keyPredicateMap.get("localidadId").getText() );
    	
    	try {
			localidadService.borrar(paisID, provinciaID, localidadID);
		} catch (Exception e) {
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
		
		Integer paisID = Integer.valueOf( keyPredicateMap.get("paisId").getText() );
    	Integer provinciaID = Integer.valueOf( keyPredicateMap.get("provinciaId").getText() );
    	Integer localidadID = Integer.valueOf( keyPredicateMap.get("localidadId").getText() );
    	
    	try
    	{
	    	LocalidadEntity localidadEntity = localidadService.buscarPorID(paisID, provinciaID, localidadID);
	    	LocalidadEdm localidadEdm = localidadEntity == null ? null : new LocalidadEdm(localidadEntity);
	    	
	    	return localidadEdm;
    	} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(false)
			.setClazz(LocalidadEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
	
		List<LocalidadEntity> localidadEntities = JPQLQuery.execute(entityManager, query);
		List<LocalidadEdm> localidadEdms = localidadEntities.stream().map(entity -> { return new LocalidadEdm(entity); }).collect(Collectors.toList());

		Map<String, LocalidadEntity> map = hazelcastInstance.getMap(LocalidadService.CACHE_NAME);
		localidadEntities.forEach(localidadEntity -> {
			String key = String.format("%s-%s-%s", localidadEntity.getProvincia().getPais().getId(), localidadEntity.getProvincia().getId(), localidadEntity.getId());
			map.put(key, localidadEntity);
		});
		
		return localidadEdms;
	}

}
