package com.cairone.odataexample;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.dtos.PaisFrmDto;
import com.cairone.odataexample.dtos.validators.PaisFrmDtoValidator;
import com.cairone.odataexample.entities.PaisEntity;
import com.cairone.odataexample.services.PaisService;
import com.cairone.odataexample.utils.SQLExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;

@Component
public class PaisOdataEntityProcessor implements EntityProcessor, PrimitiveProcessor, EntityCollectionProcessor {

	private OData odata;
	private ServiceMetadata serviceMetadata;
	
	@Autowired private PaisService paisService = null;
	@Autowired private PaisFrmDtoValidator paisFrmDtoValidator = null;
	
	@Autowired
	private MessageSource messageSource = null;
	
	@Override
	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}

	@Override
	public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

	    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
	    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
	    EdmEntityType edmEntityType = edmEntitySet.getEntityType();

	    List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
	    
	    if(edmEntityType.getName().equals("Pais")) {
	    	
	    	UriParameter uriParameter = keyPredicates.get(0);
	    	Integer paisID = Integer.valueOf(uriParameter.getText());
	    	
	    	PaisEntity paisEntity = paisService.buscarPorID(paisID);
	    	
	    	final Entity entity = new Entity()
				.addProperty(new Property(null, "id", ValueType.PRIMITIVE, paisEntity.getId()))
				.addProperty(new Property(null, "nombre", ValueType.PRIMITIVE, paisEntity.getNombre()))
				.addProperty(new Property(null, "prefijo", ValueType.PRIMITIVE, paisEntity.getPrefijo()));
			entity.setId(createId("Paises", paisEntity.getId()));
			
			EdmEntityType entityType = edmEntitySet.getEntityType();
			
		    ContextURL contextUrl = null;
			try {
				contextUrl = ContextURL.with()
						.serviceRoot(new URI(OdataexampleEdmProvider.SERVICE_ROOT))
						.entitySet(edmEntitySet)
						.suffix(Suffix.ENTITY)
						.build();
			} catch (URISyntaxException e) {
				throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
		    EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

		    ODataSerializer serializer = odata.createSerializer(responseFormat);
		    SerializerResult serializerResult = serializer.entity(serviceMetadata, entityType, entity, options);
		    InputStream entityStream = serializerResult.getContent();

		    response.setContent(entityStream);
		    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        }
	}

	@Override
	public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

	    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
	    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		
		InputStream requestInputStream = request.getBody();
		ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
		DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
		Entity requestEntity = result.getEntity();
		
		Property propertyId = requestEntity.getProperty("id");
		Property propertyNombre = requestEntity.getProperty("nombre");
		Property propertyPrefijo = requestEntity.getProperty("prefijo");
		
		Integer paisId = Integer.valueOf(propertyId.getValue().toString());
		String paisNombre = propertyNombre.getValue().toString();
		Integer paisPrefijo = propertyPrefijo == null || propertyPrefijo.getValue() == null ? null : Integer.valueOf(propertyPrefijo.getValue().toString());
		
		PaisFrmDto paisFrmDto = new PaisFrmDto(paisId, paisNombre, paisPrefijo);
		
		try {
			ValidatorUtil.validate(paisFrmDtoValidator, messageSource, paisFrmDto);
		} catch (Exception e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}
		
		PaisEntity paisEntity;
		
		try {
			paisEntity = paisService.nuevo(paisFrmDto);
		} catch (Exception e) {
			String message = SQLExceptionParser.parse(e);
			throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}
		
		Entity createdEntity = new Entity()
			.addProperty(new Property(null, "id", ValueType.PRIMITIVE, paisEntity.getId()))
			.addProperty(new Property(null, "nombre", ValueType.PRIMITIVE, paisEntity.getNombre()))
			.addProperty(new Property(null, "prefijo", ValueType.PRIMITIVE, paisEntity.getPrefijo()));
		createdEntity.setId(createId("Paises", paisEntity.getId()));
		

	    ContextURL contextUrl = null;
		try {
			contextUrl = ContextURL.with()
					.serviceRoot(new URI(OdataexampleEdmProvider.SERVICE_ROOT))
					.entitySet(edmEntitySet)
					.suffix(Suffix.ENTITY)
					.build();
		} catch (URISyntaxException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}
		EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();
		
		ODataSerializer serializer = this.odata.createSerializer(responseFormat);
		SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, options);

		final String location = request.getRawBaseUri() + '/' + odata.createUriHelper().buildCanonicalURL(edmEntitySet, createdEntity);
		
		response.setContent(serializedResponse.getContent());
		response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
		response.setHeader(HttpHeader.LOCATION, location);
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	@Override
	public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		  
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();

		if(!edmEntityType.getName().equals("Pais")) {
			return;
		}
		
		InputStream requestInputStream = request.getBody();
		ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
		DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
		Entity requestEntity = result.getEntity();
		
		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		
		HttpMethod httpMethod = request.getMethod();
		
		// ***
		
		UriParameter uriParameter = keyPredicates.get(0);
    	Integer paisID = Integer.valueOf(uriParameter.getText());
    	
    	PaisEntity paisEntity = paisService.buscarPorID(paisID);

        if(paisEntity == null) {
            throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }
        
    	final Entity entity = new Entity()
			.addProperty(new Property(null, "id", ValueType.PRIMITIVE, paisEntity.getId()))
			.addProperty(new Property(null, "nombre", ValueType.PRIMITIVE, paisEntity.getNombre()))
			.addProperty(new Property(null, "prefijo", ValueType.PRIMITIVE, paisEntity.getPrefijo()));
		entity.setId(createId("Paises", paisEntity.getId()));
		
		List<Property> properties = entity.getProperties();
		
		for(Property property : properties) {
			
			 String propName = property.getName();
			 
			 if(edmEntityType.getKeyPredicateNames().contains(propName)) {
			      continue;
			 }
			 
			 Property updateProperty = requestEntity.getProperty(propName);
			 if(updateProperty == null) {
				 
				 if(httpMethod.equals(HttpMethod.PATCH)){
					 continue;
				 }else if(httpMethod.equals(HttpMethod.PUT)){
					 property.setValue(property.getValueType(), null);
					 continue;
				 }
			 }
			 
			 property.setValue(property.getValueType(), updateProperty.getValue());
		}

		Property propertyId = entity.getProperty("id");
		Property propertyNombre = entity.getProperty("nombre");
		Property propertyPrefijo = entity.getProperty("prefijo");
		
		Integer paisId = Integer.valueOf(propertyId.getValue().toString());
		String paisNombre = propertyNombre.getValue().toString();
		Integer paisPrefijo = Integer.valueOf(propertyPrefijo.getValue().toString());
		
		PaisFrmDto paisFrmDto = new PaisFrmDto(paisId, paisNombre, paisPrefijo);
		
		try {
			ValidatorUtil.validate(paisFrmDtoValidator, messageSource, paisFrmDto);
		} catch (Exception e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}

		try {
			paisEntity = paisService.actualizar(paisFrmDto);
		} catch (Exception e) {
			String message = SQLExceptionParser.parse(e);
			throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}
		
		// ***

		response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}

	@Override
	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {

		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		  
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();

		if(!edmEntityType.getName().equals("Pais")) {
			return;
		}
		
		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		
		UriParameter uriParameter = keyPredicates.get(0);
    	Integer paisID = Integer.valueOf(uriParameter.getText());
    	
    	try {
			paisService.borrar(paisID);
		} catch (Exception e) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
    	
    	response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}

	@Override
	public void readPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		List<UriResource> resourceParts = uriInfo.getUriResourceParts();
		
		UriResourceEntitySet uriEntityset = (UriResourceEntitySet) resourceParts.get(0);
        EdmEntitySet edmEntitySet = uriEntityset.getEntitySet();
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        
        List<UriParameter> keyPredicates = uriEntityset.getKeyPredicates();
        
        if(edmEntityType.getName().equals("Pais")) {
        	
        	UriParameter uriParameter = keyPredicates.get(0);
	    	Integer paisID = Integer.valueOf(uriParameter.getText());
	    	
	    	PaisEntity paisEntity = paisService.buscarPorID(paisID);

	        if(paisEntity == null) {
	            throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
	        }
	        
	        UriResourceProperty uriProperty = (UriResourceProperty) resourceParts.get(resourceParts.size() -1);
	        EdmProperty edmProperty = uriProperty.getProperty();
	        String edmPropertyName = edmProperty.getName();
	        
	        EdmPrimitiveType edmPropertyType = (EdmPrimitiveType) edmProperty.getType();
	        
	        final Entity entity = new Entity()
				.addProperty(new Property(null, "id", ValueType.PRIMITIVE, paisEntity.getId()))
				.addProperty(new Property(null, "nombre", ValueType.PRIMITIVE, paisEntity.getNombre()))
				.addProperty(new Property(null, "prefijo", ValueType.PRIMITIVE, paisEntity.getPrefijo()));
			entity.setId(createId("Paises", paisEntity.getId()));
			
			Property property = entity.getProperty(edmPropertyName);
			
			if(property == null) {
				throw new ODataApplicationException("Property not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
			}
			
			Object value = property.getValue();
			
			if (value != null) {
	              
				ODataSerializer serializer = odata.createSerializer(responseFormat);
				ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).navOrPropertyPath(edmPropertyName).build();
				PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();
	            
				SerializerResult serializerResult = serializer.primitive(serviceMetadata, edmPropertyType, property, options);
				InputStream propertyStream = serializerResult.getContent();

				response.setContent(propertyStream);
				response.setStatusCode(HttpStatusCode.OK.getStatusCode());
				response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
				
			} else {
				response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
			}
        }
	}

	@Override
	public void updatePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
	}

	@Override
	public void deletePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
		
	}

	@Override
	public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        
        if(!edmEntityType.getName().equals("Pais")) {
        	return;
        }
        
		EntityCollection data = new EntityCollection();
		List<Entity> result = data.getEntities();
		
		paisService.ejecutarConsulta().forEach(paisEntity -> {
			final Entity entity = new Entity()
				.addProperty(new Property(null, "id", ValueType.PRIMITIVE, paisEntity.getId()))
				.addProperty(new Property(null, "nombre", ValueType.PRIMITIVE, paisEntity.getNombre()))
				.addProperty(new Property(null, "prefijo", ValueType.PRIMITIVE, paisEntity.getPrefijo()));
			entity.setId(createId("Paises", paisEntity.getId()));
			result.add(entity);
		});
		
		ODataSerializer serializer = odata.createSerializer(responseFormat);

	    ContextURL contextUrl = null;
		try {
			contextUrl = ContextURL.with()
					.serviceRoot(new URI(OdataexampleEdmProvider.SERVICE_ROOT))
					.entitySet(edmEntitySet)
					.build();
		} catch (URISyntaxException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}
		
		final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
		EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().id(id).contextURL(contextUrl).build();
		SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, data, opts);
		InputStream serializedContent = serializerResult.getContent();

		response.setContent(serializedContent);
		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}
	
	private URI createId(String entitySetName, Object id) {
	    try {
	        return new URI(entitySetName + "(" + String.valueOf(id) + ")");
	    } catch (URISyntaxException e) {
	        throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
	    }
	}
}
