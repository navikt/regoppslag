package no.nav.regoppslag.consumer.map;

import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import org.junit.jupiter.api.Test;

import static no.nav.regoppslag.consumer.map.PostadresseMapper.mapPostadresseToUtenlandskadresse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PostadresseMapperTest {

	@Test
	void skalSetteAdresseForOrganisasjonUtenPostadresse() {
		Postadresse postadresse = new Postadresse(
				"Postadresse",
				"Prospect Road",
				"Arnhall Business Park",
				"Westhill",
				null,
				null,
				null,
				"GB"
		);

		UtenlandskPostadresse utenlandskPostadresse = mapPostadresseToUtenlandskadresse(postadresse);

		assertEquals("Prospect Road", utenlandskPostadresse.getAdresselinje1());
		assertEquals("Arnhall Business Park", utenlandskPostadresse.getAdresselinje2());
		assertEquals("Westhill", utenlandskPostadresse.getAdresselinje3());
		assertEquals("GB", utenlandskPostadresse.getLand());
	}

	@Test
	void skalBrukeAdresselinje2TilPoststedForOrganisasjonMedAdresselinje1Satt() {
		Postadresse postadresse = new Postadresse(
				"Postadresse",
				"Prospect Road",
				null,
				null,
				null,
				null,
				"ABERDEEN AB32 6FE",
				"GB"
		);

		UtenlandskPostadresse utenlandskPostadresse = mapPostadresseToUtenlandskadresse(postadresse);

		assertEquals("Prospect Road", utenlandskPostadresse.getAdresselinje1());
		assertEquals("ABERDEEN AB32 6FE", utenlandskPostadresse.getAdresselinje2());
		assertNull(utenlandskPostadresse.getAdresselinje3());
		assertEquals("GB", utenlandskPostadresse.getLand());
	}

	@Test
	void skalBrukeAdresselinje3TilPoststedForOrganisasjonMedAdresselinje1Og2Satt() {
		Postadresse postadresse = new Postadresse(
				"Postadresse",
				"Prospect Road",
				"Arnhall Business Park",
				null,
				null,
				null,
				"ABERDEEN AB32 6FE",
				"GB"
		);

		UtenlandskPostadresse utenlandskPostadresse = mapPostadresseToUtenlandskadresse(postadresse);

		assertEquals("Prospect Road", utenlandskPostadresse.getAdresselinje1());
		assertEquals("Arnhall Business Park", utenlandskPostadresse.getAdresselinje2());
		assertEquals("ABERDEEN AB32 6FE", utenlandskPostadresse.getAdresselinje3());
		assertEquals("GB", utenlandskPostadresse.getLand());
	}

	@Test
	void skalBrukeAdresselinje3TilPoststedForOrganisasjonMedAlleTreAdresselinjerSatt() {
		Postadresse postadresse = new Postadresse(
				"Postadresse",
				"Prospect Road",
				"Arnhall Business Park",
				"Westhill",
				null,
				null,
				"ABERDEEN AB32 6FE",
				"GB"
		);

		UtenlandskPostadresse utenlandskPostadresse = mapPostadresseToUtenlandskadresse(postadresse);

		assertEquals("Prospect Road", utenlandskPostadresse.getAdresselinje1());
		assertEquals("Arnhall Business Park, Westhill", utenlandskPostadresse.getAdresselinje2());
		assertEquals("ABERDEEN AB32 6FE", utenlandskPostadresse.getAdresselinje3());
		assertEquals("GB", utenlandskPostadresse.getLand());
	}
}