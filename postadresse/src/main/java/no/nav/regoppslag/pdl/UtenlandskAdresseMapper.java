package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.pdl.MapPDLUtils.getAlpha2Landkode;
import static no.nav.regoppslag.pdl.MapPDLUtils.prependWithCareOfIfMissing;
import static no.nav.regoppslag.pdl.UtenlandskPoststedFormatter.formatPostkodeBystedOgOmraade;
import static no.nav.regoppslag.pdl.UtenlandskPoststedFormatter.formatUSAogKanadaPostkodeBystedOgOmraade;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

final class UtenlandskAdresseMapper {

	private static final Set<String> USA_KANADA_LANDKODE = Set.of("USA", "CAN");
	private static final int ANTALL_ADRESSELINJER = 3;

	private UtenlandskAdresseMapper() {
	}

	static Optional<PostadresseTo> mapUtenlandskPostadresse(Kontaktadresse kontaktadresse) {
		String coAdressenavn = kontaktadresse.getCoAdressenavn();

		if (kontaktadresse.getUtenlandskAdresse() != null) {
			UtenlandskAdresse utenlandskAdresse = kontaktadresse.getUtenlandskAdresse();

			return Optional.of(mapUtenlandskAdresse(utenlandskAdresse, coAdressenavn, KONTAKTADRESSE));
		} else if (kontaktadresse.getUtenlandskAdresseIFrittFormat() != null) {
			Kontaktadresse.UtenlandskAdresseIFrittFormat utenlandskAdresse = kontaktadresse.getUtenlandskAdresseIFrittFormat();
			String coAdressenavnWithCoPrefix = prependWithCareOfIfMissing(coAdressenavn);
			String adresselinje1 = isBlank(coAdressenavnWithCoPrefix) ? utenlandskAdresse.getAdresselinje1() : coAdressenavnWithCoPrefix + ", " + utenlandskAdresse.getAdresselinje1();

			return Optional.of(PostadresseTo.builder()
					.adressekilde(KONTAKTADRESSE)
					.adresseType(POSTADRESSE_UTLAND)
					.adresselinje1(adresselinje1)
					.adresselinje2(utenlandskAdresse.getAdresselinje2())
					.adresselinje3(utenlandskAdresse.getAdresselinje3())
					.landkode(getAlpha2Landkode(utenlandskAdresse.getLandkode()))
					.build());
		}

		return Optional.empty();
	}

	static PostadresseTo mapUtenlandskAdresse(UtenlandskAdresse utenlandskAdresse, String coAdressenavn, AdresseKildeCode adresseKilde) {
		String coAdressenavnWithCoPrefix = prependWithCareOfIfMissing(coAdressenavn);
		String hovedadresse = mapHovedadresse(utenlandskAdresse);
		String bygningEtasjeLeilighet = mapBygningEtasjeLeilighet(utenlandskAdresse);
		String poststed = mapPoststed(utenlandskAdresse);

		List<String> adresselinjer = mapAdresselinjer(mapPotensielleAdresselinjer(
				coAdressenavnWithCoPrefix,
				hovedadresse,
				bygningEtasjeLeilighet,
				poststed
		));

		return PostadresseTo.builder()
				.adressekilde(adresseKilde)
				.adresseType(POSTADRESSE_UTLAND)
				.adresselinje1(adresselinjer.get(0))
				.adresselinje2(adresselinjer.get(1))
				.adresselinje3(adresselinjer.get(2))
				.landkode(getAlpha2Landkode(utenlandskAdresse.getLandkode()))
				.build();
	}

	private static List<String> mapAdresselinjer(List<String> potensielleAdresselinjer) {
		List<String> adresselinjer = potensielleAdresselinjer.stream()
				.filter(StringUtils::isNotBlank)
				.limit(ANTALL_ADRESSELINJER)
				.collect(Collectors.toCollection(ArrayList::new));

		while (adresselinjer.size() < ANTALL_ADRESSELINJER) {
			adresselinjer.add(null);
		}

		return adresselinjer;
	}

	private static List<String> mapPotensielleAdresselinjer(
			String coAdressenavn,
			String hovedadresse,
			String bygningEtasjeLeilighet,
			String poststed
	) {
		if (isNoneBlank(coAdressenavn, hovedadresse, bygningEtasjeLeilighet, poststed)) {
			return List.of(
					coAdressenavn + ", " + hovedadresse,
					bygningEtasjeLeilighet,
					poststed
			);
		}

		return Arrays.asList(
				coAdressenavn,
				hovedadresse,
				bygningEtasjeLeilighet,
				poststed
		);
	}

	private static String mapPoststed(UtenlandskAdresse utenlandskAdresse) {
		if (isNotBlank(utenlandskAdresse.getLandkode()) && USA_KANADA_LANDKODE.contains(utenlandskAdresse.getLandkode())) {
			return formatUSAogKanadaPostkodeBystedOgOmraade(utenlandskAdresse);
		}

		return formatPostkodeBystedOgOmraade(utenlandskAdresse);
	}

	private static String mapHovedadresse(UtenlandskAdresse utenlandskAdresse) {
		 if (isNotBlank(utenlandskAdresse.getPostboksNummerNavn())) {
			return utenlandskAdresse.getPostboksNummerNavn();
		}

		return utenlandskAdresse.getAdressenavnNummer();
	}

	private static String mapBygningEtasjeLeilighet(UtenlandskAdresse utenlandskAdresse) {
		if (isNotBlank(utenlandskAdresse.getBygningEtasjeLeilighet())) {
			return utenlandskAdresse.getBygningEtasjeLeilighet();
		}

		return null;
	}

}
