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

import com.cairone.odataexample.dtos.UsuarioFrmDto;
import com.cairone.odataexample.dtos.validators.UsuarioFrmDtoValidator;
import com.cairone.odataexample.edm.resources.UsuarioEdm;
import com.cairone.odataexample.entities.UsuarioEntity;
import com.cairone.odataexample.exceptions.ODataBadRequestException;
import com.cairone.odataexample.exceptions.ValidationException;
import com.cairone.odataexample.services.UsuarioService;
import com.cairone.odataexample.utils.OdataExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;
import com.google.common.base.CharMatcher;

@Component
public class UsuarioDataSource extends AbstractDataSource {

	public static final String ENTITY_SET_NAME = "Usuarios";
	private static final Logger LOG = LoggerFactory.getLogger(PaisDataSource.class);
	
	@Autowired private UsuarioService usuarioService = null;
	@Autowired private UsuarioFrmDtoValidator usuarioFrmDtoValidator = null;

	@Override
	public Object create(Object entity) throws ODataApplicationException {

		if(entity instanceof UsuarioEdm) {
			
			UsuarioEdm usuarioEdm = (UsuarioEdm) entity;
			UsuarioFrmDto usuarioFrmDto = new UsuarioFrmDto(usuarioEdm);

			try {
				ValidatorUtil.validate(usuarioFrmDtoValidator, messageSource, usuarioFrmDto);
				UsuarioEntity usuarioEntity = usuarioService.nuevo(usuarioFrmDto);
				return new UsuarioEdm(usuarioEntity);
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

    	if(entity instanceof UsuarioEdm) {

        	Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("tipoDocumentoId").getText() );
        	String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
        	
    		UsuarioEdm usuario = (UsuarioEdm) entity;
    		UsuarioFrmDto usuarioFrmDto = new UsuarioFrmDto(usuario);
    		
    		usuarioFrmDto.setTipoDocumentoId(tipoDocumentoID);
    		usuarioFrmDto.setNumeroDocumento(numeroDocumento);

    		try
    		{
    			UsuarioEntity usuarioEntity = usuarioService.buscarPorId(tipoDocumentoID, numeroDocumento);
	    		
    			if(!isPut) {
    				if(usuarioFrmDto.getNombreUsuario() == null && !propertiesInJSON.contains("nombreUsuario")) usuarioFrmDto.setNombreUsuario(usuarioEntity.getNombreUsuario());
    				if(usuarioFrmDto.getCuentaVencida() == null && !propertiesInJSON.contains("cuentaVencida")) usuarioFrmDto.setCuentaVencida(usuarioEntity.getCuentaVencida());
    				if(usuarioFrmDto.getClaveVencida() == null && !propertiesInJSON.contains("cuentaVencida")) usuarioFrmDto.setClaveVencida(usuarioEntity.getClaveVencida());
    				if(usuarioFrmDto.getCuentaBloqueada() == null && !propertiesInJSON.contains("cuentaBloqueada")) usuarioFrmDto.setCuentaBloqueada(usuarioEntity.getCuentaBloqueada());
    				if(usuarioFrmDto.getUsuarioHabilitado() == null && !propertiesInJSON.contains("usuarioHabilitado")) usuarioFrmDto.setUsuarioHabilitado(usuarioEntity.getUsuarioHabilitado());
    			}
    		
				ValidatorUtil.validate(usuarioFrmDtoValidator, messageSource, usuarioFrmDto);
				return new UsuarioEdm( usuarioService.actualizar(usuarioFrmDto) );
    		} catch (ValidationException e) {
				LOG.warn(e.getMessage());
				throw new ODataBadRequestException(e.getMessage());
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw OdataExceptionParser.parse(e);
			}
    	}
    	
    	throw new ODataBadRequestException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD USUARIO");
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {

		Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("tipoDocumentoId").getText() );
		String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
    	
    	try {
			usuarioService.borrar(tipoDocumentoID, numeroDocumento);
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
			UsuarioEntity usuarioEntity = usuarioService.buscarPorId(tipoDocumentoID, numeroDocumento);
			UsuarioEdm usuarioEdm = new UsuarioEdm(usuarioEntity);
			
			return usuarioEdm;
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			throw OdataExceptionParser.parse(e);
		}
	}
	
	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(true)
			.setClazz(UsuarioEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
		
		List<UsuarioEntity> usuarioEntities = JPQLQuery.execute(entityManager, query);
		List<UsuarioEdm> usuarioEdms = usuarioEntities.stream().map(entity -> { return new UsuarioEdm(entity); }).collect(Collectors.toList());

		Map<String, UsuarioEntity> map = hazelcastInstance.getMap(UsuarioService.CACHE_NAME);
		usuarioEntities.forEach(usuarioEntity -> {
			String key = String.format("%s-%s", usuarioEntity.getPersona().getTipoDocumento().getId(), usuarioEntity.getPersona().getNumeroDocumento());
			map.put(key, usuarioEntity);
		});
		
		return usuarioEdms;
	}
}
