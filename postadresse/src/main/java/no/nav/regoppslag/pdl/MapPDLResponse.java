package no.nav.regoppslag.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.pdl.to.AdresseGyldigKilde;
import no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode;
import no.nav.regoppslag.consumer.pdl.to.Bostedsadresse;
import no.nav.regoppslag.consumer.pdl.to.HentPerson;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.Matrikkeladresse;
import no.nav.regoppslag.consumer.pdl.to.Oppholdsadresse;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.to.UkjentBosted;
import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;
import no.nav.regoppslag.consumer.pdl.to.Vegadresse;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.function.Predicate.not;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.BOSTEDSADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.OPPHOLDSADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_UTFLYTTET;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.pdl.UtenlandskAdresseService.mapUtenlandskAdresse;
import static no.nav.regoppslag.pdl.UtenlandskAdresseService.mapUtenlandskPostadresse;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Implementasjon av reglene i
 * <a href="https://pdldocs-navno.msappproxy.net/ekstern/index.html#_hvilken_adresse_b%C3%B8r_man_bruke">"Hvilken adresse bør man bruke"</a>
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
 * NB! Dersom personen har en bostedsadresse som er nyere enn den adressen som velges ved prioriteringen ovenfor her, anbefaler vi at det er bostedsadressen som benyttes.
 */
@Slf4j
@Component
public class MapPDLResponse {

	private final DoedsboAdresseService doedsboAdresseService;
	private final NorskAdresseService norskAdresseService;

	public MapPDLResponse(DoedsboAdresseService doedsboAdresseService,
						  NorskAdresseService norskAdresseService) {
		this.doedsboAdresseService = doedsboAdresseService;
		this.norskAdresseService = norskAdresseService;
	}

	public PdlMottakerInfo mapHentPerson(HentPerson hentPerson, String serviceCode, String tema) {
		// sjekk at personen ikke er død
		if (hentPerson.isDoed()) {
			return doedsboAdresseService.mapFoerDoedsbo(hentPerson, tema);
		}

		// regel "6": Bruk bostedsadresse om denne er nyere enn de andre.
		Bostedsadresse bostedsadresse = hentPerson.getBostedsadresse();
		Optional<PdlMottakerInfo> bostedsadresseOptional = Optional.empty();
		boolean erBostedsadresseGyldigMedDatoFra = false;
		if (nonNull(bostedsadresse)) {
			bostedsadresseOptional = mapBostedsadresse(hentPerson, serviceCode, bostedsadresse);
			if (bostedsadresseOptional.isPresent() && nonNull(bostedsadresse.getGyldigFraOgMed()) ) {
				erBostedsadresseGyldigMedDatoFra = true;
			}
		}

		Optional<Kontaktadresse> kontaktadresseOptional = getBestFitGyldigAdresse(hentPerson.getKontaktadresse());
		// prøver å bruke kontaktadresse - regel 1 og 2
		if (kontaktadresseOptional.isPresent()) {
			Kontaktadresse kontaktadresse = kontaktadresseOptional.get();
			Optional<PdlMottakerInfo> pdlMottakerInfo = mapKontaktadresse(hentPerson, kontaktadresse);
			if (pdlMottakerInfo.filter(not(MapPDLResponse::isInnlandAdresseTypeAndPostnummerNull)).isPresent()) {
				//Bruk bostedsadresse hvis denne er av nyere dato enn kontaktadresse
				if (erBostedsadresseGyldigMedDatoFra &&
						kontaktadresse.getGyldigFraOgMed() != null &&
						bostedsadresse.getGyldigFraOgMed().isAfter(kontaktadresse.getGyldigFraOgMed())) {
					return bostedsadresseOptional.get();
				} else {
					return pdlMottakerInfo.get();
				}
			} else {
				log.info("Fant ikke kontaktadresse og søker etter oppholdsadresse for personen i PDL data");
			}
		}

		Optional<Oppholdsadresse> oppholdsadresseOptional = getBestFitGyldigAdresse(hentPerson.getOppholdsadresse());
		// prøver å bruke oppholdsadresse - regel 3 og 4
		if (oppholdsadresseOptional.isPresent()) {
			Oppholdsadresse oppholdsadresse = oppholdsadresseOptional.get();
			Optional<PdlMottakerInfo> mottakerFraOppholdsadresse = mapOppholdsadresse(hentPerson, serviceCode, oppholdsadresse);
			if (mottakerFraOppholdsadresse.isPresent()) {
				//Bruk bostedsadresse hvis denne er av nyere dato enn oppholdsadresse
				if (erBostedsadresseGyldigMedDatoFra &&
						oppholdsadresse.getGyldigFraOgMed() != null &&
						bostedsadresse.getGyldigFraOgMed().isAfter(oppholdsadresse.getGyldigFraOgMed())) {
					return bostedsadresseOptional.get();
				} else {
					return mottakerFraOppholdsadresse.get();
				}
			} else {
				log.info("Fant ikke oppholdsadresse og søker etter bostedsadresse for personen i PDL data");
			}
		}

		// prøver bostedsadresse - regel 5
		if (nonNull(bostedsadresse)) {
			if (bostedsadresseOptional.isPresent()) {
				return bostedsadresseOptional.get();
			} else {
				throw new UkjentAdresseException("Fant ikke bostedsadresse for personen i PDL", NOT_FOUND);
			}
		}

		if (PERSONSTATUS_UTFLYTTET.equalsIgnoreCase(hentPerson.getFolkeregisterstatus())) {
			throw new UkjentAdresseException(format("Fant ikke adresse for personen i PDL, med status=utflyttet og kilde=%s",
					hentPerson.getFolkeregistermetadataKilde()), NOT_FOUND);
		}

		throw new UkjentAdresseException("Fant ikke adresse for personen i PDL", NOT_FOUND);
	}

