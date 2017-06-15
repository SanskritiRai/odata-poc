package com.cairone.odataexample.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.odataexample.entities.PermisoEntity;
import com.cairone.odataexample.exceptions.ServiceException;
import com.cairone.odataexample.repositories.PermisoRepository;

@Service
public class PermisoService {

	public static final String CACHE_NAME = "PERMISOS";

	@Autowired private PermisoRepository permisoRepository = null;

	@Transactional(readOnly=true) @Cacheable(CACHE_NAME)
	public PermisoEntity buscarPorNombre(String nombre) throws ServiceException {
		
		PermisoEntity permisoEntity = permisoRepository.findOne(nombre);

		if(permisoEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE ENCUENTRA EL PERMISO CON ID %s", nombre));
		}
		
		return permisoEntity;
	}

}
