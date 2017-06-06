package com.cairone.odataexample.cfg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;

@Configuration
@EnableCaching
public class HazelcastConfig {

	@Value("${hz.cluster.name}")
	private String hzClusterName = null;
	
	@Value("${hz.cluster.pwd}")
	private String hzClusterPwd = null;
	
	@Value("${hz.mancenter.url}")
	private String hzMancenterUrl = null;
	
	@Bean
	public Config getConfig() {
		
		Config cfg = new Config();

    	cfg.getManagementCenterConfig()
			.setEnabled(true)
			.setUrl(hzMancenterUrl);
		
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
