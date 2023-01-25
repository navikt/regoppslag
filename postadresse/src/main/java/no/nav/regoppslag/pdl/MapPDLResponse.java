package no.nav.regoppslag.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode;
import no.nav.regoppslag.consumer.pdl.to.Bostedsadresse;
import no.nav.regoppslag.consumer.pdl.to.HentPerson;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.KontaktinformasjonForDoedsbo;
import no.nav.regoppslag.consumer.pdl.to.Matrikkeladresse;
import no.nav.regoppslag.consumer.pdl.to.Oppholdsadresse;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.to.UkjentBosted;
import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;
import no.nav.regoppslag.consumer.pdl.to.Vegadresse;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoed;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.neovisionaries.i18n.CountryCode.XK;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.BOSTEDSADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTINFORMASJONFORDØDSBO;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.OPPHOLDSADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_DOED;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_UTFLYTTET;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.metrics.MetricLabels.KOSOVO_LANDKODE_NAV_REGISTRENE;
import static no.nav.regoppslag.metrics.MetricLabels.UNKNOWN_LANDKODE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Implementasjon av reglene i
 * https://pdldocs-navno.msappproxy.net/ekstern/index.html#_hvilken_adresse_b%C3%B8r_man_bruke
 * <p>
 * Informasjon fra pdl-doc gjengitt under:
 * <p>
 * Adresser til post
 * Dersom formålet er å sende ut noe i post til bruker, vil vi anbefale følgende prioritering:
 * <p>
 * 1. Kontaktadresse med master PDL
 * 2. Kontaktadresse fra Freg med nyeste registreringsdato (det er mulig med to)
 * 3. Oppholdsadresse med master PDL
 * 4. Oppholdsadresse med master Freg
 * 5. Bostedsadresse
 */
@Slf4j
@Component
public class MapPDLResponse {

	private final PostnummerService postnummerService;
	private final LandkodeService landkodeService;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;

	private static final String LANDKODE_NORGE = "NO";
	private static final String ERROR_MELDING = "Feltet %s kan ikke være null eller tomt";
	private static final String ERROR_UTENLANDSKADRESSE = "Feltet %s kan ikke være null eller tomt for utenlandskAdresse";
	private static final String ON_BEHALF_OF = "v/ ";
	private static final String CARE_OF = "C/O ";
	private static final String POSTBOKS = "Postboks ";
	private static final String MOTTAKER_DOED = "Person er død og har ingen registrerte kontaktsopplysninger for dødsbo";
	private static final String POSTNUMMER = "postnummer";
	private static final String FORNAVN = "Fornavn";
	private static final String ETTERNAVN = "Etternavn";
	private static final String UKJENT_KILDE = " Kilde Ukjent";

	@Autowired
	public MapPDLResponse(
			PostnummerService postnummerService,
			LandkodeService landkodeService,
			PdlGraphQLConsumer pdlGraphQLConsumer
	) {
		this.postnummerService = postnummerService;
		this.landkodeService = landkodeService;
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
	}

	private boolean isDoed(HentPerson hentPerson) {
		return nonNull(getDoedsdato(hentPerson)) &&
				PERSONSTATUS_DOED.equals(getFolkeregisterstatus(hentPerson));
	}

