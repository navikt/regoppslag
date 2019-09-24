package no.nav.regoppslag.consumer.norg2;

import static no.nav.regoppslag.metrics.MetricLabels.DOK_CONSUMER;
import static no.nav.regoppslag.metrics.MetricLabels.PROCESS_CODE;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.Metrics;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.HentKontaktinformasjonForEnhetBolkUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.OrganisasjonEnhetKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.meldinger.HentKontaktinformasjonForEnhetBolkRequest;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.meldinger.HentKontaktinformasjonForEnhetBolkResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Ketill Fenne, Visma Consulting
 */
@Slf4j
@Service
public class OrganisasjonEnhetKontaktinformasjonV1Consumer {
	
	private final OrganisasjonEnhetKontaktinformasjonV1 organisasjonEnhetKontaktinformasjonV1;
	private MicrometerMetrics metrics;

	public static final String HENT_ENHET_NAVN = "hentEnhetNavn";
	public static final String HENT_KONTAKTINFORMASJON_FOR_ENHET = "hentKontaktInformasjonForEnhet";
	public static final String KUNNE_IKKE_FINNE_ENHET = "NORG2 - Kunne ikke finne enhet";
	
	@Inject
	public OrganisasjonEnhetKontaktinformasjonV1Consumer(OrganisasjonEnhetKontaktinformasjonV1 organisasjonEnhetKontaktinformasjonV1,
														 MicrometerMetrics metrics) {
		this.organisasjonEnhetKontaktinformasjonV1 = organisasjonEnhetKontaktinformasjonV1;
		this.metrics = metrics;
	}
	
	@Cacheable(value = HENT_ENHET_NAVN, key = "#enhetNr")
	@Retryable(include = RegOppslagTechnicalException.class, exclude = {RegOppslagFunctionalException.class}, maxAttempts = 5, backoff = @Backoff(delay = 200))
	@Metrics(value = DOK_CONSUMER, extraTags = {PROCESS_CODE, HENT_ENHET_NAVN}, percentiles = {0.5, 0.95}, histogram = true)
	public Organisasjonsenhet hentKontaktinformasjonForEnhet(String enhetNr) throws RegOppslagFunctionalException, RegOppslagTechnicalException {

		metrics.cacheMiss(HENT_ENHET_NAVN);

		try {
			HentKontaktinformasjonForEnhetBolkResponse response = organisasjonEnhetKontaktinformasjonV1.hentKontaktinformasjonForEnhetBolk(mapEnhetNr(enhetNr));
			return mapHentKontaktinformasjonForEnhetBolkResponse(response, enhetNr);
		} catch (HentKontaktinformasjonForEnhetBolkUgyldigInput hentKontaktinformasjonForEnhetBolkUgyldigInput) {
			throw new RegOppslagFunctionalException(hentKontaktinformasjonForEnhetBolkUgyldigInput
					.getMessage(), hentKontaktinformasjonForEnhetBolkUgyldigInput, KUNNE_IKKE_FINNE_ENHET);
		} catch (Exception e) {
			throw new RegOppslagTechnicalException(String.format("Noe gikk galt i kall til Norg for enhetNr=%s, message=%s", enhetNr, e
					.getMessage()), e, "NORG2 - Teknisk feil");
		}
	}
	
	private HentKontaktinformasjonForEnhetBolkRequest mapEnhetNr(String enhetNummer) {
		HentKontaktinformasjonForEnhetBolkRequest request = new HentKontaktinformasjonForEnhetBolkRequest();
		request.getEnhetIdListe().add(enhetNummer);
		return request;
	}
	
	private Organisasjonsenhet mapHentKontaktinformasjonForEnhetBolkResponse(HentKontaktinformasjonForEnhetBolkResponse response, String enhetNr) throws HentKontaktinformasjonForEnhetBolkUgyldigInput {
		if (!response.getEnhetListe().isEmpty()) {
			return response.getEnhetListe().get(0);
		} else if (!response.getFeiletEnhetListe().isEmpty()) {
			throw new HentKontaktinformasjonForEnhetBolkUgyldigInput("Nav enhet finnes ikke for enhetNr=" + enhetNr + " Feilmelding="+response.getFeiletEnhetListe().get(0).getFeilmelding(), null);
		}
		throw new HentKontaktinformasjonForEnhetBolkUgyldigInput("Nav enhet finnes ikke for enhetNr=" + enhetNr, null);
	}
}
