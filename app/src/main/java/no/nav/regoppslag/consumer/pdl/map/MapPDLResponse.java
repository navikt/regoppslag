package no.nav.regoppslag.consumer.pdl.map;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.pdl.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.Bostedsadresse;
import no.nav.regoppslag.consumer.pdl.HentPerson;
import no.nav.regoppslag.consumer.pdl.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.KontaktinformasjonForDoedsbo;
import no.nav.regoppslag.consumer.pdl.Matrikkeladresse;
import no.nav.regoppslag.consumer.pdl.Oppholdsadresse;
import no.nav.regoppslag.consumer.pdl.UkjentBosted;
import no.nav.regoppslag.consumer.pdl.UtenlandskAdresse;
import no.nav.regoppslag.consumer.pdl.Vegadresse;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoed;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.PostnummerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static no.nav.regoppslag.consumer.pdl.PDLConstant.PERSONSTATUS_DOED;
import static no.nav.regoppslag.consumer.pdl.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.PDLConstant.POSTADRESSE_UTLAND;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
public class MapPDLResponse {

	private final PostnummerService postnummerService;
	private MicrometerMetrics metrics;

	private static final String LANDKODE_NORGE = "NO";
	private static final String ERROR_MELDING = "Feltet %s kan ikke være null eller tomt";
	private static final String ERROR_UTENLANDSKADRESSE = "Feltet %s kan ikke være null eller tomt for utenlandskAdresse";
	private static final String CARE_OF = "C/O ";

	@Inject
	public MapPDLResponse(
			PostnummerService postnummerService,
			MicrometerMetrics metrics
	) {
		this.postnummerService = postnummerService;
		this.metrics = metrics;
	}

	private boolean isDoed(HentPerson hentPerson) {
		return nonNull(getDoedsdato(hentPerson)) &&
				PERSONSTATUS_DOED.equals(getFolkeregisterstatus(hentPerson));
	}

	public PdlMottakerInfo mapHentPerson(HentPerson hentPerson, String serviceCode) {
		if (nonNull(hentPerson.getDoedsfall()) && isDoed(hentPerson)) {
			return PdlMottakerInfo.builder()
					.navn(getFulltnavn(hentPerson.getNavn()))
					.kortNavn(getForkortetNavn(hentPerson.getNavn()))
					.foedselsdato(getFoedselsdato(hentPerson))
					.doedsdato(getDoedsdato(hentPerson))
					.postadresse(mapKontaktinformasjonForDoedsbo(hentPerson.getKontaktinformasjonForDoedsbo().stream()
							.findAny()
							.filter(Objects::nonNull)
							.orElse(null)))
					.build();
		} else if (nonNull(hentPerson.getKontaktadresse())) {
			return PdlMottakerInfo.builder()
					.navn(getFulltnavn(hentPerson.getNavn()))
					.kortNavn(getForkortetNavn(hentPerson.getNavn()))
					.foedselsdato(getFoedselsdato(hentPerson))
					.doedsdato(getDoedsdato(hentPerson))
					.postadresse(mapKontaktadresse(hentPerson.getKontaktadresse().stream()
									.filter(Objects::nonNull)
									.findAny()
									.orElse(null),
							hentPerson.getKontaktadresse().stream()
									.filter(Objects::nonNull)
									.map(Kontaktadresse::getCoAdressenavn)
									.filter(Objects::nonNull)
									.findAny()
									.orElse(null)))
					.build();
		} else if (nonNull(hentPerson.getOppholdsadresse())) {
			return PdlMottakerInfo.builder()
					.navn(getFulltnavn(hentPerson.getNavn()))
					.kortNavn(getForkortetNavn(hentPerson.getNavn()))
					.postadresse(mapOppholdsadresse(hentPerson.getOppholdsadresse().stream().filter(Objects::nonNull).findAny().get(), serviceCode)).build();
		} else if (nonNull(hentPerson.getBostedsadresse())) {
			return PdlMottakerInfo.builder()
					.navn(getFulltnavn(hentPerson.getNavn()))
					.kortNavn(getForkortetNavn(hentPerson.getNavn()))
					.postadresse(mapBostedsadresse(hentPerson.getBostedsadresse().stream().filter(Objects::nonNull).findAny().get(), serviceCode))
					.build();
		}
		throw new UkjentAdresseException("Fant ikke adresse for personen i PDL");
	}

