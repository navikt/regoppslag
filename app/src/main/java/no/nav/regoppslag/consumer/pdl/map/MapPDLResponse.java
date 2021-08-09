package no.nav.regoppslag.consumer.pdl.map;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.pdl.to.Bostedsadresse;
import no.nav.regoppslag.consumer.pdl.to.HentPerson;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.KontaktinformasjonForDoedsbo;
import no.nav.regoppslag.consumer.pdl.to.Matrikkeladresse;
import no.nav.regoppslag.consumer.pdl.to.Metadata;
import no.nav.regoppslag.consumer.pdl.to.Oppholdsadresse;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.to.UkjentBosted;
import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;
import no.nav.regoppslag.consumer.pdl.to.Vegadresse;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoed;
import no.nav.regoppslag.service.PostnummerService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.FREG;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.PDL;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_DOED;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Tsigab Angosom, NAV.
 */

@Slf4j
@Component
public class MapPDLResponse {

	private final PostnummerService postnummerService;

	private static final String LANDKODE_NORGE = "NOR";
	private static final String ERROR_MELDING = "Feltet %s kan ikke være null eller tomt";
	private static final String ERROR_UTENLANDSKADRESSE = "Feltet %s kan ikke være null eller tomt for utenlandskAdresse";
	private static final String CARE_OF = "C/O ";
	private static final String MOTTAKER_DOED = "Person er død og har ingen registrerte kontaktsopplysninger for dødsbo";
	private static final String POSTNUMMER = "postnummer";
	private static final String FORNAVN = "Fornavn";
	private static final String ETTERNAVN = "Etternavn";

	@Inject
	public MapPDLResponse(
			PostnummerService postnummerService
	) {
		this.postnummerService = postnummerService;
	}

	private boolean isDoed(HentPerson hentPerson) {
		return nonNull(getDoedsdato(hentPerson)) &&
				PERSONSTATUS_DOED.equals(getFolkeregisterstatus(hentPerson));
	}

	public PdlMottakerInfo mapHentPerson(HentPerson hentPerson, String serviceCode) {
		if (nonNull(hentPerson.getDoedsfall()) && isDoed(hentPerson)) {
			return PdlMottakerInfo.builder()
					.identifikasjonsnummer(getIdentifikasjonsnummer(hentPerson.getFolkeregisteridentifikator()))
					.navn(getFulltnavn(hentPerson.getNavn()))
					.kortNavn(getForkortetNavn(hentPerson.getNavn()))
					.foedselsdato(getFoedselsdato(hentPerson))
					.doedsdato(getDoedsdato(hentPerson))
					.postadresse(mapKontaktinformasjonForDoedsbo(isNull(hentPerson.getKontaktinformasjonForDoedsbo()) ? null :
							hentPerson.getKontaktinformasjonForDoedsbo().stream()
									.filter(Objects::nonNull)
									.findAny()
									.orElseThrow(() -> new UkjentAdressePersonErDoed(MOTTAKER_DOED, GONE))))
					.build();
		} else if (nonNull(getKontaktadresse(hentPerson)) && (isSourcePdl(getMasterKilde(getKontaktadresse(hentPerson).getMetadata())) ||
				isGyldigDatoOgKilde(getKontaktadresse(hentPerson).getGyldigTilOgMed(), getKontaktadresse(hentPerson).getMetadata()))) {
			return mapKontaktadresse(hentPerson);
		} else if (nonNull(getOppholdsadresse(hentPerson)) && (isSourcePdl(getMasterKilde(getOppholdsadresse(hentPerson).getMetadata())) ||
				FREG.name().equals(getMasterKilde(getOppholdsadresse(hentPerson).getMetadata())))) {
			return mapOppholdsadresse(hentPerson, serviceCode);
		} else if (nonNull(getBostedsadresse(hentPerson))) {
			return PdlMottakerInfo.builder()
					.identifikasjonsnummer(getIdentifikasjonsnummer(hentPerson.getFolkeregisteridentifikator()))
					.navn(getFulltnavn(hentPerson.getNavn()))
					.kortNavn(getForkortetNavn(hentPerson.getNavn()))
					.postadresse(mapPostadresseFraBostedsadresse(Optional.ofNullable(getBostedsadresse(hentPerson))
							.orElseThrow(() -> new UkjentAdresseException("Fant ikke bostedsadresse for personen i PDL", NOT_FOUND)), serviceCode))
					.build();
		}
		throw new UkjentAdresseException("Fant ikke adresse for personen i PDL", NOT_FOUND);
	}

