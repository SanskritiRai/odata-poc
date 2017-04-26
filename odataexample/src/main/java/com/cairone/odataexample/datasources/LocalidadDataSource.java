package com.cairone.odataexample.datasources;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.dtos.LocalidadFrmDto;
import com.cairone.odataexample.dtos.validators.LocalidadFrmDtoValidator;
import com.cairone.odataexample.edm.resources.LocalidadEdm;
import com.cairone.odataexample.entities.LocalidadEntity;
import com.cairone.odataexample.interfaces.DataSource;
import com.cairone.odataexample.interfaces.DataSourceProvider;
import com.cairone.odataexample.services.LocalidadService;
import com.cairone.odataexample.utils.SQLExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;

@Component
public class LocalidadDataSource implements DataSourceProvider, DataSource {

	private static final String ENTITY_SET_NAME = "Localidades";

	@Autowired private LocalidadService localidadService = null;
	@Autowired private LocalidadFrmDtoValidator localidadFrmDtoValidator = null;

	@Autowired
	private MessageSource messageSource = null;
	
	@Override
	public Object create(Object entity) throws ODataException {
		
		if(entity instanceof LocalidadEdm) {
			
			LocalidadEdm localidadEdm = (LocalidadEdm) entity;
			LocalidadFrmDto localidadFrmDto = new LocalidadFrmDto(localidadEdm);
			
			try {
				ValidatorUtil.validate(localidadFrmDtoValidator, messageSource, localidadFrmDto);
				LocalidadEntity localidadEntity = localidadService.nuevo(localidadFrmDto);
				return new LocalidadEdm(localidadEntity);
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
		}

		throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD LOCALIDAD", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataException {
		
    	if(entity instanceof LocalidadEdm) {
    		
    		LocalidadEdm localidad = (LocalidadEdm) entity;
    		LocalidadFrmDto localidadFrmDto;
    		
    		Integer paisID = Integer.valueOf( keyPredicateMap.get("paisId").getText() );
    		Integer provinciaID = Integer.valueOf( keyPredicateMap.get("provinciaId").getText() );
    		Integer localidadID = Integer.valueOf( keyPredicateMap.get("localidadId").getText() );
        	
    		if(isPut) {
    			localidadFrmDto = new LocalidadFrmDto(localidad);
    			localidadFrmDto.setLocalidadId(localidadID);
    			localidadFrmDto.setProvinciaId(provinciaID);
    			localidadFrmDto.setPaisId(paisID);
    		} else {
	    		LocalidadEntity localidadEntity = localidadService.buscarPorID(paisID, provinciaID, localidadID);
	    		
	    		if(localidadEntity == null) {
	    			throw new ODataApplicationException(
	    				String.format("LA LOCALIDAD CON ID (PAIS=%s,PROVINCIA=%s,LOCALIDAD=%s) NO EXITE", paisID, provinciaID, localidadID), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
	    		}
	    		
	    		// *** CAMPO << NOMBRE >>
	    		
	    		if(propertiesInJSON.contains("nombre")) {
	    			localidadEntity.setNombre(localidad.getNombre() == null || localidad.getNombre().trim().isEmpty() ? null : localidad.getNombre().trim().toUpperCase());
	    		}
	    		
	    		localidadFrmDto = new LocalidadFrmDto(localidadEntity);
    		}
    		
			try {
				ValidatorUtil.validate(localidadFrmDtoValidator, messageSource, localidadFrmDto);
				return new LocalidadEdm( localidadService.actualizar(localidadFrmDto) );
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
    	}
    	
    	throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD LOCALIDAD", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataException {

    	Integer paisID = Integer.valueOf( keyPredicateMap.get("paisId").getText() );
    	Integer provinciaID = Integer.valueOf( keyPredicateMap.get("provinciaId").getText() );
    	Integer localidadID = Integer.valueOf( keyPredicateMap.get("localidadId").getText() );
    	
    	try {
			localidadService.borrar(paisID, provinciaID, localidadID);
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
		
		Integer paisID = Integer.valueOf( keyPredicateMap.get("paisId").getText() );
    	Integer provinciaID = Integer.valueOf( keyPredicateMap.get("provinciaId").getText() );
    	Integer localidadID = Integer.valueOf( keyPredicateMap.get("localidadId").getText() );
    	
    	LocalidadEntity localidadEntity = localidadService.buscarPorID(paisID, provinciaID, localidadID);
    	LocalidadEdm localidadEdm = localidadEntity == null ? null : new LocalidadEdm(localidadEntity);
    	
    	return localidadEdm;
	}

	@Override
	public Iterable<?> readAll(OrderByOption orderByOption) throws ODataException {

		List<Sort.Order> orderByList = new ArrayList<Sort.Order>();
		
		if(orderByOption != null) {
			orderByOption.getOrders().forEach(orderByItem -> {
				
				Expression expression = orderByItem.getExpression();
				if(expression instanceof Member){
					
					UriInfoResource resourcePath = ((Member)expression).getResourcePath();
					UriResource uriResource = resourcePath.getUriResourceParts().get(0);
					
				    if (uriResource instanceof UriResourcePrimitiveProperty) {
				    	EdmProperty edmProperty = ((UriResourcePrimitiveProperty)uriResource).getProperty();
						Direction direction = orderByItem.isDescending() ? Direction.DESC : Direction.ASC;
						String property = edmProperty.getName();
						orderByList.add(new Order(direction, property));
				    }
				}
				
			});
		}
		
		return localidadService.ejecutarConsulta(null, orderByList).stream().map(e -> new LocalidadEdm(e)).collect(Collectors.toList());
	}
}
