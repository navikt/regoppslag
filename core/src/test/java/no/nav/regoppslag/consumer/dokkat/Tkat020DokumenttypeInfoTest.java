package no.nav.regoppslag.consumer.dokkat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dokkat.api.tkat020.v3.DokumentProduksjonsInfoToV3;
import no.nav.dokkat.api.tkat020.v3.DokumentTypeInfoToV3;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.regoppslag.config.RestConsumerConfig;
import no.nav.regoppslag.config.fasit.DokumenttypeInfoV3Alias;
import no.nav.regoppslag.config.fasit.ServiceuserAlias;
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
import org.springframework.http.HttpStatus;
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
import java.util.Map;

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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Tkat020DokumenttypeInfo.class, RestConsumerConfig.class, Tkat020DokumenttypeInfoTest.Config.class})
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
		when(restTemplate.getForObject(anyString(), eq(DokumentTypeInfoToV3.class), any(Map.class)))
				.thenReturn(defaultResponse(Arrays.asList(LANG1, LANG2)));

		List<SpraakInfoTo> sprakinfos = tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID);

		assertThat(sprakinfos, hasSize(2));
		assertEquals(LANG1, sprakinfos.get(0).getSpraaklag());
		assertEquals(LANG2, sprakinfos.get(1).getSpraaklag());
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenNotFoundAndOnlyRetryOnce() {
		when(restTemplate.getForObject(anyString(), eq(DokumentTypeInfoToV3.class), any(Map.class)))
				.thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
		RegOppslagTechnicalException e = assertThrows(RegOppslagTechnicalException.class,
				() -> tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID), "Ugyldig input");
		assertThat(e.getMessage(), containsString("Dokkat.TKAT020 feilet med statusKode=404 NOT_FOUND. Fant ingen dokumenttypeInfo med dokumenttypeId=I000003."));
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenServerErrorAndRetry() {
		when(restTemplate.getForObject(anyString(), eq(DokumentTypeInfoToV3.class), any(Map.class)))
				.thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

		RegOppslagTechnicalException e = assertThrows(RegOppslagTechnicalException.class,
				() -> tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID), "Ugyldig input");

		assertThat(e.getMessage(), containsString("Dokkat.TKAT020 feilet teknisk med statusKode=500 INTERNAL_SERVER_ERROR for dokumenttypeId=I000003"));
		verify(restTemplate, times(5)).getForObject(anyString(), eq(DokumentTypeInfoToV3.class), any(Map.class));
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenServerException() {
		when(restTemplate.getForObject(anyString(), eq(DokumentTypeInfoToV3.class), any(Map.class)))
				.thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

		RegOppslagTechnicalException e = assertThrows(RegOppslagTechnicalException.class,
				() -> tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID), "Ugyldig input");

		assertThat(e.getMessage(), containsString("Dokkat.TKAT020 feilet teknisk med statusKode=503 SERVICE_UNAVAILABLE for dokumenttypeId=I000003"));
		verify(restTemplate, times(5)).getForObject(anyString(), eq(DokumentTypeInfoToV3.class), any(Map.class));
	}

	private DokumentTypeInfoToV3 defaultResponse(List<String> langs) {
		DokumentTypeInfoToV3 dokumentTypeInfoToV3 = new DokumentTypeInfoToV3();
		DokumentProduksjonsInfoToV3 dokumentProduksjonsInfo = new DokumentProduksjonsInfoToV3();
		List<SpraakInfoTo> list = new ArrayList<>();
		langs.forEach(lang -> {
			SpraakInfoTo spraakInfoTo = new SpraakInfoTo();
			spraakInfoTo.setSpraaklag(lang);
			list.add(spraakInfoTo);
		});
		dokumentProduksjonsInfo.getSpraakInfos().addAll(list);
		dokumentTypeInfoToV3.setDokumentProduksjonsInfo(dokumentProduksjonsInfo);
		return dokumentTypeInfoToV3;
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
		public ServiceuserAlias serviceuserAlias() {
			ServiceuserAlias serviceuserAlias = new ServiceuserAlias();
			serviceuserAlias.setPassword("psw");
			serviceuserAlias.setUsername("usr");
			return serviceuserAlias;
		}

		@Bean
		public DokumenttypeInfoV3Alias dokumenttypeInfoV3Alias() {
			DokumenttypeInfoV3Alias dokumenttypeInfoV3Alias = new DokumenttypeInfoV3Alias();
			dokumenttypeInfoV3Alias.setConnecttimeoutms(1000);
			dokumenttypeInfoV3Alias.setReadtimeoutms(1000);
			dokumenttypeInfoV3Alias.setUrl("asdsad");
			return dokumenttypeInfoV3Alias;
		}

		@Bean
		public MeterRegistry registry() {
			return new SimpleMeterRegistry();
		}

		@Bean
		public MicrometerMetrics metrics() {
			return new MicrometerMetrics();
		}

	}

}
