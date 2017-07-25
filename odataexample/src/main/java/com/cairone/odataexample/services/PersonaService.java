package com.cairone.odataexample.services;

import java.time.LocalDate;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.cairone.odataexample.dtos.PersonaFrmDto;
import com.cairone.odataexample.entities.LocalidadEntity;
import com.cairone.odataexample.entities.LocalidadPKEntity;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.entities.PersonaFotoEntity;
import com.cairone.odataexample.entities.PersonaPKEntity;
import com.cairone.odataexample.entities.PersonaSectorEntity;
import com.cairone.odataexample.entities.PersonaSectorPKEntity;
import com.cairone.odataexample.entities.QPersonaEntity;
import com.cairone.odataexample.entities.QPersonaSectorEntity;
import com.cairone.odataexample.entities.SectorEntity;
import com.cairone.odataexample.entities.TipoDocumentoEntity;
import com.cairone.odataexample.exceptions.ServiceException;
import com.cairone.odataexample.repositories.LocalidadRepository;
import com.cairone.odataexample.repositories.PersonaFotoRepository;
import com.cairone.odataexample.repositories.PersonaRepository;
import com.cairone.odataexample.repositories.PersonaSectorRepository;
import com.cairone.odataexample.repositories.TipoDocumentoRepository;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.HazelcastXAResource;
import com.hazelcast.transaction.TransactionContext;
import com.mysema.query.types.expr.BooleanExpression;

@Service
public class PersonaService {

	public static final String CACHE_NAME_PERSONA = "PERSONAS";
	public static final String CACHE_NAME_FOTO = "PERSONAS-FOTOS";

	@Autowired private PersonaRepository personaRepository = null;
	@Autowired private PersonaSectorRepository personaSectorRepository = null;
	@Autowired private PersonaFotoRepository personaFotoRepository = null;
	@Autowired private LocalidadRepository localidadRepository = null;
	@Autowired private TipoDocumentoRepository tipoDocumentoRepository = null;
	
	@Autowired private HazelcastInstance hazelcastInstance = null;
	@Autowired private UserTransactionManager tm = null;
	
	@Transactional(readOnly=true) @Cacheable(cacheNames=CACHE_NAME_PERSONA, key="#tipoDocumentoId + '-' + #numeroDocumento")
	public PersonaEntity buscarPorId(Integer tipoDocumentoId, String numeroDocumento) throws ServiceException {
		
		if(tipoDocumentoId == null) throw new ServiceException(ServiceException.MISSING_DATA, "EL ID DEL TIPO DE DOCUMENTO NO PUEDE SER NULO");
		if(numeroDocumento == null) throw new ServiceException(ServiceException.MISSING_DATA, "EL NUMERO DE DOCUMENTO NO PUEDE SER NULO");
		
		PersonaEntity personaEntity = personaRepository.findOne(new PersonaPKEntity(tipoDocumentoId, numeroDocumento));
		
		if(personaEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE ENCUENTRA UNA PERSONA CON CLAVE (TIPO DOCUMENTO=%s,NUMERO DOCUMENTO=%s)", tipoDocumentoId, numeroDocumento));
		}
		
		return personaEntity;
	}
		
	@Transactional @CachePut(cacheNames=CACHE_NAME_PERSONA, key="#personaFrmDto.tipoDocumentoId + '-' + #personaFrmDto.numeroDocumento")
	public PersonaEntity nuevo(PersonaFrmDto personaFrmDto) throws ServiceException {

		LocalidadEntity localidadEntity = localidadRepository.findOne(new LocalidadPKEntity(personaFrmDto.getPaisId(), personaFrmDto.getProvinciaId(), personaFrmDto.getLocalidadId()));

		if(localidadEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE ENCUENTRA LA LOCALIDAD CON ID [PAIS=%s,PROVINCIA=%s,LOCALIDAD=%s]", personaFrmDto.getPaisId(), personaFrmDto.getProvinciaId(), personaFrmDto.getLocalidadId()));
		}
		
		TipoDocumentoEntity tipoDocumentoEntity = tipoDocumentoRepository.findOne(personaFrmDto.getTipoDocumentoId());

		if(tipoDocumentoEntity == null) {
			throw new ServiceException(ServiceException.MISSING_DATA, String.format("NO SE ENCUENTRA UN TIPO DE DOCUMENTO CON ID %s", personaFrmDto.getTipoDocumentoId()));
		}

		PersonaEntity personaEntity = new PersonaEntity(tipoDocumentoEntity, personaFrmDto.getNumeroDocumento());
		
		personaEntity.setNombres(personaFrmDto.getNombres());
		personaEntity.setApellidos(personaFrmDto.getApellidos());
		personaEntity.setApodo(personaFrmDto.getApodo());
		personaEntity.setLocalidad(localidadEntity);
		personaEntity.setFechaAlta(LocalDate.now());
		personaEntity.setGenero(personaFrmDto.getGenero().toGeneroEnum());
		
		personaRepository.save(personaEntity);
		
		return personaEntity;
	}

