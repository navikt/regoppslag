package no.nav.regoppslag.itest;

import static no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup.HENT_FULLT_NAVN;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.regoppslag.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Rule;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
@ImportAutoConfiguration
public abstract class AbstractIT {

	@Value("${local.url}")
	protected String LOCAL_ENDPOINT_URL;
	
	@Inject
	private CacheManager cacheManager;
	
	@Inject
	protected RestTemplate restTemplate;
	
	@Inject
	protected RestTemplate restTemplateNoHeader;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Rule
	public WireMockRule wireMockRule;
	
	
	@BeforeEach
	public void setUp() {
		clearCachene();
		cacheManager.getCache(HENT_FULLT_NAVN).put("Z991006","en vilkaarlig saksbehandler");
	}
	
	private void clearCachene() {
		cacheManager.getCacheNames().forEach(names -> cacheManager.getCache(names).clear());
	}
	
}
