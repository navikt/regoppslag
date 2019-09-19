package no.nav.regoppslag.consumer.dokkat;

import static no.nav.regoppslag.metrics.MetricLabels.DOK_CONSUMER;
import static no.nav.regoppslag.metrics.MetricLabels.PROCESS_CODE;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokkat.api.tkat020.v3.DokumentTypeInfoToV3;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.regoppslag.config.fasit.DokumenttypeInfoV3Alias;
import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.Metrics;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Collections;
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
	private static final String DOKKAT = "DOKKAT";
	private static final String TKAT020_TEKNISKFEIL = "TKAT020 - Teknisk feil";
	public static final String TKAT020_UGYLDIG_INPUT = "TKAT020 - Ugyldig input";
	private static final String TKAT020_INGEN_TREFF = "TKAT020 - Ingen treff";
	private MicrometerMetrics metrics;

	@Inject
	public Tkat020DokumenttypeInfo(RestTemplateBuilder restTemplateBuilder,
								   HttpComponentsClientHttpRequestFactory requestFactory,
								   DokumenttypeInfoV3Alias dokumenttypeInfoV3Alias,
								   ServiceuserAlias serviceuserAlias,
								   MicrometerMetrics metrics) {
		this.restTemplate = restTemplateBuilder
				.requestFactory(requestFactory.getClass())
				.rootUri(dokumenttypeInfoV3Alias.getUrl())
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.setConnectTimeout(Duration.ofMillis(dokumenttypeInfoV3Alias.getConnecttimeoutms()))
				.setReadTimeout(Duration.ofMillis(dokumenttypeInfoV3Alias.getReadtimeoutms()))
				.build();
		this.metrics = metrics;
	}

	public Tkat020DokumenttypeInfo(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Cacheable(HENT_DOKKAT_SPRAAKINFO)
	@Retryable(include = RegOppslagTechnicalException.class, exceptionExpression = "#{!HttpStatus.NOT_FOUND.equals(httpStatus)}", maxAttempts = 5, backoff = @Backoff(delay = 200))
	@Metrics(value = DOK_CONSUMER, extraTags = {PROCESS_CODE, HENT_DOKKAT_SPRAAKINFO}, percentiles = {0.5, 0.95}, histogram = true)
	public List<SpraakInfoTo> hentDokumenttypeInfoSpraak(final String dokumenttypeId) throws RegOppslagTechnicalException {

		metrics.cacheMiss(HENT_DOKKAT_SPRAAKINFO);
		try {
			Map<String, Object> uriVariables = new HashMap<>();
			uriVariables.put("dokumenttypeId", dokumenttypeId);
			DokumentTypeInfoToV3 dokumentTypeInfoToV3 =  restTemplate.getForObject("/{dokumenttypeId}", DokumentTypeInfoToV3.class, uriVariables);
			if (dokumentTypeInfoToV3.getDokumentProduksjonsInfo() == null || dokumentTypeInfoToV3.getDokumentProduksjonsInfo().getSpraakInfos() == null) {
				return Collections.emptyList();
			} else {
				return dokumentTypeInfoToV3.getDokumentProduksjonsInfo().getSpraakInfos();
			}
		} catch (HttpClientErrorException e) {
			//Kaster teknisk feil fordi manglende dokumenttypeId på prod databasen betyr at det er noe feil på vår side som må fikses.
			throw new RegOppslagTechnicalException(String.format("Dokkat.TKAT020 feilet med statusKode=%s. Fant ingen dokumenttypeInfo med dokumenttypeId=%s. ", e
					.getStatusCode(), dokumenttypeId), e, TKAT020_INGEN_TREFF, e.getStatusCode());
		} catch (HttpServerErrorException e) {
			throw new RegOppslagTechnicalException(String.format("Dokkat.TKAT020 feilet teknisk med statusKode=%s for dokumenttypeId=%s. Feilmelding=%s", e
					.getStatusCode(), dokumenttypeId, e.getMessage()), e, TKAT020_TEKNISKFEIL, e.getStatusCode());
		}
	}
}