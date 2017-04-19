package com.cairone.odataexample;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
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
import com.cairone.odataexample.annotations.EdmEnum;
import com.cairone.odataexample.annotations.EdmNavigationProperty;
import com.cairone.odataexample.annotations.EdmProperty;

@Component
public class OdataexampleEdmProvider extends CsdlAbstractEdmProvider {

	public static Logger logger = LoggerFactory.getLogger(OdataexampleEdmProvider.class);
	
	public static final String NAME_SPACE = "com.cairone.odataexample";
	public static final String CONTAINER_NAME = "ODataExample";
	public static final String SERVICE_ROOT = "http://localhost:8080/odata/appexample.svc/";
	public static final String DEFAULT_EDM_PACKAGE = "com.cairone.odataexample.edm.resources";
	
	private HashMap<String, Class<?>> entitySetsMap = new HashMap<String, Class<?>>();
	private HashMap<String, Class<?>> enumsMap = new HashMap<String, Class<?>>();
	private HashMap<String, String> entityTypesMap = new HashMap<>();
	
	@PostConstruct
	public void init() {

		ClassPathScanningCandidateComponentProvider provider = createComponentScanner(Arrays.asList(EdmEntitySet.class, EdmEnum.class));
		Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(DEFAULT_EDM_PACKAGE);
		
		try {
			for(BeanDefinition beanDef : beanDefinitions) {
				Class<?> cl = Class.forName(beanDef.getBeanClassName());
				
				EdmEntitySet edmEntitySet = cl.getAnnotation(EdmEntitySet.class);
				EdmEnum edmEnum = cl.getAnnotation(EdmEnum.class);
				
				if(edmEntitySet != null) {
					EdmEntity edmEntity = cl.getAnnotation(EdmEntity.class);
					
					if(edmEntitySet.includedInServiceDocument()) {
						entitySetsMap.put(edmEntitySet.value(), cl);
						entityTypesMap.put(edmEntity.name(), edmEntitySet.value());
					}
				}
				
				if(edmEnum != null) {
					String name = edmEnum.name().isEmpty() ? cl.getSimpleName() : edmEnum.name();
					enumsMap.put(name, cl);
				}
			}
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
		}
	}
	