	private PostadresseTo mapBostedsadresse(Bostedsadresse bostedsadresse, String serviceCode) {
		return (harBostedsadresse(bostedsadresse)) ? getRightAdresse(bostedsadresse.getVegadresse(), bostedsadresse.getUtenlandskAdresse(),
				bostedsadresse.getMatrikkeladresse(),
				bostedsadresse.getUkjentBosted(), bostedsadresse.getCoAdressenavn(), serviceCode) : null;
	}

	private PostadresseTo mapOppholdsadresse(Oppholdsadresse oppholdsadresse, String serviceCode) {
		return harOppholdsadresse(oppholdsadresse) ? getRightAdresse(oppholdsadresse.getVegadresse(), oppholdsadresse.getUtenlandskAdresse(),
				oppholdsadresse.getMatrikkeladresse(),
				null, oppholdsadresse.getCoAdressenavn(), serviceCode) : null;

	}

	public PostadresseTo mapKontaktadresse(Kontaktadresse kontaktadresse, String coAdressenavn) {
		if (POSTADRESSE_INNLAND.equalsIgnoreCase(kontaktadresse.getType())) {
			if (nonNull(kontaktadresse.getPostboksadresse())) {
				Kontaktadresse.Postboksadresse postboksadresse = kontaktadresse.getPostboksadresse();
				return PostadresseTo.builder()
						.adresseType(POSTADRESSE_INNLAND)
						.adresselinje1("Postboks " + requireNonNull(postboksadresse.getPostboks()))
						.postnummer(postboksadresse.getPostnummer())
						.poststed(postnummerService.finnPoststed(postboksadresse.getPostnummer()))
						.build();
			} else if (nonNull(kontaktadresse.getPostadresseIFrittFormat())) {
				Kontaktadresse.PostadresseIFrittFormat postadresse = kontaktadresse.getPostadresseIFrittFormat();
				if (isBlank(kontaktadresse.getCoAdressenavn())) {
					return PostadresseTo.builder()
							.adresseType(POSTADRESSE_INNLAND)
							.adresselinje1(isBlank(postadresse.getAdresselinje1()) ? null : postadresse.getAdresselinje1())
							.adresselinje2(postadresse.getAdresselinje2()).adresselinje3(postadresse.getAdresselinje3())
							.postnummer(requireNonNull(isBlank(postadresse.getPostnummer()) ? null : postadresse.getPostnummer(), format(ERROR_MELDING, "postnummer")))
							.poststed(postnummerService.finnPoststed(postadresse.getPostnummer()))
							.landkode(isLandkodeEmptyAndHavePostnummer(null, postadresse.getPostnummer()) ? LANDKODE_NORGE : null)
							.build();
				}
				return PostadresseTo.builder()
						.adresseType(POSTADRESSE_INNLAND)
						.adresselinje1(kontaktadresse.getCoAdressenavn())
						.adresselinje2(requireNonNull(postadresse.getAdresselinje1(), format(ERROR_MELDING, "adresselinje2")))
						.adresselinje3(postadresse.getAdresselinje2())
						.postnummer(requireNonNull(isBlank(postadresse.getPostnummer()) ? null : postadresse.getPostnummer(), format(ERROR_MELDING, "postnummer")))
						.poststed(postnummerService.finnPoststed(postadresse.getPostnummer()))
						.landkode(isLandkodeEmptyAndHavePostnummer(null, postadresse.getPostnummer()) ? LANDKODE_NORGE : null)
						.build();
			} else if (nonNull(kontaktadresse.getVegadresse())) {
				return mapVegadresse(kontaktadresse.getVegadresse(), coAdressenavn).build();
			}
		} else if (POSTADRESSE_UTLAND.equalsIgnoreCase(kontaktadresse.getType())) {
			if (nonNull(kontaktadresse.getUtenlandskAdresse())) {
				UtenlandskAdresse utenlandskAdresse = kontaktadresse.getUtenlandskAdresse();
				return mapUtenlandskAdresse(utenlandskAdresse, kontaktadresse.getCoAdressenavn()).build();
			} else if (nonNull(kontaktadresse.getUtenlandskAdresseIFrittFormat())) {
				Kontaktadresse.UtenlandskAdresseIFrittFormat utenlandskAdresse = kontaktadresse.getUtenlandskAdresseIFrittFormat();
				return PostadresseTo.builder()
						.adresseType(POSTADRESSE_UTLAND)
						.adresselinje1(isNotBlank(utenlandskAdresse.getAdresselinje1()) ? utenlandskAdresse.getAdresselinje1() : utenlandskAdresse.getAdresselinje2())
						.adresselinje2(requireNonNull(utenlandskAdresse.getPostkode(), format(ERROR_UTENLANDSKADRESSE, "postnummer")))
						.adresselinje3(utenlandskAdresse.getByEllerStedsnavn())
						.landkode(utenlandskAdresse.getLandkode())
						.build();
			}
		}
		return PostadresseTo.builder().build();
	}

