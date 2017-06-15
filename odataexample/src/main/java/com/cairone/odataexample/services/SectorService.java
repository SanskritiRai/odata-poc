package com.cairone.odataexample.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.odataexample.dtos.SectorFrmDto;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.entities.PersonaSectorEntity;
import com.cairone.odataexample.entities.PersonaSectorPKEntity;
import com.cairone.odataexample.entities.QPersonaSectorEntity;
import com.cairone.odataexample.entities.SectorEntity;
import com.cairone.odataexample.exceptions.ServiceException;
import com.cairone.odataexample.repositories.PersonaSectorRepository;
import com.cairone.odataexample.repositories.SectorRepository;
import com.mysema.query.types.expr.BooleanExpression;

@Service
public class SectorService {

	private static final String CACHE_NAME = "SECTOR";

	@Autowired private SectorRepository sectorRepository = null;
	@Autowired private PersonaSectorRepository personaSectorRepository = null;

	@Transactional(readOnly=true) @Cacheable(CACHE_NAME)
	public SectorEntity buscarPorID(Integer sectorID) throws ServiceException {

		if(sectorID == null) throw new ServiceException(ServiceException.MISSING_DATA, "EL ID DEL SECTOR NO PUEDE SER NULO");
		
		SectorEntity sectorEntity = sectorRepository.findOne(sectorID);
		
		if(sectorEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE ENCUENTRA EL SECTOR CON ID %s", sectorID));
		}
		
		return sectorEntity;
	}
	
	@Transactional(readOnly=true)
	public List<SectorEntity> buscarPorPersona(PersonaEntity personaEntity) {
		
		QPersonaSectorEntity q = QPersonaSectorEntity.personaSectorEntity;
		BooleanExpression exp = q.persona.eq(personaEntity);
		Iterable<PersonaSectorEntity> iterable = personaSectorRepository.findAll(exp);
		
		List<SectorEntity> sectorEntities = StreamSupport.stream(iterable.spliterator(), false)
				.map(PersonaSectorEntity::getSector)
				.collect(Collectors.toList());
		
		return sectorEntities;
	}

	@Transactional @CachePut(cacheNames=CACHE_NAME, key="#sectorFrmDto.id")
	public SectorEntity nuevo(SectorFrmDto sectorFrmDto) {
		
		SectorEntity sectorEntity = new SectorEntity();
		
		sectorEntity.setId(sectorFrmDto.getId());
		sectorEntity.setNombre(sectorFrmDto.getNombre());
		
		sectorRepository.save(sectorEntity);
		
		return sectorEntity;
	}

	@Transactional @CachePut(cacheNames=CACHE_NAME, key="#sectorFrmDto.id")
	public SectorEntity actualizar(SectorFrmDto sectorFrmDto) throws ServiceException {
		
		if(sectorFrmDto == null || sectorFrmDto.getId() == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, "NO SE PUEDE IDENTIFICAR EL SECTOR A ACTUALIZAR");
		}
		
		SectorEntity sectorEntity = sectorRepository.findOne(sectorFrmDto.getId());
		
		if(sectorEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE PUEDE ENCONTRAR UN SECTOR CON ID %s", sectorFrmDto.getId()));
		}
		
		sectorEntity.setNombre(sectorFrmDto.getNombre());
		
		sectorRepository.save(sectorEntity);
		
		return sectorEntity;
	}

	@Transactional @CacheEvict(CACHE_NAME)
	public void borrar(Integer sectorID) throws ServiceException {
		
		SectorEntity sectorEntity = sectorRepository.findOne(sectorID);
		
		if(sectorEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE PUEDE ENCONTRAR UN SECTOR CON ID %s", sectorID));
		}
		
		sectorRepository.delete(sectorEntity);
	}
	
	@Transactional
	public PersonaSectorEntity agregarPersona(SectorEntity sectorEntity, PersonaEntity personaEntity) {
		
		PersonaSectorEntity personaSectorEntity = new PersonaSectorEntity();
		
		personaSectorEntity.setPersona(personaEntity);
		personaSectorEntity.setSector(sectorEntity);
		personaSectorEntity.setFechaIngreso(LocalDate.now());
		
		personaSectorRepository.save(personaSectorEntity);
		
		return personaSectorEntity;
	}

	@Transactional
	public void quitarPersona(SectorEntity sectorEntity, PersonaEntity personaEntity) {
		
		PersonaSectorPKEntity pk = new PersonaSectorPKEntity(personaEntity, sectorEntity);
		PersonaSectorEntity personaSectorEntity = personaSectorRepository.findOne(pk);
		
		personaSectorRepository.delete(personaSectorEntity);
	}
}
