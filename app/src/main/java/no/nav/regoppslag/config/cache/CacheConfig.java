package no.nav.regoppslag.config.cache;

import static no.nav.regoppslag.consumer.personv3.PersonV3Consumer.HENT_PERSON;
import static no.nav.regoppslag.nais.checkcore.NaisCheckSTSConfig.STS_CACHE_NAME;

import com.lambdaworks.redis.resource.DefaultClientResources;
import com.lambdaworks.redis.resource.Delay;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePool;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Iniitaliserer Redis Cache.
 *
 * @author Ugur Alpay Cenar, Visma Consulting
 */
@Profile("nais")
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig extends CachingConfigurerSupport {
	
	private static final String MASTER_NAME = "mymaster";
	
	public static final Long DEFAULT_CACHE_EXPIRATION_SECONDS = TimeUnit.DAYS.toSeconds(2);
	public static final Long HENT_PERSON_CACHE_EXPIRATION_SECONDS = 10L;
	public static final Long STS_CACHE_EXPIRATION_SECONDS = TimeUnit.MINUTES.toSeconds(50);
	
	@Value("${app.name}")
	private String appName;
	
	private final CustomRedisSerializer customRedisSerializer = new CustomRedisSerializer();
	
	@Bean
	public CacheManager cacheManager(RedisTemplate redisTemplate) {
		RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate);
		redisCacheManager.setDefaultExpiration(DEFAULT_CACHE_EXPIRATION_SECONDS);
		
		//Remaining caches uses the default value
		Map<String, Long> expiresInSeconds = new HashMap<>();
		expiresInSeconds.put(HENT_PERSON, HENT_PERSON_CACHE_EXPIRATION_SECONDS);
		expiresInSeconds.put(STS_CACHE_NAME, STS_CACHE_EXPIRATION_SECONDS);
		
		redisCacheManager.setExpires(expiresInSeconds);
		redisCacheManager.setLoadRemoteCachesOnStartup(true);
		redisCacheManager.setUsePrefix(true);
		return redisCacheManager;
	}
	
	@Bean
	public RedisTemplate<?, ?> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
		RedisTemplate<?, ?> redisTemplate = new RedisTemplate();
		redisTemplate.setConnectionFactory(lettuceConnectionFactory);
		
		redisTemplate.setDefaultSerializer(customRedisSerializer);
		redisTemplate.setEnableDefaultSerializer(true);
		return redisTemplate;
	}
	
	@Bean
	public LettuceConnectionFactory lettuceConnectionFactory(LettucePool lettucePool) {
		LettuceConnectionFactory factory = new LettuceConnectionFactory(lettucePool);
		factory.setShareNativeConnection(true);
		return factory;
	}
	
	@Bean
	public LettucePool lettucePool() {
		CustomLettucePool lettucePool = new CustomLettucePool(new RedisSentinelConfiguration()
				.master(MASTER_NAME).sentinel(new RedisNode("rfs-" + appName, 26379)));
		lettucePool.setClientResources(DefaultClientResources.builder()
				.reconnectDelay(Delay.constant(200, TimeUnit.MILLISECONDS))
				.build());
		lettucePool.setPoolConfig(poolConfig());
		lettucePool.setTimeout(200);
		lettucePool.afterPropertiesSet();
		return lettucePool;
	}
	
	
	public GenericObjectPoolConfig poolConfig() {
		GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
		genericObjectPoolConfig.setTestOnReturn(false);
		genericObjectPoolConfig.setTestOnCreate(false);
		genericObjectPoolConfig.setTestWhileIdle(false);
		genericObjectPoolConfig.setTestOnBorrow(false);
		genericObjectPoolConfig.setMaxTotal(256);
		genericObjectPoolConfig.setMaxIdle(256);
		genericObjectPoolConfig.setMinIdle(0);
		genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(3000);
		genericObjectPoolConfig.setMinEvictableIdleTimeMillis(6000);
		return genericObjectPoolConfig;
	}
	
	@Bean
	@Override
	public CacheErrorHandler errorHandler(){
		return new CustomCacheErrorHandler();
	}
	
}
