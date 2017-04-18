package com.cairone.odataexample;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

import com.cairone.odataexample.annotations.EdmEntitySet;

@Component
public class OdataexampleEdmProvider extends CsdlAbstractEdmProvider {

	public static Logger logger = LoggerFactory.getLogger(OdataexampleEdmProvider.class);
	
	public static final String NAME_SPACE = "com.cairone.odataexample";
	public static final String CONTAINER_NAME = "ODataExample";
	public static final String SERVICE_ROOT = "http://localhost:8080/odata/appexample.svc/";
	public static final String DEFAULT_EDM_PACKAGE = "com.cairone.odataexample.edm.resources";
	
	@Override
	public CsdlEntityContainer getEntityContainer() throws ODataException {
		
		ClassPathScanningCandidateComponentProvider provider = createComponentScanner(EdmEntitySet.class);
		Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(DEFAULT_EDM_PACKAGE);
		
		FullQualifiedName CONTAINER = getFullQualifiedName(CONTAINER_NAME);
		
		// create EntitySets
		List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
		
		beanDefinitions.forEach(beanDef -> {
			
			logger.info("EDM: {}", beanDef.getBeanClassName());
			
			try {
				Class<?> cl = Class.forName(beanDef.getBeanClassName());
				EdmEntitySet edmEntitySet = cl.getAnnotation(EdmEntitySet.class);
				
				if(edmEntitySet.includedInServiceDocument()) {
					entitySets.add(getEntitySet(CONTAINER, edmEntitySet.name()));
				}
			} catch (ClassNotFoundException | ODataException e) {
				logger.error(e.getMessage());
			}			
		});
		
		//entitySets.add(getEntitySet(CONTAINER, "Paises"));
		//entitySets.add(getEntitySet(CONTAINER, "Provincias"));

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
		
		FullQualifiedName CONTAINER = getFullQualifiedName(CONTAINER_NAME);
		
		if(entityContainer.equals(CONTAINER)){
			
			if(entitySetName.equals("Paises")){
				CsdlEntitySet entitySet = new CsdlEntitySet();
				entitySet.setName("Paises");
				entitySet.setType(getFullQualifiedName("Pais"));

				return entitySet;
			}
			
			if(entitySetName.equals("Provincias")){
				CsdlEntitySet entitySet = new CsdlEntitySet();
				entitySet.setName("Provincias");
				entitySet.setType(getFullQualifiedName("Provincia"));
				
				return entitySet;
			}
		}

		return null;
	}
	
	@Override
	public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
		
		// this method is called for one of the EntityTypes that are configured in the Schema
		
		FullQualifiedName fqnPais = getFullQualifiedName("Pais");
		FullQualifiedName fqnProvincia = getFullQualifiedName("Provincia");
		
		if(entityTypeName.equals(fqnPais)) {
			return createEntityTypePais();
		}

		if(entityTypeName.equals(fqnProvincia)) {
			return createEntityTypeProvincia();
		}
		
		return null;
	}

	@Override
	public List<CsdlSchema> getSchemas() throws ODataException {
		
		// create Schema
		CsdlSchema schema = new CsdlSchema();
		schema.setNamespace(NAME_SPACE);

		// add EntityTypes
		List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
		entityTypes.add(getEntityType(getFullQualifiedName("Pais")));
		entityTypes.add(getEntityType(getFullQualifiedName("Provincia")));
		
		schema.setEntityTypes(entityTypes);

		// add EntityContainer
		schema.setEntityContainer(getEntityContainer());

		// finally
		List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
		schemas.add(schema);

		return schemas;
	}

	private CsdlEntityType createEntityTypePais() {

		//create EntityType properties
		CsdlProperty propertyId = new CsdlProperty().setName("id").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
		CsdlProperty propertyNombre = new CsdlProperty().setName("nombre").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
		CsdlProperty propertyPrefijo = new CsdlProperty().setName("prefijo").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());

		// create CsdlPropertyRef for Key element
	    CsdlPropertyRef propertyRef = new CsdlPropertyRef().setName("id");

		CsdlNavigationProperty navPropProvincias = new CsdlNavigationProperty()
            .setName("Provincias")
            .setType(getFullQualifiedName("Provincia"))
            .setCollection(true)
            .setPartner("Pais");
		
	    // configure EntityType
	    CsdlEntityType entityType = new CsdlEntityType()
		    .setName("Pais")
		    .setProperties(Arrays.asList(propertyId, propertyNombre , propertyPrefijo))
		    .setKey(Collections.singletonList(propertyRef))
		    .setNavigationProperties(Arrays.asList(navPropProvincias));

	    return entityType;
	}
	
	private CsdlEntityType createEntityTypeProvincia() {

		CsdlProperty propertyId = new CsdlProperty().setName("id").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
		CsdlProperty propertyPaisId = new CsdlProperty().setName("paisId").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
		CsdlProperty propertyNombre = new CsdlProperty().setName("nombre").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
		
		CsdlNavigationProperty navPropPais = new CsdlNavigationProperty()
            .setName("Pais")
            .setType(getFullQualifiedName("Pais"))
            .setNullable(false)
            .setPartner("Provincias");

	    CsdlPropertyRef propertyRefId = new CsdlPropertyRef().setName("id");
	    CsdlPropertyRef propertyRefPaisId = new CsdlPropertyRef().setName("paisId");

	    CsdlEntityType entityType = new CsdlEntityType()
		    .setName("Provincia")
		    .setProperties(Arrays.asList(propertyId, propertyPaisId, propertyNombre))
		    .setKey(Arrays.asList(propertyRefId, propertyRefPaisId))
		    .setNavigationProperties(Arrays.asList(navPropPais));

	    return entityType;
	}

	private FullQualifiedName getFullQualifiedName(String name) {
		return new FullQualifiedName(NAME_SPACE, name);
	}
	
	private ClassPathScanningCandidateComponentProvider createComponentScanner(Class<? extends Annotation> annotationType) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(annotationType));
		return provider;
    }
}
