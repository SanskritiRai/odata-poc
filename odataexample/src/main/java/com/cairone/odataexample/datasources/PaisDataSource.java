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

import com.cairone.odataexample.dtos.PaisFrmDto;
import com.cairone.odataexample.dtos.validators.PaisFrmDtoValidator;
import com.cairone.odataexample.edm.resources.PaisEdm;
import com.cairone.odataexample.entities.PaisEntity;
import com.cairone.odataexample.interfaces.DataSource;
import com.cairone.odataexample.interfaces.DataSourceProvider;
import com.cairone.odataexample.services.PaisService;
import com.cairone.odataexample.utils.SQLExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;

@Component
public class PaisDataSource implements DataSource, DataSourceProvider {
	
	private static final String ENTITY_SET_NAME = "Paises";
	
	@Autowired private PaisService paisService = null;
	@Autowired private PaisFrmDtoValidator paisFrmDtoValidator = null;
	
	@Autowired
	private MessageSource messageSource = null;
	
	@Override
	public Object create(Object entity) throws ODataException {
		
		if(entity instanceof PaisEdm) {
			
			PaisEdm paisEdm = (PaisEdm) entity;
    		PaisFrmDto paisFrmDto = new PaisFrmDto(paisEdm);
    		
			try {
				ValidatorUtil.validate(paisFrmDtoValidator, messageSource, paisFrmDto);
				PaisEntity paisEntity = paisService.nuevo(paisFrmDto);
				return new PaisEdm(paisEntity);
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
		}
		
		throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PAIS", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}
	
	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataException {
		
    	if(entity instanceof PaisEdm) {
    		
    		PaisEdm pais = (PaisEdm) entity;
    		PaisFrmDto paisFrmDto;

        	Integer paisID = Integer.valueOf( keyPredicateMap.get("id").getText() );
        	
    		if(isPut) {
    			paisFrmDto = new PaisFrmDto(pais);
    			paisFrmDto.setId(paisID);
    		} else {
	    		PaisEntity paisEntity = paisService.buscarPorID(paisID);
	    		
	    		if(paisEntity == null) {
	    			throw new ODataApplicationException(
	    					String.format("EL PAIS CON ID %s NO EXITE", pais.getId()), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
	    		}
	    		
	    		// *** CAMPO << NOMBRE >>
	    		
	    		if(propertiesInJSON.contains("nombre")) {
	    			paisEntity.setNombre(pais.getNombre() == null || pais.getNombre().trim().isEmpty() ? null : pais.getNombre().trim().toUpperCase());
	    		}
	    		
	    		// *** CAMPO << PREFIJO >>
	    		
	    		if(propertiesInJSON.contains("prefijo")) {
	    			paisEntity.setPrefijo(pais.getPrefijo() == null ? null : pais.getPrefijo());
	    		}
	    		
	    		paisFrmDto = new PaisFrmDto(paisEntity);
    		}
    		
			try {
				ValidatorUtil.validate(paisFrmDtoValidator, messageSource, paisFrmDto);
				return new PaisEdm( paisService.actualizar(paisFrmDto) );
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
    	}
    	
    	throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PAIS", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataException {

		Integer paisID = Integer.valueOf( keyPredicateMap.get("id").getText() );

    	try {
			paisService.borrar(paisID);
		} catch (Exception e) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
    	
    	return null;
	}
	
	@Override
	public DataSource getDataSource() {
		return this;
	}

	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}
}