	@Transactional @CachePut(cacheNames=CACHE_NAME_PERSONA, key="#tipoDocumentoId + '-' + #numeroDocumento")
	public PersonaEntity actualizar(PersonaFrmDto personaFrmDto) throws ServiceException {

		if(personaFrmDto == null || personaFrmDto.getTipoDocumentoId() == null || personaFrmDto.getNumeroDocumento() == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, "NO SE PUEDE IDENTIFICAR LA PERSONA A ACTUALIZAR");
		}
		
		LocalidadEntity localidadEntity = localidadRepository.findOne(new LocalidadPKEntity(personaFrmDto.getPaisId(), personaFrmDto.getProvinciaId(), personaFrmDto.getPaisId()));

		if(localidadEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE ENCUENTRA LA LOCALIDAD CON ID [PAIS=%s,PROVINCIA=%s,LOCALIDAD=%s]", personaFrmDto.getPaisId(), personaFrmDto.getProvinciaId(), personaFrmDto.getLocalidadId()));
		}
		
		PersonaEntity personaEntity = personaRepository.findOne(new PersonaPKEntity(personaFrmDto.getTipoDocumentoId(), personaFrmDto.getNumeroDocumento()));
		
		if(personaEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE PUEDE ENCONTRAR UNA PERSONA CON ID [TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s]", personaFrmDto.getTipoDocumentoId(), personaFrmDto.getNumeroDocumento()));
		}
		
		personaEntity.setNombres(personaFrmDto.getNombres());
		personaEntity.setApellidos(personaFrmDto.getApellidos());
		personaEntity.setApodo(personaFrmDto.getApodo());
		personaEntity.setLocalidad(localidadEntity);
		personaEntity.setGenero(personaFrmDto.getGenero().toGeneroEnum());
		
		personaRepository.save(personaEntity);
		
