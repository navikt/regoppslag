package no.nav.regoppslag.consumer.norg2.support;

import no.nav.dok.metaforcemal.jaxb2.gen.NavEnhet;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.PostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Postnummer;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Stedsadresse;

public class Norg2Mapper {
	public NavEnhet mapPostadresse (Organisasjonsenhet enhet, NavEnhet navEnhet) {
		if (enhet != null) {
			navEnhet.setEnhetsNavn(enhet.getEnhetNavn());

//TODO			navEnhet.setKontakttelefon(wsEnhet.getKontaktinformasjon().getTelefonnummer());
			NorskPostadresse postadresse = new NorskPostadresse();
			if (enhet.getKontaktinformasjon() != null && enhet.getKontaktinformasjon().getPostadresse() != null) {
				if (enhet.getKontaktinformasjon().getPostadresse() instanceof Stedsadresse) {
					Gateadresse gateadresse	= 	(Gateadresse) enhet.getKontaktinformasjon().getPostadresse();

					postadresse.setAdresselinje1(gateadresse.getGatenavn() + " " + gateadresse.getHusnummer() + gateadresse.getHusbokstav());
					postadresse.setPostnummer(gateadresse.getPoststed().getValue());
					postadresse.setPoststed(gateadresse.getPoststed().getKodeverksRef());
				} else {
					PostboksadresseNorsk postboksadresseNorsk	= 	(PostboksadresseNorsk) enhet.getKontaktinformasjon().getPostadresse();
					postadresse.setAdresselinje1(postboksadresseNorsk.getPostboksnummer() + " " + postboksadresseNorsk.getPostboksanlegg());
					postadresse.setPostnummer(postboksadresseNorsk.getPoststed().getValue());
					postadresse.setPoststed(postboksadresseNorsk.getPoststed().getKodeverksRef());
				}
				navEnhet.setAdresse(postadresse);
			}
		}
		return navEnhet;
	}
	public NavEnhet mapBesokadresse (Organisasjonsenhet wsEnhet, NavEnhet navEnhet) {
		if (wsEnhet != null) {
			navEnhet.setEnhetsNavn(wsEnhet.getEnhetNavn());
			//TODO			navEnhet.setKontakttelefon(wsEnhet.getKontaktinformasjon().getTelefonnummer());
			NorskPostadresse postadresse = new NorskPostadresse();
			Gateadresse gateadresse = wsEnhet.getKontaktinformasjon().getBesoeksadresse();
			if (gateadresse != null) {
				postadresse.setAdresselinje1(gateadresse.getGatenavn() + " " + gateadresse.getHusnummer() + gateadresse.getHusbokstav());

				Postnummer stedadresse = wsEnhet.getKontaktinformasjon().getBesoeksadresse().getPoststed();

				if (stedadresse != null) {
					postadresse.setPostnummer(stedadresse.getValue());
					postadresse.setPoststed(stedadresse.getKodeverksRef());
				}
				navEnhet.setAdresse(postadresse);
			}
		}
		return navEnhet;
	}
}
