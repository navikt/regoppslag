package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.Bostedsadresse;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse.PostadresseIFrittFormat;
import no.nav.regoppslag.consumer.pdl.to.Oppholdsadresse;
import no.nav.regoppslag.consumer.pdl.to.Vegadresse;

import static no.nav.regoppslag.pdl.MapPDLUtils.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

final class PDLAdresseValidator {

	static final String ERROR_MELDING = "Feltet %s kan ikke være null eller tomt";
	static final String ERROR_MELDING_PDL = "Validering av feltet %s feilet pga. manglende data i PDL";
	static final String POSTNUMMER = "postnummer";

	private PDLAdresseValidator() {
	}

	static void valider(Kontaktadresse kontaktadresse) {
		if (kontaktadresse.getVegadresse() != null) {
			valider(kontaktadresse.getVegadresse());
		} else if (kontaktadresse.getPostadresseIFrittFormat() != null) {
			valider(kontaktadresse.getPostadresseIFrittFormat(), kontaktadresse.getCoAdressenavn());
		}
	}

	static void valider(Vegadresse vegadresse) {
		requireNonNull(vegadresse.getPostnummer(), ERROR_MELDING_PDL.formatted(POSTNUMMER));
	}

	static void valider(Bostedsadresse bostedsadresse) {
		if (bostedsadresse.getVegadresse() != null) {
			valider(bostedsadresse.getVegadresse());
		}
	}

	static void valider(Oppholdsadresse oppholdsadresse) {
		if (oppholdsadresse.getVegadresse() != null) {
			valider(oppholdsadresse.getVegadresse());
		}
	}

	private static void valider(PostadresseIFrittFormat postadresse, String coAdressenavn) {
		if (isNotBlank(coAdressenavn)) {
			requireNonNull(postadresse.getAdresselinje1(), ERROR_MELDING.formatted("adresselinje1"));
		}
	}
}
