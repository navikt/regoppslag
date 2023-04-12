package no.nav.regoppslag.treg002;

import no.nav.regoppslag.rreg003.Adresse;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseResponse.Treg002Adresse;

public class Treg002AdresseMapper {

	public static Treg002Adresse mapAdresseTilTreg002Adresse(Adresse adresse) {

		return Treg002Adresse.builder()
				.adresselinje1(adresse.getAdresselinje1())
				.adresselinje2(adresse.getAdresselinje2())
				.adresselinje3(adresse.getAdresselinje3())
				.postnummer(adresse.getPostnummer())
				.poststed(adresse.getPoststed())
				.landkode(adresse.getLandkode())
				.build();
	}
}