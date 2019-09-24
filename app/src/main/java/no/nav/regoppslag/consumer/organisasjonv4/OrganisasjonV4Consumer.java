package no.nav.regoppslag.consumer.organisasjonv4;

import static no.nav.regoppslag.metrics.MetricLabels.DOK_CONSUMER;
import static no.nav.regoppslag.metrics.MetricLabels.PROCESS_CODE;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.Metrics;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
@Slf4j
@Service
public class OrganisasjonV4Consumer {
	
	private final OrganisasjonV4 organisasjonV4;
	private MicrometerMetrics metrics;

	public static final String HENT_ORGANISASJON = "hentOrganisasjon";
	public static final String ORGV4_UGYLDIG_INPUT = "OrganisasjonV4 - Ugyldig input";
	public static final String ORGV4_ORG_IKKE_FUNNET = "OrganisasjonV4 - Organisasjon ikke funnet";
	
	@Inject
	public OrganisasjonV4Consumer(OrganisasjonV4 organisasjonV4, MicrometerMetrics metrics) {
		this.organisasjonV4 = organisasjonV4;
		this.metrics = metrics;
	}
	
	@Cacheable(value = HENT_ORGANISASJON, key = "#organisasjonsnummer")
	@Retryable(include = RegOppslagTechnicalException.class, exclude = {RegOppslagFunctionalException.class}, maxAttempts = 5, backoff = @Backoff(delay = 200))
	@Metrics(value = DOK_CONSUMER, extraTags = {PROCESS_CODE, HENT_ORGANISASJON}, percentiles = {0.5, 0.95}, histogram = true)
	public Organisasjon hentOrganisasjon(final String organisasjonsnummer, final String serviceCode) throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		
		metrics.cacheMiss(HENT_ORGANISASJON);
		
		try {
			HentOrganisasjonRequest request = mapHentNoekkelinfoOrganisasjonRequest(organisasjonsnummer);
			HentOrganisasjonResponse response = organisasjonV4.hentOrganisasjon(request);
			return mapHentOrganisasjonResponse(response);
		} catch (HentOrganisasjonOrganisasjonIkkeFunnet | HentOrganisasjonUgyldigInput e) {
			throw new RegOppslagFunctionalException(String.format("Nav enhet finnes ikke for enhetNr=%s, message=%s", organisasjonsnummer, e
					.getMessage()), e, e.getClass()
					.equals(HentOrganisasjonOrganisasjonIkkeFunnet.class) ? ORGV4_ORG_IKKE_FUNNET : ORGV4_UGYLDIG_INPUT);
		} catch (Exception e) {
			throw new RegOppslagTechnicalException(String.format("Noe gikk galt i kall til OrganisasjonV4.hentOrganisasjon for enhetNr=%s, message=%s", organisasjonsnummer, e
					.getMessage()), e, "OrganisasjonV4 - Teknisk feil");
		}
	}
	
	private HentOrganisasjonRequest mapHentNoekkelinfoOrganisasjonRequest(String avsenderId) {
		HentOrganisasjonRequest request = new HentOrganisasjonRequest();
		request.setOrgnummer(avsenderId);
		return request;
	}
	
	private Organisasjon mapHentOrganisasjonResponse(HentOrganisasjonResponse response) {
		return response.getOrganisasjon();
	}
}
