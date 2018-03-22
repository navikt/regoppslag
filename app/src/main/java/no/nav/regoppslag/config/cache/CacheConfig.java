package no.nav.regoppslag.config.cache;

import static no.nav.regoppslag.config.security.provider.rest.SecurityConfig.LDAP_CACHE_RS_LOGIN;
import static no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo.HENT_DOKKAT_SPRAAKINFO;
import static no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup.HENT_FULLT_NAVN;
import static no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer.HENT_ENHET_NAVN;
import static no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer.HENT_ORGANISASJON;
import static no.nav.regoppslag.consumer.personv3.PersonV3Consumer.HENT_PERSON;

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
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.util.Arrays;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@Configuration
@EnableCaching
@Profile("nais")
public class CacheConfig {
	private static final String MASTER_NAME = "mymaster";
	private @Value("${app.name}") String APPNAME;
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
		redisCacheManager.setCacheNames(Arrays.asList(HENT_FULLT_NAVN, LDAP_CACHE_RS_LOGIN, HENT_ENHET_NAVN,HENT_DOKKAT_SPRAAKINFO, HENT_PERSON, HENT_ORGANISASJON));
		redisCacheManager.setLoadRemoteCachesOnStartup(false);
		return redisCacheManager;
	}
	
	@Bean
	public JedisConnectionFactory jedisConnectionFactory() {
		JedisConnectionFactory factory = new JedisConnectionFactory(sentinelConfiguration());
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
	
	private RedisSentinelConfiguration sentinelConfiguration () {
		return new RedisSentinelConfiguration()
				.master(MASTER_NAME).sentinel(new RedisNode("rfs-"+APPNAME, 26379));
	}
	
	private Long daysToSeconds(Integer days){
		return days*24L*60L*60L;
	}
	
	
}
