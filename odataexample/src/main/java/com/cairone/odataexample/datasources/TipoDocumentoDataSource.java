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

import com.cairone.odataexample.dtos.TipoDocumentoFrmDto;
import com.cairone.odataexample.dtos.validators.TipoDocumentoFrmDtoValidator;
import com.cairone.odataexample.edm.resources.TipoDocumentoEdm;
import com.cairone.odataexample.entities.TipoDocumentoEntity;
import com.cairone.odataexample.exceptions.ODataBadRequestException;
import com.cairone.odataexample.exceptions.ValidationException;
import com.cairone.odataexample.services.TipoDocumentoService;
import com.cairone.odataexample.utils.OdataExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;

@Component
public class TipoDocumentoDataSource extends AbstractDataSource {

	public static final String ENTITY_SET_NAME = "TiposDocumentos";
	private static final Logger LOG = LoggerFactory.getLogger(PaisDataSource.class);
	
	@Autowired private TipoDocumentoService tipoDocumentoService = null;
	@Autowired private TipoDocumentoFrmDtoValidator tipoDocumentoFrmDtoValidator = null;
	
	@Override
	public Object create(Object entity) throws ODataApplicationException {

		if(entity instanceof TipoDocumentoEdm) {
			
			TipoDocumentoEdm tipoDocumentoEdm = (TipoDocumentoEdm) entity;
    		TipoDocumentoFrmDto tipoDocumentoFrmDto = new TipoDocumentoFrmDto(tipoDocumentoEdm);
    		
			try {
				ValidatorUtil.validate(tipoDocumentoFrmDtoValidator, messageSource, tipoDocumentoFrmDto);
				TipoDocumentoEntity tipoDocumentoEntity = tipoDocumentoService.nuevo(tipoDocumentoFrmDto);
				return new TipoDocumentoEdm(tipoDocumentoEntity);
			} catch (ValidationException e) {
				LOG.warn(e.getMessage());
				throw new ODataBadRequestException(e.getMessage());
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD TIPO DE DOCUMENTO");
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {

    	if(entity instanceof TipoDocumentoEdm) {
    		
    		Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("id").getText() );
        	
    		TipoDocumentoEdm tipoDocumento = (TipoDocumentoEdm) entity;
    		
    		TipoDocumentoFrmDto tipoDocumentoFrmDto = new TipoDocumentoFrmDto(tipoDocumento);
    		tipoDocumentoFrmDto.setId(tipoDocumentoID);

    		try
    		{
    			TipoDocumentoEntity tipoDocumentoEntity = tipoDocumentoService.buscarPorID(tipoDocumentoID);
	    		
    			if(!isPut) {
    				if(tipoDocumentoFrmDto.getNombre() == null && !propertiesInJSON.contains("nombre")) tipoDocumentoFrmDto.setNombre(tipoDocumentoEntity.getNombre());
    				if(tipoDocumentoFrmDto.getAbreviatura() == null && !propertiesInJSON.contains("abreviatura")) tipoDocumentoFrmDto.setAbreviatura(tipoDocumentoEntity.getAbreviatura());
    			}
    		
				ValidatorUtil.validate(tipoDocumentoFrmDtoValidator, messageSource, tipoDocumentoFrmDto);
				return new TipoDocumentoEdm( tipoDocumentoService.actualizar(tipoDocumentoFrmDto) );
    		} catch (ValidationException e) {
				LOG.warn(e.getMessage());
				throw new ODataBadRequestException(e.getMessage());
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw OdataExceptionParser.parse(e);
			}
    	}
    	
    	throw new ODataBadRequestException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD TIPO DE DOCUMENTO");
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {

		Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("id").getText() );

    	try {
    		tipoDocumentoService.borrar(tipoDocumentoID);
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

		Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("id").getText() );

		try
		{
			TipoDocumentoEntity tipoDocumentoEntity = tipoDocumentoService.buscarPorID(tipoDocumentoID);
			TipoDocumentoEdm tipoDocumentoEdm = tipoDocumentoEntity == null ? null : new TipoDocumentoEdm(tipoDocumentoEntity);
			
			return tipoDocumentoEdm;
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			throw OdataExceptionParser.parse(e);
		}	
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(true)
			.setClazz(TipoDocumentoEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
	
		List<TipoDocumentoEntity> tipoDocumentoEntities = JPQLQuery.execute(entityManager, query);
		List<TipoDocumentoEdm> tipoDocumentoEdms = tipoDocumentoEntities.stream().map(entity -> { return new TipoDocumentoEdm(entity); }).collect(Collectors.toList());

		Map<Integer, TipoDocumentoEntity> map = hazelcastInstance.getMap(TipoDocumentoService.CACHE_NAME);
		tipoDocumentoEntities.forEach(tipoDocumentoEntity -> {
			map.put(tipoDocumentoEntity.getId(), tipoDocumentoEntity);
		});
		
		return tipoDocumentoEdms;
	}
}