	public PdlMottakerInfo mapHentPerson(HentPerson hentPerson, String serviceCode, String tema) {
		if (nonNull(hentPerson.getDoedsfall()) && isDoed(hentPerson)) {
			return PdlMottakerInfo.builder()
					.identifikasjonsnummer(getIdentifikasjonsnummer(hentPerson.getFolkeregisteridentifikator()))
					.navn(getFulltnavn(hentPerson.getNavn()))
					.kortNavn(getForkortetNavn(hentPerson.getNavn()))
					.foedselsdato(getFoedselsdato(hentPerson))
					.doedsdato(getDoedsdato(hentPerson))
					.postadresse(mapKontaktinformasjonForDoedsbo(getKontaktForDoedsbo(hentPerson), tema))
					.build();
		} else {
			Kontaktadresse kontaktadresse = getKontaktadresse(hentPerson);
			if (nonNull(kontaktadresse) &&
					(kontaktadresse.isGyldigPdlKilde() || kontaktadresse.isGyldigFregKilde())) {
				return getMottakerKontaktadresse(hentPerson, serviceCode);
			} else {
				Oppholdsadresse oppholdsadresse = getOppholdsadresse(hentPerson);
				if (nonNull(oppholdsadresse) &&
						(oppholdsadresse.isGyldigPdlKilde() || oppholdsadresse.isGyldigFregKilde())) {
					PdlMottakerInfo pdlMottakerInfo = mapOppholdsadresse(hentPerson, serviceCode);
					return nonNull(pdlMottakerInfo.getPostadresse()) ? pdlMottakerInfo : mapBostedsadresse(hentPerson, serviceCode);
				} else {
					Bostedsadresse bostedsadresse = getBostedsadresse(hentPerson);
					if (nonNull(bostedsadresse)) {
						return mapBostedsadresse(hentPerson, serviceCode);
					} else if (PERSONSTATUS_UTFLYTTET.equalsIgnoreCase(getFolkeregisterstatus(hentPerson))) {
						throw new UkjentAdresseException(format("Fant ikke adresse for personen i PDL, med status=utflyttet og kilde=%s", getFolkeregistermetadata(hentPerson)), NOT_FOUND);
					}
				}
			}
		}
		throw new UkjentAdresseException("Fant ikke adresse for personen i PDL", NOT_FOUND);
	}

	private PdlMottakerInfo getMottakerKontaktadresse(HentPerson hentPerson, String serviceCode) {
		PdlMottakerInfo pdlMottakerInfo = mapKontaktadresse(hentPerson);
		if (isNull(pdlMottakerInfo.getPostadresse()) || isInnlandAdresseTypeAndPostnummerNull(pdlMottakerInfo)) {
			log.info("Fant ikke kontaktadresse og søker etter oppholdsadresse for personen i PDL data");
			return getAdresseFromOppholdOrBostedadresse(hentPerson, serviceCode);
		}
		return pdlMottakerInfo;
	}

	private PdlMottakerInfo getAdresseFromOppholdOrBostedadresse(HentPerson hentPerson, String serviceCode) {
		Oppholdsadresse oppholdsadresse = getOppholdsadresse(hentPerson);
		if (nonNull(oppholdsadresse) &&
				(oppholdsadresse.isGyldigPdlKilde() || oppholdsadresse.isGyldigFregKilde())) {
			PdlMottakerInfo mottakerInfoOppholdsAdresse = mapOppholdsadresse(hentPerson, serviceCode);
			return nonNull(mottakerInfoOppholdsAdresse.getPostadresse()) ? mottakerInfoOppholdsAdresse : mapBostedsadresse(hentPerson, serviceCode);
		} else {
			return mapBostedsadresse(hentPerson, serviceCode);
		}
	}

	private PdlMottakerInfo mapBostedsadresse(HentPerson hentPerson, String serviceCode) {
		if (isNull(getBostedsadresse(hentPerson))) {
			throw new UkjentAdresseException("Fant ikke bostedsadresse for personen i PDL", NOT_FOUND);
		}
		return PdlMottakerInfo.builder()
				.identifikasjonsnummer(getIdentifikasjonsnummer(hentPerson.getFolkeregisteridentifikator()))
				.navn(getFulltnavn(hentPerson.getNavn()))
				.foedselsdato(getFoedselsdato(hentPerson))
				.kortNavn(getForkortetNavn(hentPerson.getNavn()))
				.postadresse(mapPostadresseFraBostedsadresse(Optional.ofNullable(getBostedsadresse(hentPerson))
						.orElseThrow(() -> new UkjentAdresseException("Fant ikke bostedsadresse for personen i PDL", NOT_FOUND)), serviceCode))
				.build();
	}

	private PdlMottakerInfo mapKontaktadresse(HentPerson hentPerson) {
		String coAdressenavn = hentPerson.getKontaktadresse().stream()
				.filter(Objects::nonNull)
				.map(Kontaktadresse::getCoAdressenavn)
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
		Kontaktadresse kontaktadresse = getKontaktadresse(hentPerson);
		PostadresseTo postadresse = mapKontaktadresse(kontaktadresse, coAdressenavn);
		return PdlMottakerInfo.builder().identifikasjonsnummer(getIdentifikasjonsnummer(hentPerson.getFolkeregisteridentifikator()))
				.navn(getFulltnavn(hentPerson.getNavn()))
				.kortNavn(getForkortetNavn(hentPerson.getNavn()))
				.foedselsdato(getFoedselsdato(hentPerson))
				.doedsdato(getDoedsdato(hentPerson))
				.postadresse(postadresse)
				.build();
	}

