package no.nav.regoppslag.consumer.map;

import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostadresseMapperTest {

	@Test
	void skalSettePoststedForOrganisasjonerSomBrukerAlleTreAdresselinjeneIEreg() {
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

		UtenlandskPostadresse utenlandskPostadresse = PostadresseMapper.mapPostadresseToUtenlandskadresse(postadresse);

		assertEquals("Prospect Road", utenlandskPostadresse.getAdresselinje1());
		assertEquals("Arnhall Business Park, Westhill", utenlandskPostadresse.getAdresselinje2());
		assertEquals("ABERDEEN AB32 6FE", utenlandskPostadresse.getAdresselinje3());
		assertEquals("GB", utenlandskPostadresse.getLand());
	}
}