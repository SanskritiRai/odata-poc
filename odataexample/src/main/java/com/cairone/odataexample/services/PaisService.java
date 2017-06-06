package com.cairone.odataexample.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.odataexample.dtos.PaisFrmDto;
import com.cairone.odataexample.entities.PaisEntity;
import com.cairone.odataexample.repositories.PaisRepository;

@Service
public class PaisService {
	
	private static Logger LOG = LoggerFactory.getLogger(PaisService.class);
	public static final String CACHE_NAME = "PAISES";

	@Autowired private PaisRepository paisRepository = null;

	@Transactional(readOnly=true) @Cacheable(CACHE_NAME)
	public PaisEntity buscarPorID(Integer paisID) {
		
		LOG.info(">>> BUSCANDO PAIS CON ID {}", paisID);
		
		PaisEntity paisEntity = paisRepository.findOne(paisID);
		return paisEntity;
	}
	
	@Transactional @CachePut(cacheNames=CACHE_NAME, key="#paisFrmDto.id")
	public PaisEntity nuevo(PaisFrmDto paisFrmDto) {
		
		PaisEntity paisEntity = new PaisEntity();
		
		paisEntity.setId(paisFrmDto.getId());
		paisEntity.setNombre(paisFrmDto.getNombre());
		paisEntity.setPrefijo(paisFrmDto.getPrefijo());
		
		paisRepository.save(paisEntity);
		
		LOG.info(">>> NUEVO PAIS CREADO CON ID {}", paisEntity.getId());
		
		return paisEntity;
	}

	@Transactional @CachePut(cacheNames=CACHE_NAME, key="#paisFrmDto.id")
	public PaisEntity actualizar(PaisFrmDto paisFrmDto) throws Exception {
		
		if(paisFrmDto == null || paisFrmDto.getId() == null) {
			throw new Exception("NO SE PUEDE IDENTIFICAR EL PAIS A ACTUALIZAR");
		}
		
		PaisEntity paisEntity = paisRepository.findOne(paisFrmDto.getId());
		
		if(paisEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UN PAIS CON ID %s", paisFrmDto.getId()));
		}
		
		paisEntity.setNombre(paisFrmDto.getNombre());
		paisEntity.setPrefijo(paisFrmDto.getPrefijo());
		
		paisRepository.save(paisEntity);
		
		return paisEntity;
	}

	@Transactional @CacheEvict(CACHE_NAME)
	public void borrar(Integer paisID) throws Exception {
		
		PaisEntity paisEntity = paisRepository.findOne(paisID);
		
		if(paisEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UN TIPO DE DOCUMENTO CON ID %s", paisID));
		}
		
		paisRepository.delete(paisEntity);
	}
}
