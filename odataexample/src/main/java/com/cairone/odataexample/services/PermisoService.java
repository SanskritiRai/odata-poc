package com.cairone.odataexample.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.odataexample.entities.PermisoEntity;
import com.cairone.odataexample.repositories.PermisoRepository;
import com.cairone.odataexample.repositories.UsuarioPermisoRepository;

@Service
public class PermisoService {

	public static final String CACHE_NAME = "PERMISOS";

	@Autowired private PermisoRepository permisoRepository = null;
	@Autowired private UsuarioPermisoRepository usuarioPermisoRepository = null;

	@Transactional(readOnly=true) @Cacheable(CACHE_NAME)
	public PermisoEntity buscarPorNombre(String nombre) {
		
		PermisoEntity permisoEntity = permisoRepository.findOne(nombre);
		return permisoEntity;
	}

}