	private PostadresseTo mapKontaktinformasjonForDoedsbo(KontaktinformasjonForDoedsbo kontaktinformasjon) {
		if (isNull(kontaktinformasjon)) {
			log.warn("Mottaker er registrert som død og har ugyldig postadresse");
			throw new UkjentAdressePersonErDoed("Mottaker er registrert som død og har ugyldig postadresse");
		}

		if (!hasDoedPersonValidKontaktAdresse(kontaktinformasjon)) {
			log.warn("Mottaker er registrert som død og har ugyldig postadresse");
			throw new UkjentAdressePersonErDoed("Mottaker er registrert som død og har ugyldig postadresse");
		}

		return Optional.of(mapAndValidateKontaktinformasjonForDoeds(kontaktinformasjon)).orElseThrow(
				() -> new UkjentAdressePersonErDoed("Mottaker er registrert som død og har ugyldig postadresse"));
	}

	private boolean hasDoedPersonValidKontaktAdresse(KontaktinformasjonForDoedsbo kontaktinformasjon) {
		return (nonNull(kontaktinformasjon.getAttestutstedelsesdato())) && ((nonNull(kontaktinformasjon.getOrganisasjonSomKontakt()) ||
				nonNull(kontaktinformasjon.getAdvokatSomKontakt()) || nonNull(kontaktinformasjon.getPersonSomKontakt())));
	}