	private PdlMottakerInfo mapKontaktadresse(HentPerson hentPerson) {
		return PdlMottakerInfo.builder().identifikasjonsnummer(getIdentifikasjonsnummer(hentPerson.getFolkeregisteridentifikator()))
				.navn(getFulltnavn(hentPerson.getNavn()))
				.kortNavn(getForkortetNavn(hentPerson.getNavn()))
				.foedselsdato(getFoedselsdato(hentPerson))
				.doedsdato(getDoedsdato(hentPerson))
				.postadresse(mapKontaktadresse(hentPerson.getKontaktadresse().stream()
								.filter(Objects::nonNull)
								.findAny()
								.orElseThrow(() -> new UkjentAdresseException("Fant ikke adresse for personen i PDL", NOT_FOUND)),
						hentPerson.getKontaktadresse().stream()
								.filter(Objects::nonNull)
								.map(Kontaktadresse::getCoAdressenavn)
								.filter(Objects::nonNull)
								.findAny()
								.orElse(null)))
				.build();
	}

	private PdlMottakerInfo mapOppholdsadresse(HentPerson hentPerson, String serviceCode) {

		return PdlMottakerInfo.builder()
				.identifikasjonsnummer(getIdentifikasjonsnummer(hentPerson.getFolkeregisteridentifikator()))
				.navn(getFulltnavn(hentPerson.getNavn()))
				.kortNavn(getForkortetNavn(hentPerson.getNavn()))
				.postadresse(mapPostadresseFraOppholdsadresse(hentPerson.getOppholdsadresse().stream()
						.filter(Objects::nonNull).findAny()
						.orElseThrow(() -> new UkjentAdresseException("Fant ikke oppholdsadresse for personen i PDL", NOT_FOUND)), serviceCode))
				.build();
	}


	private PostadresseTo mapPostadresseFraBostedsadresse(Bostedsadresse bostedsadresse, String serviceCode) {
		return (harBostedsadresse(bostedsadresse)) ? getValidAdresse(bostedsadresse.getVegadresse(), bostedsadresse.getUtenlandskAdresse(),
				bostedsadresse.getMatrikkeladresse(),
				bostedsadresse.getUkjentBosted(), bostedsadresse.getCoAdressenavn(), serviceCode) : null;
	}

	private PostadresseTo mapPostadresseFraOppholdsadresse(Oppholdsadresse oppholdsadresse, String serviceCode) {
		return harOppholdsadresse(oppholdsadresse) ? getValidAdresse(oppholdsadresse.getVegadresse(), oppholdsadresse.getUtenlandskAdresse(),
				oppholdsadresse.getMatrikkeladresse(),
				null, oppholdsadresse.getCoAdressenavn(), serviceCode) : null;

	}


	private String getIdentifikasjonsnummer(List<HentPerson.Folkeregisteridentifikator> folkeregisteridentifikator) {
		return folkeregisteridentifikator.stream()
				.filter(Objects::nonNull)
				.map(HentPerson.Folkeregisteridentifikator::getIdentifikasjonsnummer)
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
	}

	private PostadresseTo mapKontaktadresse(Kontaktadresse kontaktadresse, String coAdressenavn) {
		if (POSTADRESSE_INNLAND.equalsIgnoreCase(kontaktadresse.getType())) {
			return mapNorskPostAdresse(kontaktadresse, coAdressenavn);
		} else if (POSTADRESSE_UTLAND.equalsIgnoreCase(kontaktadresse.getType())) {
			return mapUtenlandskPostAdresse(kontaktadresse);
		}
		return PostadresseTo.builder().build();
	}

