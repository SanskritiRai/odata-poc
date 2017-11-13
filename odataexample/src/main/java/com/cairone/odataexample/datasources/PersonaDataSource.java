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

import com.cairone.odataexample.dtos.PersonaFrmDto;
import com.cairone.odataexample.dtos.validators.PersonaFrmDtoValidator;
import com.cairone.odataexample.edm.resources.PersonaEdm;
import com.cairone.odataexample.entities.LocalidadEntity;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.exceptions.ODataBadRequestException;
import com.cairone.odataexample.exceptions.ValidationException;
import com.cairone.odataexample.services.LocalidadService;
import com.cairone.odataexample.services.PersonaService;
import com.cairone.odataexample.utils.OdataExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;
import com.google.common.base.CharMatcher;

@Component
public class PersonaDataSource extends AbstractDataSource {

	public static final String ENTITY_SET_NAME = "Personas";
	private static final Logger LOG = LoggerFactory.getLogger(PaisDataSource.class);
	
	@Autowired private PersonaService personaService = null;
	@Autowired private LocalidadService localidadService = null;
	@Autowired private PersonaFrmDtoValidator personaFrmDtoValidator = null;

	@Override
	public Object create(Object entity) throws ODataApplicationException {

		if(entity instanceof PersonaEdm) {
			
			PersonaEdm personaEdm = (PersonaEdm) entity;
			PersonaFrmDto personaFrmDto = new PersonaFrmDto(personaEdm);

			try {
				ValidatorUtil.validate(personaFrmDtoValidator, messageSource, personaFrmDto);
				PersonaEntity personaEntity = personaService.nuevo(personaFrmDto);
				return new PersonaEdm(personaEntity);
			} catch (ValidationException e) {
				LOG.warn(e.getMessage());
				throw new ODataBadRequestException(e.getMessage());
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PERSONA");
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {

    	if(entity instanceof PersonaEdm) {

        	Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("tipoDocumentoId").getText() );
        	String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
        	
    		PersonaEdm persona = (PersonaEdm) entity;
    		PersonaFrmDto personaFrmDto = new PersonaFrmDto(persona);
    		
    		personaFrmDto.setTipoDocumentoId(tipoDocumentoID);
    		personaFrmDto.setNumeroDocumento(numeroDocumento);
    		
    		try
    		{
    			PersonaEntity personaEntity = personaService.buscarPorId(tipoDocumentoID, numeroDocumento);

    			if(!isPut) {
    				if(personaFrmDto.getNombres() == null && !propertiesInJSON.contains("nombres")) personaFrmDto.setNombres(personaEntity.getNombres());
    				if(personaFrmDto.getApellidos() == null && !propertiesInJSON.contains("apellidos")) personaFrmDto.setApellidos(personaEntity.getApellidos());
    				if(personaFrmDto.getApodo() == null && !propertiesInJSON.contains("apodo")) personaFrmDto.setApellidos(personaEntity.getApodo());
    				if(personaFrmDto.getGenero() == null && !propertiesInJSON.contains("genero")) personaFrmDto.setGenero(personaEntity.getGenero().toGeneroOdataEnum());
    				
    				if(propertiesInJSON.contains("localidad")) {
    	    			
    	    			Integer paisID = persona.getLocalidad().getPaisId();
    	    			Integer provinciaID = persona.getLocalidad().getProvinciaId();
    	    			Integer localidadID = persona.getLocalidad().getLocalidadId();
    	    			
    	    			LocalidadEntity localidadEntity = localidadService.buscarPorID(paisID, provinciaID, localidadID);
    	    			personaEntity.setLocalidad(localidadEntity);
    	    		}
    				
    			}
    		
				ValidatorUtil.validate(personaFrmDtoValidator, messageSource, personaFrmDto);
				personaEntity = personaService.actualizar(personaFrmDto);
				
				return new PersonaEdm(personaEntity);
    		} catch (ValidationException e) {
				LOG.warn(e.getMessage());
				throw new ODataBadRequestException(e.getMessage());
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw OdataExceptionParser.parse(e);
			}
    	}
    	
    	throw new ODataBadRequestException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PERSONA");
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {

		Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("tipoDocumentoId").getText() );
		String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
    	
    	try {
			personaService.borrar(tipoDocumentoID, numeroDocumento);
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
		
		Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("tipoDocumentoId").getText() );
		String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
    	
		try
		{
	    	PersonaEntity personaEntity = personaService.buscarPorId(tipoDocumentoID, numeroDocumento);
	    	PersonaEdm personaEdm = new PersonaEdm(personaEntity);
	    	
	    	return personaEdm;
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(false)
			.setClazz(PersonaEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
	
		List<PersonaEntity> personaEntities = JPQLQuery.execute(entityManager, query);
		List<PersonaEdm> personaEdms = personaEntities.stream().map(entity -> { return new PersonaEdm(entity); }).collect(Collectors.toList());
		
		Map<String, PersonaEntity> map = hazelcastInstance.getMap(PersonaService.CACHE_NAME_PERSONA);
		personaEntities.forEach(personaEntity -> {
			String key = String.format("%s-%s", personaEntity.getTipoDocumento().getId(), personaEntity.getNumeroDocumento());
			map.put(key, personaEntity);
		});
		
		return personaEdms;
	}
}
