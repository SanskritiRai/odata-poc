package com.cairone.odataexample;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.annotations.EdmEntity;
import com.cairone.odataexample.annotations.EdmEntitySet;
import com.cairone.odataexample.annotations.EdmNavigationProperty;
import com.cairone.odataexample.annotations.EdmProperty;

@Component
public class OdataexampleEdmProvider extends CsdlAbstractEdmProvider {

	public static Logger logger = LoggerFactory.getLogger(OdataexampleEdmProvider.class);
	
	public static final String NAME_SPACE = "com.cairone.odataexample";
	public static final String CONTAINER_NAME = "ODataExample";
	public static final String SERVICE_ROOT = "http://localhost:8080/odata/appexample.svc/";
	public static final String DEFAULT_EDM_PACKAGE = "com.cairone.odataexample.edm.resources";
	
	private HashMap<String, Class<?>> classesMap = new HashMap<String, Class<?>>();
	private HashMap<String, String> entityTypesMap = new HashMap<>();

	@Override
	public List<CsdlSchema> getSchemas() throws ODataException {

		ClassPathScanningCandidateComponentProvider provider = createComponentScanner(EdmEntitySet.class);
		Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(DEFAULT_EDM_PACKAGE);
		
		try {
			for(BeanDefinition beanDef : beanDefinitions) {
				Class<?> cl = Class.forName(beanDef.getBeanClassName());
				
				EdmEntitySet edmEntitySet = cl.getAnnotation(EdmEntitySet.class);
				EdmEntity edmEntity = cl.getAnnotation(EdmEntity.class);
				
				if(edmEntitySet.includedInServiceDocument()) {
					classesMap.put(edmEntitySet.value(), cl);
					entityTypesMap.put(edmEntity.name(), edmEntitySet.value());
				}
			}
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
		}
		
		// create Schema
		CsdlSchema schema = new CsdlSchema();
		schema.setNamespace(NAME_SPACE);

		// add EntityTypes
		List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
		
		for(Map.Entry<String, Class<?>> entry : classesMap.entrySet()) {
			Class<?> clazz = entry.getValue();
			EdmEntity edmEntity = clazz.getAnnotation(EdmEntity.class);
			entityTypes.add(getEntityType(getFullQualifiedName(edmEntity.name())));
		}
		
		schema.setEntityTypes(entityTypes);
		
		//FIXME
		// Para enumeraciones: http://stackoverflow.com/questions/36056649/how-to-create-an-enum-entity-type-in-olingo-odata-v4-java-api
		//schema.setEnumTypes(enumTypes);
		
		// add EntityContainer
		schema.setEntityContainer(getEntityContainer());

		// finally
		List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
		schemas.add(schema);

		return schemas;
	}

	@Override
	public CsdlEntityContainer getEntityContainer() throws ODataException {

		FullQualifiedName CONTAINER = getFullQualifiedName(CONTAINER_NAME);
		
		List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
		
		for(Map.Entry<String, Class<?>> entry : classesMap.entrySet()) {
			String entitySet = entry.getKey();
			entitySets.add(getEntitySet(CONTAINER, entitySet));
		}
		
		// create EntityContainer
		CsdlEntityContainer entityContainer = new CsdlEntityContainer();
		entityContainer.setName(CONTAINER_NAME);
		entityContainer.setEntitySets(entitySets);

		return entityContainer;
	}

