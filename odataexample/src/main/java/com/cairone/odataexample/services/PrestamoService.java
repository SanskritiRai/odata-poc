package com.cairone.odataexample.services;

import java.time.LocalDate;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.edm.resources.PrestamoPendienteEdm;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.entities.PrestamoCuotaEntity;
import com.cairone.odataexample.entities.PrestamoEntity;
import com.cairone.odataexample.entities.QPrestamoEntity;
import com.cairone.odataexample.exceptions.ServiceException;
import com.cairone.odataexample.repositories.PrestamoCuotaRepository;
import com.cairone.odataexample.repositories.PrestamoRepository;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;
import com.mysema.query.jpa.impl.JPAQuery;

@Component
public class PrestamoService {

	public static final String CACHE_NAME_PENDIENTES = "PRESTAMOS-PENDIENTES";
	public static final String CACHE_NAME_DESARROLLO = "PRESTAMOS-DESARROLLO";
	
	@Autowired private HazelcastInstance hazelcastInstance = null;
	@Autowired private EntityManagerFactory entityManagerFactory;
	
	@Autowired private PersonaService personaService = null;
	
	@Autowired private PrestamoRepository prestamoRepository = null;
	@Autowired private PrestamoCuotaRepository prestamoCuotaRepository = null;
	
	@Transactional
	public PrestamoEntity aprobar(String clave) throws ServiceException {
		
		try
		{
			TransactionContext context = hazelcastInstance.newTransactionContext();
			context.beginTransaction();

			TransactionalMap<String, PrestamoPendienteEdm> map = context.getMap(CACHE_NAME_PENDIENTES);
			PrestamoPendienteEdm prestamoPendienteEdm = map.get(clave);
			
			if(prestamoPendienteEdm == null) {
				throw new ServiceException(ServiceException.ENTITY_NOT_FOUND, String.format("NO EXISTE UN PRESTAMO PENDIENTE CON CLAVE %s", clave));
			}
			
			PersonaEntity personaEntity = personaService.buscarPorId(
					prestamoPendienteEdm.getPersona().getTipoDocumentoId(), prestamoPendienteEdm.getPersona().getNumeroDocumento());
			
			JPAQuery query = new JPAQuery(entityManagerFactory.createEntityManager());
			QPrestamoEntity qPrestamo = QPrestamoEntity.prestamoEntity;
			
			Integer prestamoID = query.from(qPrestamo).uniqueResult(qPrestamo.id.max());
			if(prestamoID == null) prestamoID = 1; else prestamoID++;
			
			PrestamoEntity prestamoEntity = new PrestamoEntity();
			prestamoEntity.setId(prestamoID);
			prestamoEntity.setPersona(personaEntity);
			prestamoEntity.setFechaAlta(prestamoPendienteEdm.getFechaAlta());
			prestamoEntity.setFechaAprobacion(LocalDate.now());
			prestamoEntity.setCapitalPrestado(prestamoPendienteEdm.getPrestamo());
			prestamoEntity.setIntereses(prestamoPendienteEdm.getIntereses());
			prestamoEntity.setIva(prestamoPendienteEdm.getIva());
			prestamoEntity.setTotalIntereses(prestamoEntity.getIntereses().add(prestamoEntity.getIva()));
			prestamoEntity.setCuotas(prestamoPendienteEdm.getCuotas().size());
			
			prestamoRepository.save(prestamoEntity);
			
			prestamoPendienteEdm.getCuotas().forEach(prestamoCuotaEdm -> {
				
				PrestamoCuotaEntity prestamoCuotaEntity = new PrestamoCuotaEntity(prestamoEntity, prestamoCuotaEdm.getNumero());
				
				prestamoCuotaEntity.setCapital(prestamoCuotaEdm.getCapital());
				prestamoCuotaEntity.setIntereses(prestamoCuotaEdm.getInteres());
				prestamoCuotaEntity.setIva(prestamoCuotaEdm.getIva());
				prestamoCuotaEntity.setInteresesGravados(prestamoCuotaEdm.getInteresesGravados());
				prestamoCuotaEntity.setMonto(prestamoCuotaEdm.getMonto());
				prestamoCuotaEntity.setSaldoCapital(prestamoCuotaEdm.getSaldoCapital());
				
				prestamoCuotaRepository.save(prestamoCuotaEntity);
			});
			
			map.remove(clave);
			context.commitTransaction();
			
			return prestamoEntity;
			
		} catch(IllegalStateException e) {
			throw new ServiceException(ServiceException.TRANSACION_API_EXCEPTION, e.getMessage());
		}
	}
}
