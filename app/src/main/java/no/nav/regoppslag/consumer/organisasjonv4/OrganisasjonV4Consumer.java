package no.nav.regoppslag.consumer.organisasjonv4;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentNoekkelinfoOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentNoekkelinfoOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentNoekkelinfoOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentNoekkelinfoOrganisasjonResponse;
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

	@Inject
	public OrganisasjonV4Consumer(OrganisasjonV4 organisasjonV4) {
		this.organisasjonV4 = organisasjonV4;
	}

	public Organisasjon hentOrganisasjon(final String organisasjonsnummer) {
		try {
			HentOrganisasjonRequest request = mapHentNoekkelinfoOrganisasjonRequest(organisasjonsnummer);
			HentOrganisasjonResponse response = organisasjonV4.hentOrganisasjon(request);
			return mapHentOrganisasjonResponse(response);
		} catch (HentOrganisasjonOrganisasjonIkkeFunnet | HentOrganisasjonUgyldigInput e) {
			log.info("Organisasjonen finnes ikke for organisasjonsnummer={}, message={}", organisasjonsnummer, e.getMessage());
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
