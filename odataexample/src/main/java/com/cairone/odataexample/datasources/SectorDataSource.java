package com.cairone.odataexample.datasources;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.http.HttpStatusCode;
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

import com.cairone.odataexample.dtos.SectorFrmDto;
import com.cairone.odataexample.dtos.validators.SectorFrmDtoValidator;
import com.cairone.odataexample.edm.resources.SectorEdm;
import com.cairone.odataexample.entities.SectorEntity;
import com.cairone.odataexample.exceptions.ODataBadRequestException;
import com.cairone.odataexample.services.ProvinciaService;
import com.cairone.odataexample.services.SectorService;
import com.cairone.odataexample.utils.OdataExceptionParser;
import com.cairone.odataexample.utils.SQLExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;

@Component
public class SectorDataSource extends AbstractDataSource {

	public static final String ENTITY_SET_NAME = "Sectores";
	private static final Logger LOG = LoggerFactory.getLogger(PaisDataSource.class);
	
	@Autowired private SectorService sectorService = null;
	@Autowired private SectorFrmDtoValidator sectorFrmDtoValidator = null;

	@Override
	public Object create(Object entity) throws ODataApplicationException {

		if(entity instanceof SectorEdm) {
			
			SectorEdm sectorEdm = (SectorEdm) entity;
			SectorFrmDto sectorFrmDto = new SectorFrmDto(sectorEdm);
    		
			try {
				ValidatorUtil.validate(sectorFrmDtoValidator, messageSource, sectorFrmDto);
				SectorEntity sectorEntity = sectorService.nuevo(sectorFrmDto);
				return new SectorEdm(sectorEntity);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD SECTOR");
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {

    	if(entity instanceof SectorEdm) {

        	Integer sectorID = Integer.valueOf( keyPredicateMap.get("id").getText() );
        	    		
    		SectorEdm sectorEdm = (SectorEdm) entity;
    		SectorFrmDto sectorFrmDto = new SectorFrmDto(sectorEdm);
    		
    		sectorFrmDto.setId(sectorID);
    		
    		try
    		{
    			SectorEntity sectorEntity = sectorService.buscarPorID(sectorID);

    			if(!isPut) {
    				if(sectorFrmDto.getNombre() == null && !propertiesInJSON.contains("nombre")) sectorFrmDto.setNombre(sectorEntity.getNombre());
    			}
    		
				ValidatorUtil.validate(sectorFrmDtoValidator, messageSource, sectorFrmDto);
				return new SectorEdm( sectorService.actualizar(sectorFrmDto) );
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw OdataExceptionParser.parse(e);
			}
    	}
    	
    	throw new ODataBadRequestException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD SECTOR");
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {

		Integer sectorID = Integer.valueOf( keyPredicateMap.get("id").getText() );

    	try {
    		sectorService.borrar(sectorID);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			String message = SQLExceptionParser.parse(e);
			throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}
    	
    	return null;
	}

	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption) throws ODataApplicationException {
		
		Integer sectorID = Integer.valueOf( keyPredicateMap.get("id").getText() );
		
		try
		{
			SectorEntity sectorEntity = sectorService.buscarPorID(sectorID);
			SectorEdm sectorEdm = sectorEntity == null ? null : new SectorEdm(sectorEntity);
			
			return sectorEdm;
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(true)
			.setClazz(SectorEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
	
		List<SectorEntity> sectorEntities = JPQLQuery.execute(entityManager, query);
		List<SectorEdm> sectorEdms = sectorEntities.stream().map(entity -> { return new SectorEdm(entity); }).collect(Collectors.toList());

		Map<Integer, SectorEntity> map = hazelcastInstance.getMap(ProvinciaService.CACHE_NAME);
		sectorEntities.forEach(sectorEntity -> {
			map.put(sectorEntity.getId(), sectorEntity);
		});
		
		return sectorEdms;
	}
}
