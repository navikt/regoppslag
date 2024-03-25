package no.nav.regoppslag.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo.PostadresseToBuilder;
import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;

import java.util.Optional;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Objects.nonNull;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.pdl.MapPDLUtils.getAlpha2Landkode;
import static no.nav.regoppslag.pdl.MapPDLUtils.prependWithCareOfIfMissing;
import static no.nav.regoppslag.pdl.MapPDLUtils.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class UtenlandskAdresseService {

	private static final String ERROR_UTENLANDSKADRESSE = "Feltet %s kan ikke være null eller tomt for utenlandskAdresse";
	private static final String USA_LANDKODE = "USA";

	static Optional<PostadresseTo> mapUtenlandskPostadresse(Kontaktadresse kontaktadresse) {
		String coAdressenavn = kontaktadresse.getCoAdressenavn();

		if (nonNull(kontaktadresse.getUtenlandskAdresse())) {
			UtenlandskAdresse utenlandskAdresse = kontaktadresse.getUtenlandskAdresse();

			return Optional.of(mapUtenlandskAdresse(utenlandskAdresse, coAdressenavn)
					.adressekilde(KONTAKTADRESSE)
					.build());
		} else if (nonNull(kontaktadresse.getUtenlandskAdresseIFrittFormat())) {
			Kontaktadresse.UtenlandskAdresseIFrittFormat utenlandskAdresse = kontaktadresse.getUtenlandskAdresseIFrittFormat();
			String coAdressenavnWithCoPrefix = prependWithCareOfIfMissing(coAdressenavn);

			return Optional.of(PostadresseTo.builder()
					.adressekilde(KONTAKTADRESSE)
					.adresseType(POSTADRESSE_UTLAND)
					.adresselinje1(isBlank(coAdressenavnWithCoPrefix) ? utenlandskAdresse.getAdresselinje1() : coAdressenavnWithCoPrefix + ", " + utenlandskAdresse.getAdresselinje1())
					.adresselinje2(utenlandskAdresse.getAdresselinje2())
					.adresselinje3(utenlandskAdresse.getAdresselinje3())
					.landkode(requireNonNull(getAlpha2Landkode(utenlandskAdresse.getLandkode()), format(ERROR_UTENLANDSKADRESSE, "landkode")))
					.build());
		}

		return Optional.empty();
	}

	static PostadresseToBuilder mapUtenlandskAdresse(UtenlandskAdresse utenlandskAdresse, String coAdressenavn) {
		String coAdressenavnWithCoPrefix = prependWithCareOfIfMissing(coAdressenavn);

		return PostadresseTo.builder()
				.adresseType(POSTADRESSE_UTLAND)
				.adresselinje1(mapUtenlandskAdresselinje1(utenlandskAdresse, coAdressenavnWithCoPrefix))
				.adresselinje2(mapUtenlandskAdresselinje2(utenlandskAdresse, coAdressenavnWithCoPrefix))
				.adresselinje3(mapUtenlandskAdresselinje3(utenlandskAdresse, coAdressenavnWithCoPrefix))
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

		return bygningEtasjeLeilighet;
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
			if (isNotBlank(bygningEtasjeLeilighet) && isNotBlank(postboksOrAdressenavnNummer)) {
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
				if (USA_LANDKODE.equals(utenlandskAdresse.getLandkode())) {
					return joinAdresseUtenKomma(utenlandskAdresse.getBySted(), utenlandskAdresse.getRegionDistriktOmraade(), utenlandskAdresse.getPostkode());
				}
				return joinAdresseMedKomma(utenlandskAdresse.getPostkode(), utenlandskAdresse.getBySted(), utenlandskAdresse.getRegionDistriktOmraade());
			} else {
				if (!hasBySted && hasRegionDistriktOmr) {
					if (USA_LANDKODE.equals(utenlandskAdresse.getLandkode())) {
						return joinAdresseUtenKomma(utenlandskAdresse.getRegionDistriktOmraade(), utenlandskAdresse.getPostkode(), "");
					}
					return joinAdresseMedKomma(utenlandskAdresse.getPostkode(), "", utenlandskAdresse.getRegionDistriktOmraade());

				} else if (hasBySted) {
					if (USA_LANDKODE.equals(utenlandskAdresse.getLandkode())) {
						return joinAdresseUtenKomma(utenlandskAdresse.getBySted(), utenlandskAdresse.getPostkode(), "");
					}
					return joinAdresseUtenKomma(utenlandskAdresse.getPostkode(), utenlandskAdresse.getBySted(), "");
				} else {
					return utenlandskAdresse.getPostkode();
				}
			}
		} else if (hasBySted) {
			if (hasRegionDistriktOmr) {
				if (USA_LANDKODE.equals(utenlandskAdresse.getLandkode())) {
					return joinAdresseUtenKomma(utenlandskAdresse.getBySted(), utenlandskAdresse.getRegionDistriktOmraade(), "");
				}
				return joinAdresseMedKomma("", utenlandskAdresse.getBySted(), utenlandskAdresse.getRegionDistriktOmraade());
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

	private static String joinAdresseMedKomma(String adresse1, String adresse2, String adresse3) {
		return format("%s %s, %s", adresse1, adresse2, adresse3).strip().replaceAll("\\s*,",",");
	}

	private static String joinAdresseUtenKomma(String adresse1, String adresse2, String adresse3) {
		return join(" ", adresse1, adresse2, adresse3).strip();
	}

}
