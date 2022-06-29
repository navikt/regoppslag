package no.nav.regoppslag.consumer.map;

import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class PostadresseMapper {

	private PostadresseMapper() {
	}

	public static NorskPostadresse mapPostadresseToNorskpostadresse(Postadresse postadresse) {
		NorskPostadresse norskPostadresse = new NorskPostadresse();
		norskPostadresse.setLand(postadresse.getLand());
		norskPostadresse.setAdresselinje1(postadresse.getAdresselinje1());
		norskPostadresse.setAdresselinje2(postadresse.getAdresselinje2());
		norskPostadresse.setAdresselinje3(postadresse.getAdresselinje3());
		norskPostadresse.setPostnummer(postadresse.getPostnummer());
		norskPostadresse.setPoststed(postadresse.getPoststed());

		return norskPostadresse;
	}

	public static UtenlandskPostadresse mapPostadresseToUtenlandskadresse(Postadresse postadresse) {
		UtenlandskPostadresse utenlandskPostadresse = new UtenlandskPostadresse();
		utenlandskPostadresse.setLand(postadresse.getLand());
		utenlandskPostadresse.setAdresselinje1(postadresse.getAdresselinje1());
		utenlandskPostadresse.setAdresselinje2(postadresse.getAdresselinje2());
		utenlandskPostadresse.setAdresselinje3(postadresse.getAdresselinje3());

		if (postadresse.getPoststed() != null) {
			if (utenlandskPostadresse.getAdresselinje2() == null) {
				utenlandskPostadresse.setAdresselinje2(postadresse.getPoststed());
			} else if (utenlandskPostadresse.getAdresselinje3() == null) {
				utenlandskPostadresse.setAdresselinje3(postadresse.getPoststed());
			} else {
				String kombinertAdresse = postadresse.getAdresselinje2() + ", " + postadresse.getAdresselinje3();
				utenlandskPostadresse.setAdresselinje2(kombinertAdresse);
				utenlandskPostadresse.setAdresselinje3(postadresse.getPoststed());
			}
		}
		return utenlandskPostadresse;
	}
}
