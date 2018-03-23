package no.nav.regoppslag.consumer.dokkat;

import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_HIT;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_MISS;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.cacheCounter;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestLatency;

import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokkat.api.tkat020.v3.DokumentTypeInfoToV3;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.regoppslag.config.fasit.DokumenttypeInfoV3Alias;
import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
@Service
@Slf4j
public class Tkat020DokumenttypeInfo {
	private final RestTemplate restTemplate;
	public static final String HENT_DOKKAT_SPRAAKINFO = "hentDokumenttypeInfoSpraak";
	private Histogram.Timer requestTimer;

	@Inject
	public Tkat020DokumenttypeInfo(RestTemplateBuilder restTemplateBuilder,
								   HttpComponentsClientHttpRequestFactory requestFactory,
								   DokumenttypeInfoV3Alias dokumenttypeInfoV3Alias,
								   ServiceuserAlias serviceuserAlias) {
		this.restTemplate = restTemplateBuilder
				.requestFactory(requestFactory)
				.rootUri(dokumenttypeInfoV3Alias.getUrl())
				.basicAuthorization(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.setConnectTimeout(dokumenttypeInfoV3Alias.getConnecttimeoutms())
				.setReadTimeout(dokumenttypeInfoV3Alias.getReadtimeoutms())
				.build();
	}

	public Tkat020DokumenttypeInfo(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Cacheable(HENT_DOKKAT_SPRAAKINFO)
	@Retryable(value = RegOppslagTechnicalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public List<SpraakInfoTo> hentDokumenttypeInfoSpraak(final String dokumenttypeId) throws RegOppslagFunctionalException,RegOppslagTechnicalException{
		
		cacheCounter.labels(HENT_DOKKAT_SPRAAKINFO, "DOKKAT", CACHE_HIT).dec();
		cacheCounter.labels(HENT_DOKKAT_SPRAAKINFO, "DOKKAT", CACHE_MISS).inc();
		
		try {
			Map<String, Object> uriVariables = new HashMap<>();
			uriVariables.put("dokumenttypeId", dokumenttypeId);
			requestTimer = requestLatency.labels(SERVICE_CODE_TREG001, "DOKKAT", "hentDokumenttypeInfoSpraak").startTimer();
			DokumentTypeInfoToV3 dokumentTypeInfoToV3 =  restTemplate.getForObject("/{dokumenttypeId}", DokumentTypeInfoToV3.class, uriVariables);
			if (dokumentTypeInfoToV3.getDokumentProduksjonsInfo() != null && dokumentTypeInfoToV3.getDokumentProduksjonsInfo().getSpraakInfos() != null) {
				return dokumentTypeInfoToV3.getDokumentProduksjonsInfo().getSpraakInfos();
			} else {
				return null;
			}
		} catch (HttpClientErrorException e) {
			if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode())) {
				throw new RegOppslagFunctionalException("Dokkat.TKAT020 failed with bad request for dokumenttypeId:" + dokumenttypeId, e);
			} else {
				throw new RegOppslagTechnicalException("Dokkat.TKAT020 failed. (HttpStatus=" + e.getStatusCode() + ") for dokumenttypeId:" + dokumenttypeId, e);
			}
		} catch (HttpServerErrorException e) {
			throw new RegOppslagTechnicalException("Dokkat.TKAT020 failed with statusCode=" + e.getRawStatusCode(), e);
		} catch (Exception e) {
			throw new RegOppslagTechnicalException("Dokkat.TKAT020 failed with message=" + e.getMessage(), e);
		} finally {
			requestTimer.observeDuration();
		}
	}
}