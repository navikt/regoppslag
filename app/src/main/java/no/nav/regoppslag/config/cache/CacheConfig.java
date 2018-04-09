package no.nav.regoppslag.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@Profile("nais")
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig extends CachingConfigurerSupport {
	
	private static final String MASTER_NAME = "mymaster";
	
	@Value("${app.name}")
	private String APPNAME;
	
	private final CustomRedisSerializer customRedisSerializer = new CustomRedisSerializer();
	
	@Bean
	@Override
	public CacheManager cacheManager() {
		try {
			RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate());
			
			//default expiration in seconds (equal to two days)
			redisCacheManager.setDefaultExpiration(daysToSeconds(2));
			redisCacheManager.setLoadRemoteCachesOnStartup(true);
			return redisCacheManager;
		} catch (Exception e){
			log.warn(e.getMessage());
			return new NoOpCacheManager();
		}
		
	}
	
	@Bean
	public RedisTemplate<?, ?> redisTemplate() {
		RedisTemplate<?, ?> redisTemplate = new RedisTemplate();
		redisTemplate.setConnectionFactory(redisConnectionFactory());
		
		redisTemplate.setDefaultSerializer(customRedisSerializer);
		redisTemplate.setEnableDefaultSerializer(true);
		
		return redisTemplate;
	}
	
	@Bean
	@Override
	public CacheErrorHandler errorHandler(){
		return new CustomCacheErrorHandler();
	}
	
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		
		try {
			JedisConnectionFactory factory = new JedisConnectionFactory(new RedisSentinelConfiguration()
					.master(MASTER_NAME).sentinel(new RedisNode("rfs-" + APPNAME, 26379)));
			factory.setUsePool(true);
			return factory;
		} catch (Exception e){
			log.warn("Not connected to redis cache");
			return null;
		}
	
	}
	
	private Long daysToSeconds(Integer days) {
		return days * 24L * 60L * 60L;
	}
	
	
}
