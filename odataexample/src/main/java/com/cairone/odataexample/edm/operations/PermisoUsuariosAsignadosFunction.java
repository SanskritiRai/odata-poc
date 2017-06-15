package com.cairone.odataexample.edm.operations;

import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.edm.resources.UsuarioEdm;
import com.cairone.odataexample.entities.PermisoEntity;
import com.cairone.odataexample.entities.UsuarioEntity;
import com.cairone.odataexample.exceptions.ServiceException;
import com.cairone.odataexample.services.PermisoService;
import com.cairone.odataexample.services.UsuarioService;
import com.cairone.odataexample.utils.OdataExceptionParser;
import com.cairone.olingo.ext.jpa.annotations.EdmFunction;
import com.cairone.olingo.ext.jpa.annotations.EdmReturnType;
import com.cairone.olingo.ext.jpa.interfaces.Operation;
import com.google.common.base.CharMatcher;

@Component
@EdmFunction(namespace = OdataExample.NAME_SPACE, name = "UsuariosAsignados", isBound = true, entitySetPath = "Permisos")
@EdmReturnType(type = "Collection(Usuario)")
public class PermisoUsuariosAsignadosFunction implements Operation<List<UsuarioEdm>> {

	@Autowired PermisoService permisoService = null;
	@Autowired UsuarioService usuarioService = null;
	
	@Override
	public List<UsuarioEdm> doOperation(boolean isBound, Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		UriParameter key = keyPredicateMap.get("id");
		String permisoID = CharMatcher.is('\'').trimFrom(key.getText());
		
		try {
			PermisoEntity permisoEntity = permisoService.buscarPorNombre(permisoID);

			List<UsuarioEntity> usuarioEntities = usuarioService.buscarUsuariosAsignados(permisoEntity);
			List<UsuarioEdm> usuarioEdms = UsuarioEdm.crearLista(usuarioEntities);
			
			return usuarioEdms;
		} catch (ServiceException e) {
			throw OdataExceptionParser.parse(e);
		}
	}
}
