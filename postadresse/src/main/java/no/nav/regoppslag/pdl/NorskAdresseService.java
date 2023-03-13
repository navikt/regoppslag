package no.nav.regoppslag.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.Matrikkeladresse;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.to.Vegadresse;
import no.nav.regoppslag.service.PostnummerService;

import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.pdl.MapPDLUtils.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class NorskAdresseService {

	private final PostnummerService postnummerService;

	private static final String LANDKODE_NORGE = "NO";
	private static final String ERROR_MELDING = "Feltet %s kan ikke være null eller tomt";
	private static final String CARE_OF = "C/O ";
	private static final String POSTBOKS = "Postboks ";
	private static final String POSTNUMMER = "postnummer";


	public NorskAdresseService(PostnummerService postnummerService) {
		this.postnummerService = postnummerService;
	}

	PostadresseTo mapNorskPostAdresse(Kontaktadresse kontaktadresse, String coAdressenavn) {
		if (nonNull(kontaktadresse.getVegadresse())) {
			return mapVegadresse(kontaktadresse.getVegadresse(), coAdressenavn).adressekilde(KONTAKTADRESSE).build();
		} else if (nonNull(kontaktadresse.getPostadresseIFrittFormat())) {
			Kontaktadresse.PostadresseIFrittFormat postadresse = kontaktadresse.getPostadresseIFrittFormat();
			if (isBlank(kontaktadresse.getCoAdressenavn())) {
				return PostadresseTo.builder()
						.adressekilde(KONTAKTADRESSE)
						.adresseType(POSTADRESSE_INNLAND)
						.adresselinje1(isBlank(postadresse.getAdresselinje1()) ? null : postadresse.getAdresselinje1())
						.adresselinje2(postadresse.getAdresselinje2()).adresselinje3(postadresse.getAdresselinje3())
						.postnummer(isBlank(postadresse.getPostnummer()) ? null : postadresse.getPostnummer())
						.poststed(isBlank(postadresse.getPostnummer()) ? null : postnummerService.finnPoststed(postadresse.getPostnummer()))
						.landkode(LANDKODE_NORGE)
						.build();
			}
			return PostadresseTo.builder()
					.adressekilde(KONTAKTADRESSE)
					.adresseType(POSTADRESSE_INNLAND)
					.adresselinje1(kontaktadresse.getCoAdressenavn())
					.adresselinje2(requireNonNull(postadresse.getAdresselinje1(), format(ERROR_MELDING, "adresselinje2")))
					.adresselinje3(postadresse.getAdresselinje2())
					.postnummer(isBlank(postadresse.getPostnummer()) ? null : postadresse.getPostnummer())
					.poststed(isBlank(postadresse.getPostnummer()) ? null : postnummerService.finnPoststed(postadresse.getPostnummer()))
					.landkode(LANDKODE_NORGE)
					.build();
		} else if (nonNull(kontaktadresse.getPostboksadresse())) {
			Kontaktadresse.Postboksadresse postboksadresse = kontaktadresse.getPostboksadresse();
			return PostadresseTo.builder()
					.adressekilde(KONTAKTADRESSE)
					.adresseType(POSTADRESSE_INNLAND)
					.adresselinje1(isNotBlank(postboksadresse.getPostbokseier()) ? CARE_OF + postboksadresse.getPostbokseier() : POSTBOKS + requireNonNull(postboksadresse.getPostboks(), format(ERROR_MELDING, "postboks")))
					.adresselinje2(isNotBlank(postboksadresse.getPostbokseier()) ? POSTBOKS + requireNonNull(postboksadresse.getPostboks(), format(ERROR_MELDING, "postboks")) : null)
					.postnummer(postboksadresse.getPostnummer())
					.poststed(postnummerService.finnPoststed(postboksadresse.getPostnummer()))
					.landkode(LANDKODE_NORGE)
					.build();
		}
		return null;
	}

	PostadresseTo.PostadresseToBuilder mapVegadresse(Vegadresse vegadresse, String coAdressenavn) {
		return isBlank(coAdressenavn) ?
				PostadresseTo.builder()
						.adresseType(POSTADRESSE_INNLAND)
						.adresselinje1(Optional.ofNullable(vegadresse.getAdressenavn())
								.orElse("") + " " + Optional.ofNullable(isNull(vegadresse.getHusnummer()) ? null : vegadresse.getHusnummer())
								.orElse("") + Optional.ofNullable(vegadresse.getHusbokstav()).orElse(""))
						.postnummer(requireNonNull(vegadresse.getPostnummer(), format(ERROR_MELDING, POSTNUMMER)))
						.poststed(requireNonNull(postnummerService.finnPoststed(vegadresse.getPostnummer()), format(ERROR_MELDING, "poststed")))
						.landkode(LANDKODE_NORGE) :
				PostadresseTo.builder()
						.adresseType(POSTADRESSE_INNLAND)
						.adresselinje1(coAdressenavn)
						.adresselinje2(Optional.ofNullable(vegadresse.getAdressenavn())
								.orElse("") + " " + Optional.ofNullable(isNull(vegadresse.getHusnummer()) ? null : vegadresse.getHusnummer())
								.orElse("") + Optional.ofNullable(vegadresse.getHusbokstav()).orElse(""))
						.postnummer(requireNonNull(vegadresse.getPostnummer(), format(ERROR_MELDING, POSTNUMMER)))
						.poststed(requireNonNull(postnummerService.finnPoststed(vegadresse.getPostnummer()), format(ERROR_MELDING, "poststed")))
						.landkode(LANDKODE_NORGE);
	}

	PostadresseTo mapMatrikkeladresse(Matrikkeladresse matrikkeladresse, AdresseKildeCode adresseKildeCode) {
		return PostadresseTo.builder()
				.adressekilde(adresseKildeCode)
				.adresseType(POSTADRESSE_INNLAND)
				.adresselinje1(matrikkeladresse.getTilleggsnavn())
				.postnummer(matrikkeladresse.getPostnummer())
				.poststed(postnummerService.finnPoststed(matrikkeladresse.getPostnummer()))
				.landkode(LANDKODE_NORGE)
				.build();
	}
}
