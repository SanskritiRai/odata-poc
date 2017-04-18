package com.cairone.odataexample;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.entities.ProvinciaEntity;
import com.cairone.odataexample.services.ProvinciaService;

@Component
public class ProvinciaOdataEntityProcessor implements EntityProcessor {
	
	private OData odata;
	private ServiceMetadata serviceMetadata;
	
	@Autowired private ProvinciaService provinciaService = null;
	
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
	    
	    if(edmEntityType.getName().equals("Provincia")) {
	    	
	    	UriParameter uriParameterPais = keyPredicates.stream().filter(uriParameter -> uriParameter.getName().equals("id")).findFirst().get();
	    	UriParameter uriParameterProvincia = keyPredicates.stream().filter(uriParameter -> uriParameter.getName().equals("paisId")).findFirst().get();
	    	
	    	Integer paisID = Integer.valueOf(uriParameterPais.getText());
	    	Integer provinciaID = Integer.valueOf(uriParameterProvincia.getText());
	    	
	    	ProvinciaEntity provinciaEntity = provinciaService.buscarPorID(paisID, provinciaID);
	    	
	    	final Entity entity = new Entity()
				.addProperty(new Property(null, "id", ValueType.PRIMITIVE, provinciaEntity.getId()))
				.addProperty(new Property(null, "paisId", ValueType.PRIMITIVE, provinciaEntity.getPais().getId()))
				.addProperty(new Property(null, "nombre", ValueType.PRIMITIVE, provinciaEntity.getNombre()));
			entity.setId(createId("Paises", provinciaEntity.getId(), provinciaEntity.getPais().getId()));
			
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
		
	}

	@Override
	public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
	}

	@Override
	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
		
	}

	private URI createId(String entitySetName, Object id, Object paisId) {
	    try {
	        return new URI(entitySetName + "(id=" + String.valueOf(id) + ",paisId=" + String.valueOf(paisId) + ")");
	    } catch (URISyntaxException e) {
	        throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
	    }
	}
}
