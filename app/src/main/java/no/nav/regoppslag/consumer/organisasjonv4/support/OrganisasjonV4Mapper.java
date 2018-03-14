package no.nav.regoppslag.consumer.organisasjonv4.support;

import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.dok.metaforcemal.jaxb2.gen.Spraakkode;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.GeografiskAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.NoekkelVerdiAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.OrganisasjonsDetaljer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SemistrukturertAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.StedsadresseNorge;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.StrukturertAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */

@Component
public class OrganisasjonV4Mapper {
	private boolean harPostnummer = false;
	private boolean harPoststed	= false;
	public void map(Organisasjon wsOrganisasjon, Mottaker mottaker) {
		OrganisasjonsDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();
		mottaker.setKortNavn(StringUtils.collectionToDelimitedString(((UstrukturertNavn) wsOrganisasjon.getNavn()).getNavnelinje(), " "));

		mottaker.setNavn(StringUtils.collectionToDelimitedString(((UstrukturertNavn)orgDet.getNavn().get(0).getNavn()).getNavnelinje(), " "));
		if (orgDet.getGjeldendeMaalform() != null) {
			if (orgDet.getGjeldendeMaalform().getValue() == "NO") {
				mottaker.setSpraakkode(Spraakkode.NB);
			} else {
				mottaker.setSpraakkode(Spraakkode.valueOf(orgDet.getGjeldendeMaalform().getValue()));
			}
		}

		NorskPostadresse norskPostadresse = new NorskPostadresse();
		if (!orgDet.getPostadresse().isEmpty()) {
			if (orgDet.getPostadresse().get(0) instanceof SemistrukturertAdresse) {
				SemistrukturertAdresse adresse = (SemistrukturertAdresse) orgDet.getPostadresse().get(0);
				settAdresseledd(adresse, norskPostadresse);
			} else {
				Gateadresse gateadresse = (Gateadresse) orgDet.getPostadresse().get(0);
				norskPostadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn()).orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer().toString()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
				if (orgDet.getPostadresse().get(0) instanceof StrukturertAdresse) {
					StedsadresseNorge stedsadresseNorge = (StedsadresseNorge) orgDet.getPostadresse().get(0);
					//TODO Hente fra kodeverk + validere NO
					if (stedsadresseNorge.getPoststed() != null) {
						norskPostadresse.setPostnummer(stedsadresseNorge.getPoststed().getKodeverksRef());
						norskPostadresse.setPoststed(stedsadresseNorge.getPoststed().getValue());
					}
				}
			}
			GeografiskAdresse geografiskAdresse = orgDet.getPostadresse().get(0);
			if (geografiskAdresse.getLandkode() != null) {
				norskPostadresse.setLand(geografiskAdresse.getLandkode().getKodeverksRef());
			}
		} else if (!orgDet.getForretningsadresse().isEmpty()) {
			if (orgDet.getForretningsadresse().get(0) instanceof SemistrukturertAdresse) {
				SemistrukturertAdresse adresse = (SemistrukturertAdresse) orgDet.getForretningsadresse().get(0);
				settAdresseledd(adresse, norskPostadresse);
			} else {
				Gateadresse gateadresse = (Gateadresse) orgDet.getForretningsadresse().get(0);
				norskPostadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn()).orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer().toString()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
				if (orgDet.getForretningsadresse().get(0) instanceof StrukturertAdresse) {
					StedsadresseNorge stedsadresseNorge = (StedsadresseNorge) orgDet.getForretningsadresse().get(0);
					//TODO Hente fra kodeverk + validere NO
					if (stedsadresseNorge.getPoststed() != null) {
						norskPostadresse.setPostnummer(stedsadresseNorge.getPoststed().getKodeverksRef());
						norskPostadresse.setPoststed(stedsadresseNorge.getPoststed().getValue());
					}
				}
			}
			GeografiskAdresse geografiskAdresse = orgDet.getForretningsadresse().get(0);
			if (geografiskAdresse.getLandkode() != null) {
				//TODO Hente fra kodeverk
				norskPostadresse.setLand(geografiskAdresse.getLandkode().getKodeverksRef());
			}
		}
		mottaker.setAdresse(norskPostadresse);
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
				if (!(StringUtils.isEmpty(nokler.getVerdi()))) {
					harPostnummer = true;
				}
			} else if ("poststed".equals(nokler.getNoekkel().getKodeverksRef())) {
				norskPostadresse.setPoststed(nokler.getVerdi());
				if (!(StringUtils.isEmpty(nokler.getVerdi()))) {
					harPoststed = true;
				}
			}
		}
	}
}