	private PdlMottakerInfo mapOppholdsadresse(HentPerson hentPerson, String serviceCode) {
		return PdlMottakerInfo.builder()
				.identifikasjonsnummer(getIdentifikasjonsnummer(hentPerson.getFolkeregisteridentifikator()))
				.navn(getFulltnavn(hentPerson.getNavn()))
				.kortNavn(getForkortetNavn(hentPerson.getNavn()))
				.foedselsdato(getFoedselsdato(hentPerson))
				.postadresse(mapPostadresseFraOppholdsadresse(hentPerson.getOppholdsadresse().stream()
						.filter(Objects::nonNull).findAny()
						.orElse(null), serviceCode))
				.build();
	}

	private PostadresseTo mapPostadresseFraBostedsadresse(Bostedsadresse bostedsadresse, String serviceCode) {
		if (!harBostedsadresse(bostedsadresse)) {
			throw new UkjentAdresseException("Fant ikke bostedsadresse for personen i PDL", NOT_FOUND);
		}
		return getValidAdresse(bostedsadresse.getVegadresse(), bostedsadresse.getUtenlandskAdresse(),
				bostedsadresse.getMatrikkeladresse(),
				bostedsadresse.getUkjentBosted(), bostedsadresse.getCoAdressenavn(), serviceCode, BOSTEDSADRESSE);
	}

	private PostadresseTo mapPostadresseFraOppholdsadresse(Oppholdsadresse oppholdsadresse, String serviceCode) {
		return nonNull(oppholdsadresse) && harOppholdsadresse(oppholdsadresse) ? getValidAdresse(oppholdsadresse.getVegadresse(), oppholdsadresse.getUtenlandskAdresse(),
				oppholdsadresse.getMatrikkeladresse(),
				null, oppholdsadresse.getCoAdressenavn(), serviceCode, OPPHOLDSADRESSE) : null;

	}

	private String getIdentifikasjonsnummer
			(List<HentPerson.Folkeregisteridentifikator> folkeregisteridentifikator) {
		return folkeregisteridentifikator.stream()
				.filter(Objects::nonNull)
				.map(HentPerson.Folkeregisteridentifikator::getIdentifikasjonsnummer)
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
	}

	private PostadresseTo mapKontaktadresse(Kontaktadresse kontaktadresse, String coAdressenavn) {
		if (nonNull(kontaktadresse)) {
			if (POSTADRESSE_INNLAND.equalsIgnoreCase(kontaktadresse.getType())) {
				return mapNorskPostAdresse(kontaktadresse, coAdressenavn);
			} else if (POSTADRESSE_UTLAND.equalsIgnoreCase(kontaktadresse.getType()) || nonNull(kontaktadresse.getUtenlandskAdresse())
					|| nonNull(kontaktadresse.getUtenlandskAdresseIFrittFormat())) {
				return mapUtenlandskPostAdresse(kontaktadresse, coAdressenavn);
			}
		}
		return null;
	}

