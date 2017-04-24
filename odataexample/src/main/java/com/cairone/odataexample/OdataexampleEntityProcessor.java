package com.cairone.odataexample;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.ex.ODataException;
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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.annotations.EdmEntity;
import com.cairone.odataexample.annotations.EdmEnum;
import com.cairone.odataexample.dtos.validators.PaisFrmDtoValidator;
import com.cairone.odataexample.entities.PaisEntity;
import com.cairone.odataexample.interfaces.DataSource;
import com.cairone.odataexample.interfaces.DataSourceProvider;
import com.cairone.odataexample.services.PaisService;

@Component
public class OdataexampleEntityProcessor implements EntityProcessor, PrimitiveProcessor, EntityCollectionProcessor {

	private OData odata;
	private ServiceMetadata serviceMetadata;
	
	private Map<String, DataSourceProvider> dataSourceProviderMap = new HashMap<>();
	private Map<String, Class<?>> entitySetMap = new HashMap<>();
	
	@Autowired private PaisService paisService = null;
	@Autowired private PaisFrmDtoValidator paisFrmDtoValidator = null;
	
	@Autowired
	private MessageSource messageSource = null;
	
	@Autowired
	private ApplicationContext context = null;
	
	@Override
	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}
	
	@PostConstruct
	public void init() throws ODataApplicationException {
		
		context.getBeansOfType(DataSourceProvider.class).entrySet()
			.stream()
			.forEach(entry -> {
				DataSourceProvider dataSourceProvider = entry.getValue();
				dataSourceProviderMap.put(dataSourceProvider.isSuitableFor(), dataSourceProvider);
			});

		ClassPathScanningCandidateComponentProvider provider = createComponentScanner(Arrays.asList(com.cairone.odataexample.annotations.EdmEntitySet.class));
		Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(OdataexampleEdmProvider.DEFAULT_EDM_PACKAGE);

		try {
			for(BeanDefinition beanDef : beanDefinitions) {
				Class<?> cl = Class.forName(beanDef.getBeanClassName());
				
				com.cairone.odataexample.annotations.EdmEntitySet edmEntitySet = cl.getAnnotation(com.cairone.odataexample.annotations.EdmEntitySet.class);
				
				if(edmEntitySet != null) {
					entitySetMap.put(edmEntitySet.value(), cl);
				}
			}
		} catch (ClassNotFoundException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
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
		
		DataSourceProvider dataSourceProvider = dataSourceProviderMap.get(edmEntitySet.getName());
		
		if(dataSourceProvider == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", edmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}
		
		DataSource dataSource = dataSourceProvider.getDataSource();
		
		InputStream requestInputStream = request.getBody();
		ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
		DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
		Entity requestEntity = result.getEntity();
		
		Class<?> clazz = entitySetMap.get(edmEntitySet.getName());
		Object object, createdObject;
		
    	EdmEntity edmEntity = clazz.getAnnotation(EdmEntity.class);
    	String[] keys = edmEntity.key();
    	Map<String, Object> keyValues = Arrays.asList(keys)
    		.stream()
    		.collect(Collectors.toMap(x -> x, x -> x));
    	
    	try {
	    	
			Constructor<?> constructor = clazz.getConstructor();
			object = constructor.newInstance();
	    	
			for (Field fld : clazz.getDeclaredFields()) {
				
				com.cairone.odataexample.annotations.EdmProperty edmProperty = fld.getAnnotation(com.cairone.odataexample.annotations.EdmProperty.class);
				
	            if (edmProperty != null) {
	            	
	            	String propertyName = edmProperty.name().isEmpty() ? fld.getName() : edmProperty.name();
	            	Property property = requestEntity.getProperty(propertyName);
	            		
	            	fld.setAccessible(true);
	            	fld.set(object, property.getValue());
	            	
	            } else {
	            	
	            	com.cairone.odataexample.annotations.EdmNavigationProperty edmNavigationProperty = fld.getAnnotation(com.cairone.odataexample.annotations.EdmNavigationProperty.class);
					
	            	if(edmNavigationProperty != null) {
	            		
	            		String propertyName = edmNavigationProperty.name().isEmpty() ? fld.getName() : edmNavigationProperty.name();
	            		
	            		Class<?> fieldClass = fld.getType();

	    				if(Collection.class.isAssignableFrom(fieldClass)) {
	    					
	    				} else {
	    					
	    					fld.setAccessible(true);
	    					Object navpropField = fld.get(object);

	    					com.cairone.odataexample.annotations.EdmEntitySet targetEdmEntitySet = fieldClass.getAnnotation(com.cairone.odataexample.annotations.EdmEntitySet.class);
	    					String targetEntitySetName = targetEdmEntitySet.value();
	    					Class<?> cl = entitySetMap.get(targetEntitySetName);
	    					
	    					if(navpropField == null) {
	    						Constructor<?> c = cl.getConstructor();
		    					navpropField = c.newInstance();
		    					fld.set(object, navpropField);
	    					}
	    					
	    					for(Field f : cl.getDeclaredFields()) {

	    						com.cairone.odataexample.annotations.EdmProperty fieldEdmProperty = f.getAnnotation(com.cairone.odataexample.annotations.EdmProperty.class);
	    						
	    			            if (fieldEdmProperty != null) {
	    			            	
	    			            	String linkName = fieldEdmProperty.name().isEmpty() ? f.getName() : fieldEdmProperty.name();
	    			            	Link link = requestEntity.getNavigationLink(propertyName);
	    			            	Property property = link == null || link.getInlineEntity() == null ? null : link.getInlineEntity().getProperty(linkName);
	    			            	String text = property == null ? null : property.getValue().toString();
	    			            	
	    			            	if(text != null) {
		    			            	f.setAccessible(true);
		    			            	
		    			            	if(f.getType().isAssignableFrom(Integer.class)) {
		    		            			f.set(navpropField, Integer.valueOf(text));
		    		            		} else if(f.getType().isAssignableFrom(String.class)) {
		    		            			f.set(navpropField, text);
		    							} else if(f.getType().isAssignableFrom(LocalDate.class)) {
		    								f.set(navpropField, LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MMM-dd")));
		    							} else if(f.getType().isAssignableFrom(Boolean.class)) {
		    								f.set(navpropField, text.trim().toUpperCase().equals("TRUE"));
		    							} else {
		    								f.set(navpropField, property.getValue());
		    							}
	    			            	}
	    			            }
	    					}
	    				}
	            	}
	            }
			}
    	
    	} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InstantiationException | InvocationTargetException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
    	
		Entity createdEntity = new Entity();
		
		try
		{
	    	createdObject = dataSource.create(object);
	    	for(Field fld : createdObject.getClass().getDeclaredFields()) {
	    		
	    		com.cairone.odataexample.annotations.EdmProperty edmProperty = fld.getAnnotation(com.cairone.odataexample.annotations.EdmProperty.class);
				
	            if (edmProperty != null) {
	            	
	            	fld.setAccessible(true);
	            	
	            	String name = edmProperty.name().isEmpty() ? fld.getName() : edmProperty.name();
	            	Object value = fld.get(createdObject);
	            	createdEntity.addProperty(new Property(null, name, ValueType.PRIMITIVE, value));
	            	
	            	if(keyValues.containsKey(name)) {
	            		keyValues.put(name, value);
	            	}
	            }
	    	}
		} catch(ODataException | IllegalArgumentException | IllegalAccessException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
		String entityID = keyValues.entrySet().stream().map(Entry::toString).collect(Collectors.joining(",", "(", ")"));
		try {
			createdEntity.setId(new URI(edmEntitySet.getName() + entityID));
		} catch (URISyntaxException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
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

		DataSourceProvider dataSourceProvider = dataSourceProviderMap.get(edmEntitySet.getName());
		
		if(dataSourceProvider == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", edmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}
		
		DataSource dataSource = dataSourceProvider.getDataSource();
		
		InputStream requestInputStream = request.getBody();
		ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
		DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
		Entity requestEntity = result.getEntity();

		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		Map<String, UriParameter> keyPredicateMap = keyPredicates
				.stream()
				.collect(Collectors.toMap(UriParameter::getName, x -> x));
		
		List<String> propertiesInJSON = Stream.concat(
				requestEntity.getProperties().stream().map(Property::getName), 
				requestEntity.getNavigationLinks().stream().map(Link::getTitle))
			.collect(Collectors.toList());
		
		Class<?> clazz = entitySetMap.get(edmEntitySet.getName());
		Object object;

    	try {
	    	
			Constructor<?> constructor = clazz.getConstructor();
			object = constructor.newInstance();
	    	
			for (Field fld : clazz.getDeclaredFields()) {
				
				com.cairone.odataexample.annotations.EdmProperty edmProperty = fld.getAnnotation(com.cairone.odataexample.annotations.EdmProperty.class);
				
	            if (edmProperty != null) {
	            	
	            	String propertyName = edmProperty.name().isEmpty() ? fld.getName() : edmProperty.name();
	            	Property property = requestEntity.getProperty(propertyName);
	            	
	            	fld.setAccessible(true);
	            	
	            	if(keyPredicateMap.containsKey(propertyName)) {
	            		String text = keyPredicateMap.get(propertyName).getText();
	            		if(fld.getType().isAssignableFrom(Integer.class)) {
	            			fld.set(object, Integer.valueOf(text));
	            		} else if(fld.getType().isAssignableFrom(String.class)) {
	            			fld.set(object, text);
						} else if(fld.getType().isAssignableFrom(LocalDate.class)) {
							fld.set(object, LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MMM-dd")));
						} else if(fld.getType().isAssignableFrom(Boolean.class)) {
							fld.set(object, text.trim().toUpperCase().equals("TRUE"));
						} else {
							Class<?> enumClazz = fld.getType();
							EdmEnum edmEnum = enumClazz.getAnnotation(EdmEnum.class);
							if(edmEnum != null) {
								// FIXME QUE HACEMOS ?????
							}
						}
	            	} else if(property != null) {
	            		fld.set(object, property.getValue());
	            	}
	            } else {

	            	com.cairone.odataexample.annotations.EdmNavigationProperty edmNavigationProperty = fld.getAnnotation(com.cairone.odataexample.annotations.EdmNavigationProperty.class);
					
	            	if(edmNavigationProperty != null) {
	            		
	            		String propertyName = edmNavigationProperty.name().isEmpty() ? fld.getName() : edmNavigationProperty.name();
	            		
	            		Class<?> fieldClass = fld.getType();

	    				if(Collection.class.isAssignableFrom(fieldClass)) {
	    					
	    				} else {
	    					
	    					fld.setAccessible(true);
	    					Object navpropField = fld.get(object);

	    					com.cairone.odataexample.annotations.EdmEntitySet targetEdmEntitySet = fieldClass.getAnnotation(com.cairone.odataexample.annotations.EdmEntitySet.class);
	    					String targetEntitySetName = targetEdmEntitySet.value();
	    					Class<?> cl = entitySetMap.get(targetEntitySetName);
	    					
	    					if(navpropField == null) {
	    						Constructor<?> c = cl.getConstructor();
		    					navpropField = c.newInstance();
		    					fld.set(object, navpropField);
	    					}
	    					
	    					for(Field f : cl.getDeclaredFields()) {

	    						com.cairone.odataexample.annotations.EdmProperty fieldEdmProperty = f.getAnnotation(com.cairone.odataexample.annotations.EdmProperty.class);
	    						
	    			            if (fieldEdmProperty != null) {
	    			            	
	    			            	String linkName = fieldEdmProperty.name().isEmpty() ? f.getName() : fieldEdmProperty.name();
	    			            	Link link = requestEntity.getNavigationLink(propertyName);
	    			            	Property property = link == null || link.getInlineEntity() == null ? null : link.getInlineEntity().getProperty(linkName);
	    			            	String text = property == null ? null : property.getValue().toString();
	    			            	
	    			            	if(text != null) {
		    			            	f.setAccessible(true);
		    			            	
		    			            	if(f.getType().isAssignableFrom(Integer.class)) {
		    		            			f.set(navpropField, Integer.valueOf(text));
		    		            		} else if(f.getType().isAssignableFrom(String.class)) {
		    		            			f.set(navpropField, text);
		    							} else if(f.getType().isAssignableFrom(LocalDate.class)) {
		    								f.set(navpropField, LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MMM-dd")));
		    							} else if(f.getType().isAssignableFrom(Boolean.class)) {
		    								f.set(navpropField, text.trim().toUpperCase().equals("TRUE"));
		    							} else {
		    								f.set(navpropField, property.getValue());
		    							}
	    			            	}
	    			            }
	    					}
	    				}
	            	}
	            }
			}
    	
    	} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InstantiationException | InvocationTargetException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
    	
		try {
			dataSource.update(object, propertiesInJSON, request.getMethod().equals(HttpMethod.PUT));
		} catch(ODataException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
		response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}

	@Override
	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {

		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		  
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		
		DataSourceProvider dataSourceProvider = dataSourceProviderMap.get(edmEntitySet.getName());
		
		if(dataSourceProvider == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", edmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}
		
		DataSource dataSource = dataSourceProvider.getDataSource();
		
		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
    	
    	try {
    		dataSource.delete(keyPredicates);
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
	
	private ClassPathScanningCandidateComponentProvider createComponentScanner(Iterable<Class<? extends Annotation>> annotationTypes) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		for(Class<? extends Annotation> annotationType : annotationTypes) provider.addIncludeFilter(new AnnotationTypeFilter(annotationType));
		return provider;
    }
}