	private PostadresseTo mapNorskPostAdresse(Kontaktadresse kontaktadresse, String coAdressenavn) {
		if (nonNull(kontaktadresse.getPostboksadresse())) {
			Kontaktadresse.Postboksadresse postboksadresse = kontaktadresse.getPostboksadresse();
			return PostadresseTo.builder()
					.adresseType(POSTADRESSE_INNLAND)
					.adresselinje1("Postboks " + requireNonNull(postboksadresse.getPostboks(), format(ERROR_MELDING, "postboks")))
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
						.postnummer(requireNonNull(isBlank(postadresse.getPostnummer()) ? null : postadresse.getPostnummer(), format(ERROR_MELDING, POSTNUMMER)))
						.poststed(postnummerService.finnPoststed(postadresse.getPostnummer()))
						.landkode(isLandkodeEmptyAndHavePostnummer(null, postadresse.getPostnummer()) ? LANDKODE_NORGE : null)
						.build();
			}
			return PostadresseTo.builder()
					.adresseType(POSTADRESSE_INNLAND)
					.adresselinje1(kontaktadresse.getCoAdressenavn())
					.adresselinje2(requireNonNull(postadresse.getAdresselinje1(), format(ERROR_MELDING, "adresselinje2")))
					.adresselinje3(postadresse.getAdresselinje2())
					.postnummer(requireNonNull(isBlank(postadresse.getPostnummer()) ? null : postadresse.getPostnummer(), format(ERROR_MELDING, POSTNUMMER)))
					.poststed(postnummerService.finnPoststed(postadresse.getPostnummer()))
					.landkode(isLandkodeEmptyAndHavePostnummer(null, postadresse.getPostnummer()) ? LANDKODE_NORGE : null)
					.build();
		} else if (nonNull(kontaktadresse.getVegadresse())) {
			return mapVegadresse(kontaktadresse.getVegadresse(), coAdressenavn).build();
		}
		return null;
	}

	private PostadresseTo mapUtenlandskPostAdresse(Kontaktadresse kontaktadresse) {
		if (nonNull(kontaktadresse.getUtenlandskAdresse())) {
			UtenlandskAdresse utenlandskAdresse = kontaktadresse.getUtenlandskAdresse();
			return mapUtenlandskAdresse(utenlandskAdresse).build();
		} else if (nonNull(kontaktadresse.getUtenlandskAdresseIFrittFormat())) {
			Kontaktadresse.UtenlandskAdresseIFrittFormat utenlandskAdresse = kontaktadresse.getUtenlandskAdresseIFrittFormat();
			return PostadresseTo.builder()
					.adresseType(POSTADRESSE_UTLAND)
					.adresselinje1(requireNonNull(utenlandskAdresse.getAdresselinje1(), format(ERROR_UTENLANDSKADRESSE, "adresselinje1")))
					.adresselinje2(utenlandskAdresse.getAdresselinje2())
					.adresselinje3(utenlandskAdresse.getAdresselinje3())
					.poststed(utenlandskAdresse.getByEllerStedsnavn())
					.landkode(requireNonNull(utenlandskAdresse.getLandkode(), format(ERROR_UTENLANDSKADRESSE, "landkode")))
					.build();
		}
		return null;
	}

	private PostadresseTo mapKontaktinformasjonForDoedsbo(KontaktinformasjonForDoedsbo kontaktinformasjon) {
		if (isNull(kontaktinformasjon)) {
			log.warn("Mottaker er registrert som død og har ugyldig postadresse");
			throw new UkjentAdressePersonErDoed(MOTTAKER_DOED, GONE);
		}

		if (!isDoedPersonValidKontaktAdresse(kontaktinformasjon)) {
			log.warn("Mottaker er registrert som død og har ugyldig postadresse");
			throw new UkjentAdressePersonErDoed(MOTTAKER_DOED, GONE);
		}

		return Optional.ofNullable(mapAndValidateKontaktinformasjonForDoeds(kontaktinformasjon)).orElseThrow(
				() -> new UkjentAdressePersonErDoed(MOTTAKER_DOED, GONE));
	}

	private boolean isDoedPersonValidKontaktAdresse(KontaktinformasjonForDoedsbo kontaktinformasjon) {
		return nonNull(kontaktinformasjon.getAttestutstedelsesdato()) && ((nonNull(kontaktinformasjon.getOrganisasjonSomKontakt()) ||
				nonNull(kontaktinformasjon.getAdvokatSomKontakt()) || nonNull(kontaktinformasjon.getPersonSomKontakt())));
	}

	private PostadresseTo mapAndValidateKontaktinformasjonForDoeds(KontaktinformasjonForDoedsbo kontaktinformasjonForDoedsbo) {
		KontaktinformasjonForDoedsbo.KontaktAdresse kontaktAdresse = Optional.ofNullable(kontaktinformasjonForDoedsbo.getAdresse())
				.orElseThrow(() -> new UkjentAdressePersonErDoed(MOTTAKER_DOED, GONE));
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
					.adresselinje2(getAdresselinje(kontaktAdresse.getAdresselinje1()))
					.adresselinje3(getAdresselinje(kontaktAdresse.getAdresselinje2()))
					.postnummer(requireNonNull(kontaktAdresse.getPostnummer(), format(ERROR_MELDING, POSTNUMMER)))
					.poststed(requireNonNull(isBlank(kontaktAdresse.getPoststedsnavn()) ? postnummerService.finnPoststed(kontaktAdresse.getPostnummer()) : kontaktAdresse.getPoststedsnavn(), format(ERROR_MELDING, "poststed")))
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
						.postnummer(requireNonNull(adresse.getPostnummer(), format(ERROR_MELDING, POSTNUMMER)))
						.poststed(isBlank(adresse.getPoststedsnavn()) ? postnummerService.finnPoststed(kontaktinformasjonForDoedsbo.getAdresse().getPostnummer())
								: adresse.getPoststedsnavn())
						.landkode(isLandkodeEmptyAndHavePostnummer(adresse.getLandkode(), adresse.getPostnummer()) ? LANDKODE_NORGE : adresse.getLandkode())
						.build() : null;
	}

	private PostadresseTo getValidAdresse(Vegadresse vegadresse, UtenlandskAdresse utenlandskAdresse,
										  Matrikkeladresse matrikkeladresse, UkjentBosted ukjentBosted,
										  String coAdressenavn, String serviceCode) {
		if (nonNull(vegadresse)) {
			return mapVegadresse(vegadresse, coAdressenavn).build();
		} else if (nonNull(utenlandskAdresse)) {
			return mapUtenlandskAdresse(utenlandskAdresse).build();
		} else if (nonNull(matrikkeladresse)) {
			return mapMatrikkeladresse(matrikkeladresse);
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
						.landkode(isLandkodeEmptyAndHavePostnummer(null, vegadresse.getPostnummer()) ? LANDKODE_NORGE : null) :
				PostadresseTo.builder()
						.adresseType(POSTADRESSE_INNLAND)
						.adresselinje1(coAdressenavn)
						.adresselinje2(Optional.ofNullable(vegadresse.getAdressenavn())
								.orElse("") + " " + Optional.ofNullable(isNull(vegadresse.getHusnummer()) ? null : vegadresse.getHusnummer())
								.orElse("") + Optional.ofNullable(vegadresse.getHusbokstav()).orElse(""))
						.postnummer(requireNonNull(vegadresse.getPostnummer(), format(ERROR_MELDING, POSTNUMMER)))
						.poststed(requireNonNull(postnummerService.finnPoststed(vegadresse.getPostnummer()), format(ERROR_MELDING, "poststed")))
						.landkode(isLandkodeEmptyAndHavePostnummer(null, vegadresse.getPostnummer()) ? LANDKODE_NORGE : null);
	}

	private PostadresseTo.PostadresseToBuilder mapUtenlandskAdresse(UtenlandskAdresse utenlandskAdresse) {
		return PostadresseTo.builder()
				.adresseType(POSTADRESSE_UTLAND)
				.adresselinje1(requireNonNull(isNotBlank(utenlandskAdresse.getPostboksNummerNavn()) ?
						utenlandskAdresse.getPostboksNummerNavn() :
						utenlandskAdresse.getAdressenavnNummer(), format(ERROR_UTENLANDSKADRESSE, "adresselinje1")))
				.adresselinje2(utenlandskAdresse.getPostkode())
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

	private String getMasterKilde(Metadata metadata) {
		return nonNull(metadata) ? metadata.getMaster() : null;
	}

	private boolean isGyldigDatoOgKilde(LocalDateTime gyldigDato, Metadata metadata) {
		return FREG.name().equals(getMasterKilde(metadata)) && (nonNull(gyldigDato) && now().isBefore(gyldigDato));
	}

	private boolean isSourcePdl(String source) {
		return PDL.name().equals(source);
	}

	public String getFulltnavn(List<HentPerson.PersonNavn> navns) {
		return navns.stream().filter(Objects::nonNull)
				.map(personNavn -> mapPersonnavn(personNavn))
				.filter(Objects::nonNull)
				.findFirst().orElseThrow(() -> new RegoppslagIllegalArgumentException(format(ERROR_MELDING, "Personnavn"), BAD_REQUEST));

	}

	private String mapPersonnavn(HentPerson.PersonNavn personNavn) {
		if (isBlank(personNavn.getFornavn()) || isBlank(personNavn.getEtternavn())) {
			throw new RegoppslagIllegalArgumentException(format(ERROR_MELDING, isBlank(personNavn.getFornavn()) ? FORNAVN : ETTERNAVN), BAD_REQUEST);
		}
		return trim(getNavn(personNavn.getFornavn()) + getNavn(personNavn.getMellomnavn()) + getNavn(personNavn.getEtternavn()));
	}

	private Kontaktadresse getKontaktadresse(HentPerson hentPerson) {
		return isNull(hentPerson.getKontaktadresse()) ? null : hentPerson.getKontaktadresse().stream()
				.filter(Objects::nonNull)
				.findAny().orElse(null);
	}

	private Oppholdsadresse getOppholdsadresse(HentPerson hentPerson) {
		return isNull(hentPerson.getOppholdsadresse()) ? null : hentPerson.getOppholdsadresse().stream()
				.filter(Objects::nonNull).findAny().orElse(null);
	}

	private Bostedsadresse getBostedsadresse(HentPerson hentPerson) {
		return isNull(hentPerson.getBostedsadresse()) ? null : hentPerson.getBostedsadresse().stream()
				.filter(Objects::nonNull).findAny().orElse(null);
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

	private boolean isLandkodeEmptyAndHavePostnummer(String landkode, String postnummer) {
		return isBlank(landkode) && isNotBlank(postnummerService.finnPoststed(postnummer));
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