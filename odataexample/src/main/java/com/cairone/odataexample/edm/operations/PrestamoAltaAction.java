package com.cairone.odataexample.edm.operations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.edm.resources.PersonaEdm;
import com.cairone.odataexample.edm.resources.PrestamoCuotaEdm;
import com.cairone.odataexample.edm.resources.PrestamoPendienteEdm;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.services.PersonaService;
import com.cairone.odataexample.services.PrestamoService;
import com.cairone.odataexample.utils.OdataExceptionParser;
import com.cairone.odataexample.utils.RandomString;
import com.cairone.olingo.ext.jpa.annotations.EdmAction;
import com.cairone.olingo.ext.jpa.annotations.EdmParameter;
import com.cairone.olingo.ext.jpa.annotations.EdmReturnType;
import com.cairone.olingo.ext.jpa.interfaces.Operation;
import com.google.common.base.CharMatcher;
import com.hazelcast.core.HazelcastInstance;

@Component
@EdmAction(namespace = OdataExample.NAME_SPACE, name = "PrestamoAltaAction", isBound = true, entitySetPath = "Personas")
@EdmReturnType(type = "PrestamoPendiente")
public class PrestamoAltaAction implements Operation<PrestamoPendienteEdm> {
	
	@EdmParameter(nullable = false)
	private List<PrestamoCuotaEdm> cuotas = null;
	
	@Autowired private PersonaService personaService = null;
	@Autowired private HazelcastInstance hazelcastInstance = null;
	
	@Override
	public PrestamoPendienteEdm doOperation(boolean isBound, Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		Integer tipoDocumentoId = Integer.valueOf(keyPredicateMap.get("tipoDocumentoId").getText());
		String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
		
		try
		{
			PersonaEntity personaEntity = personaService.buscarPorId(tipoDocumentoId, numeroDocumento);
			
			if(personaEntity == null) {
				throw new ODataApplicationException(
					String.format("NO SE ENCUENTRA UNA PERSONA CON LA CLAVE [TIPO DOCUMENTO %s - NUMERO DOCUMENTO %s]", tipoDocumentoId, numeroDocumento), 
					HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
			
			if(cuotas == null || cuotas.isEmpty()) {
				throw new ODataApplicationException("NO HAY UN DETALLE DE CUOTAS PARA EL PRESTAMO", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
			
			PrestamoPendienteEdm prestamoPendienteEdm = new PrestamoPendienteEdm();
			prestamoPendienteEdm.setClave(RandomString.generate(6));
			prestamoPendienteEdm.setFechaAlta(LocalDate.now());
			prestamoPendienteEdm.setPrestamo(BigDecimal.ZERO);
			prestamoPendienteEdm.setIntereses(BigDecimal.ZERO);
			prestamoPendienteEdm.setIva(BigDecimal.ZERO);
			prestamoPendienteEdm.setTotal(BigDecimal.ZERO);
			
			cuotas.forEach(prestamoCuotaEdm -> {
				prestamoPendienteEdm.getCuotas().add(prestamoCuotaEdm);
				
				prestamoPendienteEdm.setPrestamo( prestamoPendienteEdm.getPrestamo().add(prestamoCuotaEdm.getCapital()) );
				prestamoPendienteEdm.setIntereses( prestamoPendienteEdm.getIntereses().add(prestamoCuotaEdm.getInteres()) );
				prestamoPendienteEdm.setIva( prestamoPendienteEdm.getIva().add(prestamoCuotaEdm.getIva()) );
			});
			
			prestamoPendienteEdm.setTotal(prestamoPendienteEdm.getIntereses().add(prestamoPendienteEdm.getIva()).add(prestamoPendienteEdm.getPrestamo()));
			prestamoPendienteEdm.setPersona(new PersonaEdm(personaEntity));
	
			Map<String, PrestamoPendienteEdm> map = hazelcastInstance.getMap(PrestamoService.CACHE_NAME_PENDIENTES);
			map.put(prestamoPendienteEdm.getClave(), prestamoPendienteEdm);
			
			return prestamoPendienteEdm;
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}
}
