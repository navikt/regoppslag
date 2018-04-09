package no.nav.regoppslag.itest;

import static no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup.HENT_FULLT_NAVN;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.regoppslag.Application;
import no.nav.regoppslag.rest.RegisteroppslagRestController;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
public abstract class AbstractIT {
	
	@Value("${local.server.port}")
	protected String LOCALPORT;
	
	protected String LOCAL_ENDPOINT_URL;
	
	@Inject
	protected RegisteroppslagRestController registeroppslagRestController;
	
	@Inject
	private CacheManager cacheManager;
	
	@Inject
	protected RestTemplate restTemplate;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Rule
	public WireMockRule wireMockRule;
	
	
	@Before
	public void setUp() {
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
		
		LOCAL_ENDPOINT_URL ="http://localhost:"+ LOCALPORT;
		clearCachene();
		cacheManager.getCache(HENT_FULLT_NAVN).put("Z991006","en vilkaarlig saksbehandler");
	}
	
	private void clearCachene() {
		cacheManager.getCacheNames().forEach(names -> cacheManager.getCache(names).clear());
	}
	
	private HttpHeaders createSamlHeader(String token) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("Authorization", "SAML "+new String(Base64.getEncoder().encode(token.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
		return httpHeaders;
	}
	
}
