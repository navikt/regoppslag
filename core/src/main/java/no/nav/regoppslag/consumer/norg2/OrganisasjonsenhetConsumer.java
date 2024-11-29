package no.nav.regoppslag.consumer.norg2;

import no.nav.regoppslag.config.NavHeaderFilter;
import no.nav.regoppslag.config.properties.RegoppslagProperties;
import no.nav.regoppslag.consumer.norg2.to.EnhetKontaktinformasjon;
import no.nav.regoppslag.consumer.norg2.to.EnhetNavn;
import no.nav.regoppslag.exceptions.Norg2FunctionalException;
import no.nav.regoppslag.exceptions.Norg2TechnicalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static java.lang.String.format;
import static no.nav.regoppslag.config.cache.CacheConfig.HENT_ENHET_KONTAKTINFO;
import static no.nav.regoppslag.config.cache.CacheConfig.HENT_ENHET_NAVN;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class OrganisasjonsenhetConsumer {

	private final WebClient webClient;

	public OrganisasjonsenhetConsumer(WebClient webClient,
									  RegoppslagProperties regoppslagProperties) {
		this.webClient = webClient.mutate()
				.baseUrl(regoppslagProperties.getEndpoints().getNorg2().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.filter(new NavHeaderFilter())
				.build();
	}

	@Cacheable(value = HENT_ENHET_NAVN, key = "#enhetNr")
	@Retryable(retryFor = RegOppslagTechnicalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public EnhetNavn hentEnhetNavn(String enhetNr) {

		return webClient.get()
				.uri("/{enhetNr}", enhetNr)
				.retrieve()
				.bodyToMono(EnhetNavn.class)
				.doOnError(this::handleError)
				.block();
	}

	@Cacheable(value = HENT_ENHET_KONTAKTINFO, key = "#enhetNr")
	@Retryable(retryFor = RegOppslagTechnicalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public EnhetKontaktinformasjon hentEnhetKontaktinformasjon(String enhetNr) {

		return webClient.get()
				.uri("/{enhetNr}/kontaktinformasjon", enhetNr)
				.retrieve()
				.bodyToMono(EnhetKontaktinformasjon.class)
				.doOnError(this::handleError)
				.block();
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			throw new Norg2FunctionalException(format("Kall mot norg2 feilet funksjonelt med status=%s, feilmelding=%s", response.getStatusCode(), response.getMessage()), error, response.getStatusCode());
		} else {
			throw new Norg2TechnicalException(format("Kall mot norg2 feilet feilet teknisk med feilmelding=%s", error.getMessage()), error);
		}
	}
}
