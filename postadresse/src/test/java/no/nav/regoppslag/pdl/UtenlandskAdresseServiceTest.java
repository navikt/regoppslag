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
import static no.nav.regoppslag.util.PDLResponseUtil.BYGNING_ETASJE_LEILIGHET_BVH;
import static no.nav.regoppslag.util.PDLResponseUtil.BYSTED_BVH;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_USA;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTKODE_BVH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UtenlandskAdresseServiceTest {

	@Test
	public void ShouldMapKontaktadresseForUtlandWithUtlandsAddresse() {
		UtenlandskAdresse adresse = UtenlandskAdresse.builder()
				.adressenavnNummer(null)
				.bygningEtasjeLeilighet(BYGNING_ETASJE_LEILIGHET_BVH)
				.postboksNummerNavn(null)
				.postkode(POSTKODE_BVH)
				.bySted(BYSTED_BVH)
				.regionDistriktOmraade("")
				.landkode(LANDKODE_USA)
				.build();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.UtenlandskAdresse(adresse)
				.type(POSTADRESSE_UTLAND)
				.build();
		kontaktadresse.setMetadata(Metadata.builder().master(PDL.name()).build());

		Optional<PostadresseTo> mottakerInfo = UtenlandskAdresseService.mapUtenlandskPostAdresse(kontaktadresse);
		assertThat(mottakerInfo).isPresent();

		PostadresseTo response = mottakerInfo.get();

		assertEquals(BYGNING_ETASJE_LEILIGHET_BVH, response.getAdresselinje1());
		assertEquals(POSTKODE_BVH + " " + BYSTED_BVH, response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_UTLAND, response.getAdresseType());
		assertNull(response.getPostnummer());
		assertNull(response.getPoststed());
		assertEquals(KONTAKTADRESSE, response.getAdressekilde());
	}

}