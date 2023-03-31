package no.nav.regoppslag.consumer.map;

import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class OrganisasjonPostadresseMapper {

	public static NorskPostadresse mapPostadresseToNorskPostadresse(Postadresse postadresse) {
		NorskPostadresse norskPostadresse = new NorskPostadresse();
		norskPostadresse.setLand(postadresse.getLand());
		norskPostadresse.setAdresselinje1(postadresse.getAdresselinje1());
		norskPostadresse.setAdresselinje2(postadresse.getAdresselinje2());
		norskPostadresse.setAdresselinje3(postadresse.getAdresselinje3());
		norskPostadresse.setPostnummer(postadresse.getPostnummer());
		norskPostadresse.setPoststed(postadresse.getPoststed());

		return norskPostadresse;
	}

	public static UtenlandskPostadresse mapPostadresseToUtenlandskPostadresse(Postadresse postadresse) {
		UtenlandskPostadresse utenlandskPostadresse = new UtenlandskPostadresse();
		utenlandskPostadresse.setAdresselinje1(postadresse.getAdresselinje1());
		utenlandskPostadresse.setLand(postadresse.getLand());

		if (isNotBlank(postadresse.getPoststed())) {
			if (isBlank(postadresse.getAdresselinje2()) && isBlank(postadresse.getAdresselinje3())) {
				utenlandskPostadresse.setAdresselinje2(postadresse.getPoststed());
			} else if (isBlank(postadresse.getAdresselinje2())) {
				utenlandskPostadresse.setAdresselinje2(postadresse.getAdresselinje3());
				utenlandskPostadresse.setAdresselinje3(postadresse.getPoststed());
			} else if (isBlank(postadresse.getAdresselinje3())) {
				utenlandskPostadresse.setAdresselinje2(postadresse.getAdresselinje2());
				utenlandskPostadresse.setAdresselinje3(postadresse.getPoststed());
			} else {
				String kombinertAdresse = postadresse.getAdresselinje2() + ", " + postadresse.getAdresselinje3();
				utenlandskPostadresse.setAdresselinje2(kombinertAdresse);
				utenlandskPostadresse.setAdresselinje3(postadresse.getPoststed());
			}
		} else {
			utenlandskPostadresse.setAdresselinje2(postadresse.getAdresselinje2());
			utenlandskPostadresse.setAdresselinje3(postadresse.getAdresselinje3());
		}

		return utenlandskPostadresse;
	}
}
