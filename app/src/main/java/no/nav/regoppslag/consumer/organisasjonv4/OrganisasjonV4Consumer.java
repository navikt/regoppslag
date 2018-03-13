package no.nav.regoppslag.consumer.organisasjonv4;

import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestLatency;

import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
@Slf4j
@Service
public class OrganisasjonV4Consumer {

	private final OrganisasjonV4 organisasjonV4;
	private Histogram.Timer requestTimer;

	@Inject
	public OrganisasjonV4Consumer(OrganisasjonV4 organisasjonV4) {
		this.organisasjonV4 = organisasjonV4;
	}

	public Organisasjon hentOrganisasjon(final String organisasjonsnummer) {
		try {
			HentOrganisasjonRequest request = mapHentNoekkelinfoOrganisasjonRequest(organisasjonsnummer);
			requestTimer = requestLatency.labels(SERVICE_CODE_TREG001, "ORGANISASJON_V4", "hentOrganisasjon").startTimer();
			HentOrganisasjonResponse response = organisasjonV4.hentOrganisasjon(request);
			return mapHentOrganisasjonResponse(response);
		} catch (HentOrganisasjonOrganisasjonIkkeFunnet | HentOrganisasjonUgyldigInput e) {
			log.info("Organisasjonen finnes ikke for organisasjonsnummer={}, message={}", organisasjonsnummer, e.getMessage());
		} finally {
			requestTimer.observeDuration();
		}
		return null;
	}

	private HentOrganisasjonRequest mapHentNoekkelinfoOrganisasjonRequest(String avsenderId) {
		HentOrganisasjonRequest request = new HentOrganisasjonRequest();
		request.setOrgnummer(avsenderId);
		return request;
	}

	private Organisasjon mapHentOrganisasjonResponse(HentOrganisasjonResponse response) {
		if (response == null || response.getOrganisasjon() == null) {
			return null;
		}
		return response.getOrganisasjon();
	}
}
