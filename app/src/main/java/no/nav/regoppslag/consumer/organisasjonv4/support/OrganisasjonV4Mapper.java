package no.nav.regoppslag.consumer.organisasjonv4.support;

import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.dok.metaforcemal.jaxb2.gen.Spraakkode;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
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

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */

@Component
public class OrganisasjonV4Mapper {
	private final boolean harPostnummer = false;

	@Inject
	private final LandkodeService landkodeService;

	@Inject
	private final PostnummerService postnummerService;

	public OrganisasjonV4Mapper(PostnummerService postnummerService, LandkodeService landkodeService) {
		this.landkodeService = landkodeService;
		this.postnummerService = postnummerService;
	}

	public void map(Organisasjon wsOrganisasjon, Mottaker mottaker) throws RegOppslagFunctionalException {
		OrganisasjonsDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();
		mottaker.setKortNavn(StringUtils.collectionToDelimitedString(((UstrukturertNavn) wsOrganisasjon.getNavn()).getNavnelinje(), " "));
		mottaker.setNavn(StringUtils.collectionToDelimitedString(((UstrukturertNavn) orgDet.getNavn().get(0).getNavn()).getNavnelinje(), " "));

		if (orgDet.getGjeldendeMaalform() != null) {
			if ("NO".equals(orgDet.getGjeldendeMaalform().getKodeRef())) {
				mottaker.setSpraakkode(Spraakkode.NB);
			} else {
				mottaker.setSpraakkode(Spraakkode.valueOf(orgDet.getGjeldendeMaalform().getKodeRef()));
			}
		}

		NorskPostadresse norskPostadresse = new NorskPostadresse();
		if (!(orgDet.getPostadresse() == null || orgDet.getPostadresse().isEmpty())) {
			if (orgDet.getPostadresse().get(0) instanceof SemistrukturertAdresse) {
				SemistrukturertAdresse adresse = (SemistrukturertAdresse) orgDet.getPostadresse().get(0);
				settAdresseledd(adresse, norskPostadresse);
				if (harPostnummer) {
					norskPostadresse.setPoststed(postnummerService.finnPoststed(norskPostadresse.getPostnummer()));
				}
			} else {
				Gateadresse gateadresse = (Gateadresse) orgDet.getPostadresse().get(0);
				norskPostadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn()).orElse("") + " " + Optional.of(gateadresse.getHusnummer().toString()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
				if (orgDet.getPostadresse().get(0) instanceof StrukturertAdresse) {
					StedsadresseNorge stedsadresseNorge = (StedsadresseNorge) orgDet.getPostadresse().get(0);
					if (stedsadresseNorge.getPoststed() != null) {
						norskPostadresse.setPostnummer(stedsadresseNorge.getPoststed().getKodeRef());
						norskPostadresse.setPoststed(postnummerService.finnPoststed(stedsadresseNorge.getPoststed().getKodeRef()));
					}
				}
			}
			GeografiskAdresse geografiskAdresse = orgDet.getPostadresse().get(0);
			if (geografiskAdresse.getLandkode() != null) {
				norskPostadresse.setLand(landkodeService.finnLandnavn(geografiskAdresse.getLandkode().getKodeRef()));
			}
		} else if (!orgDet.getForretningsadresse().isEmpty()) {
			if (orgDet.getForretningsadresse().get(0) instanceof SemistrukturertAdresse) {
				SemistrukturertAdresse adresse = (SemistrukturertAdresse) orgDet.getForretningsadresse().get(0);
				settAdresseledd(adresse, norskPostadresse);
				if (harPostnummer) {
					norskPostadresse.setPoststed(postnummerService.finnPoststed(norskPostadresse.getPostnummer()));
				}
			} else {
				Gateadresse gateadresse = (Gateadresse) orgDet.getForretningsadresse().get(0);
				norskPostadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn()).orElse("") + " " + Optional.of(gateadresse.getHusnummer().toString()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
				if (orgDet.getForretningsadresse().get(0) instanceof StrukturertAdresse) {
					StedsadresseNorge stedsadresseNorge = (StedsadresseNorge) orgDet.getForretningsadresse().get(0);
					if (stedsadresseNorge.getPoststed() != null) {
						norskPostadresse.setPostnummer(stedsadresseNorge.getPoststed().getKodeRef());
						norskPostadresse.setPoststed(postnummerService.finnPoststed(stedsadresseNorge.getPoststed().getKodeRef()));
					}
				}
			}
			GeografiskAdresse geografiskAdresse = orgDet.getForretningsadresse().get(0);
			if (geografiskAdresse.getLandkode() != null) {
				norskPostadresse.setLand(landkodeService.finnLandnavn(geografiskAdresse.getLandkode().getKodeRef()));
			}
		}
		validatePostadresse(norskPostadresse, mottaker);

		mottaker.setAdresse(norskPostadresse);
	}

	private void settAdresseledd(SemistrukturertAdresse adresse, NorskPostadresse norskPostadresse) {
		for (NoekkelVerdiAdresse nokler : adresse.getAdresseledd()) {
			if ("ADR1".equals(nokler.getNoekkel().getKodeRef())) {
				norskPostadresse.setAdresselinje1(nokler.getVerdi());
			} else if ("ADR2".equals(nokler.getNoekkel().getKodeRef())) {
				norskPostadresse.setAdresselinje2(nokler.getVerdi());
			} else if ("ADR3_1".equals(nokler.getNoekkel().getKodeRef())) {
				norskPostadresse.setAdresselinje3(nokler.getVerdi());
			} else if ("ADR3_2".equals(nokler.getNoekkel().getKodeRef())) {
				norskPostadresse.setAdresselinje4(nokler.getVerdi());
			} else if ("PONR".equals(nokler.getNoekkel().getKodeRef())) {
				norskPostadresse.setPostnummer(nokler.getVerdi());
			} else if ("POST".equals(nokler.getNoekkel().getKodeRef())) {
				norskPostadresse.setPoststed(nokler.getVerdi());
			}
		}
	}

	private void validatePostadresse(NorskPostadresse postadresse, Mottaker mottaker) throws RegOppslagFunctionalException {
		if ("Norway".equalsIgnoreCase(postadresse.getLand()) && StringUtils.isEmpty(postadresse.getPostnummer())) {
			throw new RegOppslagFunctionalException("Mottaker orgoppslag - mangler postnummer for organisasjon: " + mottaker.getId());
		}
	}

}