	@Override
	public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) throws ODataException {
		
		// This method is invoked when displaying the Service Document at e.g. http://localhost:8080/DemoService/DemoService.svc
		
		FullQualifiedName CONTAINER = new FullQualifiedName(NAME_SPACE, CONTAINER_NAME);
		
		if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
			
			CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
	        entityContainerInfo.setContainerName(CONTAINER);
	        return entityContainerInfo;
	    }

	    return null;
	}
	
	@Override
	public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
		
		Class<?> clazz = classesMap.get(entitySetName);
		EdmEntity edmEntity = clazz.getAnnotation(EdmEntity.class);
		
		CsdlEntitySet entitySet = new CsdlEntitySet();
		entitySet.setName(entitySetName);
		entitySet.setType(getFullQualifiedName(edmEntity.namespace(), edmEntity.name()));

		return entitySet;
	}
	
	private List<CsdlNavigationProperty> getCsdlNavigationProperties(Field[] fields) {
		
		List<CsdlNavigationProperty> csdlNavigationProperties = new ArrayList<CsdlNavigationProperty>();
		
		for (Field fld : fields) {
			
			EdmNavigationProperty navigationProperty = fld.getAnnotation(EdmNavigationProperty.class);
			if(navigationProperty != null) {
				
				String navigationPropertyTypeName = navigationProperty.type().isEmpty() ? null : navigationProperty.type();
				boolean isCollection = false;
				
				if(navigationPropertyTypeName == null) {
					Class<?> fieldClass = fld.getType();
					
					if(Collection.class.isAssignableFrom(fieldClass)) {
						isCollection = true;
						Type type = fld.getGenericType();
						if (type instanceof ParameterizedType) {
							ParameterizedType pt = (ParameterizedType) type;
							for(Type t : pt.getActualTypeArguments()) {
								Class<?> clazz = (Class<?>) t;
								EdmEntity edmEntity = clazz.getAnnotation(EdmEntity.class);
								navigationPropertyTypeName = edmEntity == null ? null : edmEntity.name();
							}
						}						
					} else {
						EdmEntity edmEntity = fieldClass.getAnnotation(EdmEntity.class);
						navigationPropertyTypeName = edmEntity == null ? null : edmEntity.name();
					}
				}
				
				CsdlNavigationProperty csdlNavigationProperty = new CsdlNavigationProperty()
			        .setName(navigationProperty.name())
			        .setType(getFullQualifiedName(navigationPropertyTypeName))
			        .setCollection(isCollection)
			        .setNullable(navigationProperty.nullable());
				
				if(!navigationProperty.partner().isEmpty()) {
					csdlNavigationProperty.setPartner(navigationProperty.partner());
				}
				
				csdlNavigationProperties.add(csdlNavigationProperty);
			}
		}
		
		return csdlNavigationProperties;
	}
	
	private List<CsdlProperty> getCsdlProperties(Field[] fields) {
		
		List<CsdlProperty> csdlProperties = new ArrayList<CsdlProperty>();
		
		for (Field fld : fields) {
			
			EdmProperty property = fld.getAnnotation(EdmProperty.class);
			if(property != null) {
				
				String propertyName = property.name().isEmpty() ? fld.getName() : property.name();
				FullQualifiedName propertyType = null;
				
				if(property.type().isEmpty()) {					
					if(fld.getType().isAssignableFrom(Integer.class)) {
						propertyType = EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(String.class)) {
						propertyType = EdmPrimitiveTypeKind.String.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(LocalDate.class)) {
						propertyType = EdmPrimitiveTypeKind.Date.getFullQualifiedName();
					}
				} else {
					switch(property.type()) {
					case "Edm.Int32":
						propertyType = EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
						break;
					case "Edm.String":
						propertyType = EdmPrimitiveTypeKind.String.getFullQualifiedName();
						break;
					case "Edm.Date":
						propertyType = EdmPrimitiveTypeKind.Date.getFullQualifiedName();
						break;
					}
				}
				
				CsdlProperty csdlProperty = new CsdlProperty().setName(propertyName).setType(propertyType);
				csdlProperties.add(csdlProperty);
			}
		}
		
		return csdlProperties;
	}
	
	@Override
	public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
		
		String entityTypeNameString = entityTypeName.getName();
		String entitySetName = entityTypesMap.get(entityTypeNameString);
		
		Class<?> clazz = classesMap.get(entitySetName);
		EdmEntity edmEntity = clazz.getAnnotation(EdmEntity.class);

		logger.info("EdmEntity: {}", edmEntity.name());
		
		Field[] fields = clazz.getDeclaredFields();
		
		List<CsdlProperty> csdlProperties = getCsdlProperties(fields);
		List<CsdlNavigationProperty> csdlNavigationProperties = getCsdlNavigationProperties(fields);
		List<CsdlPropertyRef> csdlPropertyRefs = Arrays.asList(edmEntity.key()).stream().map(key -> new CsdlPropertyRef().setName(key)).collect(Collectors.toList());
		
		CsdlEntityType entityType = new CsdlEntityType()
			.setName(edmEntity.name())
    		.setProperties(csdlProperties)
    		.setKey(csdlPropertyRefs)
    		.setNavigationProperties(csdlNavigationProperties);

		return entityType;
	}

	private FullQualifiedName getFullQualifiedName(String namespace, String name) {
		return new FullQualifiedName(namespace, name);
	}
	
	private FullQualifiedName getFullQualifiedName(String name) {
		return getFullQualifiedName(NAME_SPACE, name);
	}
	
	private ClassPathScanningCandidateComponentProvider createComponentScanner(Class<? extends Annotation> annotationType) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(annotationType));
		return provider;
    }
}
