package com.cairone.odataexample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;
import org.springframework.stereotype.Component;

@Component
public class OdataexampleEdmProvider extends CsdlAbstractEdmProvider {

	public static final String NAME_SPACE = "com.cairone.odataexample";
	public static final String CONTAINER_NAME = "ODataExample";
	
	@Override
	public CsdlEntityContainer getEntityContainer() throws ODataException {
		
		// create EntitySets
		List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
		entitySets.add(getEntitySet(new FullQualifiedName(NAME_SPACE, CONTAINER_NAME), "Paises"));

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
		
		FullQualifiedName CONTAINER = new FullQualifiedName(NAME_SPACE, CONTAINER_NAME);
		FullQualifiedName ENTITY_SET_TYPE = new FullQualifiedName(NAME_SPACE, "Pais");
		
		if(entityContainer.equals(CONTAINER)){
			if(entitySetName.equals("Paises")){
				CsdlEntitySet entitySet = new CsdlEntitySet();
				entitySet.setName("Paises");
				entitySet.setType(ENTITY_SET_TYPE);

				return entitySet;
		    }
		  }

		return null;
	}

	@Override
	public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
		
		// this method is called for one of the EntityTypes that are configured in the Schema
		
		String entityTypeNamePAIS = "Pais";
		FullQualifiedName fqn = new FullQualifiedName(NAME_SPACE, entityTypeNamePAIS);
		
		if(entityTypeName.equals(fqn)) {
			
			//create EntityType properties
			CsdlProperty propertyId = new CsdlProperty().setName("id").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
			CsdlProperty propertyNombre = new CsdlProperty().setName("nombre").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty propertyPrefijo = new CsdlProperty().setName("prefijo").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());

			// create CsdlPropertyRef for Key element
		    CsdlPropertyRef propertyRef = new CsdlPropertyRef();
		    propertyRef.setName("id");

		    // configure EntityType
		    CsdlEntityType entityType = new CsdlEntityType();
		    entityType.setName(entityTypeNamePAIS);
		    entityType.setProperties(Arrays.asList(propertyId, propertyNombre , propertyPrefijo));
		    entityType.setKey(Collections.singletonList(propertyRef));

		    return entityType;
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
		
		FullQualifiedName ENTITY_SET_TYPE_PAIS = new FullQualifiedName(NAME_SPACE, "Pais");
		entityTypes.add(getEntityType(ENTITY_SET_TYPE_PAIS));
		
		schema.setEntityTypes(entityTypes);

		// add EntityContainer
		schema.setEntityContainer(getEntityContainer());

		// finally
		List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
		schemas.add(schema);

		return schemas;
	}
}
