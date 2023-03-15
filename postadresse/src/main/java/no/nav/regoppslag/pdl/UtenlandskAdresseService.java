package no.nav.regoppslag.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;
import no.nav.regoppslag.service.LandkodeService;

import java.util.Optional;

import static com.neovisionaries.i18n.CountryCode.XK;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.metrics.MetricLabels.KOSOVO_LANDKODE_NAV_REGISTRENE;
import static no.nav.regoppslag.metrics.MetricLabels.UNKNOWN_LANDKODE;
import static no.nav.regoppslag.pdl.MapPDLUtils.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class UtenlandskAdresseService {

	private static final String ERROR_UTENLANDSKADRESSE = "Feltet %s kan ikke være null eller tomt for utenlandskAdresse";

	static Optional<PostadresseTo> mapUtenlandskPostAdresse(Kontaktadresse kontaktadresse) {
		String coAdressenavn = kontaktadresse.getCoAdressenavn();
		if (nonNull(kontaktadresse.getUtenlandskAdresse())) {
			UtenlandskAdresse utenlandskAdresse = kontaktadresse.getUtenlandskAdresse();
			return Optional.of(mapUtenlandskAdresse(utenlandskAdresse, coAdressenavn)
					.adressekilde(KONTAKTADRESSE)
					.build());
		} else if (nonNull(kontaktadresse.getUtenlandskAdresseIFrittFormat())) {
			Kontaktadresse.UtenlandskAdresseIFrittFormat utenlandskAdresse = kontaktadresse.getUtenlandskAdresseIFrittFormat();
			return Optional.of(PostadresseTo.builder()
					.adressekilde(KONTAKTADRESSE)
					.adresseType(POSTADRESSE_UTLAND)
					.adresselinje1(isBlank(coAdressenavn) ? utenlandskAdresse.getAdresselinje1() : coAdressenavn + ", " + utenlandskAdresse.getAdresselinje1())
					.adresselinje2(utenlandskAdresse.getAdresselinje2())
					.adresselinje3(utenlandskAdresse.getAdresselinje3())
					.landkode(requireNonNull(getAlpha2Landkode(utenlandskAdresse.getLandkode()), format(ERROR_UTENLANDSKADRESSE, "landkode")))
					.build());
		}
		return Optional.empty();
	}

	static PostadresseTo.PostadresseToBuilder mapUtenlandskAdresse(UtenlandskAdresse utenlandskAdresse, String coAdressenavn) {
		return PostadresseTo.builder()
				.adresseType(POSTADRESSE_UTLAND)
				.adresselinje1(mapUtenlandskAdresselinje1(utenlandskAdresse, coAdressenavn))
				.adresselinje2(mapUtenlandskAdresselinje2(utenlandskAdresse, coAdressenavn))
				.adresselinje3(mapUtenlandskAdresselinje3(utenlandskAdresse, coAdressenavn))
				.landkode(requireNonNull(getAlpha2Landkode(utenlandskAdresse.getLandkode()), format(ERROR_UTENLANDSKADRESSE, "landkode")));
	}

	private static String mapUtenlandskAdresselinje1(UtenlandskAdresse utenlandskAdresse, String coAdressenavn) {
		String postboksOrAdressenavnNummer = getPostboksOrAdressenavnNummer(utenlandskAdresse);
		String postkodeAndByStedAndOmraade = mapUtenlandskPostkodeAndByStedAndOmraade(utenlandskAdresse);
		String bygningEtasjeLeilighet = mapBygningEtasjeLeilighet(utenlandskAdresse);

		if (isNotBlank(coAdressenavn)) {
			if (isNotBlank(postkodeAndByStedAndOmraade) && isNotBlank(bygningEtasjeLeilighet)) {
				return coAdressenavn + ", " + postboksOrAdressenavnNummer;
			} else if (isNotBlank(postkodeAndByStedAndOmraade) && isNotBlank(postboksOrAdressenavnNummer)) {
				return coAdressenavn;
			}
		} else if (isBlank(coAdressenavn) && isNotBlank(postboksOrAdressenavnNummer)) {
			return postboksOrAdressenavnNummer;
		}
		return postboksOrAdressenavnNummer;
	}

	private static String mapUtenlandskAdresselinje2(UtenlandskAdresse utenlandskAdresse, String coAdressenavn) {
		String postboksOrAdressenavnNummer = getPostboksOrAdressenavnNummer(utenlandskAdresse);
		String postkodeAndByStedAndOmraade = mapUtenlandskPostkodeAndByStedAndOmraade(utenlandskAdresse);
		String bygningEtasjeLeilighet = mapBygningEtasjeLeilighet(utenlandskAdresse);

		if (isNotBlank(coAdressenavn)) {
			if (isNotBlank(postkodeAndByStedAndOmraade) && isNotBlank(bygningEtasjeLeilighet)) {
				return bygningEtasjeLeilighet;
			} else if (isNotBlank(postkodeAndByStedAndOmraade) && isBlank(bygningEtasjeLeilighet)) {
				return postboksOrAdressenavnNummer;
			}
		} else {
			if (isNotBlank(bygningEtasjeLeilighet)) {
				return bygningEtasjeLeilighet;
			}
		}
		return postkodeAndByStedAndOmraade;
	}

	private static String mapUtenlandskAdresselinje3(UtenlandskAdresse utenlandskAdresse, String coAdressenavn) {
		String postboksOrAdressenavnNummer = getPostboksOrAdressenavnNummer(utenlandskAdresse);
		String postkodeAndByStedAndOmraade = mapUtenlandskPostkodeAndByStedAndOmraade(utenlandskAdresse);
		String bygningEtasjeLeilighet = mapBygningEtasjeLeilighet(utenlandskAdresse);

		if (isNotBlank(coAdressenavn)) {
			if (isNotBlank(postkodeAndByStedAndOmraade) && isNotBlank(bygningEtasjeLeilighet)) {
				return postkodeAndByStedAndOmraade;
			} else if (isBlank(postkodeAndByStedAndOmraade) && isNotBlank(postboksOrAdressenavnNummer)) {
				return bygningEtasjeLeilighet;
			}
		} else if (isBlank(bygningEtasjeLeilighet) || isBlank(postboksOrAdressenavnNummer)) {
			return null;
		}
		return postkodeAndByStedAndOmraade;
	}

	private static String mapUtenlandskPostkodeAndByStedAndOmraade(UtenlandskAdresse utenlandskAdresse) {
		boolean hasPostKode = isNotBlank(utenlandskAdresse.getPostkode());
		boolean hasBySted = isNotBlank(utenlandskAdresse.getBySted());
		boolean hasRegionDistriktOmr = isNotBlank(utenlandskAdresse.getRegionDistriktOmraade());

		if (hasPostKode) {
			if (hasBySted && hasRegionDistriktOmr) {
				return format("%s %s, %s", utenlandskAdresse.getPostkode(), utenlandskAdresse.getBySted(), utenlandskAdresse.getRegionDistriktOmraade());
			} else {
				if (!hasBySted && hasRegionDistriktOmr) {
					return format("%s, %s", utenlandskAdresse.getPostkode(), utenlandskAdresse.getRegionDistriktOmraade());
				} else if (hasBySted) {
					return format("%s %s", utenlandskAdresse.getPostkode(), utenlandskAdresse.getBySted());
				} else {
					return utenlandskAdresse.getPostkode();
				}
			}
		} else if (hasBySted) {
			if (hasRegionDistriktOmr) {
				return format("%s, %s", utenlandskAdresse.getBySted(), utenlandskAdresse.getRegionDistriktOmraade());
			} else {
				return utenlandskAdresse.getBySted();
			}
		} else {
			return utenlandskAdresse.getRegionDistriktOmraade();
		}
	}

	private static String getPostboksOrAdressenavnNummer(UtenlandskAdresse utenlandskAdresse) {
		return isNotBlank(utenlandskAdresse.getPostboksNummerNavn()) ? utenlandskAdresse.getPostboksNummerNavn() :
				utenlandskAdresse.getAdressenavnNummer();
	}

	private static String mapBygningEtasjeLeilighet(UtenlandskAdresse utenlandskAdresse) {
		return isNotBlank(utenlandskAdresse.getBygningEtasjeLeilighet()) ? utenlandskAdresse.getBygningEtasjeLeilighet() : null;
	}

	private static String getAlpha2Landkode(String alpha3Landkode) {
		String alpha2Landkode = KOSOVO_LANDKODE_NAV_REGISTRENE.equalsIgnoreCase(alpha3Landkode) ? XK.name() : LandkodeService.finnLandkodeAlpha2FraAlpha3(alpha3Landkode);
		if (alpha2Landkode == null) {
			log.info("Mottaker har ingen gyldig landkode registert. alpha3Landkode={}. Setter landkode={}.", alpha3Landkode, UNKNOWN_LANDKODE);
			return UNKNOWN_LANDKODE;
		}
		return alpha2Landkode;
	}
}
