package com.cairone.odataexample.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.odataexample.dtos.PaisFrmDto;
import com.cairone.odataexample.entities.PaisEntity;
import com.cairone.odataexample.exceptions.ServiceException;
import com.cairone.odataexample.repositories.PaisRepository;

@Service
public class PaisService {
	
	public static final String CACHE_NAME = "PAISES";

	@Autowired private PaisRepository paisRepository = null;

	@Transactional(readOnly=true) @Cacheable(CACHE_NAME)
	public PaisEntity buscarPorID(Integer paisID) throws ServiceException {
		
		if(paisID == null) throw new ServiceException(ServiceException.MISSING_DATA, "EL ID DEL PAIS NO PUEDE SER NULO");
		
		PaisEntity paisEntity = paisRepository.findOne(paisID);

		if(paisEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE ENCUENTRA EL PAIS CON ID %s", paisID));
		}
		
		return paisEntity;
	}
	
	@Transactional @CachePut(cacheNames=CACHE_NAME, key="#paisFrmDto.id")
	public PaisEntity nuevo(PaisFrmDto paisFrmDto) {
		
		PaisEntity paisEntity = new PaisEntity();
		
		paisEntity.setId(paisFrmDto.getId());
		paisEntity.setNombre(paisFrmDto.getNombre());
		paisEntity.setPrefijo(paisFrmDto.getPrefijo());
		
		paisRepository.save(paisEntity);
		
		return paisEntity;
	}

	@Transactional @CachePut(cacheNames=CACHE_NAME, key="#paisFrmDto.id")
	public PaisEntity actualizar(PaisFrmDto paisFrmDto) throws ServiceException {
		
		if(paisFrmDto == null || paisFrmDto.getId() == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, "NO SE PUEDE IDENTIFICAR EL PAIS A ACTUALIZAR");
		}
		
		PaisEntity paisEntity = paisRepository.findOne(paisFrmDto.getId());
		
		if(paisEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE PUEDE ENCONTRAR UN PAIS CON ID %s", paisFrmDto.getId()));
		}
		
		paisEntity.setNombre(paisFrmDto.getNombre());
		paisEntity.setPrefijo(paisFrmDto.getPrefijo());
		
		paisRepository.save(paisEntity);
		
		return paisEntity;
	}

	@Transactional @CacheEvict(CACHE_NAME)
	public void borrar(Integer paisID) throws ServiceException {
		
		PaisEntity paisEntity = paisRepository.findOne(paisID);
		
		if(paisEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE PUEDE ENCONTRAR UN TIPO DE DOCUMENTO CON ID %s", paisID));
		}
		
		paisRepository.delete(paisEntity);
	}
}
