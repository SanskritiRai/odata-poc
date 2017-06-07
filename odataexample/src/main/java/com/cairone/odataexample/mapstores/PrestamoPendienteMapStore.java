package com.cairone.odataexample.mapstores;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import com.cairone.odataexample.edm.resources.PrestamoPendienteEdm;
import com.cairone.odataexample.entities.HzPrestamoPendienteEntity;
import com.cairone.odataexample.repositories.HzPrestamoPendienteRepository;
import com.hazelcast.core.MapStore;

@Transactional
public class PrestamoPendienteMapStore implements MapStore<String, PrestamoPendienteEdm> {

	private HzPrestamoPendienteRepository hzPrestamoPendienteRepository = null;
	private EntityManagerFactory entityManagerFactory;
	
	public PrestamoPendienteMapStore(EntityManagerFactory entityManagerFactory, HzPrestamoPendienteRepository hzPrestamoPendienteRepository) {
		super();
		this.entityManagerFactory = entityManagerFactory;
		this.hzPrestamoPendienteRepository = hzPrestamoPendienteRepository;
	}

	@Override
	public synchronized PrestamoPendienteEdm load(String key) {
		
		try {
			
			HzPrestamoPendienteEntity hzPrestamoPendienteEntity = hzPrestamoPendienteRepository.findOne(key);
			
			if(hzPrestamoPendienteEntity == null) {
				return null;
			}
			
			byte[] buf = hzPrestamoPendienteEntity.getSerializedObject();
	
			ObjectInputStream ois = null;
			
			if (buf != null) {
				ois = new ObjectInputStream(new ByteArrayInputStream(buf));
			}
			
			Object deserializedObject = ois.readObject();
			PrestamoPendienteEdm prestamoPendienteEdm = null;
			
			if(deserializedObject instanceof PrestamoPendienteEdm) {
				prestamoPendienteEdm = (PrestamoPendienteEdm) deserializedObject;
			}
			
			return prestamoPendienteEdm;
			
		} catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
	}

	@Override
	public synchronized Map<String, PrestamoPendienteEdm> loadAll(Collection<String> keys) {
		Map<String, PrestamoPendienteEdm> map = new HashMap<String, PrestamoPendienteEdm>();
		for(String key : keys) map.put(key, load(key));
		return map;
	}

	@Override
	public synchronized Iterable<String> loadAllKeys() {
		
		EntityManager em = entityManagerFactory.createEntityManager();
		TypedQuery<String> query = em.createQuery("SELECT c.clave FROM HzPrestamoPendienteEntity c", String.class);
		List<String> results = query.getResultList();
		
		return results;
	}

	@Override
	public synchronized void store(String key, PrestamoPendienteEdm value) {

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(value);
			oos.close();
			
			byte[] bin = baos.toByteArray();
			baos.close();
			
			HzPrestamoPendienteEntity hzPrestamoPendienteEntity = new HzPrestamoPendienteEntity(key);
			hzPrestamoPendienteEntity.setSerializedObject(bin);
			
			hzPrestamoPendienteRepository.save(hzPrestamoPendienteEntity);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void storeAll(Map<String, PrestamoPendienteEdm> map) {
		for (Map.Entry<String, PrestamoPendienteEdm> entry : map.entrySet()) {
            store(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public synchronized void delete(String key) {
		HzPrestamoPendienteEntity hzPrestamoPendienteEntity = new HzPrestamoPendienteEntity(key);
		hzPrestamoPendienteRepository.delete(hzPrestamoPendienteEntity);
	}

	@Override
	public synchronized void deleteAll(Collection<String> keys) {
		for (String key : keys) delete(key);
	}

}
