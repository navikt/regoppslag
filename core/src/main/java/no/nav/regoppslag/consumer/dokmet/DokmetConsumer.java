package no.nav.regoppslag.consumer.dokmet;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokmet.api.tkat020.DokumenttypeInfoTo;
import no.nav.dokmet.api.tkat020.SpraakInfoTo;
import no.nav.regoppslag.config.properties.RegoppslagProperties;
import no.nav.regoppslag.consumer.NavHeadersExchangeFilterFunction;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.String.format;
import static no.nav.regoppslag.config.cache.CacheConfig.HENT_DOKMET_SPRAAKINFO;
import static no.nav.regoppslag.util.NavHeaders.NAV_CALLID;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
@Slf4j
public class DokmetConsumer {
	public static final String DOKUMENTTYPE_INFO_URI = "/rest/dokumenttypeinfo/";
	private static final String DOKUMENTTYPE_INFO_URI_DOKUMENTTYPEID = DOKUMENTTYPE_INFO_URI + "{dokumenttypeId}";

	private final WebClient webClient;

	public DokmetConsumer(WebClient webClient,
						  RegoppslagProperties regoppslagProperties) {
		this.webClient = webClient
				.mutate()
				.baseUrl(regoppslagProperties.getEndpoints().getDokmet().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.filter(new NavHeadersExchangeFilterFunction(NAV_CALLID))
				.build();
	}

	@Cacheable(value = HENT_DOKMET_SPRAAKINFO, key = "#dokumenttypeId")
	@Retryable(retryFor = RegOppslagTechnicalException.class, exceptionExpression = "isRetryable()", backoff = @Backoff(delay = 200))
	public List<SpraakInfoTo> hentDokumenttypeInfoSpraak(final String dokumenttypeId) throws RegOppslagTechnicalException {
		return webClient.get()
				.uri(DOKUMENTTYPE_INFO_URI_DOKUMENTTYPEID, dokumenttypeId)
				.retrieve()
				.bodyToMono(DokumenttypeInfoTo.class)
				.map(DokmetConsumer::getSpraakInfos)
				.doOnError(createErrorHandler(dokumenttypeId))
				.block();
	}

	private static List<SpraakInfoTo> getSpraakInfos(DokumenttypeInfoTo dokumenttypeInfoTo) {
		if (dokumenttypeInfoTo.getDokumentProduksjonsInfo() == null || dokumenttypeInfoTo.getDokumentProduksjonsInfo().getSpraakInfos() == null) {
			return Collections.emptyList();
		} else {
			return dokumenttypeInfoTo.getDokumentProduksjonsInfo().getSpraakInfos();
		}
	}

	private Consumer<Throwable> createErrorHandler(String dokumenttypeId) {
		return error -> {
			if (error instanceof WebClientResponseException responseException) {
				if (responseException.getStatusCode() == NOT_FOUND) {
					//Kaster teknisk feil fordi manglende dokumenttypeId på prod databasen betyr at det er noe feil på vår side som må fikses.
					throw new RegOppslagTechnicalException(
							format("TKAT020 feilet med statusKode=%s. Fant ingen dokumenttypeInfo med dokumenttypeId=%s. ",
									responseException.getStatusCode(), dokumenttypeId),
							responseException, false);
				} else {
					throw new RegOppslagTechnicalException(
							format("TKAT020 feilet teknisk med statusKode=%s for dokumenttypeId=%s. Feilmelding=%s",
									responseException.getStatusCode(), dokumenttypeId, responseException.getMessage()),
							responseException);
				}
			}
		};
	}
}