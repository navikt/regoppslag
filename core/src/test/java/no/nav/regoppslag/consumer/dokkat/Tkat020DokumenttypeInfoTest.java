package no.nav.regoppslag.consumer.dokkat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dokkat.api.tkat020.v4.DokumentProduksjonsInfoToV4;
import no.nav.dokkat.api.tkat020.v4.DokumentTypeInfoToV4;
import no.nav.dokkat.api.tkat020.v4.SpraakInfoToV4;
import no.nav.regoppslag.config.DokumenttypeInfoProperties;
import no.nav.regoppslag.config.RestConsumerConfig;
import no.nav.regoppslag.config.properties.RegoppslagProperties;
import no.nav.regoppslag.consumer.azure.AzureProperties;
import no.nav.regoppslag.consumer.azure.AzureTestConfig;
import no.nav.regoppslag.consumer.azure.TokenConsumer;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Tkat020DokumenttypeInfo.class,
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
		when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(DokumentTypeInfoToV4.class)))
				.thenReturn(new ResponseEntity<>(defaultResponse(Arrays.asList(LANG1, LANG2)), HttpStatus.OK));

		List<SpraakInfoToV4> sprakinfos = tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID);

		assertThat(sprakinfos, hasSize(2));
		assertEquals(LANG1, sprakinfos.get(0).getSpraaklag());
		assertEquals(LANG2, sprakinfos.get(1).getSpraaklag());
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenNotFoundAndOnlyRetryOnce() {
		when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(DokumentTypeInfoToV4.class)))
				.thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
		RegOppslagTechnicalException e = assertThrows(RegOppslagTechnicalException.class,
				() -> tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID), "Ugyldig input");
		assertThat(e.getMessage(), containsString("Dokkat.TKAT020 feilet med statusKode=404 NOT_FOUND. Fant ingen dokumenttypeInfo med dokumenttypeId=I000003."));
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenServerErrorAndRetry() {
		when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(DokumentTypeInfoToV4.class)))
				.thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

		RegOppslagTechnicalException e = assertThrows(RegOppslagTechnicalException.class,
				() -> tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID), "Ugyldig input");

		assertThat(e.getMessage(), containsString("Dokkat.TKAT020 feilet teknisk med statusKode=500 INTERNAL_SERVER_ERROR for dokumenttypeId=I000003"));
		verify(restTemplate, times(5)).exchange(anyString(), eq(GET), any(HttpEntity.class), eq(DokumentTypeInfoToV4.class));
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenServerException() {
		when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(DokumentTypeInfoToV4.class)))
				.thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

		RegOppslagTechnicalException e = assertThrows(RegOppslagTechnicalException.class,
				() -> tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID), "Ugyldig input");

		assertThat(e.getMessage(), containsString("Dokkat.TKAT020 feilet teknisk med statusKode=503 SERVICE_UNAVAILABLE for dokumenttypeId=I000003"));
		verify(restTemplate, times(5)).exchange(anyString(), eq(GET), any(HttpEntity.class), eq(DokumentTypeInfoToV4.class));
	}

	private DokumentTypeInfoToV4 defaultResponse(List<String> langs) {
		DokumentTypeInfoToV4 dokumentTypeInfoToV4 = new DokumentTypeInfoToV4();
		DokumentProduksjonsInfoToV4 dokumentProduksjonsInfo = new DokumentProduksjonsInfoToV4();
		List<SpraakInfoToV4> list = new ArrayList<>();
		langs.forEach(lang -> {
			SpraakInfoToV4 spraakInfoTo = new SpraakInfoToV4();
			spraakInfoTo.setSpraaklag(lang);
			list.add(spraakInfoTo);
		});
		dokumentProduksjonsInfo.getSpraakInfos().addAll(list);
		dokumentTypeInfoToV4.setDokumentProduksjonsInfo(dokumentProduksjonsInfo);
		return dokumentTypeInfoToV4;
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
		public RegoppslagProperties.Serviceuser serviceuserAlias() {
			RegoppslagProperties.Serviceuser serviceuser = new RegoppslagProperties.Serviceuser();
			serviceuser.setPassword("psw");
			serviceuser.setUsername("usr");
			return serviceuser;
		}

		@Bean
		public DokumenttypeInfoProperties dokumenttypeInfoProperties() {
			DokumenttypeInfoProperties dokumenttypeInfoProperties = new DokumenttypeInfoProperties();
			dokumenttypeInfoProperties.setConnecttimeoutms(1000);
			dokumenttypeInfoProperties.setReadtimeoutms(1000);
			dokumenttypeInfoProperties.setUrl("asdsad");
			return dokumenttypeInfoProperties;
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
		public TokenConsumer tokenConsumer() {
			return (String s) -> new String();
		}

		@Bean
		public AzureProperties azureProperties() {
			AzureProperties azureproperties = new AzureProperties();
			azureproperties.setAppScopedigdirkrr("scope");
			azureproperties.setAppScopeDokmet("scope");
			azureproperties.setAppClientId("clientId");
			azureproperties.setAppClientSecret("secret");
			azureproperties.setOpenidConfigTokenEndpoint("url");
			return azureproperties;
		}
	}

}
