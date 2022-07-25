package no.nav.regoppslag.itest;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.nimbusds.jose.JOSEObjectType;
import no.nav.regoppslag.Application;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static no.nav.regoppslag.consumer.azure.AzureAdGraphService.HENT_FULLT_NAVN;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */


@ExtendWith(SpringExtension.class)
@SpringBootTest(
		classes = {Application.class},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
@EnableMockOAuth2Server
public abstract class AbstractIT {

	@Autowired
	private MockOAuth2Server server;

	@Value("${local.url}")
	protected String LOCAL_ENDPOINT_URL;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	protected RestTemplate restTemplate;

	@Autowired
	protected RestTemplate restTemplateNoHeader;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Rule
	public WireMockRule wireMockRule;


	@BeforeEach
	public void setUp() {
		clearCachene();
		cacheManager.getCache(HENT_FULLT_NAVN).put("Z991006", "en vilkaarlig saksbehandler");
	}

	private void clearCachene() {
		cacheManager.getCacheNames().forEach(names -> cacheManager.getCache(names).clear());
	}

	public String token(String subject) {

		String issuerId = "tokenx";
		String audience = "regoppslag";

		return server.issueToken(
				issuerId,
				"regoppslag",
				new DefaultOAuth2TokenCallback(
						issuerId,
						subject,
						JOSEObjectType.JWT.getType(),
						List.of(audience),
						Collections.emptyMap(),
						3600
				)
		).serialize();
	}
}