	@Override
	public List<CsdlSchema> getSchemas() throws ODataException {
		
		// create Schema
		CsdlSchema schema = new CsdlSchema();
		schema.setNamespace(NAME_SPACE);

		// add EntityTypes
		List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
		
		for(Map.Entry<String, Class<?>> entry : entitySetsMap.entrySet()) {
			Class<?> clazz = entry.getValue();
			EdmEntity edmEntity = clazz.getAnnotation(EdmEntity.class);
			String namespace = edmEntity.namespace().isEmpty() ? NAME_SPACE : edmEntity.namespace();
			String name = edmEntity.name().isEmpty() ? clazz.getSimpleName() : edmEntity.name();
			entityTypes.add(getEntityType(getFullQualifiedName(namespace, name)));
		}
		
		entityTypes.sort(new Comparator<CsdlEntityType>() {
			@Override
			public int compare(CsdlEntityType o1, CsdlEntityType o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		schema.setEntityTypes(entityTypes);
		
		// add EnumTypes
		List<CsdlEnumType> enumTypes = new ArrayList<CsdlEnumType>();
		
		for(Map.Entry<String, Class<?>> entry : enumsMap.entrySet()) {
			Class<?> clazz = entry.getValue();
			EdmEnum edmEnum = clazz.getAnnotation(EdmEnum.class);
			String namespace = edmEnum.namespace().isEmpty() ? NAME_SPACE : edmEnum.namespace();
			String name = edmEnum.name().isEmpty() ? clazz.getSimpleName() : edmEnum.name();
			enumTypes.add(getEnumType(getFullQualifiedName(namespace, name)));
		}
		
		schema.setEnumTypes(enumTypes);
		
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
		
		for(Map.Entry<String, Class<?>> entry : entitySetsMap.entrySet()) {
			String entitySet = entry.getKey();
			entitySets.add(getEntitySet(CONTAINER, entitySet));
		}
		
		entitySets.sort(new Comparator<CsdlEntitySet>() {
			@Override
			public int compare(CsdlEntitySet o1, CsdlEntitySet o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
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
		
		Class<?> clazz = entitySetsMap.get(entitySetName);
		EdmEntity edmEntity = clazz.getAnnotation(EdmEntity.class);
		
		List<CsdlNavigationPropertyBinding> navigationPropertyBindings = new ArrayList<>();
		
		for(Field fld : clazz.getDeclaredFields()) {
			
			EdmNavigationProperty navigationProperty = fld.getAnnotation(EdmNavigationProperty.class);
			if(navigationProperty != null) {
				
				String path = navigationProperty.name();
				String target = null;
				
				Class<?> fieldClass = fld.getType();
				
				if(Collection.class.isAssignableFrom(fieldClass)) {
					Type type = fld.getGenericType();
					if (type instanceof ParameterizedType) {
						ParameterizedType pt = (ParameterizedType) type;
						for(Type t : pt.getActualTypeArguments()) {
							Class<?> cl = (Class<?>) t;
							EdmEntitySet edmEntitySetInField = cl.getAnnotation(EdmEntitySet.class);
							target = edmEntitySetInField.value();
						}
					}
				} else {
					EdmEntitySet edmEntitySet = fieldClass.getAnnotation(EdmEntitySet.class);
					target = edmEntitySet.value();
				}
				
				CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding().setPath(path).setTarget(target);
				navigationPropertyBindings.add(navPropBinding);
			}
		}
		
		CsdlEntitySet entitySet = new CsdlEntitySet()
			.setName(entitySetName)
			.setType(getFullQualifiedName(edmEntity.namespace(), edmEntity.name()))
			.setNavigationPropertyBindings(navigationPropertyBindings);
		
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
					} else if(fld.getType().isAssignableFrom(Boolean.class)) {
						propertyType = EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
					} else {
						logger.info("Campo: {}", fld.getName());
						Class<?> enumClazz = fld.getType();
						EdmEnum edmEnum = enumClazz.getAnnotation(EdmEnum.class);
						if(edmEnum != null) {
							String namespace = edmEnum.namespace().isEmpty() ? NAME_SPACE : edmEnum.namespace();
							String name = edmEnum.name().isEmpty() ? enumClazz.getSimpleName() : edmEnum.name();
							propertyType = getFullQualifiedName(namespace, name);
						}
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
	
	public CsdlEnumType getEnumType(FullQualifiedName enumTypeName) throws ODataException {
		
		String edmEnumName = enumTypeName.getName();
		Class<?> clazz = enumsMap.get(edmEnumName);
		
		if(clazz.isEnum()) {

			EdmEnum edmEnum = clazz.getAnnotation(EdmEnum.class);
			List<Object> constants = Arrays.asList(clazz.getEnumConstants());
			
			CsdlEnumType enumType = new CsdlEnumType()
				.setName(edmEnumName)
				.setUnderlyingType(edmEnum.underlyingType());
			
			for(Object obj : constants) {
				Class<?> sub = obj.getClass();
				try {
					Method mth = sub.getDeclaredMethod("getValor");
					String val = mth.invoke(obj).toString();
					enumType.getMembers().add(new CsdlEnumMember().setName(obj.toString()).setValue(val));
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new ODataException(e.getMessage());
				}
			}
			
			return enumType;
		}
		
		throw new ODataException(String.format("%s IS NOT AN ENUMERATION", edmEnumName));
	}
	
	@Override
	public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
		
		String entityTypeNameString = entityTypeName.getName();
		String entitySetName = entityTypesMap.get(entityTypeNameString);
		
		Class<?> clazz = entitySetsMap.get(entitySetName);
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
	
	private ClassPathScanningCandidateComponentProvider createComponentScanner(Iterable<Class<? extends Annotation>> annotationTypes) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		for(Class<? extends Annotation> annotationType : annotationTypes) provider.addIncludeFilter(new AnnotationTypeFilter(annotationType));
		return provider;
    }
}
