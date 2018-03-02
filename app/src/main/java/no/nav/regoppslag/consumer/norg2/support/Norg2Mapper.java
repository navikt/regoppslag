package no.nav.regoppslag.consumer.norg2.support;

import no.nav.dok.metaforcemal.jaxb2.gen.NavEnhet;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSGateadresse;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSOrganisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSPostboksadresse;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSPostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSStedsadresseNorge;

public class Norg2Mapper {
	public NavEnhet map (WSOrganisasjonsenhet wsEnhet, NavEnhet navEnhet) {
		if (wsEnhet != null) {
			navEnhet.setEnhetsNavn(wsEnhet.getEnhetNavn());
//TODO			navEnhet.setKontakttelefon(wsEnhet.getKontaktinformasjon().getTelefonnummer());
			if (wsEnhet.getKontaktinformasjon() != null && wsEnhet.getKontaktinformasjon().getPostadresse() != null) {
				NorskPostadresse postadresse = new NorskPostadresse();
				WSGateadresse gateadresse = wsEnhet.getKontaktinformasjon().getBesoeksadresse();
				if (gateadresse != null) {
					postadresse.setAdresselinje1(gateadresse.getGatenavn() + " " + gateadresse.getHusnummer() + gateadresse.getHusbokstav());
				} else {
					WSPostboksadresse postboksadresse = (WSPostboksadresse) wsEnhet.getKontaktinformasjon().getPostadresse();
					postadresse.setAdresselinje1(postboksadresse.getPostboksnummer() + " " + postboksadresse.getPostboksanlegg());
				}

				if (wsEnhet.getKontaktinformasjon().getPostadresse() instanceof WSStedsadresseNorge) {
					WSStedsadresseNorge stedAdresse	= 	(WSStedsadresseNorge) wsEnhet.getKontaktinformasjon().getPostadresse();
					postadresse.setPostnummer(stedAdresse.getPoststed().getValue());
					postadresse.setPoststed(stedAdresse.getPoststed().getKodeverksRef());
				} else {
					WSPostboksadresseNorsk postnummer	= 	(WSPostboksadresseNorsk) wsEnhet.getKontaktinformasjon().getPostadresse();
					postadresse.setPostnummer(postnummer.getPoststed().getValue());
					postadresse.setPoststed(postnummer.getPoststed().getKodeverksRef());
				}
				navEnhet.setAdresse(postadresse);
			}
		}
		return navEnhet;
	}
}
