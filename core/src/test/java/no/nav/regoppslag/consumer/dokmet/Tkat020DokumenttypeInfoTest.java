package no.nav.regoppslag.consumer.dokmet;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dokmet.api.tkat020.DokumentProduksjonsInfoTo;
import no.nav.dokmet.api.tkat020.DokumenttypeInfoTo;
import no.nav.dokmet.api.tkat020.SpraakInfoTo;
import no.nav.regoppslag.config.RestConsumerConfig;
import no.nav.regoppslag.config.WebClientConfig;
import no.nav.regoppslag.config.properties.RegoppslagProperties;
import no.nav.regoppslag.consumer.azure.AzureProperties;
import no.nav.regoppslag.consumer.azure.AzureTestConfig;
import no.nav.regoppslag.consumer.azure.AzureTokenConsumer;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Tkat020DokumenttypeInfo.class,
		WebClientAutoConfiguration.class,
		WebClientConfig.class,
		RestConsumerConfig.class,
		Tkat020DokumenttypeInfoTest.Config.class,
		AzureTestConfig.class})
public class Tkat020DokumenttypeInfoTest {

	private static final String DOKDUMENTYPE_ID = "I000003";
	private static final String LANG1 = "nb";
	private static final String LANG2 = "no";

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Tkat020DokumenttypeInfo tkatConsumer;

	@Mock
	private MicrometerMetrics metrics;


	@BeforeEach
	public void setUp() {
		reset(restTemplate);
	}

	@Test
	public void shouldHentSpraakinfo() {
		when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(DokumenttypeInfoTo.class)))
				.thenReturn(new ResponseEntity<>(defaultResponse(Arrays.asList(LANG1, LANG2)), OK));

		List<SpraakInfoTo> sprakinfos = tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID);

		assertThat(sprakinfos, hasSize(2));
		assertEquals(LANG1, sprakinfos.get(0).getSpraaklag());
		assertEquals(LANG2, sprakinfos.get(1).getSpraaklag());
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenNotFoundAndOnlyRetryOnce() {
		when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(DokumenttypeInfoTo.class)))
				.thenThrow(new HttpClientErrorException(NOT_FOUND));

		RegOppslagTechnicalException e = assertThrows(RegOppslagTechnicalException.class,
				() -> tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID), "Ugyldig input");

		assertThat(e.getMessage(), containsString("TKAT020 feilet med statusKode=404 NOT_FOUND. Fant ingen dokumenttypeInfo med dokumenttypeId=I000003."));
		verify(restTemplate, times(1)).exchange(anyString(), eq(GET), any(HttpEntity.class), eq(DokumenttypeInfoTo.class));
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenServerErrorAndRetry() {
		when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(DokumenttypeInfoTo.class)))
				.thenThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

		RegOppslagTechnicalException e = assertThrows(RegOppslagTechnicalException.class,
				() -> tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID), "Ugyldig input");

		assertThat(e.getMessage(), containsString("TKAT020 feilet teknisk med statusKode=500 INTERNAL_SERVER_ERROR for dokumenttypeId=I000003"));
		verify(restTemplate, times(3)).exchange(anyString(), eq(GET), any(HttpEntity.class), eq(DokumenttypeInfoTo.class));
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenServerException() {
		when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(DokumenttypeInfoTo.class)))
				.thenThrow(new HttpServerErrorException(SERVICE_UNAVAILABLE));

		RegOppslagTechnicalException e = assertThrows(RegOppslagTechnicalException.class,
				() -> tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID), "Ugyldig input");

		assertThat(e.getMessage(), containsString("TKAT020 feilet teknisk med statusKode=503 SERVICE_UNAVAILABLE for dokumenttypeId=I000003"));
		verify(restTemplate, times(3)).exchange(anyString(), eq(GET), any(HttpEntity.class), eq(DokumenttypeInfoTo.class));
	}

	private DokumenttypeInfoTo defaultResponse(List<String> langs) {
		DokumenttypeInfoTo dokumentTypeInfoTo = new DokumenttypeInfoTo();
		DokumentProduksjonsInfoTo dokumentProduksjonsInfo = new DokumentProduksjonsInfoTo();
		List<SpraakInfoTo> list = new ArrayList<>();
		langs.forEach(lang -> {
			SpraakInfoTo spraakInfoTo = new SpraakInfoTo();
			spraakInfoTo.setSpraaklag(lang);
			list.add(spraakInfoTo);
		});
		dokumentProduksjonsInfo.getSpraakInfos().addAll(list);
		dokumentTypeInfoTo.setDokumentProduksjonsInfo(dokumentProduksjonsInfo);
		return dokumentTypeInfoTo;
	}

	@EnableRetry
	@Configuration
	public static class Config {

		@Bean
		public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {

			return new PropertySourcesPlaceholderConfigurer();
		}

		@Bean
		public RestTemplate restTemplate() {
			return mock(RestTemplate.class);
		}

		@Bean
		public RestTemplateBuilder restTemplateBuilder(RestTemplate restTemplate) {
			RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
			when(restTemplateBuilder.requestFactory(HttpComponentsClientHttpRequestFactory.class)).thenReturn(restTemplateBuilder);
			when(restTemplateBuilder.requestFactory(ClientHttpRequestFactory.class)).thenReturn(restTemplateBuilder);
			when(restTemplateBuilder.setConnectTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
			when(restTemplateBuilder.setReadTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
			when(restTemplateBuilder.basicAuthentication(anyString(), anyString())).thenReturn(restTemplateBuilder);
			when(restTemplateBuilder.rootUri(anyString())).thenReturn(restTemplateBuilder);
			when(restTemplateBuilder.build()).thenReturn(restTemplate);
			return restTemplateBuilder;
		}

		@Bean
		public MeterRegistry registry() {
			return new SimpleMeterRegistry();
		}

		@Bean
		public MicrometerMetrics metrics() {
			return new MicrometerMetrics();
		}

		@Bean
		public AzureTokenConsumer azureTokenConsumer(WebClient webClient) {
			AzureProperties azureProperties = new AzureProperties();
			azureProperties.setOpenidConfigTokenEndpoint("https://azuredummy");
			return new AzureTokenConsumer(azureProperties, null, webClient) {
				@Override
				public String getClientCredentialToken(String scope) {
					return "token";
				}

				@Override
				public String getOnBehalfOfToken(String scope, JwtToken token) {
					return "token";
				}
			};
		}

		@Bean
		public AzureProperties azureProperties() {
			AzureProperties azureproperties = new AzureProperties();
			azureproperties.setAppClientId("clientId");
			azureproperties.setAppClientSecret("secret");
			azureproperties.setOpenidConfigTokenEndpoint("url");
			return azureproperties;
		}

		@Bean
		public RegoppslagProperties regoppslagProperties() {
			RegoppslagProperties regoppslagProperties = new RegoppslagProperties();
			RegoppslagProperties.Oauth2SecuredEndpoint dokmet = new RegoppslagProperties.Oauth2SecuredEndpoint();
			dokmet.setScope("scope");
			dokmet.setUrl("https://dokmet");
			regoppslagProperties.getEndpoints().setDokmet(dokmet);
			return regoppslagProperties;
		}
	}

}
