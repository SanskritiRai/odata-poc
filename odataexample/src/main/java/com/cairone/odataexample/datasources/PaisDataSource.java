package com.cairone.odataexample.datasources;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.data.Property;
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
	public Object updateByPut(Object entity) throws ODataException {

    	if(entity instanceof PaisEdm) {
    		
    		PaisEdm pais = (PaisEdm) entity;
    		PaisFrmDto paisFrmDto = new PaisFrmDto(pais);
    		
			try {
				ValidatorUtil.validate(paisFrmDtoValidator, messageSource, paisFrmDto);
				PaisEntity paisEntity = paisService.actualizar(paisFrmDto);
				return new PaisEdm(paisEntity);
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
    	}
    	
    	throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PAIS", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}
	
	@Override
	public Object updateByPatch(Object entity, Map<String, Property> properties) throws ODataException {
		
    	if(entity instanceof PaisEdm) {
    		
    		PaisEdm pais = (PaisEdm) entity;
    		PaisEntity paisEntity = paisService.buscarPorID(pais.getId());
    		
    		if(paisEntity == null) {
    			throw new ODataApplicationException(
    					String.format("EL PAIS CON ID %s NO EXITE", pais.getId()), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    		}
    		
    		// *** CAMPO << NOMBRE >>
    		
    		Property updatePropNombre = properties.get("nombre");
    		if(updatePropNombre != null) {
    			paisEntity.setNombre(pais.getNombre() == null || pais.getNombre().trim().isEmpty() ? null : pais.getNombre().trim().toUpperCase());
    		}
    		
    		// *** CAMPO << PREFIJO >>
    		
    		Property updatePropPrefijo = properties.get("prefijo");
    		if(updatePropPrefijo != null) {
    			paisEntity.setPrefijo(pais.getPrefijo() == null ? null : pais.getPrefijo());
    		}
    		
    		PaisFrmDto paisFrmDto = new PaisFrmDto(paisEntity);
    		
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
	public Object delete(List<UriParameter> keyPredicates) throws ODataException {
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
