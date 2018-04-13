package no.nav.regoppslag.config.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

import javax.inject.Inject;
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
	
	@Inject
	private JedisConnectionFactory jedisConnectionFactory;
	
	@Bean
	public CacheManager cacheManager(RedisTemplate redisTemplate) {
		RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate);
		//default expiration in seconds (equal to two days)
		redisCacheManager.setDefaultExpiration(TimeUnit.DAYS.toSeconds(2));
		redisCacheManager.setLoadRemoteCachesOnStartup(true);
		return redisCacheManager;
	}
	
	@Bean
	public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		
		RedisTemplate<?, ?> redisTemplate = new RedisTemplate();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		
		redisTemplate.setDefaultSerializer(customRedisSerializer);
		redisTemplate.setEnableDefaultSerializer(true);
		return redisTemplate;
	}
	
	@Bean
	public JedisConnectionFactory redisConnectionFactory() {
		
		JedisConnectionFactory factory = new JedisConnectionFactory(new RedisSentinelConfiguration()
				.master(MASTER_NAME).sentinel(new RedisNode("rfs-" + appName, 26379)));
		
		factory.setUsePool(true);
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxTotal(-1); // Sets the cap on the number of objects that can be allocated by the pool (checked out to clients, or idle awaiting checkout) at a given time.
		jedisPoolConfig.setMinIdle(1); //Sets the minimum number of objects allowed in the pool before the evictor thread (if active) spawns new objects.
		jedisPoolConfig.setMaxIdle(5); // controls the maximum number of objects that can sit idle in the pool at any time
		jedisPoolConfig.setMaxWaitMillis(2000); //Sets the maximum amount of time (in milliseconds) the borrowObject() method should block before throwing an exception when the pool is exhausted and the "when exhausted" action is WHEN_EXHAUSTED_BLOCK.
		jedisPoolConfig.setTimeBetweenEvictionRunsMillis(200); //Sets the maximum amount of time (in milliseconds) the borrowObject() method should block before throwing an exception when the pool is exhausted and the "when exhausted" action is WHEN_EXHAUSTED_BLOCK.
		factory.setPoolConfig(jedisPoolConfig);
		//Timeout i ms
		factory.setTimeout(2000);
		return factory;
	}
	
	@Bean
	@Override
	public CacheErrorHandler errorHandler(){
		return new CustomCacheErrorHandler(jedisConnectionFactory);
	}
	
}