	public PostadresseTo mapAndValidateKontaktinformasjonForDoeds(KontaktinformasjonForDoedsbo kontaktinformasjonForDoedsbo) {
		KontaktinformasjonForDoedsbo.KontaktAdresse kontaktAdresse = nonNull(kontaktinformasjonForDoedsbo.getAdresse()) ? kontaktinformasjonForDoedsbo.getAdresse() : null;
		if (nonNull(kontaktinformasjonForDoedsbo.getAdvokatSomKontakt())) {
			KontaktinformasjonForDoedsbo.AdvokatSomKontakt advokatSomKontakt = kontaktinformasjonForDoedsbo.getAdvokatSomKontakt();
			PostadresseTo postadresse = mapMidlertidigPostboksadresse(kontaktinformasjonForDoedsbo);
			postadresse.setAdresselinje1(CARE_OF + getFulltnavn(advokatSomKontakt.getPersonnavn()));
			return postadresse;
		} else if (nonNull(kontaktinformasjonForDoedsbo.getPersonSomKontakt())) {
			KontaktinformasjonForDoedsbo.PersonSomKontakt personSomKontakt = kontaktinformasjonForDoedsbo.getPersonSomKontakt();
			PostadresseTo postadresse = mapMidlertidigPostboksadresse(kontaktinformasjonForDoedsbo);
			postadresse.setAdresselinje1(CARE_OF + getFulltnavn(personSomKontakt.getPersonnavn()));
			return postadresse;
		} else if (nonNull(kontaktinformasjonForDoedsbo.getOrganisasjonSomKontakt())) {
			KontaktinformasjonForDoedsbo.OrganisasjonSomKontakt organisasjonSomKontakt = kontaktinformasjonForDoedsbo.getOrganisasjonSomKontakt();
			return PostadresseTo.builder()
					.adresseType(POSTADRESSE_INNLAND)
					.adresselinje1(CARE_OF + getFulltnavn(organisasjonSomKontakt.getKontaktperson()))
					.adresselinje2(requireNonNull(kontaktAdresse.getAdresselinje1(), format(ERROR_MELDING, "adresselinje1")))
					.adresselinje3(isBlank(kontaktAdresse.getAdresselinje2()) ? null : kontaktAdresse.getAdresselinje2())
					.postnummer(requireNonNull(kontaktAdresse.getPostnummer(), format(ERROR_MELDING, "postnummer")))
					.poststed(requireNonNull(kontaktAdresse.getPoststedsnavn(), format(ERROR_MELDING, "poststed")))
					.landkode(kontaktAdresse.getLandkode())
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

	private PostadresseTo mapMidlertidigPostboksadresse(KontaktinformasjonForDoedsbo kontaktinformasjonForDoedsbo) {
		KontaktinformasjonForDoedsbo.KontaktAdresse adresse = kontaktinformasjonForDoedsbo.getAdresse();
		return nonNull(adresse) ?
				PostadresseTo.builder()
						.adresseType(POSTADRESSE_INNLAND)
						.adresselinje2(adresse.getAdresselinje1())
						.adresselinje3(adresse.getAdresselinje2())
						.postnummer(requireNonNull(adresse.getPostnummer(), format(ERROR_MELDING, "postnummer")))
						.poststed(isBlank(adresse.getPoststedsnavn()) ? postnummerService.finnPoststed(kontaktinformasjonForDoedsbo.getAdresse().getPostnummer())
								: adresse.getPoststedsnavn())
						.landkode(isLandkodeEmptyAndHavePostnummer(adresse.getLandkode(), adresse.getPostnummer()) ? LANDKODE_NORGE : adresse.getLandkode())
						.build() : null;
	}

	private PostadresseTo getRightAdresse(Vegadresse vegadresse, UtenlandskAdresse utenlandskAdresse,
										  Matrikkeladresse matrikkeladresse, UkjentBosted ukjentBosted,
										  String coAdressenavn, String serviceCode) {
		PostadresseTo postadresseToBuilder = PostadresseTo.builder().build();
		if (nonNull(vegadresse)) {
			return mapVegadresse(vegadresse, coAdressenavn).build();
		} else if (nonNull(utenlandskAdresse)) {
			return mapUtenlandskAdresse(utenlandskAdresse, coAdressenavn).build();
		} else if (nonNull(matrikkeladresse)) {
			return mapMatrikkeladresse(matrikkeladresse);
		} else if (nonNull(ukjentBosted)) {
			throw new UkjentAdresseException(serviceCode + "Kunne ikke mappe postadresse for UkjentBosted mottaker");
		}
		return null;
	}

	public PostadresseTo.PostadresseToBuilder mapVegadresse(Vegadresse vegadresse, String coAdressenavn) {
		return isBlank(coAdressenavn) ?
				PostadresseTo.builder()
						.adresseType(POSTADRESSE_INNLAND)
						.adresselinje1(Optional.ofNullable(vegadresse.getAdressenavn())
								.orElse("") + " " + Optional.ofNullable(isNull(vegadresse.getHusnummer()) ? null : vegadresse.getHusnummer())
								.orElse("") + Optional.ofNullable(vegadresse.getHusbokstav()).orElse(""))
						.postnummer(requireNonNull(vegadresse.getPostnummer(), format(ERROR_MELDING, "postnummer")))
						.poststed(requireNonNull(postnummerService.finnPoststed(vegadresse.getPostnummer()), format(ERROR_MELDING, "poststed")))
						.landkode(isLandkodeEmptyAndHavePostnummer(null, vegadresse.getPostnummer()) ? LANDKODE_NORGE : null) :
				PostadresseTo.builder()
						.adresseType(POSTADRESSE_INNLAND)
						.adresselinje1(coAdressenavn)
						.adresselinje2(Optional.ofNullable(vegadresse.getAdressenavn())
								.orElse("") + " " + Optional.ofNullable(isNull(vegadresse.getHusnummer()) ? null : vegadresse.getHusnummer())
								.orElse("") + Optional.ofNullable(vegadresse.getHusbokstav()).orElse(""))
						.postnummer(requireNonNull(vegadresse.getPostnummer(), format(ERROR_MELDING, "postnummer")))
						.poststed(requireNonNull(postnummerService.finnPoststed(vegadresse.getPostnummer()), format(ERROR_MELDING, "poststed")))
						.landkode(isLandkodeEmptyAndHavePostnummer(null, vegadresse.getPostnummer()) ? LANDKODE_NORGE : null);
	}

	private PostadresseTo.PostadresseToBuilder mapUtenlandskAdresse(UtenlandskAdresse utenlandskAdresse, String coAdressenav) {
		return PostadresseTo.builder()
				.adresseType(POSTADRESSE_UTLAND)
				.adresselinje1(requireNonNull(isNotBlank(utenlandskAdresse.getPostboksNummerNavn()) ?
						utenlandskAdresse.getPostboksNummerNavn() :
						utenlandskAdresse.getAdressenavnNummer(), format(ERROR_UTENLANDSKADRESSE, "adresselinje1")))
				.adresselinje2(requireNonNull(utenlandskAdresse.getPostkode(), format(ERROR_UTENLANDSKADRESSE, "postkode")))
				.adresselinje3(isNotBlank(utenlandskAdresse.getBySted()) ? utenlandskAdresse.getBySted() : utenlandskAdresse.getRegionDistriktOmraade())
				.landkode(requireNonNull(utenlandskAdresse.getLandkode(), format(ERROR_UTENLANDSKADRESSE, "landkode")));
	}

	private PostadresseTo mapMatrikkeladresse(Matrikkeladresse matrikkeladresse) {
		return PostadresseTo.builder()
				.adresseType(POSTADRESSE_INNLAND)
				.adresselinje1(matrikkeladresse.getTilleggsnavn())
				.postnummer(matrikkeladresse.getPostnummer())
				.poststed(postnummerService.finnPoststed(matrikkeladresse.getPostnummer()))
				.landkode(isLandkodeEmptyAndHavePostnummer(null, matrikkeladresse.getPostnummer()) ? LANDKODE_NORGE : null)
				.build();
	}

	public String getFulltnavn(List<HentPerson.PersonNavn> navns) {
		return navns.stream().filter(personNavn -> nonNull(personNavn)).map(personNavn -> nonNull(personNavn.getFornavn()) ? personNavn.getFornavn() + " " +
				(StringUtils.isBlank(personNavn.getMellomnavn()) ? "" : personNavn.getMellomnavn() + " ") +
				personNavn.getEtternavn() : null).filter(Objects::nonNull)
				.findFirst().orElse(null);

	}

	private String getForkortetNavn(List<HentPerson.PersonNavn> navns) {
		return navns.stream().map(personNavn -> personNavn.getForkortetNavn()).filter(Objects::nonNull)
				.findFirst().orElse(null);
	}

	private LocalDate getDoedsdato(HentPerson hentPerson) {
		return hentPerson.getDoedsfall().stream()
				.map(doedsfall -> doedsfall.getDoedsdato())
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
	}

	private LocalDate getFoedselsdato(HentPerson hentPerson) {
		return hentPerson.getFoedsel().stream()
				.map(foedsel -> foedsel.getFoedselsdato())
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
	}

	private String getFolkeregisterstatus(HentPerson hentPerson) {
		return hentPerson.getFolkeregisterpersonstatus().stream()
				.filter(Objects::nonNull)
				.map(folkeregisterstatus -> folkeregisterstatus.getStatus())
				.filter(Objects::nonNull)
				.findAny().orElse(null);
	}

	private boolean isLandkodeEmptyAndHavePostnummer(String landkode, String postnummer) {
		return isBlank(landkode) && isNotBlank(postnummerService.finnPoststed(postnummer));
	}

	public String getFulltnavn(KontaktinformasjonForDoedsbo.Personnavn personnavn) {
		return (nonNull(personnavn)) ? personnavn.getFornavn() + " " +
				(isBlank(personnavn.getMellomnavn()) ? "" : personnavn.getMellomnavn() + " ") +
				(isBlank(personnavn.getEtternavn()) ? "" : personnavn.getEtternavn()) : null;
	}
}