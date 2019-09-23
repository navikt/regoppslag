package no.nav.regoppslag.config.cache;

import static no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo.HENT_DOKKAT_SPRAAKINFO;
import static no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer.HENT_ENHET_NAVN;
import static no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer.HENT_ORGANISASJON;
import static no.nav.regoppslag.consumer.personv3.PersonV3Consumer.HENT_PERSON;
import static no.nav.regoppslag.nais.NaisCheckSTSTokenRetriever.STS_CACHE_NAME;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.JuridiskEnhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
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
	
	static final Duration DEFAULT_CACHE_EXPIRATION_TIME = Duration.ofSeconds(2L);
	static final Duration HENT_PERSON_CACHE_EXPIRATION_TIME = Duration.ofSeconds(10L);
	static final Duration STS_CACHE_EXPIRATION_TIME = Duration.ofSeconds(50L);

	@Value("${redis.hostname:regoppslag-redis}")
	private String redisHost;

	@Value("${redis.port:6379}")
	private int redisPort;

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		//Remaining caches uses the default value
		HashMap<String, RedisCacheConfiguration> initialConfigs = new HashMap<>();
		initialConfigs.put(STS_CACHE_NAME, generateCacheConfig(STS_CACHE_EXPIRATION_TIME, null));
		initialConfigs.put(HENT_PERSON, generateCacheConfig(HENT_PERSON_CACHE_EXPIRATION_TIME, Bruker.class));
		initialConfigs.put(HENT_DOKKAT_SPRAAKINFO, generateCacheConfig(DEFAULT_CACHE_EXPIRATION_TIME, SpraakInfoTo.class));
		initialConfigs.put(HENT_ENHET_NAVN, generateCacheConfig(DEFAULT_CACHE_EXPIRATION_TIME, Organisasjonsenhet.class));
		initialConfigs.put(HENT_ORGANISASJON, generateCacheConfig(DEFAULT_CACHE_EXPIRATION_TIME, JuridiskEnhet.class));

		return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(generateCacheConfig(DEFAULT_CACHE_EXPIRATION_TIME, null))
				.withInitialCacheConfigurations(initialConfigs)
				.build();
	}

	private RedisCacheConfiguration generateCacheConfig(Duration duration, Class cachedClass) {
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
		if(duration != null) {
			redisCacheConfiguration.entryTtl(duration);
		}
		if(cachedClass != null) {
			redisCacheConfiguration.serializeKeysWith(fromSerializer(new StringRedisSerializer()));
			redisCacheConfiguration.serializeValuesWith(fromSerializer(new Jackson2JsonRedisSerializer<>(cachedClass)));
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
