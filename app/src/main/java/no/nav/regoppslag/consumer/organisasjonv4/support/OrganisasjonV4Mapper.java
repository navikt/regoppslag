package no.nav.regoppslag.consumer.organisasjonv4.support;

import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.GeografiskAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.NoekkelVerdiAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.OrganisasjonsDetaljer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SemistrukturertAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.StedsadresseNorge;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.StrukturertAdresse;
import org.springframework.util.StringUtils;

public class OrganisasjonV4Mapper {
	public void map(Organisasjon wsOrganisasjon, Mottaker mottaker) {
		OrganisasjonsDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();
		mottaker.setNavn(StringUtils.collectionToCommaDelimitedString(orgDet.getNavn()));
		mottaker.setKortNavn(StringUtils.collectionToCommaDelimitedString(wsOrganisasjon.getOrganisasjonDetaljer().getNavn()));

		NorskPostadresse norskPostadresse = new NorskPostadresse();
		if (!orgDet.getPostadresse().isEmpty()) {
			Gateadresse gateadresse = (Gateadresse) orgDet.getPostadresse().get(0);
			if (orgDet.getPostadresse().get(0) instanceof SemistrukturertAdresse) {
				SemistrukturertAdresse adresse = (SemistrukturertAdresse) orgDet.getPostadresse().get(0);
				settAdresseledd(adresse, norskPostadresse);
			} else {
				norskPostadresse.setAdresselinje1(gateadresse.getGatenavn() + " " + gateadresse.getHusnummer() + gateadresse.getHusbokstav());
				if (orgDet.getPostadresse().get(0) instanceof StrukturertAdresse) {
					StedsadresseNorge stedsadresseNorge = (StedsadresseNorge) orgDet.getPostadresse().get(0);
					norskPostadresse.setPostnummer(stedsadresseNorge.getPoststed().getValue());
					norskPostadresse.setPoststed(stedsadresseNorge.getPoststed().getKodeverksRef());
				}
			}
			GeografiskAdresse geografiskAdresse = orgDet.getPostadresse().get(0);
			if (geografiskAdresse.getLandkode() != null) {
				norskPostadresse.setLand(geografiskAdresse.getLandkode().getKodeverksRef());
			}
		} else if (!orgDet.getForretningsadresse().isEmpty()) {
			Gateadresse gateadresse = (Gateadresse) orgDet.getForretningsadresse().get(0);
			if (orgDet.getForretningsadresse().get(0) instanceof SemistrukturertAdresse) {
				SemistrukturertAdresse adresse = (SemistrukturertAdresse) orgDet.getForretningsadresse().get(0);
				settAdresseledd(adresse, norskPostadresse);
			} else {
				norskPostadresse.setAdresselinje1(gateadresse.getGatenavn() + " " + gateadresse.getHusnummer() + gateadresse.getHusbokstav());
				if (orgDet.getForretningsadresse().get(0) instanceof StrukturertAdresse) {
					StedsadresseNorge stedsadresseNorge = (StedsadresseNorge) orgDet.getForretningsadresse().get(0);
					norskPostadresse.setPostnummer(stedsadresseNorge.getPoststed().getValue());
					norskPostadresse.setPoststed(stedsadresseNorge.getPoststed().getKodeverksRef());
				}
			}
			GeografiskAdresse geografiskAdresse = orgDet.getForretningsadresse().get(0);
			if (geografiskAdresse.getLandkode() != null) {
				norskPostadresse.setLand(geografiskAdresse.getLandkode().getKodeverksRef());
			}
		}
	}

	private void settAdresseledd(SemistrukturertAdresse adresse, NorskPostadresse norskPostadresse)  {
		for (NoekkelVerdiAdresse nokler : adresse.getAdresseledd()) {
			//TODO Konstanter for disse?
			if ("adresselinje1".equals(nokler.getNoekkel().getKodeverksRef())) {
				norskPostadresse.setAdresselinje1(nokler.getVerdi());
			} else if ("adresselinje2".equals(nokler.getNoekkel().getKodeverksRef())) {
				norskPostadresse.setAdresselinje2(nokler.getVerdi());
			} else if ("adresselinje3split1".equals(nokler.getNoekkel().getKodeverksRef())) {
				norskPostadresse.setAdresselinje3(nokler.getVerdi());
			} else if ("adresselinje3split2".equals(nokler.getNoekkel().getKodeverksRef())) {
				//norskPostadresse.setAdresselinje4(nokler.getVerdi());
			} else if ("postnr".equals(nokler.getNoekkel().getKodeverksRef())) {
				norskPostadresse.setPostnummer(nokler.getVerdi());
			} else if ("poststed".equals(nokler.getNoekkel().getKodeverksRef())) {
				norskPostadresse.setPoststed(nokler.getVerdi());
			}
		}
	}
}