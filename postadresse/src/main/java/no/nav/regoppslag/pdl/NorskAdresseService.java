package no.nav.regoppslag.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse.PostadresseIFrittFormat;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse.Postboksadresse;
import no.nav.regoppslag.consumer.pdl.to.Matrikkeladresse;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo.PostadresseToBuilder;
import no.nav.regoppslag.consumer.pdl.to.Vegadresse;
import no.nav.regoppslag.service.PostnummerService;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.pdl.MapPDLUtils.prependWithCareOfIfMissing;
import static no.nav.regoppslag.pdl.MapPDLUtils.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
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

	Optional<PostadresseTo> mapNorskPostadresse(Kontaktadresse kontaktadresse) {

		if (nonNull(kontaktadresse.getVegadresse())) {
			return Optional.of(mapVegadresse(kontaktadresse.getVegadresse(), kontaktadresse.getCoAdressenavn())
					.adressekilde(KONTAKTADRESSE).build());
		} else if (nonNull(kontaktadresse.getPostadresseIFrittFormat())) {
			return Optional.of(mapPostadresseFrittFormat(kontaktadresse));
		} else if (nonNull(kontaktadresse.getPostboksadresse())) {
			return Optional.ofNullable(mapPostboksadresse(kontaktadresse));
		}

		return Optional.empty();
	}

	PostadresseToBuilder mapVegadresse(Vegadresse vegadresse, String coAdressenavn) {
		String coAdressenavnWithCoPrefix = prependWithCareOfIfMissing(coAdressenavn);

		PostadresseToBuilder builder = PostadresseTo.builder()
				.adresseType(POSTADRESSE_INNLAND)
				.postnummer(requireNonNull(vegadresse.getPostnummer(), format(ERROR_MELDING, POSTNUMMER)))
				.poststed(requireNonNull(postnummerService.finnPoststed(vegadresse.getPostnummer()), format(ERROR_MELDING, "poststed")))
				.landkode(LANDKODE_NORGE);

		if (isBlank(coAdressenavnWithCoPrefix)) {
			builder.adresselinje1(vegadresse.mapAdresselinjeFromVegadresse());
		} else {
			builder.adresselinje1(coAdressenavnWithCoPrefix)
					.adresselinje2(vegadresse.mapAdresselinjeFromVegadresse());
		}

		return builder;
	}

	private PostadresseTo mapPostadresseFrittFormat(Kontaktadresse kontaktadresse) {
		PostadresseIFrittFormat postadresse = kontaktadresse.getPostadresseIFrittFormat();

		PostadresseToBuilder builder = PostadresseTo.builder()
				.adressekilde(KONTAKTADRESSE)
				.adresseType(POSTADRESSE_INNLAND)
				.postnummer(isBlank(postadresse.getPostnummer()) ? null : postadresse.getPostnummer())
				.poststed(isBlank(postadresse.getPostnummer()) ? null : postnummerService.finnPoststed(postadresse.getPostnummer()))
				.landkode(LANDKODE_NORGE);

		if (isBlank(kontaktadresse.getCoAdressenavn())) {
			return builder
					.adresselinje1(isBlank(postadresse.getAdresselinje1()) ? null : postadresse.getAdresselinje1())
					.adresselinje2(postadresse.getAdresselinje2())
					.adresselinje3(postadresse.getAdresselinje3())
					.build();
		}

		return builder
				.adresselinje1(kontaktadresse.getCoAdressenavn())
				.adresselinje2(requireNonNull(postadresse.getAdresselinje1(), format(ERROR_MELDING, "adresselinje2")))
				.adresselinje3(postadresse.getAdresselinje2())
				.build();
	}

	private PostadresseTo mapPostboksadresse(Kontaktadresse kontaktadresse) {
		Postboksadresse postboksadresse = kontaktadresse.getPostboksadresse();

		PostadresseToBuilder builder = PostadresseTo.builder()
				.adressekilde(KONTAKTADRESSE)
				.adresseType(POSTADRESSE_INNLAND)
				.postnummer(postboksadresse.getPostnummer())
				.poststed(postnummerService.finnPoststed(postboksadresse.getPostnummer()))
				.landkode(LANDKODE_NORGE);

		if (isNotBlank(postboksadresse.getPostbokseier()) && isNotBlank(postboksadresse.getPostboks())) {
			return builder
					.adresselinje1(CARE_OF + postboksadresse.getPostbokseier())
					.adresselinje2(mapPostboks(postboksadresse.getPostboks()))
					.build();
		}

		return isBlank(postboksadresse.getPostboks()) ? null : builder
				.adresselinje1(mapPostboks(postboksadresse.getPostboks()))
				.build();
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

	private String mapPostboks(String postboks) {
		return postboks.toLowerCase().contains(POSTBOKS.toLowerCase()) ? postboks : POSTBOKS + postboks;
	}
}
