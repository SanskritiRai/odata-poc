package com.cairone.odataexample.edm.operations;

import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.entities.SectorEntity;
import com.cairone.odataexample.services.PersonaService;
import com.cairone.odataexample.services.SectorService;
import com.cairone.odataexample.utils.OdataExceptionParser;
import com.cairone.olingo.ext.jpa.annotations.EdmAction;
import com.cairone.olingo.ext.jpa.annotations.EdmParameter;
import com.cairone.olingo.ext.jpa.interfaces.Operation;
import com.google.common.base.CharMatcher;

@Component
@EdmAction(namespace = OdataExample.NAME_SPACE, name = "SectorQuitar", isBound = true, entitySetPath = "Personas") 
public class PersonaSectorQuitarAction implements Operation<Void> {

	@EdmParameter(nullable = false)
	private Integer sectorId = null;
	
	@Autowired
	private SectorService sectorService = null;
	
	@Autowired
	private PersonaService personaService = null; 

	@Override
	public Void doOperation(boolean isBound, Map<String, UriParameter> keyPredicateMap) throws ODataException {

		Integer tipoDocumentoId = Integer.valueOf(keyPredicateMap.get("tipoDocumentoId").getText());
		String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );

		try {		
			PersonaEntity personaEntity = personaService.buscarPorId(tipoDocumentoId, numeroDocumento);
			SectorEntity sectorEntity = sectorService.buscarPorID(sectorId);
		
			sectorService.quitarPersona(sectorEntity, personaEntity);

			return null;
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}
}
