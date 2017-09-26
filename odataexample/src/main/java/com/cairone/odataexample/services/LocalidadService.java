package com.cairone.odataexample.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.odataexample.dtos.LocalidadFrmDto;
import com.cairone.odataexample.entities.LocalidadEntity;
import com.cairone.odataexample.entities.LocalidadPKEntity;
import com.cairone.odataexample.entities.ProvinciaEntity;
import com.cairone.odataexample.entities.ProvinciaPKEntity;
import com.cairone.odataexample.exceptions.ServiceException;
import com.cairone.odataexample.repositories.LocalidadRepository;
import com.cairone.odataexample.repositories.ProvinciaRepository;

@Service
public class LocalidadService {

	public static final String CACHE_NAME = "LOCALIDADES";

	@Autowired private ProvinciaRepository provinciaRepository = null;
	@Autowired private LocalidadRepository localidadRepository = null;

	@Transactional(readOnly=true) @Cacheable(value=CACHE_NAME, key="#paisID + '-' + #provinciaID + '-' + #localidadID")
	public LocalidadEntity buscarPorID(Integer paisID, Integer provinciaID, Integer localidadID) throws ServiceException {
		
		if(paisID == null) throw new ServiceException(ServiceException.MISSING_DATA, "EL ID DEL PAIS NO PUEDE SER NULO");
		if(provinciaID == null) throw new ServiceException(ServiceException.MISSING_DATA, "EL ID DE LA PROVINCIA NO PUEDE SER NULO");
		if(localidadID == null) throw new ServiceException(ServiceException.MISSING_DATA, "EL ID DE LA LOCALIDAD NO PUEDE SER NULO");
		
		LocalidadEntity localidadEntity = localidadRepository.findOne(new LocalidadPKEntity(paisID, provinciaID, localidadID));
		
		if(localidadEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE ENCUENTRA LA LOCALIDAD CON CLAVE [PAIS: %s,PROVINCIA: %s,LOCALIDAD: %s]", paisID, provinciaID, localidadID));
		}
		
		return localidadEntity;
	}

	@Transactional @CachePut(cacheNames=CACHE_NAME, key="#localidadFrmDto.paisId + '-' + #localidadFrmDto.provinciaId + '-' + #localidadFrmDto.localidadId")
	public LocalidadEntity nuevo(LocalidadFrmDto localidadFrmDto) throws ServiceException {
		
		ProvinciaEntity provinciaEntity = provinciaRepository.findOne(new ProvinciaPKEntity(localidadFrmDto.getPaisId(), localidadFrmDto.getProvinciaId()));

		if(provinciaEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE ENCUENTRA LA PROVINCIA CON ID [PAIS=%s,PROVINCIA=%s]", localidadFrmDto.getPaisId(), localidadFrmDto.getProvinciaId()));
		}
		
		LocalidadEntity localidadEntity = new LocalidadEntity();
		
		localidadEntity.setId(localidadFrmDto.getLocalidadId());
		localidadEntity.setProvincia(provinciaEntity);
		localidadEntity.setNombre(localidadFrmDto.getNombre());
		localidadEntity.setCp(localidadFrmDto.getCp());
		localidadEntity.setPrefijo(localidadFrmDto.getPrefijo());
		
		localidadRepository.save(localidadEntity);
		
		return localidadEntity;
	}

	@Transactional @CachePut(cacheNames=CACHE_NAME, key="#localidadFrmDto.paisId + '-' + #localidadFrmDto.provinciaId + '-' + #localidadFrmDto.localidadId")
	public LocalidadEntity actualizar(LocalidadFrmDto localidadFrmDto) throws ServiceException {
		
		if(localidadFrmDto == null || localidadFrmDto.getLocalidadId() == null || localidadFrmDto.getProvinciaId() == null || localidadFrmDto.getPaisId() == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, "NO SE PUEDE IDENTIFICAR LA LOCALIDAD A ACTUALIZAR");
		}
		
		LocalidadEntity localidadEntity = localidadRepository.findOne(new LocalidadPKEntity(localidadFrmDto.getPaisId(), localidadFrmDto.getProvinciaId(), localidadFrmDto.getLocalidadId()));
		
		if(localidadEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE PUEDE ENCONTRAR UNA LOCALIDAD CON ID [PAIS=%s,PROVINCIA=%s,LOCALIDAD=%s]", localidadFrmDto.getPaisId(), localidadFrmDto.getProvinciaId(), localidadFrmDto.getLocalidadId()));
		}
		
		localidadEntity.setNombre(localidadFrmDto.getNombre());
		localidadEntity.setCp(localidadFrmDto.getCp());
		localidadEntity.setPrefijo(localidadFrmDto.getPrefijo());
		
		localidadRepository.save(localidadEntity);
		
		return localidadEntity;
	}

	@Transactional @CacheEvict(value=CACHE_NAME, key="#paisID + '-' + #provinciaID + '-' + #localidadID")
	public void borrar(Integer paisID, Integer provinciaID, Integer localidadID) throws ServiceException {
		
		LocalidadEntity localidadEntity = localidadRepository.findOne(new LocalidadPKEntity(paisID, provinciaID, localidadID));
		
		if(localidadEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE PUEDE ENCONTRAR UNA LOCALIDAD CON ID [PAIS=%s,PROVINCIA=%s,LOCALIDAD=%s]", paisID, provinciaID, localidadID));
		}
		
		localidadRepository.delete(localidadEntity);
	}
}
