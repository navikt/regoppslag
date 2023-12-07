package no.nav.regoppslag.consumer.dokmet;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokmet.api.tkat020.DokumenttypeInfoTo;
import no.nav.dokmet.api.tkat020.SpraakInfoTo;
import no.nav.regoppslag.config.properties.RegoppslagProperties;
import no.nav.regoppslag.config.properties.RegoppslagProperties.Oauth2SecuredEndpoint;
import no.nav.regoppslag.consumer.azure.AzureTokenConsumer;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import org.slf4j.MDC;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static no.nav.regoppslag.config.cache.CacheConfig.HENT_DOKMET_SPRAAKINFO;
import static no.nav.regoppslag.util.MDCConstants.CALL_ID;
import static no.nav.regoppslag.util.NavHeaders.NAV_CALLID;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Slf4j
public class Tkat020DokumenttypeInfo {

	private final RestTemplate restTemplate;
	private final AzureTokenConsumer azureTokenConsumer;
	private final Oauth2SecuredEndpoint dokmet;

	public Tkat020DokumenttypeInfo(RestTemplateBuilder restTemplateBuilder,
								   HttpComponentsClientHttpRequestFactory requestFactory,
								   RegoppslagProperties regoppslagProperties,
								   AzureTokenConsumer azureTokenConsumer) {
		this.azureTokenConsumer = azureTokenConsumer;
		this.dokmet = regoppslagProperties.getEndpoints().getDokmet();
		this.restTemplate = restTemplateBuilder
				.requestFactory(requestFactory.getClass())
				.rootUri(this.dokmet.getUrl())
				.setConnectTimeout(Duration.ofSeconds(3))
				.setReadTimeout(Duration.ofSeconds(10))
				.build();
	}

	@Cacheable(value = HENT_DOKMET_SPRAAKINFO, key = "#dokumenttypeId")
	@Retryable(retryFor = RegOppslagTechnicalException.class, exceptionExpression = "T(org.springframework.http.HttpStatus).NOT_FOUND != getHttpStatusCode()", backoff = @Backoff(delay = 200))
	public List<SpraakInfoTo> hentDokumenttypeInfoSpraak(final String dokumenttypeId) throws RegOppslagTechnicalException {
		HttpHeaders headers = createHeaders();

		try {
			HttpEntity<String> request = new HttpEntity(headers);

			DokumenttypeInfoTo response = restTemplate.exchange(dokmet.getUrl() + "/" + dokumenttypeId, GET, request, DokumenttypeInfoTo.class).getBody();
			if (response.getDokumentProduksjonsInfo() == null || response.getDokumentProduksjonsInfo().getSpraakInfos() == null) {
				return Collections.emptyList();
			} else {
				return response.getDokumentProduksjonsInfo().getSpraakInfos();
			}
		} catch (HttpClientErrorException e) {
			//Kaster teknisk feil fordi manglende dokumenttypeId på prod databasen betyr at det er noe feil på vår side som må fikses.
			throw new RegOppslagTechnicalException(format("TKAT020 feilet med statusKode=%s. Fant ingen dokumenttypeInfo med dokumenttypeId=%s. ",
					e.getStatusCode(), dokumenttypeId), e, INTERNAL_SERVER_ERROR);
		} catch (HttpServerErrorException e) {
			throw new RegOppslagTechnicalException(format("TKAT020 feilet teknisk med statusKode=%s for dokumenttypeId=%s. Feilmelding=%s",
					e.getStatusCode(), dokumenttypeId, e.getMessage()), e, e.getStatusCode());
		}
	}

	private HttpHeaders createHeaders() {
		String clientCredentialToken = azureTokenConsumer.getClientCredentialToken(dokmet.getScope());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(clientCredentialToken);
		headers.add(NAV_CALLID, MDC.get(CALL_ID));
		return headers;
	}
}