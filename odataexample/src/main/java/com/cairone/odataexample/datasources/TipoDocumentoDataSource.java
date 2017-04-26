package com.cairone.odataexample.datasources;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.dtos.TipoDocumentoFrmDto;
import com.cairone.odataexample.dtos.validators.TipoDocumentoFrmDtoValidator;
import com.cairone.odataexample.edm.resources.TipoDocumentoEdm;
import com.cairone.odataexample.entities.TipoDocumentoEntity;
import com.cairone.odataexample.interfaces.DataSource;
import com.cairone.odataexample.interfaces.DataSourceProvider;
import com.cairone.odataexample.services.TipoDocumentoService;
import com.cairone.odataexample.utils.SQLExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;

@Component
public class TipoDocumentoDataSource implements DataSourceProvider, DataSource {

	private static final String ENTITY_SET_NAME = "TiposDocumentos";

	@Autowired private TipoDocumentoService tipoDocumentoService = null;
	@Autowired private TipoDocumentoFrmDtoValidator tipoDocumentoFrmDtoValidator = null;
	
	@Autowired
	private MessageSource messageSource = null;
	
	@Override
	public Object create(Object entity) throws ODataException {

		if(entity instanceof TipoDocumentoEdm) {
			
			TipoDocumentoEdm tipoDocumentoEdm = (TipoDocumentoEdm) entity;
    		TipoDocumentoFrmDto tipoDocumentoFrmDto = new TipoDocumentoFrmDto(tipoDocumentoEdm);
    		
			try {
				ValidatorUtil.validate(tipoDocumentoFrmDtoValidator, messageSource, tipoDocumentoFrmDto);
				TipoDocumentoEntity tipoDocumentoEntity = tipoDocumentoService.nuevo(tipoDocumentoFrmDto);
				return new TipoDocumentoEdm(tipoDocumentoEntity);
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
		}
		
		throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD TIPO DE DOCUMENTO", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataException {

    	if(entity instanceof TipoDocumentoEdm) {
    		
    		TipoDocumentoEdm tipoDocumento = (TipoDocumentoEdm) entity;
    		TipoDocumentoFrmDto tipoDocumentoFrmDto;

        	Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("id").getText() );
        	
    		if(isPut) {
    			tipoDocumentoFrmDto = new TipoDocumentoFrmDto(tipoDocumento);
    			tipoDocumentoFrmDto.setId(tipoDocumentoID);
    		} else {
    			TipoDocumentoEntity tipoDocumentoEntity = tipoDocumentoService.buscarPorID(tipoDocumentoID);
	    		
	    		if(tipoDocumentoEntity == null) {
	    			throw new ODataApplicationException(
	    					String.format("EL TIPO DE DOCUMENTO CON ID %s NO EXITE", tipoDocumentoID), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
	    		}
	    		
	    		// *** CAMPO << NOMBRE >>
	    		
	    		if(propertiesInJSON.contains("nombre")) {
	    			tipoDocumentoEntity.setNombre(tipoDocumento.getNombre() == null || tipoDocumento.getNombre().trim().isEmpty() ? null : tipoDocumento.getNombre().trim().toUpperCase());
	    		}
	    		
	    		// *** CAMPO << ABREVIATURA >>

	    		if(propertiesInJSON.contains("abreviatura")) {
	    			tipoDocumentoEntity.setAbreviatura(tipoDocumento.getAbreviatura() == null || tipoDocumento.getAbreviatura().trim().isEmpty() ? null : tipoDocumento.getAbreviatura().trim().toUpperCase());
	    		}
	    		
	    		
	    		tipoDocumentoFrmDto = new TipoDocumentoFrmDto(tipoDocumentoEntity);
    		}
    		
			try {
				ValidatorUtil.validate(tipoDocumentoFrmDtoValidator, messageSource, tipoDocumentoFrmDto);
				return new TipoDocumentoEdm( tipoDocumentoService.actualizar(tipoDocumentoFrmDto) );
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
    	}
    	
    	throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD TIPO DE DOCUMENTO", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataException {

		Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("id").getText() );

    	try {
    		tipoDocumentoService.borrar(tipoDocumentoID);
		} catch (Exception e) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
    	
    	return null;
	}

	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public DataSource getDataSource() {
		return this;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap) throws ODataException {

		Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("id").getText() );

		TipoDocumentoEntity tipoDocumentoEntity = tipoDocumentoService.buscarPorID(tipoDocumentoID);
		TipoDocumentoEdm tipoDocumentoEdm = tipoDocumentoEntity == null ? null : new TipoDocumentoEdm(tipoDocumentoEntity);
		
		return tipoDocumentoEdm;
	}
}
