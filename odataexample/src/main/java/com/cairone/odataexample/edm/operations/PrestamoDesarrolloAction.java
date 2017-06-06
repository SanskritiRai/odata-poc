package com.cairone.odataexample.edm.operations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.dtos.PrestamoDesarrolloParamFrmDto;
import com.cairone.odataexample.edm.resources.PrestamoCuotaEdm;
import com.cairone.odataexample.processes.DesarrolloPrestamoProcess;
import com.cairone.olingo.ext.jpa.annotations.EdmAction;
import com.cairone.olingo.ext.jpa.annotations.EdmParameter;
import com.cairone.olingo.ext.jpa.annotations.EdmReturnType;
import com.cairone.olingo.ext.jpa.interfaces.Operation;

@Component
@EdmAction(namespace = OdataExample.NAME_SPACE, name = "PrestamoDesarrolloAction", isBound = false) 
@EdmReturnType(type = "Collection(PrestamoCuota)")
public class PrestamoDesarrolloAction implements Operation<List<PrestamoCuotaEdm>>{

	@EdmParameter(nullable = false)
	private BigDecimal prestamo = null;
	
	@EdmParameter(nullable = false)
	private String tipoTasa = null;
	
	@EdmParameter(nullable = false)
	private BigDecimal tasa = null;
	
	@EdmParameter(nullable = false)
	private Integer cuotas = null;
	
	@EdmParameter(nullable = false)
	private BigDecimal alicuota = null;
	
	@Autowired
	private DesarrolloPrestamoProcess desarrolloPrestamoProcess = null;
	
	@Override
	public List<PrestamoCuotaEdm> doOperation(boolean isBound, Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		PrestamoDesarrolloParamFrmDto prestamoDesarrolloParamFrmDto = new PrestamoDesarrolloParamFrmDto()
			.setPrestamo(prestamo)
			.setTipoTasa(tipoTasa)
			.setTasa(tasa)
			.setCuotas(cuotas)
			.setAlicuota(alicuota);
		
		List<PrestamoCuotaEdm> prestamoCuotaEdms = (List<PrestamoCuotaEdm>) desarrolloPrestamoProcess.desarrollar(prestamoDesarrolloParamFrmDto);
		
		return prestamoCuotaEdms;
	}

}
