package no.nav.regoppslag.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import no.nav.modig.core.test.FileUtils;
import no.nav.modig.testcertificates.TestCertificates;
import no.nav.regoppslag.Application;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;

@RunWith(SpringRunner.class)
@Import(ApplicationTestConfig.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest")
public class RegOppslagRouteITest {

	@ClassRule
	public static WireMockClassRule personV3Static = new WireMockClassRule(options().dynamicPort());
	@ClassRule
	public static WireMockClassRule organisasjonV4Static = new WireMockClassRule(options().dynamicPort());

//	@Produce(uri = "direct:start")
	@Inject
	private ProducerTemplate producerTemplate;

	@Before
	public void setUp() throws Exception {
		personV3Static.stubFor(post(anyUrl()).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("personv3/personv3-happy.xml")));
		organisasjonV4Static.stubFor(post(anyUrl()).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("organisasjonv4/organisasjonv4-happy.xml")));
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		TestCertificates.setupTemporaryTrustStore("no/nav/modig/testcertificates/truststore.jts", "changeit");
		File file = FileUtils.putInTempFile(TestCertificates.class.getClassLoader()
				.getResourceAsStream("no/nav/modig/testcertificates/keystore.jks"));
		System.setProperty("SRVREGOPPSLAG_CERT_KEYSTORE", file.getAbsolutePath());
		System.setProperty("VIRKSOMHET_PERSON_V3_ENDPOINTURL", "http://localhost:" + personV3Static.port() + "/personv3");
		System.setProperty("VIRKSOMHET_ORGANISASJON_V4_ENDPOINTURL", "http://localhost:" + organisasjonV4Static.port() + "/organisasjonv4");
	}

	@Test
	public void testRoute() throws Exception {
		sendStringMessage("direct:start", readFromClasspath("brevdata.xml").replace("\r", ""));
//		sendStringMessage("direct:start", "Ketill");
	}

	protected void sendStringMessage(String endpoint, final String message) {
		producerTemplate.sendBody(endpoint, message);
	}

	private String readFromClasspath(String Path) throws Exception {
		InputStream inputStream = new ClassPathResource(Path).getInputStream();
		return IOUtils.toString(inputStream, UTF_8);
	}
}
