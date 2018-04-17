package no.nav.regoppslag.config.cache;

import static no.nav.regoppslag.consumer.personv3.PersonV3Consumer.HENT_PERSON;
import static no.nav.regoppslag.nais.NaisContract.STS_CACHE_NAME;

import com.lambdaworks.redis.ClientOptions;
import com.lambdaworks.redis.resource.DefaultClientResources;
import com.lambdaworks.redis.resource.Delay;
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
import org.springframework.data.redis.connection.lettuce.DefaultLettucePool;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePool;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@Profile("nais")
@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {
	
	private static final String MASTER_NAME = "mymaster";
	
	@Value("${app.name}")
	private String appName;
	
	private final CustomRedisSerializer customRedisSerializer = new CustomRedisSerializer();
	
	@Bean
	public CacheManager cacheManager(RedisTemplate redisTemplate) {
		RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate);
		redisCacheManager.setDefaultExpiration(TimeUnit.DAYS.toSeconds(2));
		
		//Remaining caches uses the default value
		Map<String, Long> expiresInSeconds = new HashMap<>();
		expiresInSeconds.put(HENT_PERSON, 10L);
		expiresInSeconds.put(STS_CACHE_NAME, TimeUnit.MINUTES.toSeconds(30));
		
		redisCacheManager.setExpires(expiresInSeconds);
		redisCacheManager.setLoadRemoteCachesOnStartup(true);
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
		factory.setShareNativeConnection(false);
		return factory;
	}
	
	@Bean
	public LettucePool lettucePool() {
		DefaultLettucePool lettucePool = new DefaultLettucePool(new RedisSentinelConfiguration()
				.master(MASTER_NAME).sentinel(new RedisNode("rfs-" + appName, 26379)));
		
		lettucePool.setTimeout(TimeUnit.MILLISECONDS.toMillis(100));
		lettucePool.setPoolConfig(poolConfig());
		//Important. The default value is exponential delay with max reconnect delay of 30 seconds
		lettucePool.setClientResources(DefaultClientResources.builder()
				.reconnectDelay(Delay.exponential(0, 200, TimeUnit.MILLISECONDS, 2))
				.build());
		
		lettucePool.getClient().setOptions(ClientOptions.builder()
				.autoReconnect(true)
				.cancelCommandsOnReconnectFailure(true)
				.pingBeforeActivateConnection(true)
				.suspendReconnectOnProtocolFailure(true)
				.build());
		return lettucePool;
	}
	
	private GenericObjectPoolConfig poolConfig() {
		GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
		genericObjectPoolConfig.setMaxIdle(128);
		genericObjectPoolConfig.setMaxTotal(128);
		genericObjectPoolConfig.setMaxWaitMillis(100);
		genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(1000);
		genericObjectPoolConfig.setTestWhileIdle(false);
		genericObjectPoolConfig.setTestOnBorrow(false);
		genericObjectPoolConfig.setTestOnCreate(false);
		genericObjectPoolConfig.setTestOnReturn(false);
		return genericObjectPoolConfig;
	}
	
	
	@Bean
	@Override
	public CacheErrorHandler errorHandler(){
		return new CustomCacheErrorHandler();
	}
	
}
