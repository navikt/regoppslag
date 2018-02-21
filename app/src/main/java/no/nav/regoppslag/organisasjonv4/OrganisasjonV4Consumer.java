package no.nav.regoppslag.organisasjonv4;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentNoekkelinfoOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentNoekkelinfoOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentNoekkelinfoOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentNoekkelinfoOrganisasjonResponse;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Service
public class OrganisasjonV4Consumer {

	private final OrganisasjonV4 organisasjonV4;

	@Inject
	public OrganisasjonV4Consumer(OrganisasjonV4 organisasjonV4) {
		this.organisasjonV4 = organisasjonV4;
	}

	public String hentOrganisasjonsnavn(final String organisasjonsnummer) {
		try {
			HentNoekkelinfoOrganisasjonRequest request = mapHentNoekkelinfoOrganisasjonRequest(organisasjonsnummer);
			HentNoekkelinfoOrganisasjonResponse response = organisasjonV4.hentNoekkelinfoOrganisasjon(request);
			return mapHentNoekkelinfoOrganisasjonResponse(response);
		} catch (HentNoekkelinfoOrganisasjonOrganisasjonIkkeFunnet | HentNoekkelinfoOrganisasjonUgyldigInput e) {
			log.info("Organisasjonen finnes ikke for organisasjonsnummer={}, message={}", organisasjonsnummer, e.getMessage());
		}
		return null;
	}

	private HentNoekkelinfoOrganisasjonRequest mapHentNoekkelinfoOrganisasjonRequest(String avsenderId) {
		HentNoekkelinfoOrganisasjonRequest request = new HentNoekkelinfoOrganisasjonRequest();
		request.setOrgnummer(avsenderId);
		return request;
	}

	private String mapHentNoekkelinfoOrganisasjonResponse(HentNoekkelinfoOrganisasjonResponse response) {
		if (response == null || response.getNavn() == null) {
			return null;
		}

		if (response.getNavn() instanceof UstrukturertNavn) {
			UstrukturertNavn navn = (UstrukturertNavn) response.getNavn();
			StringBuilder sb = new StringBuilder();
			navn.getNavnelinje().forEach(s -> sb.append(s.trim()).append(" "));
			return sb.toString().trim();
		} else {
			log.warn("Forventet en instans av UstrukturertNavn, men var "
					+ response.getNavn().getClass());
			return null;
		}
	}
}
