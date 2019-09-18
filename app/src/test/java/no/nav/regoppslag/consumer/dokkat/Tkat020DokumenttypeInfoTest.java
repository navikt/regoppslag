package no.nav.regoppslag.consumer.dokkat;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokkat.api.tkat020.v3.DokumentProduksjonsInfoToV3;
import no.nav.dokkat.api.tkat020.v3.DokumentTypeInfoToV3;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.regoppslag.config.RestConsumerConfig;
import no.nav.regoppslag.config.fasit.DokumenttypeInfoV3Alias;
import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Tkat020DokumenttypeInfo.class, RestConsumerConfig.class, Tkat020DokumenttypeInfoTest.Config.class})
public class Tkat020DokumenttypeInfoTest {
	
	private static final String DOKDUMENTYPE_ID = "I000003";
	private static final String LANG1 = "nb";
	private static final String LANG2 = "no";
	
	
	@Inject
	private RestTemplate restTemplate;
	
	@Inject
	private Tkat020DokumenttypeInfo tkatConsumer;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void setUp() {
		reset(restTemplate);
		
	}
	
	@Test
	public void shouldHentSpraakinfo() throws Exception {
		when(restTemplate.getForObject(any(String.class), eq(DokumentTypeInfoToV3.class), any(Map.class)))
				.thenReturn(defaultResponse(Arrays.asList(LANG1, LANG2)));
		
		List<SpraakInfoTo> sprakinfos = tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID);
		
		assertThat(sprakinfos, hasSize(2));
		assertThat(sprakinfos.get(0).getSpraaklag(), is(LANG1));
		assertThat(sprakinfos.get(1).getSpraaklag(), is(LANG2));
	}
	
	@Test
	public void shouldThrowTechnicalExceptionWhenNotFoundAndOnlyRetryOnce() throws Exception {
		when(restTemplate.getForObject(any(String.class), eq(DokumentTypeInfoToV3.class), any(Map.class)))
				.thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
		
		try {
			tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID);
			assertFalse("Should throw exception", true);
		} catch (RegOppslagTechnicalException e) {
			assertThat(e.getMessage(), containsString("Dokkat.TKAT020 feilet med statusKode=404 NOT_FOUND. Fant ingen dokumenttypeInfo med dokumenttypeId=I000003. "));
			verify(restTemplate, times(1)).getForObject(any(String.class), eq(DokumentTypeInfoToV3.class), any(Map.class));
		}
	}
	
	@Test
	public void shouldThrowTechnicalExceptionWhenServerErrorAndRetry() throws Exception {
		when(restTemplate.getForObject(any(String.class), eq(DokumentTypeInfoToV3.class), any(Map.class)))
				.thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
		
		try {
			tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID);
			assertFalse("Should throw exception", true);
		} catch (RegOppslagTechnicalException e) {
			assertThat(e.getMessage(), containsString("Dokkat.TKAT020 feilet teknisk med statusKode=500 INTERNAL_SERVER_ERROR for dokumenttypeId=I000003"));
			verify(restTemplate, times(5)).getForObject(any(String.class), eq(DokumentTypeInfoToV3.class), any(Map.class));
		}
	}
	
	@Test
	public void shouldThrowTechnicalExceptionWhenServerException() throws Exception {
		when(restTemplate.getForObject(any(String.class), eq(DokumentTypeInfoToV3.class), any(Map.class)))
				.thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));
		
		try {
			tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID);
			assertFalse("Should throw exception", true);
		} catch (RegOppslagTechnicalException e) {
			assertThat(e.getMessage(), containsString("Dokkat.TKAT020 feilet teknisk med statusKode=503 SERVICE_UNAVAILABLE for dokumenttypeId=I000003"));
			verify(restTemplate, times(5)).getForObject(any(String.class), eq(DokumentTypeInfoToV3.class), any(Map.class));
		}
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
			when(restTemplateBuilder.basicAuthentication(any(String.class), any(String.class))).thenReturn(restTemplateBuilder);
			when(restTemplateBuilder.rootUri(any(String.class))).thenReturn(restTemplateBuilder);
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
		
		
	}
	
}
