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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.edm.resources.PersonaFotoEdm;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.entities.PersonaFotoEntity;
import com.cairone.odataexample.exceptions.ODataNotImplementedException;
import com.cairone.odataexample.services.PersonaService;
import com.cairone.odataexample.utils.OdataExceptionParser;
import com.cairone.olingo.ext.jpa.interfaces.MediaDataSource;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;
import com.google.common.base.CharMatcher;

@Component
public class PersonaFotoDataSource extends AbstractDataSource implements MediaDataSource {
	
	private static final String ENTITY_SET_NAME = "PersonasFotos";
	
	@Autowired private PersonaService personaService = null;
	
	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public byte[] findMediaResource(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {

		String uuid = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("uuid").getText() );
    	
		try
		{
			PersonaFotoEntity personaFotoEntity = personaService.buscarFoto(uuid);
	    	byte[] foto = personaFotoEntity == null ? null : personaFotoEntity.getFoto();
	    	
	    	return foto;
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Object createMediaResource(byte[] binary) throws ODataApplicationException {
		
		PersonaFotoEntity personaFotoEntity = personaService.nuevaFoto(binary);
		PersonaFotoEdm personaFotoEdm = new PersonaFotoEdm(personaFotoEntity.getUuid());
		
		return personaFotoEdm;
	}

	@Override
	public void updateMediaResource(Map<String, UriParameter> keyPredicateMap, byte[] binary) throws ODataApplicationException {
		
		String uuid = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("uuid").getText() );
		
		try
		{
	    	PersonaEntity personaEntity = personaService.buscarPorFotoUUID(uuid);
	    	personaService.actualizarFoto(personaEntity, binary);
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Object create(Object entity) throws ODataApplicationException {
		throw new ODataNotImplementedException("OPERACION NO IMPLEMENTADA");
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {
		
		if(entity instanceof PersonaFotoEdm) {
			
			PersonaFotoEdm personaFotoEdm = (PersonaFotoEdm) entity;
			
			String uuid = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("uuid").getText() );
			
			try
			{
				PersonaFotoEntity personaFotoEntity = personaService.buscarFoto(uuid);
			
				Integer tipoDocumentoId = personaFotoEdm.getTipoDocumentoId();
				String numeroDocumento = personaFotoEdm.getNumeroDocumento();
				PersonaEntity personaEntity = personaService.buscarPorId(tipoDocumentoId, numeroDocumento);
				
				personaService.asignarFoto(personaEntity, personaFotoEntity);
				personaFotoEdm.setUuid(uuid);
				
				return personaFotoEdm;
			} catch (Exception e) {
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD FOTO PERSONA", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {
		
		String uuid = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("uuid").getText() );
    	
    	try {
    		personaService.quitarFoto(uuid);
    		return null;
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}
	
	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption) throws ODataApplicationException {
		
		String uuid = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("uuid").getText() );
    	
		try
		{
			PersonaFotoEntity personaFotoEntity = personaService.buscarFoto(uuid);
	    	PersonaEntity personaEntity = personaService.buscarPorFotoUUID(uuid);
	    	
	    	PersonaFotoEdm personaFotoEdm = new PersonaFotoEdm(personaFotoEntity.getUuid());
	    	personaFotoEdm.setTipoDocumentoId(personaEntity.getTipoDocumento().getId());
	    	personaFotoEdm.setNumeroDocumento(personaEntity.getNumeroDocumento());
	    	
	    	return personaFotoEdm;
	    	
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(false)
			.setClazz(PersonaFotoEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
	
		List<PersonaFotoEntity> personaFotoEntities = JPQLQuery.execute(entityManager, query);
		List<PersonaFotoEdm> personaFotoEdms = personaFotoEntities.stream().map(entity -> {
			
			PersonaEntity personaEntity;
			try {
				personaEntity = personaService.buscarPorFotoUUID(entity.getUuid());
			} catch (Exception e) {
				//FIXME
				personaEntity = null;
			}
			PersonaFotoEdm personaFotoEdm = new PersonaFotoEdm(entity.getUuid()); 
			
			if(personaEntity != null) {
				personaFotoEdm.setTipoDocumentoId(personaEntity.getTipoDocumento().getId());
				personaFotoEdm.setNumeroDocumento(personaEntity.getNumeroDocumento());
			}
			
			return personaFotoEdm; 
		}).collect(Collectors.toList());
		
		Map<String, PersonaFotoEntity> map = hazelcastInstance.getMap(PersonaService.CACHE_NAME_FOTO);
		personaFotoEntities.forEach(personaFotoEntity -> {
			map.put(personaFotoEntity.getUuid(), personaFotoEntity);
		});
		
		return personaFotoEdms;
	}
}
