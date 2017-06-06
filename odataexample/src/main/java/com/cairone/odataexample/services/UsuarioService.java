package com.cairone.odataexample.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.odataexample.dtos.UsuarioFrmDto;
import com.cairone.odataexample.entities.PermisoEntity;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.entities.PersonaPKEntity;
import com.cairone.odataexample.entities.QUsuarioPermisoEntity;
import com.cairone.odataexample.entities.UsuarioEntity;
import com.cairone.odataexample.entities.UsuarioPKEntity;
import com.cairone.odataexample.entities.UsuarioPermisoEntity;
import com.cairone.odataexample.entities.UsuarioPermisoPKEntity;
import com.cairone.odataexample.repositories.PersonaRepository;
import com.cairone.odataexample.repositories.UsuarioPermisoRepository;
import com.cairone.odataexample.repositories.UsuarioRepository;
import com.mysema.query.types.expr.BooleanExpression;

@Service
public class UsuarioService {

	public static final String CACHE_NAME = "USUARIOS";

	@Autowired private UsuarioRepository usuarioRepository = null;
	@Autowired private UsuarioPermisoRepository usuarioPermisoRepository = null;
	@Autowired private PersonaRepository personaRepository = null;

	@Transactional(readOnly=true) @Cacheable(cacheNames=CACHE_NAME, key="#tipoDocumentoId + '-' + #numeroDocumento")
	public UsuarioEntity buscarPorId(Integer tipoDocumentoId, String numeroDocumento) {
		
		UsuarioEntity usuarioEntity = usuarioRepository.findOne(new UsuarioPKEntity(tipoDocumentoId, numeroDocumento));
		return usuarioEntity;
	}

	@Transactional(readOnly=true) @Cacheable(cacheNames=CACHE_NAME, key="#personaEntity.tipoDocumento.id + '-' + #personaEntity.numeroDocumento")
	public UsuarioEntity buscarPorPersona(PersonaEntity personaEntity) {
		
		UsuarioEntity usuarioEntity = usuarioRepository.findOne(
				new UsuarioPKEntity(
						personaEntity.getTipoDocumento().getId(), 
						personaEntity.getNumeroDocumento()));
		
		return usuarioEntity;
	}

	@Transactional(readOnly=true)
	public List<UsuarioEntity> buscarUsuariosAsignados(PermisoEntity permisoEntity) {
		
		List<UsuarioEntity> usuarioEntities = new ArrayList<UsuarioEntity>();
		
		QUsuarioPermisoEntity q = QUsuarioPermisoEntity.usuarioPermisoEntity;
		BooleanExpression exp = q.permiso.eq(permisoEntity);
		
		Iterable<UsuarioPermisoEntity> iterable = usuarioPermisoRepository.findAll(exp);
		
		for(UsuarioPermisoEntity usuarioPermisoEntity : iterable) {
			usuarioEntities.add(usuarioPermisoEntity.getUsuario());
		}
		
		return usuarioEntities;
	}
	
	@Transactional @CachePut(cacheNames=CACHE_NAME, key="#usuarioFrmDto.tipoDocumentoId + '-' + #usuarioFrmDto.numeroDocumento")
	public UsuarioEntity nuevo(UsuarioFrmDto usuarioFrmDto) throws Exception {
		
		PersonaEntity personaEntity = personaRepository.findOne(new PersonaPKEntity(usuarioFrmDto.getTipoDocumentoId(), usuarioFrmDto.getNumeroDocumento()));

		if(personaEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UNA PERSONA CON ID [TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s]", usuarioFrmDto.getTipoDocumentoId(), usuarioFrmDto.getNumeroDocumento()));
		}
		
		UsuarioEntity usuarioEntity = new UsuarioEntity(personaEntity);
		
		usuarioEntity.setNombreUsuario(usuarioFrmDto.getNombreUsuario());
		usuarioEntity.setClave("DEBE-DEFINIRSE");
		usuarioEntity.setFechaAlta(LocalDate.now());
		usuarioEntity.setCuentaVencida(usuarioFrmDto.getCuentaVencida());
		usuarioEntity.setClaveVencida(usuarioFrmDto.getClaveVencida());
		usuarioEntity.setCuentaBloqueada(usuarioFrmDto.getCuentaBloqueada());
		usuarioEntity.setUsuarioHabilitado(usuarioFrmDto.getUsuarioHabilitado());
		
		usuarioRepository.save(usuarioEntity);
		
		return usuarioEntity;
	}


