package no.nav.regoppslag.personv3;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.AktoerHarNavn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Feil;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkResponse;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Service
public class PersonV3Consumer {
	private static final String SEPARATOR = " ";
	private final PersonV3 personV3;

	@Inject
	public PersonV3Consumer(PersonV3 personV3) {
		this.personV3 = personV3;
	}

	public String hentPersonnavn(final String personidentifikator) {
		HentPersonnavnBolkRequest request = mapHentPersonnavnBolkRequest(personidentifikator);

		HentPersonnavnBolkResponse response = personV3.hentPersonnavnBolk(request);
		if (response != null && response.getAktoerHarNavnListe().size() == 1) {
			return getFullName(response.getAktoerHarNavnListe().get(0));
		} else if (response != null && !response.getFeilListe().isEmpty()) {
			logFeilmelding(response, personidentifikator);
		}
		return null;
	}

	private HentPersonnavnBolkRequest mapHentPersonnavnBolkRequest(String personidentifikator) {
		HentPersonnavnBolkRequest request = new HentPersonnavnBolkRequest();
		PersonIdent personIdent = new PersonIdent();
		NorskIdent norskIdent = new NorskIdent();
		norskIdent.setIdent(personidentifikator);
		personIdent.setIdent(norskIdent);
		request.getAktoerListe().add(personIdent);
		return request;
	}

	private String getFullName(AktoerHarNavn navn) {
		if (navn.getPersonnavn() != null) {
			String fornavn = navn.getPersonnavn().getFornavn();
			String mellomnavn = navn.getPersonnavn().getMellomnavn();
			String etternavn = navn.getPersonnavn().getEtternavn();
			return Joiner.on(SEPARATOR)
					.skipNulls()
					.join(fornavn == null ? null : fornavn.trim(),
							mellomnavn == null ? null : mellomnavn.trim(),
							etternavn == null ? null : etternavn.trim());
		}
		return null;
	}

	private void logFeilmelding(HentPersonnavnBolkResponse response, String personidentifikator) {
		for (Feil feil : response.getFeilListe()) {
			if (feil.getAktoer() instanceof PersonIdent) {
				PersonIdent ident = (PersonIdent) feil.getAktoer();
				if (ident.getIdent().getIdent().equals(personidentifikator)) {
					log.info("Personen finnes ikke for personidentifikator={}, message={}",
							personidentifikator, feil.getFeilBeskrivelse());
				}
			}
		}
	}
}
