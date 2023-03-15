package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.Metadata;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.PDL;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UtenlandskAdresseServiceTest {
	public static final String BY_STED = "Beverly Hills";
	public static final String LANDKODE = "EST";
	private static final String BYGNING_ETASJE_LEILIGHET = "ESTLANDSHUSET";
	private static final String POSTKODE = "90210";

	@Test
	public void ShouldMapKontaktadresseForUtlandWithUtlandsAddresse() {
		UtenlandskAdresse adresse = UtenlandskAdresse.builder()
				.adressenavnNummer(null)
				.bygningEtasjeLeilighet(BYGNING_ETASJE_LEILIGHET)
				.postboksNummerNavn(null)
				.postkode(POSTKODE)
				.bySted(BY_STED)
				.regionDistriktOmraade("")
				.landkode(LANDKODE)
				.build();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.UtenlandskAdresse(adresse)
				.type(POSTADRESSE_UTLAND)
				.build();
		kontaktadresse.setMetadata(Metadata.builder().master(PDL.name()).build());

		Optional<PostadresseTo> mottakerInfo = UtenlandskAdresseService.mapUtenlandskPostAdresse(kontaktadresse);
		assertThat(mottakerInfo).isPresent();

		PostadresseTo response = mottakerInfo.get();

		assertEquals(BYGNING_ETASJE_LEILIGHET, response.getAdresselinje1());
		assertEquals(POSTKODE + " " + BY_STED, response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_UTLAND, response.getAdresseType());
		assertNull(response.getPostnummer());
		assertNull(response.getPoststed());
		assertEquals(KONTAKTADRESSE, response.getAdressekilde());
	}

}