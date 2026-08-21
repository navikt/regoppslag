package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse.PostadresseIFrittFormat;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse.Postboksadresse;
import no.nav.regoppslag.consumer.pdl.to.Matrikkeladresse;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.to.Vegadresse;
import no.nav.regoppslag.service.PostnummerService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.pdl.MapPDLUtils.prependWithCareOfIfMissing;
import static org.apache.commons.lang3.StringUtils.isBlank;


final class NorskAdresseMapper {

	private static final String LANDKODE_NORGE = "NO";
	private static final String POSTBOKS = "Postboks ";
	private static final int ANTALL_ADRESSELINJER = 3;

	private NorskAdresseMapper() {
	}

	static Optional<PostadresseTo> mapPostadresse(Kontaktadresse kontaktadresse) {

		if (kontaktadresse.getVegadresse() != null) {
			return Optional.of(mapVegadresse(kontaktadresse.getVegadresse(), kontaktadresse.getCoAdressenavn(), KONTAKTADRESSE));
		} else if (kontaktadresse.getPostadresseIFrittFormat() != null) {
			return Optional.of(mapPostadresseFrittFormat(kontaktadresse));
		} else if (kontaktadresse.getPostboksadresse() != null) {
			return Optional.ofNullable(mapPostboksadresse(kontaktadresse));
		}

		return Optional.empty();
	}

	static PostadresseTo mapVegadresse(Vegadresse vegadresse, String coAdressenavn, AdresseKildeCode adresseKilde) {
		List<String> adresselinjer = mapAdresselinjer(prependWithCareOfIfMissing(coAdressenavn), vegadresse.mapAdresselinjeFromVegadresse());
		String postnummer = vegadresse.getPostnummer();
		String poststed = PostnummerService.finnPoststed(postnummer);

		return byggPostadresse(adresselinjer, postnummer, poststed, adresseKilde);
	}

	static PostadresseTo mapMatrikkeladresse(Matrikkeladresse matrikkeladresse, AdresseKildeCode adresseKilde) {
		List<String> adresselinjer = mapAdresselinjer(matrikkeladresse.getTilleggsnavn());

		return byggPostadresse(
				adresselinjer,
				matrikkeladresse.getPostnummer(),
				PostnummerService.finnPoststed(matrikkeladresse.getPostnummer()),
				adresseKilde
		);
	}

	private static PostadresseTo mapPostadresseFrittFormat(Kontaktadresse kontaktadresse) {
		PostadresseIFrittFormat postadresse = kontaktadresse.getPostadresseIFrittFormat();

		List<String> adresselinjer = mapAdresselinjer(
				kontaktadresse.getCoAdressenavn(),
				postadresse.getAdresselinje1(),
				postadresse.getAdresselinje2(),
				isBlank(kontaktadresse.getCoAdressenavn()) ? postadresse.getAdresselinje3() : null
		);

		String postnummer = isBlank(postadresse.getPostnummer()) ? null : postadresse.getPostnummer();
		String poststed = postnummer == null ? null : PostnummerService.finnPoststed(postnummer);

		return byggPostadresse(adresselinjer, postnummer, poststed, KONTAKTADRESSE);
	}

	private static List<String> mapAdresselinjer(String... potensielleAdresselinjer) {
		List<String> adresselinjer = Arrays.stream(potensielleAdresselinjer)
				.filter(StringUtils::isNotBlank)
				.limit(ANTALL_ADRESSELINJER)
				.collect(Collectors.toCollection(ArrayList::new));

		while (adresselinjer.size() < ANTALL_ADRESSELINJER) {
			adresselinjer.add(null);
		}

		return adresselinjer;
	}

	private static PostadresseTo mapPostboksadresse(Kontaktadresse kontaktadresse) {
		Postboksadresse postboksadresse = kontaktadresse.getPostboksadresse();

		if (isBlank(postboksadresse.getPostboks())) {
			return null;
		}

		String postbokseier = prependWithCareOfIfMissing(postboksadresse.getPostbokseier());
		List<String> adresselinjer = mapAdresselinjer(postbokseier, mapPostboks(postboksadresse.getPostboks()));

		return byggPostadresse(
				adresselinjer,
				postboksadresse.getPostnummer(),
				PostnummerService.finnPoststed(postboksadresse.getPostnummer()),
				KONTAKTADRESSE
		);
	}

	private static String mapPostboks(String postboks) {
		return postboks.toLowerCase().contains(POSTBOKS.toLowerCase()) ? postboks : POSTBOKS + postboks;
	}

	private static PostadresseTo byggPostadresse(List<String> adresselinjer, String postnummer, String poststed, AdresseKildeCode adresseKilde) {
		return PostadresseTo.builder()
				.adressekilde(adresseKilde)
				.adresseType(POSTADRESSE_INNLAND)
				.adresselinje1(adresselinjer.get(0))
				.adresselinje2(adresselinjer.get(1))
				.adresselinje3(adresselinjer.get(2))
				.postnummer(postnummer)
				.poststed(poststed)
				.landkode(LANDKODE_NORGE)
				.build();
	}
}
