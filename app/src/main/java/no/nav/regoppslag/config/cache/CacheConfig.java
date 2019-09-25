package no.nav.regoppslag.config.cache;

import static no.nav.regoppslag.consumer.personv3.PersonV3Consumer.HENT_PERSON;
import static no.nav.regoppslag.nais.NaisCheckSTSTokenRetriever.STS_CACHE_NAME;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;

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
	
	static final Duration DEFAULT_CACHE_EXPIRATION_TIME = Duration.ofDays(2L);
	static final Duration HENT_PERSON_CACHE_EXPIRATION_TIME = Duration.ofSeconds(10L);
	static final Duration STS_CACHE_EXPIRATION_TIME = Duration.ofMinutes(50L);

	@Value("${redis.hostname:regoppslag-redis}")
	private String redisHost;

	@Value("${redis.port:6379}")
	private int redisPort;

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, RedisCacheConfiguration defaultCacheConfig) {
		//Remaining caches uses the default value
		HashMap<String, RedisCacheConfiguration> initialConfigs = new HashMap<>();
		initialConfigs.put(STS_CACHE_NAME, generateCacheConfig(STS_CACHE_EXPIRATION_TIME));
		initialConfigs.put(HENT_PERSON, generateCacheConfig(HENT_PERSON_CACHE_EXPIRATION_TIME));

		return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(defaultCacheConfig)
				.withInitialCacheConfigurations(initialConfigs)
				.build();
	}

	@Bean
	RedisTemplate redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setDefaultSerializer(new CustomRedisSerializer<>());
		template.setConnectionFactory(factory);
		return template;
	}

	@Bean
	@Primary
	public RedisCacheConfiguration defaultCacheConfig() {
		return RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(DEFAULT_CACHE_EXPIRATION_TIME)
				.serializeKeysWith(fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(fromSerializer(new CustomRedisSerializer<>()));
	}

	private RedisCacheConfiguration generateCacheConfig(Duration duration) {
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
		redisCacheConfiguration.disableCachingNullValues();
		if(duration != null) {
			redisCacheConfiguration.entryTtl(duration);
		}

		return redisCacheConfiguration;
	}

	@Bean
	public RedisConnectionFactory connectionFactory(LettuceClientConfiguration clientConfiguration) {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
		log.info("Starting redis connection to {} on port {}", redisHost, redisPort);
		config.setHostName(redisHost);
		config.setPort(redisPort);
		LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfiguration);
		factory.setShareNativeConnection(true);
		return factory;
	}

	@Bean
	public LettuceClientConfiguration lettucePoolingClientConfiguration() {
		return LettucePoolingClientConfiguration.builder()
				.poolConfig(poolConfig())
				.clientResources(io.lettuce.core.resource.DefaultClientResources.builder()
						.reconnectDelay(io.lettuce.core.resource.Delay.constant(Duration.ofMillis(200)))
						.build())
				.clientOptions(ClientOptions.builder()
						.autoReconnect(true)
						.cancelCommandsOnReconnectFailure(true)
						.pingBeforeActivateConnection(true)
						.disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
						.suspendReconnectOnProtocolFailure(false)
						.socketOptions(SocketOptions.builder().connectTimeout(Duration.ofMillis(400)).build())
						.build())
				.build();
	}

	private GenericObjectPoolConfig poolConfig() {
		GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
		genericObjectPoolConfig.setTestOnReturn(false);
		genericObjectPoolConfig.setTestOnCreate(false);
		genericObjectPoolConfig.setTestWhileIdle(false);
		genericObjectPoolConfig.setTestOnBorrow(false);
		genericObjectPoolConfig.setMaxTotal(512);
		genericObjectPoolConfig.setMaxIdle(512);
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
