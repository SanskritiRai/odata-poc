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
import com.cairone.odataexample.repositories.LocalidadRepository;
import com.cairone.odataexample.repositories.PaisRepository;
import com.cairone.odataexample.repositories.ProvinciaRepository;

@Service
public class LocalidadService {

	public static final String CACHE_NAME = "LOCALIDADES";

	@Autowired private PaisRepository paisRepository = null;
	@Autowired private ProvinciaRepository provinciaRepository = null;
	@Autowired private LocalidadRepository localidadRepository = null;

	@Transactional(readOnly=true) @Cacheable(value=CACHE_NAME, key="#paisID + '-' + #provinciaID + '-' + #localidadID")
	public LocalidadEntity buscarPorID(Integer paisID, Integer provinciaID, Integer localidadID) {
		
		LocalidadEntity localidadEntity = localidadRepository.findOne(new LocalidadPKEntity(paisID, provinciaID, localidadID));
		return localidadEntity;
	}

	@Transactional @CachePut(cacheNames=CACHE_NAME, key="#localidadFrmDto.paisId + '-' + #localidadFrmDto.provinciaId + '-' + #localidadFrmDto.localidadId")
	public LocalidadEntity nuevo(LocalidadFrmDto localidadFrmDto) throws Exception {
		
		ProvinciaEntity provinciaEntity = provinciaRepository.findOne(new ProvinciaPKEntity(localidadFrmDto.getPaisId(), localidadFrmDto.getProvinciaId()));

		if(provinciaEntity == null) {
			throw new Exception(String.format("NO SE ENCUENTRA LA PROVINCIA CON ID [PAIS=%s,PROVINCIA=%s]", localidadFrmDto.getPaisId(), localidadFrmDto.getProvinciaId()));
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
	public LocalidadEntity actualizar(LocalidadFrmDto provinciaFrmDto) throws Exception {
		
		if(provinciaFrmDto == null || provinciaFrmDto.getLocalidadId() == null || provinciaFrmDto.getProvinciaId() == null || provinciaFrmDto.getPaisId() == null) {
			throw new Exception("NO SE PUEDE IDENTIFICAR LA LOCALIDAD A ACTUALIZAR");
		}
		
		LocalidadEntity localidadEntity = localidadRepository.findOne(new LocalidadPKEntity(provinciaFrmDto.getPaisId(), provinciaFrmDto.getProvinciaId(), provinciaFrmDto.getLocalidadId()));
		
		if(localidadEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UNA LOCALIDAD CON ID [PAIS=%s,PROVINCIA=%s,LOCALIDAD=%s]", provinciaFrmDto.getPaisId(), provinciaFrmDto.getProvinciaId(), provinciaFrmDto.getLocalidadId()));
		}
		
		localidadEntity.setNombre(provinciaFrmDto.getNombre());
		localidadEntity.setCp(provinciaFrmDto.getCp());
		localidadEntity.setPrefijo(provinciaFrmDto.getPrefijo());
		
		localidadRepository.save(localidadEntity);
		
		return localidadEntity;
	}

	@Transactional @CacheEvict(value=CACHE_NAME, key="#paisID + '-' + #provinciaID + '-' + #localidadID")
	public void borrar(Integer paisID, Integer provinciaID, Integer localidadID) throws Exception {
		
		LocalidadEntity localidadEntity = localidadRepository.findOne(new LocalidadPKEntity(paisID, provinciaID, localidadID));
		
		if(localidadEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UNA LOCALIDAD CON ID [PAIS=%s,PROVINCIA=%s,LOCALIDAD=%s]", paisID, provinciaID, localidadID));
		}
		
		localidadRepository.delete(localidadEntity);
	}
}