		return personaEntity;
	}

	@Transactional @CacheEvict(cacheNames=CACHE_NAME_PERSONA, key="#tipoDocumentoId + '-' + #numeroDocumento")
	public void borrar(Integer tipoDocumentoID, String numeroDocumento) throws ServiceException {
		
		PersonaEntity personaEntity = personaRepository.findOne(new PersonaPKEntity(tipoDocumentoID, numeroDocumento));

		if(personaEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE PUEDE ENCONTRAR UNA PERSONA CON ID [TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s]", tipoDocumentoID, numeroDocumento));
		}

		
		personaRepository.delete(personaEntity);
	}
	
	// ***** SECTORES
	
	@Transactional(readOnly=true)
	public Iterable<PersonaSectorEntity> buscarSectores(PersonaEntity personaEntity) {
		
		QPersonaSectorEntity q = QPersonaSectorEntity.personaSectorEntity;
		BooleanExpression exp = q.persona.eq(personaEntity);
		
		Iterable<PersonaSectorEntity> personaSectorEntities = personaSectorRepository.findAll(exp);
		
		return personaSectorEntities;
	}

	@Transactional(readOnly=true)
	public PersonaSectorEntity buscarIngresoEnSector(PersonaEntity personaEntity, SectorEntity sectorEntity) {
		
		QPersonaSectorEntity q = QPersonaSectorEntity.personaSectorEntity;
		BooleanExpression exp = q.pk.eq(new PersonaSectorPKEntity(personaEntity, sectorEntity));
		
		PersonaSectorEntity personaSectorEntity = personaSectorRepository.findOne(exp);
		
		return personaSectorEntity;
	}
	
	@Transactional
	public PersonaSectorEntity ingresarSector(PersonaEntity personaEntity, SectorEntity sectorEntity, LocalDate fechaIngreso) {
		
		PersonaSectorEntity personaSectorEntity = new PersonaSectorEntity();
		
		personaSectorEntity.setPersona(personaEntity);
		personaSectorEntity.setSector(sectorEntity);
		personaSectorEntity.setFechaIngreso(fechaIngreso);
		
		personaSectorRepository.save(personaSectorEntity);
		
		return personaSectorEntity;
	}

	@Transactional
	public void quitarDeSector(PersonaEntity personaEntity, SectorEntity sectorEntity) {
		
		PersonaSectorEntity personaSectorEntity = personaSectorRepository.findOne(new PersonaSectorPKEntity(personaEntity, sectorEntity));
		
		if(personaSectorEntity != null) {
			personaSectorRepository.delete(personaSectorEntity);
		}
	}

	// ***** FOTOS

	@Transactional(readOnly=true) @CachePut(cacheNames=CACHE_NAME_PERSONA, key="#result.tipoDocumento.id + '-' + #result.numeroDocumento")
	public PersonaEntity buscarPorFotoUUID(String uuid) throws ServiceException {
		
		QPersonaEntity qPersona = QPersonaEntity.personaEntity;
		BooleanExpression exp = qPersona.fotoUUID.eq(uuid);
		
		PersonaEntity personaEntity = personaRepository.findOne(exp);

		if(personaEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE ENCUENTRA UNA PERSONA ASOCIADA CON EL ID DE FOTO %s)", uuid));
		}
				
		return personaEntity;
	}
	
	@Transactional(readOnly=true) @Cacheable(value=CACHE_NAME_FOTO, key="#uuid")
	public PersonaFotoEntity buscarFoto(String uuid) throws ServiceException {
		
		PersonaFotoEntity personaFotoEntity = personaFotoRepository.findOne(uuid);
		
		if(personaFotoEntity == null) {
			throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO SE PUEDE ENCONTRAR UNA FOTO DE PERSONA CON ID %s", uuid));
		}
		
		return personaFotoEntity;
	}

	@Transactional(readOnly=true) @Cacheable(value=CACHE_NAME_FOTO, key="#uuid")
	public PersonaFotoEntity buscarFoto(PersonaEntity personaEntity) throws ServiceException {
		return buscarFoto(personaEntity.getFotoUUID());
	}

	@Transactional @CachePut(cacheNames=CACHE_NAME_FOTO, key="#result.uuid")
	public PersonaFotoEntity nuevaFoto(byte[] foto) {
		
		PersonaFotoEntity personaFotoEntity = new PersonaFotoEntity(foto);
		personaFotoRepository.save(personaFotoEntity);
		
		return personaFotoEntity;
	}
	
	@Transactional
	public void asignarFoto(PersonaEntity personaEntity, PersonaFotoEntity personaFotoEntity) throws ServiceException {
		
		// **** ACTUALIZACION DE LA FOTO EN LA BASE DE DATOS
		
		personaEntity.setFotoUUID(personaFotoEntity.getUuid());
		personaRepository.save(personaEntity);
		
		// **** ACTUALIZACION DE LA FOTO EN EL CACHE

		HazelcastXAResource xaResource = hazelcastInstance.getXAResource();
		
		try
		{
			Transaction transaction = tm.getTransaction();
			transaction.enlistResource(xaResource);
			
		} catch(SystemException | IllegalStateException | RollbackException e) {
			throw new ServiceException(ServiceException.TRANSACION_API_EXCEPTION, e.getMessage());
		}
		
		TransactionContext context = xaResource.getTransactionContext();
		TransactionalMap<String, PersonaEntity> map = context.getMap(CACHE_NAME_PERSONA);

		String key = String.format("%s-%s", personaEntity.getTipoDocumento().getId(), personaEntity.getNumeroDocumento());
		map.put(key, personaEntity);
	}
	
	@Transactional @CachePut(cacheNames=CACHE_NAME_FOTO, key="#personaEntity.fotoUUID")
	public PersonaFotoEntity actualizarFoto(PersonaEntity personaEntity, byte[] foto) {

		String uuid = personaEntity.getFotoUUID();
		PersonaFotoEntity fotoEntity = uuid == null ? null : personaFotoRepository.findOne(uuid);
		
		if(fotoEntity == null) {
			fotoEntity = new PersonaFotoEntity(foto);
		} else {
			fotoEntity.setFoto(foto);
		}
		
		personaFotoRepository.save(fotoEntity);
		
		if(uuid == null) {
			personaEntity.setFotoUUID(uuid);
			personaRepository.save(personaEntity);
		}
		
		return fotoEntity;
	}

	@Transactional @CacheEvict(cacheNames=CACHE_NAME_FOTO, key="#personaEntity.fotoUUID")
	public void quitarFoto(PersonaEntity personaEntity) {
		
		if(personaEntity.getFotoUUID() != null) {
			PersonaFotoEntity fotoEntity = personaFotoRepository.findOne(personaEntity.getFotoUUID());
			personaEntity.setFotoUUID(null);
			
			personaRepository.save(personaEntity);
			personaFotoRepository.delete(fotoEntity);
		}
	}
	
	@Transactional @CacheEvict(cacheNames=CACHE_NAME_FOTO, key="#uuid")
	public void quitarFoto(String uuid) {
		
		QPersonaEntity qPersona = QPersonaEntity.personaEntity;
		BooleanExpression exp = qPersona.fotoUUID.eq(uuid);
		
		PersonaEntity personaEntity = personaRepository.findOne(exp);
		
		if(personaEntity != null) {
			personaEntity.setFotoUUID(null);
			personaRepository.save(personaEntity);
		}
		
		PersonaFotoEntity personaFotoEntity = personaFotoRepository.findOne(uuid);
		personaFotoRepository.delete(personaFotoEntity);
	}
}
