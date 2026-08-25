package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.Bostedsadresse;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse.PostadresseIFrittFormat;
import no.nav.regoppslag.consumer.pdl.to.Oppholdsadresse;
import no.nav.regoppslag.consumer.pdl.to.Vegadresse;

import static no.nav.regoppslag.pdl.PDLAdresseUtil.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

final class PDLAdresseValidator {

	static final String ERROR_MELDING = "Feltet %s kan ikke være null eller tomt";
	static final String ERROR_MELDING_PDL = "Validering av feltet %s feilet pga. manglende data i PDL";
	static final String POSTNUMMER = "postnummer";

	private PDLAdresseValidator() {
	}

	static void validerKontaktadresse(Kontaktadresse kontaktadresse) {
		if (kontaktadresse.getVegadresse() != null) {
			validerVegadresse(kontaktadresse.getVegadresse());
		} else if (kontaktadresse.getPostadresseIFrittFormat() != null) {
			validerPostadresseIFrittFormat(kontaktadresse.getPostadresseIFrittFormat(), kontaktadresse.getCoAdressenavn());
		}
	}

	static void validerVegadresse(Vegadresse vegadresse) {
		requireNonNull(vegadresse.getPostnummer(), ERROR_MELDING_PDL.formatted(POSTNUMMER));
	}

	static void validerBostedsadresse(Bostedsadresse bostedsadresse) {
		if (bostedsadresse.getVegadresse() != null) {
			validerVegadresse(bostedsadresse.getVegadresse());
		}
	}

	static void validerOppholdsadresse(Oppholdsadresse oppholdsadresse) {
		if (oppholdsadresse.getVegadresse() != null) {
			validerVegadresse(oppholdsadresse.getVegadresse());
		}
	}

	private static void validerPostadresseIFrittFormat(PostadresseIFrittFormat postadresse, String coAdressenavn) {
		if (isNotBlank(coAdressenavn)) {
			requireNonNull(postadresse.getAdresselinje1(), ERROR_MELDING.formatted("adresselinje1"));
		}
	}
}
