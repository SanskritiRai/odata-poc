package com.cairone.odataexample.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.odataexample.dtos.TipoDocumentoFrmDto;
import com.cairone.odataexample.entities.TipoDocumentoEntity;
import com.cairone.odataexample.repositories.TipoDocumentoRepository;

@Service
public class TipoDocumentoService {

	public static final String CACHE_NAME = "TIPOS_DOCUMENTOS";

	@Autowired private TipoDocumentoRepository tipoDocumentoRepository = null;
	
	@Transactional(readOnly=true) @Cacheable(CACHE_NAME)
	public TipoDocumentoEntity buscarPorID(Integer tipoDocumentoID) {
		
		TipoDocumentoEntity tipoDocumentoEntity = tipoDocumentoRepository.findOne(tipoDocumentoID);
		return tipoDocumentoEntity;
	}

	@Transactional @CachePut(cacheNames=CACHE_NAME, key="#tipoDocumentoFrmDto.id")
	public TipoDocumentoEntity nuevo(TipoDocumentoFrmDto tipoDocumentoFrmDto) {
		
		TipoDocumentoEntity tipoDocumentoEntity = new TipoDocumentoEntity();
		
		tipoDocumentoEntity.setId(tipoDocumentoFrmDto.getId());
		tipoDocumentoEntity.setNombre(tipoDocumentoFrmDto.getNombre());
		tipoDocumentoEntity.setAbreviatura(tipoDocumentoFrmDto.getAbreviatura());
		
		tipoDocumentoRepository.save(tipoDocumentoEntity);
		
		return tipoDocumentoEntity;
	}

	@Transactional @CachePut(cacheNames=CACHE_NAME, key="#tipoDocumentoFrmDto.id")
	public TipoDocumentoEntity actualizar(TipoDocumentoFrmDto tipoDocumentoFrmDto) throws Exception {
		
		if(tipoDocumentoFrmDto == null || tipoDocumentoFrmDto.getId() == null) {
			throw new Exception("NO SE PUEDE IDENTIFICAR EL TIPO DE DOCUMENTO A ACTUALIZAR");
		}
		
		TipoDocumentoEntity tipoDocumentoEntity = tipoDocumentoRepository.findOne(tipoDocumentoFrmDto.getId());
		
		if(tipoDocumentoEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UN TIPO DE DOCUMENTO CON ID %s", tipoDocumentoFrmDto.getId()));
		}
		
		tipoDocumentoEntity.setNombre(tipoDocumentoFrmDto.getNombre());
		tipoDocumentoEntity.setAbreviatura(tipoDocumentoFrmDto.getAbreviatura());
		
		tipoDocumentoRepository.save(tipoDocumentoEntity);
		
		return tipoDocumentoEntity;
	}

	@Transactional @CacheEvict(CACHE_NAME)
	public void borrar(Integer tipoDocumentoID) throws Exception {
		
		TipoDocumentoEntity tipoDocumentoEntity = tipoDocumentoRepository.findOne(tipoDocumentoID);
		
		if(tipoDocumentoEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UN TIPO DE DOCUMENTO CON ID %s", tipoDocumentoID));
		}
		
		tipoDocumentoRepository.delete(tipoDocumentoEntity);
	}
}
