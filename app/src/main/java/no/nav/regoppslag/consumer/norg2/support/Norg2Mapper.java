package no.nav.regoppslag.consumer.norg2.support;

import no.nav.dok.metaforcemal.jaxb2.gen.NavEnhet;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.PostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Stedsadresse;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
@Component
public class Norg2Mapper {
	public void mapPostadresse(Organisasjonsenhet enhet, NavEnhet navEnhet) {
		if (enhet != null) {
			navEnhet.setEnhetsNavn(enhet.getEnhetNavn());
//TODO			navEnhet.setKontakttelefon(wsEnhet.getKontaktinformasjon().getTelefonnummer());
			NorskPostadresse postadresse = new NorskPostadresse();
			if (enhet.getKontaktinformasjon() != null && enhet.getKontaktinformasjon().getPostadresse() != null) {
				if (enhet.getKontaktinformasjon().getPostadresse() instanceof Stedsadresse) {
					Gateadresse gateadresse = (Gateadresse) enhet.getKontaktinformasjon().getPostadresse();
					postadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn()).orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
					postadresse.setPostnummer(gateadresse.getPoststed().getKodeverksRef());
					postadresse.setPoststed(gateadresse.getPoststed().getValue());
				} else {
					PostboksadresseNorsk postboksadresseNorsk = (PostboksadresseNorsk) enhet.getKontaktinformasjon().getPostadresse();
					postadresse.setAdresselinje1(Optional.ofNullable(postboksadresseNorsk.getPostboksnummer()).orElse("") + " " + Optional.ofNullable(postboksadresseNorsk.getPostboksanlegg()).orElse(""));
					postadresse.setPostnummer(postboksadresseNorsk.getPoststed().getKodeverksRef());
					postadresse.setPoststed(postboksadresseNorsk.getPoststed().getValue());
				}
				navEnhet.setAdresse(postadresse);
			}
		}
	}

	public void mapBesokadresse(Organisasjonsenhet wsEnhet, NavEnhet navEnhet) {
		if (wsEnhet != null) {
			navEnhet.setEnhetsNavn(wsEnhet.getEnhetNavn());
			//TODO			navEnhet.setKontakttelefon(wsEnhet.getKontaktinformasjon().getTelefonnummer());
			NorskPostadresse postadresse = new NorskPostadresse();
			if (wsEnhet.getKontaktinformasjon().getBesoeksadresse() != null) {
				Gateadresse gateadresse = wsEnhet.getKontaktinformasjon().getBesoeksadresse();
				if (gateadresse != null) {
					postadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn()).orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));

					if (gateadresse.getPoststed() != null) {
						postadresse.setPostnummer(gateadresse.getPoststed().getKodeverksRef());
						postadresse.setPoststed(gateadresse.getPoststed().getValue());
					}
					navEnhet.setAdresse(postadresse);
				}
			}
		}
	}
}