	private PostadresseTo mapNorskPostAdresse(Kontaktadresse kontaktadresse, String coAdressenavn) {
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

	private PostadresseTo mapUtenlandskPostAdresse(Kontaktadresse kontaktadresse, String coAdressenavn) {
		if (nonNull(kontaktadresse.getUtenlandskAdresse())) {
			UtenlandskAdresse utenlandskAdresse = kontaktadresse.getUtenlandskAdresse();
			return mapUtenlandskAdresse(utenlandskAdresse, coAdressenavn)
					.adressekilde(KONTAKTADRESSE)
					.build();
		} else if (nonNull(kontaktadresse.getUtenlandskAdresseIFrittFormat())) {
			Kontaktadresse.UtenlandskAdresseIFrittFormat utenlandskAdresse = kontaktadresse.getUtenlandskAdresseIFrittFormat();
			return PostadresseTo.builder()
					.adressekilde(KONTAKTADRESSE)
					.adresseType(POSTADRESSE_UTLAND)
					.adresselinje1(isBlank(coAdressenavn) ? utenlandskAdresse.getAdresselinje1() : coAdressenavn + ", " + utenlandskAdresse.getAdresselinje1())
					.adresselinje2(utenlandskAdresse.getAdresselinje2())
					.adresselinje3(utenlandskAdresse.getAdresselinje3())
					.landkode(requireNonNull(getAlpha2Landkode(utenlandskAdresse.getLandkode()), format(ERROR_UTENLANDSKADRESSE, "landkode")))
					.build();
		}
		return null;
	}

	private PostadresseTo mapKontaktinformasjonForDoedsbo(KontaktinformasjonForDoedsbo kontaktinformasjon, String tema) {
		if (isNull(kontaktinformasjon)) {
			log.warn(MOTTAKER_DOED);
			throw new UkjentAdressePersonErDoed(MOTTAKER_DOED, GONE);
		}

		if (!isDoedPersonValidKontaktAdresse(kontaktinformasjon)) {
			log.warn(MOTTAKER_DOED);
			throw new UkjentAdressePersonErDoed(MOTTAKER_DOED, GONE);
		}

		return Optional.ofNullable(mapAndValidateKontaktinformasjonForDoeds(kontaktinformasjon, tema)).orElseThrow(
				() -> new UkjentAdressePersonErDoed(MOTTAKER_DOED, GONE));
	}

	private boolean isDoedPersonValidKontaktAdresse(KontaktinformasjonForDoedsbo kontaktinformasjon) {
		return nonNull(kontaktinformasjon.getAttestutstedelsesdato()) && ((nonNull(kontaktinformasjon.getOrganisasjonSomKontakt()) ||
				nonNull(kontaktinformasjon.getAdvokatSomKontakt()) || nonNull(kontaktinformasjon.getPersonSomKontakt())));
	}

	private PostadresseTo mapAndValidateKontaktinformasjonForDoeds(KontaktinformasjonForDoedsbo
																		   kontaktinformasjonForDoedsbo, String tema) {
		KontaktinformasjonForDoedsbo.KontaktAdresse kontaktAdresse = Optional.ofNullable(kontaktinformasjonForDoedsbo.getAdresse())
				.orElseThrow(() -> new UkjentAdressePersonErDoed(MOTTAKER_DOED, GONE));
		if (nonNull(kontaktinformasjonForDoedsbo.getAdvokatSomKontakt())) {
			KontaktinformasjonForDoedsbo.AdvokatSomKontakt advokatSomKontakt = kontaktinformasjonForDoedsbo.getAdvokatSomKontakt();
			return mapMidlertidigPostboksadresse(kontaktinformasjonForDoedsbo, getAdvokatOrOrgKontaktNavn(advokatSomKontakt.getPersonnavn(), advokatSomKontakt.getOrganisasjonsnavn()));
		} else if (nonNull(kontaktinformasjonForDoedsbo.getPersonSomKontakt())) {
			KontaktinformasjonForDoedsbo.PersonSomKontakt personSomKontakt = kontaktinformasjonForDoedsbo.getPersonSomKontakt();
			return mapMidlertidigPostboksadresse(kontaktinformasjonForDoedsbo, getPersonSomKontaktNavn(personSomKontakt, tema));
		} else if (nonNull(kontaktinformasjonForDoedsbo.getOrganisasjonSomKontakt())) {
			KontaktinformasjonForDoedsbo.OrganisasjonSomKontakt organisasjonSomKontakt = kontaktinformasjonForDoedsbo.getOrganisasjonSomKontakt();
			String fulltnavn = getAdvokatOrOrgKontaktNavn(organisasjonSomKontakt.getKontaktperson(), organisasjonSomKontakt.getOrganisasjonsnavn());
			return PostadresseTo.builder()
					.adressekilde(KONTAKTINFORMASJONFORDØDSBO)
					.adresseType(POSTADRESSE_INNLAND)
					.adresselinje1(isBlank(fulltnavn) ? getAdresselinje(kontaktAdresse.getAdresselinje1()) : ON_BEHALF_OF + fulltnavn)
					.adresselinje2(isBlank(fulltnavn) ? getAdresselinje(kontaktAdresse.getAdresselinje2()) : getAdresselinje(kontaktAdresse.getAdresselinje1()))
					.adresselinje3(isBlank(fulltnavn) ? null : getAdresselinje(kontaktAdresse.getAdresselinje2()))
					.postnummer(requireNonNull(kontaktAdresse.getPostnummer(), format(ERROR_MELDING, POSTNUMMER)))
					.poststed(requireNonNull(isBlank(kontaktAdresse.getPoststedsnavn()) ? postnummerService.finnPoststed(kontaktAdresse.getPostnummer()) : kontaktAdresse.getPoststedsnavn(), format(ERROR_MELDING, "poststed")))
					.landkode(LANDKODE_NORGE)
					.build();
		}
		return null;
	}

	private boolean harBostedsadresse(Bostedsadresse bostedsadresse) {
		return (nonNull(bostedsadresse.getMatrikkeladresse()) || nonNull(bostedsadresse.getVegadresse())
				|| nonNull(bostedsadresse.getUtenlandskAdresse()) || nonNull(bostedsadresse.getUkjentBosted()));
	}

	private boolean harOppholdsadresse(Oppholdsadresse oppholdsadresse) {
		return (nonNull(oppholdsadresse.getMatrikkeladresse()) || nonNull(oppholdsadresse.getVegadresse())
				|| nonNull(oppholdsadresse.getUtenlandskAdresse()));
	}

	private PostadresseTo mapMidlertidigPostboksadresse(KontaktinformasjonForDoedsbo kontaktinformasjonForDoedsbo, String navn) {
		KontaktinformasjonForDoedsbo.KontaktAdresse adresse = kontaktinformasjonForDoedsbo.getAdresse();
		return nonNull(adresse) ?
				PostadresseTo.builder()
						.adressekilde(KONTAKTINFORMASJONFORDØDSBO)
						.adresseType(POSTADRESSE_INNLAND)
						.adresselinje1(isBlank(navn) ? adresse.getAdresselinje1() : ON_BEHALF_OF + navn)
						.adresselinje2(isBlank(navn) ? adresse.getAdresselinje2() : adresse.getAdresselinje1())
						.adresselinje3(isBlank(navn) ? null : adresse.getAdresselinje2())
						.postnummer(requireNonNull(adresse.getPostnummer(), format(ERROR_MELDING, POSTNUMMER)))
						.poststed(isBlank(adresse.getPoststedsnavn()) ? postnummerService.finnPoststed(kontaktinformasjonForDoedsbo.getAdresse().getPostnummer())
								: adresse.getPoststedsnavn())
						.landkode(LANDKODE_NORGE)
						.build() : null;
	}

	private PostadresseTo getValidAdresse(Vegadresse vegadresse, UtenlandskAdresse utenlandskAdresse,
										  Matrikkeladresse matrikkeladresse, UkjentBosted ukjentBosted,
										  String coAdressenavn, String serviceCode, AdresseKildeCode adresseKilde) {
		if (nonNull(vegadresse)) {
			return mapVegadresse(vegadresse, coAdressenavn).adressekilde(adresseKilde).build();
		} else if (nonNull(utenlandskAdresse)) {
			return mapUtenlandskAdresse(utenlandskAdresse, coAdressenavn).adressekilde(adresseKilde).build();
		} else if (nonNull(matrikkeladresse)) {
			return mapMatrikkeladresse(matrikkeladresse, adresseKilde);
		} else if (nonNull(ukjentBosted)) {
			throw new UkjentAdresseException(serviceCode + ": Kunne ikke mappe postadresse for UkjentBosted mottaker", NOT_FOUND);
		}
		return null;
	}

	private PostadresseTo.PostadresseToBuilder mapVegadresse(Vegadresse vegadresse, String coAdressenavn) {
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

	private PostadresseTo.PostadresseToBuilder mapUtenlandskAdresse(UtenlandskAdresse utenlandskAdresse, String coAdressenavn) {
		return PostadresseTo.builder()
				.adresseType(POSTADRESSE_UTLAND)
				.adresselinje1(mapUtenlandskAdresselinje1(utenlandskAdresse, coAdressenavn))
				.adresselinje2(mapUtenlandskAdresselinje2(utenlandskAdresse, coAdressenavn))
				.adresselinje3(mapUtenlandskAdresselinje3(utenlandskAdresse, coAdressenavn))
				.landkode(requireNonNull(getAlpha2Landkode(utenlandskAdresse.getLandkode()), format(ERROR_UTENLANDSKADRESSE, "landkode")));
	}

	private String mapUtenlandskAdresselinje1(UtenlandskAdresse utenlandskAdresse, String coAdressenavn) {
		String postOrAdressenavnNummer = getPostOrAdressenavnNummer(utenlandskAdresse);
		String utenlandskPostkodeAndByStedAndOmraade = mapUtenlandskPostkodeAndByStedAndOmraade(utenlandskAdresse);
		String bygningEtasjeLeilighet = mapBygningEtasjeLeilighet(utenlandskAdresse);

		if (isBlank(coAdressenavn) && isNotBlank(postOrAdressenavnNummer)) {
			return postOrAdressenavnNummer;
		} else if (isNotBlank(coAdressenavn)) {
			if (isNotBlank(utenlandskPostkodeAndByStedAndOmraade) && isNotBlank(bygningEtasjeLeilighet)) {
				return coAdressenavn + ", " + postOrAdressenavnNummer;
			} else if (isNotBlank(utenlandskPostkodeAndByStedAndOmraade) && isNotBlank(postOrAdressenavnNummer)) {
				return coAdressenavn;
			}
		}
		return postOrAdressenavnNummer;
	}

	private String getPostOrAdressenavnNummer(UtenlandskAdresse utenlandskAdresse) {
		return isNotBlank(utenlandskAdresse.getPostboksNummerNavn()) ? utenlandskAdresse.getPostboksNummerNavn() :
				utenlandskAdresse.getAdressenavnNummer();
	}

	private String mapUtenlandskAdresselinje2(UtenlandskAdresse utenlandskAdresse, String coAdressenavn) {
		String postkodeAndByStedAndOmraade = mapUtenlandskPostkodeAndByStedAndOmraade(utenlandskAdresse);
		String bygningEtasjeLeilighet = mapBygningEtasjeLeilighet(utenlandskAdresse);

		if (isNotBlank(coAdressenavn)) {
			if (isNotBlank(postkodeAndByStedAndOmraade) && isNotBlank(bygningEtasjeLeilighet)) {
				return bygningEtasjeLeilighet;
			} else if (isNotBlank(postkodeAndByStedAndOmraade) && isBlank(bygningEtasjeLeilighet)) {
				return getPostOrAdressenavnNummer(utenlandskAdresse);
			}
		} else if (isBlank(coAdressenavn) && isNotBlank(mapBygningEtasjeLeilighet(utenlandskAdresse))) {
			return mapBygningEtasjeLeilighet(utenlandskAdresse);
		}
		return postkodeAndByStedAndOmraade;
	}

	private String mapUtenlandskAdresselinje3(UtenlandskAdresse utenlandskAdresse, String coAdressenavn) {

		String postkodeAndByStedAndOmraade = mapUtenlandskPostkodeAndByStedAndOmraade(utenlandskAdresse);
		String postOrAdressenavnNummer = getPostOrAdressenavnNummer(utenlandskAdresse);
		String bygningEtasjeLeilighet = mapBygningEtasjeLeilighet(utenlandskAdresse);

		if (isNotBlank(coAdressenavn)) {
			if (isNotBlank(postkodeAndByStedAndOmraade) && isNotBlank(bygningEtasjeLeilighet)) {
				return postkodeAndByStedAndOmraade;
			} else if (isBlank(postkodeAndByStedAndOmraade) && isNotBlank(postOrAdressenavnNummer)) {
				return bygningEtasjeLeilighet;
			}
		} else if (isBlank(bygningEtasjeLeilighet) || isBlank(postOrAdressenavnNummer)) {
			return null;
		}
		return postkodeAndByStedAndOmraade;
	}

	private String mapUtenlandskPostkodeAndByStedAndOmraade(UtenlandskAdresse utenlandskAdresse) {
		if (isNotBlank(utenlandskAdresse.getPostkode())) {
			if (isNotBlank(utenlandskAdresse.getBySted()) && isNotBlank(utenlandskAdresse.getRegionDistriktOmraade())) {
				return format("%s %s, %s", utenlandskAdresse.getPostkode(), utenlandskAdresse.getBySted(), utenlandskAdresse.getRegionDistriktOmraade());
			} else if (isBlank(utenlandskAdresse.getBySted()) && isNotBlank(utenlandskAdresse.getRegionDistriktOmraade())) {
				return format("%s, %s", utenlandskAdresse.getPostkode(), utenlandskAdresse.getRegionDistriktOmraade());
			} else if (isNotBlank(utenlandskAdresse.getBySted()) && isBlank(utenlandskAdresse.getRegionDistriktOmraade())) {
				return format("%s %s", utenlandskAdresse.getPostkode(), utenlandskAdresse.getBySted());
			} else if (isBlank(utenlandskAdresse.getRegionDistriktOmraade())) {
				return utenlandskAdresse.getPostkode();
			}
		} else if (isNotBlank(utenlandskAdresse.getBySted())) {
			if (isNotBlank(utenlandskAdresse.getRegionDistriktOmraade())) {
				return format("%s, %s", utenlandskAdresse.getBySted(), utenlandskAdresse.getRegionDistriktOmraade());
			} else if (isBlank(utenlandskAdresse.getRegionDistriktOmraade())) {
				return utenlandskAdresse.getBySted();
			}
		}
		return utenlandskAdresse.getRegionDistriktOmraade();
	}

	private String mapBygningEtasjeLeilighet(UtenlandskAdresse utenlandskAdresse) {
		return isNotBlank(utenlandskAdresse.getBygningEtasjeLeilighet()) ? utenlandskAdresse.getBygningEtasjeLeilighet() : null;
	}

	private PostadresseTo mapMatrikkeladresse(Matrikkeladresse matrikkeladresse, AdresseKildeCode adresseKildeCode) {
		return PostadresseTo.builder()
				.adressekilde(adresseKildeCode)
				.adresseType(POSTADRESSE_INNLAND)
				.adresselinje1(matrikkeladresse.getTilleggsnavn())
				.postnummer(matrikkeladresse.getPostnummer())
				.poststed(postnummerService.finnPoststed(matrikkeladresse.getPostnummer()))
				.landkode(LANDKODE_NORGE)
				.build();
	}

	public String getFulltnavn(List<HentPerson.PersonNavn> navns) {
		return navns.stream().filter(Objects::nonNull)
				.map(this::mapPersonnavn)
				.filter(Objects::nonNull)
				.findFirst().orElseThrow(() -> new RegoppslagIllegalArgumentException(format(ERROR_MELDING, "Personnavn"), BAD_REQUEST));

	}

	private String mapPersonnavn(HentPerson.PersonNavn personNavn) {
		if (isBlank(personNavn.getFornavn()) || isBlank(personNavn.getEtternavn())) {
			throw new RegoppslagIllegalArgumentException(format(ERROR_MELDING, isBlank(personNavn.getFornavn()) ? FORNAVN : ETTERNAVN), BAD_REQUEST);
		}
		return trim(getNavn(personNavn.getFornavn()) + getNavn(personNavn.getMellomnavn()) + getNavn(personNavn.getEtternavn()));
	}

	private KontaktinformasjonForDoedsbo getKontaktForDoedsbo(HentPerson hentPerson) {
		if (isNull(hentPerson.getKontaktinformasjonForDoedsbo()) || hentPerson.getKontaktinformasjonForDoedsbo().isEmpty()) {
			throw new UkjentAdressePersonErDoed(MOTTAKER_DOED, GONE);
		}
		return hentPerson.getKontaktinformasjonForDoedsbo().stream().filter(Objects::nonNull).findAny()
				.orElseThrow(() -> new UkjentAdressePersonErDoed(MOTTAKER_DOED, GONE));
	}

	// Implementerer regler 1,2
	private Kontaktadresse getKontaktadresse(HentPerson hentPerson) {
		return isNull(hentPerson.getKontaktadresse()) || hentPerson.getKontaktadresse().isEmpty() ? null :
				hentPerson.getKontaktadresse().stream()
						// Regel 1. Kontaktadresse med master PDL
						.filter(Kontaktadresse::isGyldigPdlKilde)
						.findFirst()
						.orElse(hentPerson.getKontaktadresse().stream()
								.filter(Objects::nonNull)
								.filter(Kontaktadresse::isGyldigFregKilde)
								// Regel 2. Kontaktadresse fra Freg med nyeste registreringsdato (det er mulig med to)
								// Sorteres naturlig etter kontaktadresse.gyldigFraOgMed
								.max(Comparator.naturalOrder())
								.orElse(null));

	}

	private Oppholdsadresse getOppholdsadresse(HentPerson hentPerson) {
		return isNull(hentPerson.getOppholdsadresse()) || hentPerson.getOppholdsadresse().isEmpty() ? null :
				hentPerson.getOppholdsadresse().stream()
						.filter(Oppholdsadresse::isGyldigPdlKilde)
						.findAny().orElse(
								hentPerson.getOppholdsadresse().stream()
										.filter(Oppholdsadresse::isGyldigFregKilde)
										.findAny().orElse(null));
	}

	private Bostedsadresse getBostedsadresse(HentPerson hentPerson) {
		return isNull(hentPerson.getBostedsadresse()) || hentPerson.getBostedsadresse().isEmpty() ? null : hentPerson.getBostedsadresse().stream()
				.filter(Objects::nonNull).findAny().orElseThrow(() -> new UkjentAdresseException("Fant ikke bostedsadresse for personen i PDL", NOT_FOUND));
	}

	private String getAdvokatOrOrgKontaktNavn(KontaktinformasjonForDoedsbo.Personnavn personnavn, String
			organisasjonsnavn) {
		return isNotBlank(getFulltnavn(personnavn)) ? getFulltnavn(personnavn) : organisasjonsnavn;
	}

	private String getPersonSomKontaktNavn(KontaktinformasjonForDoedsbo.PersonSomKontakt personSomKontakt, String
			tema) {
		if (nonNull(personSomKontakt.getPersonnavn()) && nonNull(personSomKontakt.getPersonnavn())) {
			return getFulltnavn(personSomKontakt.getPersonnavn());
		}
		return nonNull(personSomKontakt) && isNotBlank(personSomKontakt.getIdentifikasjonsnummer()) ?
				pdlGraphQLConsumer.hentDoedsBoKontaktPersonnavn(personSomKontakt.getIdentifikasjonsnummer(), tema).orElse(null) : null;
	}

	private String getForkortetNavn(List<HentPerson.PersonNavn> navns) {
		return navns.stream().map(HentPerson.PersonNavn::getForkortetNavn).filter(Objects::nonNull)
				.findFirst().orElse(null);
	}

	private LocalDate getDoedsdato(HentPerson hentPerson) {
		return isNull(hentPerson.getDoedsfall()) ? null : hentPerson.getDoedsfall().stream()
				.map(HentPerson.Doedsfall::getDoedsdato)
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
	}

	private LocalDate getFoedselsdato(HentPerson hentPerson) {
		return hentPerson.getFoedsel().stream()
				.map(HentPerson.Foedsel::getFoedselsdato)
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
	}

	private String getFolkeregisterstatus(HentPerson hentPerson) {
		return hentPerson.getFolkeregisterpersonstatus().stream()
				.filter(Objects::nonNull)
				.map(HentPerson.Folkeregisterpersonstatus::getStatus)
				.filter(Objects::nonNull)
				.findAny().orElse(null);
	}

	private String getAlpha2Landkode(String alpha3Landkode) {
		String alpha2Landkode = KOSOVO_LANDKODE_NAV_REGISTRENE.equalsIgnoreCase(alpha3Landkode) ? XK.name() : landkodeService.finnLandkodeAlpha2FraAlpha3(alpha3Landkode);
		if (alpha2Landkode == null) {
			log.info("Mottaker har ingen gyldig landkode registert. alpha3Landkode={}. Setter landkode={}.", alpha3Landkode, UNKNOWN_LANDKODE);
			return UNKNOWN_LANDKODE;
		}
		return alpha2Landkode;
	}

	private boolean isInnlandAdresseTypeAndPostnummerNull(PdlMottakerInfo pdlMottakerInfo) {
		return isBlank(pdlMottakerInfo.getPostadresse().getPostnummer()) && POSTADRESSE_INNLAND.equals(pdlMottakerInfo.getPostadresse().getAdresseType());
	}

	private String getFolkeregistermetadata(HentPerson hentPerson) {
		return hentPerson.getFolkeregisterpersonstatus().stream()
				.filter(Objects::nonNull)
				.map(status ->
						nonNull(status.getFolkeregistermetadata()) ? status.getFolkeregistermetadata().getKilde() :
								UKJENT_KILDE
				)
				.findAny().orElse(UKJENT_KILDE);
	}

	public String getFulltnavn(KontaktinformasjonForDoedsbo.Personnavn personnavn) {
		return nonNull(personnavn) ? trim(getNavn(personnavn.getFornavn()) + getNavn(personnavn.getMellomnavn()) +
				getNavn(personnavn.getEtternavn())) : null;
	}

	private String getNavn(String navn) {
		return isBlank(navn) ? "" : navn + " ";
	}

	private String getAdresselinje(String adresselinje) {
		return isBlank(adresselinje) ? null : adresselinje;
	}

	public static <T> T requireNonNull(T obj, String message) {
		if (obj == null)
			throw new RegoppslagIllegalArgumentException(message, BAD_REQUEST);
		return obj;
	}
}