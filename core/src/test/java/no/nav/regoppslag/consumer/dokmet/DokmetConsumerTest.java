package no.nav.regoppslag.consumer.dokmet;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.nav.dokmet.api.tkat020.DokumentProduksjonsInfoTo;
import no.nav.dokmet.api.tkat020.DokumenttypeInfoTo;
import no.nav.dokmet.api.tkat020.SpraakInfoTo;
import no.nav.regoppslag.config.RestConsumerConfig;
import no.nav.regoppslag.config.WebClientConfig;
import no.nav.regoppslag.config.properties.RegoppslagProperties;
import no.nav.regoppslag.consumer.azure.AzureProperties;
import no.nav.regoppslag.consumer.azure.AzureTestConfig;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration;
import org.springframework.boot.webclient.test.autoconfigure.AutoConfigureWebClient;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.wiremock.spring.EnableWireMock;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(
		webEnvironment = RANDOM_PORT
)
@EnableConfigurationProperties({
		RegoppslagProperties.class
})
@EnableResilientMethods
@EnableWireMock
@ActiveProfiles("itest")
@AutoConfigureWebClient
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DokmetConsumer.class,
		WebClientAutoConfiguration.class,
		WebClientConfig.class,
		RestConsumerConfig.class,
		AzureTestConfig.class})
public class DokmetConsumerTest {

	private static final String DOKDUMENTYPE_ID = "I000003";
	private static final String LANG1 = "nb";
	private static final String LANG2 = "no";
	public static final String DOKUMENTINFO_URL_REGEX = "/rest/dokumenttypeinfo/.*";

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	private DokmetConsumer tkatConsumer;

	@MockitoBean
	AzureProperties azureProperties;

	@Test
	public void shouldHentSpraakinfo() throws JsonProcessingException {
		String body = jsonMapper.writeValueAsString(defaultResponse(LANG1, LANG2));
		stubFor(get(urlMatching(DOKUMENTINFO_URL_REGEX))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(body)
				));

		List<SpraakInfoTo> sprakinfos = tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID);

		verify(1, getRequestedFor(urlMatching(DOKUMENTINFO_URL_REGEX)));
		assertThat(sprakinfos, hasSize(2));
		assertEquals(LANG1, sprakinfos.get(0).getSpraaklag());
		assertEquals(LANG2, sprakinfos.get(1).getSpraaklag());
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenNotFoundAndNotRetry() {
		stubFor(get(urlMatching(DOKUMENTINFO_URL_REGEX))
				.willReturn(aResponse().withStatus(NOT_FOUND.value())));
		RegOppslagFunctionalException e = assertThrows(RegOppslagFunctionalException.class,
				() -> tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID), "Ugyldig input");
		assertThat(e.getMessage(), containsString("TKAT020 feilet med statusKode=404 NOT_FOUND. Fant ingen dokumenttypeInfo med dokumenttypeId=I000003."));
		assertThat(e.getHttpStatusCode(), equalTo(INTERNAL_SERVER_ERROR));
		verify(1, getRequestedFor(urlMatching(DOKUMENTINFO_URL_REGEX)));
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenServerErrorAndRetry() {
		stubFor(get(urlMatching(DOKUMENTINFO_URL_REGEX))
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));

		RegOppslagTechnicalException e = assertThrows(RegOppslagTechnicalException.class,
				() -> tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID), "Ugyldig input");

		assertThat(e.getMessage(), containsString("TKAT020 feilet teknisk med statusKode=500 INTERNAL_SERVER_ERROR for dokumenttypeId=I000003"));
		verify(3, getRequestedFor(urlMatching(DOKUMENTINFO_URL_REGEX)));
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenServerException() {
		stubFor(get(urlMatching(DOKUMENTINFO_URL_REGEX))
				.willReturn(aResponse().withStatus(SERVICE_UNAVAILABLE.value())));

		RegOppslagTechnicalException e = assertThrows(RegOppslagTechnicalException.class,
				() -> tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID), "Ugyldig input");

		assertThat(e.getMessage(), containsString("TKAT020 feilet teknisk med statusKode=503 SERVICE_UNAVAILABLE for dokumenttypeId=I000003"));
		verify(3, getRequestedFor(urlMatching(DOKUMENTINFO_URL_REGEX)));
	}

	private DokumenttypeInfoTo defaultResponse(String... langs) {
		DokumenttypeInfoTo dokumentTypeInfoTo = new DokumenttypeInfoTo();
		DokumentProduksjonsInfoTo dokumentProduksjonsInfo = new DokumentProduksjonsInfoTo();
		List<SpraakInfoTo> list = new ArrayList<>();
		List.of(langs).forEach(lang -> {
			SpraakInfoTo spraakInfoTo = new SpraakInfoTo();
			spraakInfoTo.setSpraaklag(lang);
			list.add(spraakInfoTo);
		});
		dokumentProduksjonsInfo.getSpraakInfos().addAll(list);
		dokumentTypeInfoTo.setDokumentProduksjonsInfo(dokumentProduksjonsInfo);
		return dokumentTypeInfoTo;
	}

}
