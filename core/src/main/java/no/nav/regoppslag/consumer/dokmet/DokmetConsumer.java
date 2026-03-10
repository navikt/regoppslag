package no.nav.regoppslag.consumer.dokmet;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokmet.api.tkat020.DokumenttypeInfoTo;
import no.nav.dokmet.api.tkat020.SpraakInfoTo;
import no.nav.regoppslag.config.properties.RegoppslagProperties;
import no.nav.regoppslag.consumer.NavHeadersExchangeFilterFunction;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static no.nav.regoppslag.config.cache.CacheConfig.HENT_DOKMET_SPRAAKINFO;
import static no.nav.regoppslag.util.NavHeaders.NAV_CALLID;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
@Slf4j
public class DokmetConsumer {
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
	@Retryable(includes = RegOppslagTechnicalException.class, maxRetries = 2, delay = 200)
	public List<SpraakInfoTo> hentDokumenttypeInfoSpraak(final String dokumenttypeId) throws RegOppslagTechnicalException {
		return webClient.get()
				.uri("/" + dokumenttypeId)
				.retrieve()
				.bodyToMono(DokumenttypeInfoTo.class)
				.map(DokmetConsumer::getSpraakInfos)
				.onErrorMap(error -> mapError(error, dokumenttypeId))
				.block();
	}

	private static List<SpraakInfoTo> getSpraakInfos(DokumenttypeInfoTo dokumenttypeInfoTo) {
		if (dokumenttypeInfoTo.getDokumentProduksjonsInfo() == null || dokumenttypeInfoTo.getDokumentProduksjonsInfo().getSpraakInfos() == null) {
			return Collections.emptyList();
		} else {
			return dokumenttypeInfoTo.getDokumentProduksjonsInfo().getSpraakInfos();
		}
	}

	private Throwable mapError(Throwable error, String dokumenttypeId) {
		if (error instanceof WebClientResponseException responseException) {
			if (responseException.getStatusCode() == NOT_FOUND) {
				// Kaster funksjonell feil fordi manglende dokumenttypeId på prod databasen betyr at det er noe feil på
				// vår side som må fikses, og retry ikke gir mening.
				return new RegOppslagFunctionalException(
						format("TKAT020 feilet med statusKode=%s. Fant ingen dokumenttypeInfo med dokumenttypeId=%s. ",
								responseException.getStatusCode(), dokumenttypeId),
						responseException, INTERNAL_SERVER_ERROR);
			} else {
				return new RegOppslagTechnicalException(
						format("TKAT020 feilet teknisk med statusKode=%s for dokumenttypeId=%s. Feilmelding=%s",
								responseException.getStatusCode(), dokumenttypeId, responseException.getMessage()),
						responseException);
			}
		}
		return new RegOppslagTechnicalException(format("TKAT020 feilet teknisk for dokumenttypeId=%s. Feilmelding=%s",
				dokumenttypeId, error.getMessage()), error);
	}
}