	// Implementerer regler 1,2 (eller 3 og 4)
	private static <T extends AdresseGyldigKilde> Optional<T> getBestFitGyldigAdresse(List<T> kontaktadresse) {
		if (isNull(kontaktadresse)) {
			return empty();
		}
		return kontaktadresse.stream()
				// Regel 1. Kontaktadresse med master PDL
				.filter(AdresseGyldigKilde::isGyldigPdlKilde)
				.findFirst()
				.or(() -> kontaktadresse.stream()
						.filter(AdresseGyldigKilde::isGyldigFregKilde)
						// Regel 2. Kontaktadresse fra Freg med nyeste registreringsdato (det er mulig med to)
						// Sorteres naturlig etter kontaktadresse.gyldigFraOgMed
						.max(Comparator.naturalOrder()));
	}

	private Optional<PdlMottakerInfo> mapKontaktadresse(HentPerson hentPerson, Kontaktadresse kontaktadresse) {
		return mapPostadresseFraKontaktadresse(kontaktadresse)
				.map(postadresse -> PdlMottakerInfo.builder().identifikasjonsnummer(hentPerson.getIdentifikasjonsnummer())
						.navn(hentPerson.getFulltnavn())
						.kortNavn(hentPerson.getForkortetNavn())
						.foedselsdato(hentPerson.getFoedselsdato())
						.doedsdato(hentPerson.getDoedsdato().orElse(null))
						.postadresse(postadresse)
						.build());
	}

	private Optional<PdlMottakerInfo> mapBostedsadresse(HentPerson hentPerson, String serviceCode, Bostedsadresse bostedsadresse) {
		return mapPostadresseFraBostedsadresse(bostedsadresse, serviceCode)
				.map(adresse -> PdlMottakerInfo.builder()
						.identifikasjonsnummer(hentPerson.getIdentifikasjonsnummer())
						.navn(hentPerson.getFulltnavn())
						.foedselsdato(hentPerson.getFoedselsdato())
						.kortNavn(hentPerson.getForkortetNavn())
						.postadresse(adresse)
						.build());
	}

	private Optional<PdlMottakerInfo> mapOppholdsadresse(HentPerson hentPerson, String serviceCode, Oppholdsadresse oppholdsadresse) {
		return mapPostadresseFraOppholdsadresse(oppholdsadresse, serviceCode)
				.map(adresse ->
						PdlMottakerInfo.builder()
								.identifikasjonsnummer(hentPerson.getIdentifikasjonsnummer())
								.navn(hentPerson.getFulltnavn())
								.kortNavn(hentPerson.getForkortetNavn())
								.foedselsdato(hentPerson.getFoedselsdato())
								.postadresse(adresse)
								.build());
	}

	private Optional<PostadresseTo> mapPostadresseFraKontaktadresse(Kontaktadresse kontaktadresse) {
		if (POSTADRESSE_INNLAND.equalsIgnoreCase(kontaktadresse.getType())) {
			return norskAdresseService.mapNorskPostadresse(kontaktadresse);
		} else if (POSTADRESSE_UTLAND.equalsIgnoreCase(kontaktadresse.getType())) {
			return mapUtenlandskPostadresse(kontaktadresse);
		}
		return empty();
	}

	private Optional<PostadresseTo> mapPostadresseFraBostedsadresse(Bostedsadresse bostedsadresse, String serviceCode) {
		return getValidAdresse(
				bostedsadresse.getVegadresse(),
				bostedsadresse.getUtenlandskAdresse(),
				bostedsadresse.getMatrikkeladresse(),
				bostedsadresse.getUkjentBosted(),
				bostedsadresse.getCoAdressenavn(),
				serviceCode,
				BOSTEDSADRESSE
		);
	}

	private Optional<PostadresseTo> mapPostadresseFraOppholdsadresse(Oppholdsadresse oppholdsadresse, String serviceCode) {
		return getValidAdresse(
				oppholdsadresse.getVegadresse(),
				oppholdsadresse.getUtenlandskAdresse(),
				oppholdsadresse.getMatrikkeladresse(),
				null,
				oppholdsadresse.getCoAdressenavn(),
				serviceCode,
				OPPHOLDSADRESSE
		);
	}

	private Optional<PostadresseTo> getValidAdresse(Vegadresse vegadresse,
													UtenlandskAdresse utenlandskAdresse,
													Matrikkeladresse matrikkeladresse,
													UkjentBosted ukjentBosted,
													String coAdressenavn,
													String serviceCode,
													AdresseKildeCode adresseKilde) {
		if (nonNull(vegadresse)) {
			return Optional.of(norskAdresseService.mapVegadresse(vegadresse, coAdressenavn).adressekilde(adresseKilde).build());
		} else if (nonNull(utenlandskAdresse)) {
			return Optional.of(mapUtenlandskAdresse(utenlandskAdresse, coAdressenavn).adressekilde(adresseKilde).build());
		} else if (nonNull(matrikkeladresse)) {
			return Optional.of(norskAdresseService.mapMatrikkeladresse(matrikkeladresse, adresseKilde));
		} else if (nonNull(ukjentBosted)) {
			throw new UkjentAdresseException(serviceCode + ": Kunne ikke mappe postadresse for UkjentBosted mottaker", NOT_FOUND);
		}

		return empty();
	}

	private static boolean isInnlandAdresseTypeAndPostnummerNull(PdlMottakerInfo pdlMottakerInfo) {
		return isBlank(pdlMottakerInfo.getPostadresse().getPostnummer()) && POSTADRESSE_INNLAND.equals(pdlMottakerInfo.getPostadresse().getAdresseType());
	}

}