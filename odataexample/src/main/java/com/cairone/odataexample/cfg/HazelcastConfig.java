package com.cairone.odataexample.cfg;

import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cairone.odataexample.mapstores.PrestamoPendienteMapStore;
import com.cairone.odataexample.repositories.HzPrestamoPendienteRepository;
import com.cairone.odataexample.services.PrestamoService;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;

@Configuration
@EnableCaching
public class HazelcastConfig {
	
	private final static Logger LOG = LoggerFactory.getLogger(HazelcastConfig.class);

	@Value("${hz.cluster.name}")
	private String hzClusterName = null;
	
	@Value("${hz.cluster.pwd}")
	private String hzClusterPwd = null;
	
	@Value("${hz.mancenter.url}")
	private String hzMancenterUrl = null;
	
	@Autowired private ApplicationContext context = null;
		
	@Bean
	public Config getConfig() {
		
		LOG.info("Configurando HAZELCAST ...");
		
		Config cfg = new Config();

    	cfg.getManagementCenterConfig()
			.setEnabled(true)
			.setUrl(hzMancenterUrl);
    	
    	HzPrestamoPendienteRepository hzPrestamoPendienteRepository = context.getBean(HzPrestamoPendienteRepository.class);
    	EntityManagerFactory entityManagerFactory = context.getBean(EntityManagerFactory.class);
    	PrestamoPendienteMapStore prestamoPendienteMapStore = new PrestamoPendienteMapStore(entityManagerFactory, hzPrestamoPendienteRepository);
		
    	MapStoreConfig mapStoreConfig = new MapStoreConfig();
        mapStoreConfig.setImplementation(prestamoPendienteMapStore);
        mapStoreConfig.setWriteDelaySeconds(0);
        
    	cfg.getMapConfig(PrestamoService.CACHE_NAME_PENDIENTES).setMapStoreConfig(mapStoreConfig);
    	
    	cfg.getGroupConfig().setName(hzClusterName).setPassword(hzClusterPwd);
    	
    	return cfg;
	}

	@Bean
	public HazelcastInstance getHazelcastInstance() {
    	return Hazelcast.newHazelcastInstance(getConfig());
	}
	
	@Bean
    public CacheManager getCacheManager() {
        return new HazelcastCacheManager(getHazelcastInstance());
    }
}