	@Transactional @CachePut(cacheNames=CACHE_NAME, key="#usuarioFrmDto.tipoDocumentoId + '-' + #usuarioFrmDto.numeroDocumento")
	public UsuarioEntity actualizar(UsuarioFrmDto usuarioFrmDto) throws Exception {

		if(usuarioFrmDto == null || usuarioFrmDto.getTipoDocumentoId() == null || usuarioFrmDto.getNumeroDocumento() == null) {
			throw new Exception("NO SE PUEDE IDENTIFICAR EL USUARIO A ACTUALIZAR");
		}

		UsuarioEntity usuarioEntity = usuarioRepository.findOne(new UsuarioPKEntity(usuarioFrmDto.getTipoDocumentoId(), usuarioFrmDto.getNumeroDocumento()));

		if(usuarioEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UN USUARIO CON ID [TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s]", usuarioFrmDto.getTipoDocumentoId(), usuarioFrmDto.getNumeroDocumento()));
		}
		
		usuarioEntity.setNombreUsuario(usuarioFrmDto.getNombreUsuario());
		usuarioEntity.setCuentaVencida(usuarioFrmDto.getCuentaVencida());
		usuarioEntity.setClaveVencida(usuarioFrmDto.getClaveVencida());
		usuarioEntity.setCuentaBloqueada(usuarioFrmDto.getCuentaBloqueada());
		usuarioEntity.setUsuarioHabilitado(usuarioFrmDto.getUsuarioHabilitado());
		
		usuarioRepository.save(usuarioEntity);
		
		return usuarioEntity;
	}

	@Transactional @Cacheable(cacheNames=CACHE_NAME, key="#tipoDocumentoID + '-' + #numeroDocumento")
	public void borrar(Integer tipoDocumentoID, String numeroDocumento) throws Exception {
		
		UsuarioEntity usuarioEntity = usuarioRepository.findOne(new UsuarioPKEntity(tipoDocumentoID, numeroDocumento));

		if(usuarioEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UN USUARIO CON ID [TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s]", tipoDocumentoID, numeroDocumento));
		}
		
		usuarioRepository.delete(usuarioEntity);
	}

	@Transactional(readOnly=true)
	public UsuarioPermisoEntity buscarUnPermisoAsignado(UsuarioEntity usuarioEntity, PermisoEntity permisoEntity) {
		
		UsuarioPermisoEntity usuarioPermisoEntity = usuarioPermisoRepository.findOne(new UsuarioPermisoPKEntity(usuarioEntity, permisoEntity));
		return usuarioPermisoEntity;
	}
	
	@Transactional
	public UsuarioPermisoEntity asignarPermiso(UsuarioEntity usuarioEntity, PermisoEntity permisoEntity) {
		
		UsuarioPermisoEntity usuarioPermisoEntity = new UsuarioPermisoEntity(usuarioEntity, permisoEntity);
		usuarioPermisoRepository.save(usuarioPermisoEntity);
		
		return usuarioPermisoEntity;
	}

	@Transactional
	public void quitarPermiso(UsuarioEntity usuarioEntity, PermisoEntity permisoEntity) {
		
		UsuarioPermisoEntity usuarioPermisoEntity = usuarioPermisoRepository.findOne(new UsuarioPermisoPKEntity(usuarioEntity, permisoEntity));
		
		if(usuarioPermisoEntity != null) {
			usuarioPermisoRepository.delete(usuarioPermisoEntity);
		}
	}
}
