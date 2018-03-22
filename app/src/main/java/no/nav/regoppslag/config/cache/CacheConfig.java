package no.nav.regoppslag.config.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.cache.RedisCacheManager;
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
public class CacheConfig {
	
	private static final String MASTER_NAME = "mymaster";
	
	@Value("${app.name}")
	private String APPNAME;
	
	private CustomRedisSerializer customRedisSerializer = new CustomRedisSerializer();
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	@Bean
	public CacheManager cacheManager(RedisTemplate redisTemplate) {
		RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate);
		
		//default expiration in seconds (equal to two days)
		redisCacheManager.setDefaultExpiration(daysToSeconds(2));
		redisCacheManager.setLoadRemoteCachesOnStartup(true);
		return redisCacheManager;
	}
	
	@Bean
	public JedisConnectionFactory jedisConnectionFactory() {
		JedisConnectionFactory factory = new JedisConnectionFactory(new RedisSentinelConfiguration()
				.master(MASTER_NAME).sentinel(new RedisNode("rfs-" + APPNAME, 26379)));
		factory.setUsePool(true);
		return factory;
	}
	
	@Bean
	public RedisTemplate<?, ?> redisTemplate() {
		RedisTemplate<?, ?> redisTemplate = new RedisTemplate();
		redisTemplate.setConnectionFactory(jedisConnectionFactory());
		
		redisTemplate.setDefaultSerializer(customRedisSerializer);
		redisTemplate.setEnableDefaultSerializer(true);
		
		return redisTemplate;
	}
	
	
	private Long daysToSeconds(Integer days) {
		return days * 24L * 60L * 60L;
	}
	
	
}
