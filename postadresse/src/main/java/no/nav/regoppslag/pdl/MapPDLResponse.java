package no.nav.regoppslag.pdl;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Objects;
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
import static no.nav.regoppslag.pdl.UtenlandskAdresseService.mapUtenlandskPostAdresse;
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
 */
@Slf4j
@Component
public class MapPDLResponse {

	private final DoedsboAdresseService doedsboAdresseService;
	private final NorskAdresseService norskAdresseService;


	@Autowired
	public MapPDLResponse(
			DoedsboAdresseService doedsboAdresseService, NorskAdresseService norskAdresseService) {
		this.doedsboAdresseService = doedsboAdresseService;
		this.norskAdresseService = norskAdresseService;
	}

	public PdlMottakerInfo mapHentPerson(HentPerson hentPerson, String serviceCode, String tema) {
		// sjekk at personen ikke er død
		if (hentPerson.isDoed()) {
			return doedsboAdresseService.mapFoerDoedsbo(hentPerson, tema);
		}

		// prøver å bruke kontaktadresse
		Optional<Kontaktadresse> kontaktadresseOptional = getKontaktadresse(hentPerson);
		if (kontaktadresseOptional.isPresent()) {
			Optional<PdlMottakerInfo> pdlMottakerInfo = kontaktadresseOptional
					.flatMap(kontaktadresse -> mapKontaktadresse(hentPerson, kontaktadresse));
			if (pdlMottakerInfo.filter(not(MapPDLResponse::isInnlandAdresseTypeAndPostnummerNull)).isPresent()) {
				return pdlMottakerInfo.get();
			}
			log.info("Fant ikke kontaktadresse og søker etter oppholdsadresse for personen i PDL data");
		}

		// prøver å bruke oppholdsadresse
		Optional<Oppholdsadresse> oppholdsadresse = getOppholdsadresse(hentPerson);
		if (oppholdsadresse.isPresent()) {
			Optional<PdlMottakerInfo> pdlMottakerInfo = mapOppholdsadresse(hentPerson, serviceCode, oppholdsadresse.get());
			if (pdlMottakerInfo.isPresent()) {
				return pdlMottakerInfo.get();
			}
			log.info("Fant ikke oppholdsadresse og søker etter bostedsadresse for personen i PDL data");
		}

		// prøver bostedsadresse
		Bostedsadresse bostedsadresse = hentPerson.getBostedsadresse();
		if (nonNull(bostedsadresse)) {
			return mapBostedsadresse(hentPerson, serviceCode, bostedsadresse).orElseThrow(() ->
					new UkjentAdresseException("Fant ikke bostedsadresse for personen i PDL", NOT_FOUND));
		}

		if (PERSONSTATUS_UTFLYTTET.equalsIgnoreCase(hentPerson.getFolkeregisterstatus())) {
			throw new UkjentAdresseException(format("Fant ikke adresse for personen i PDL, med status=utflyttet og kilde=%s",
					hentPerson.getFolkeregistermetadataKilde()), NOT_FOUND);
		}

		throw new UkjentAdresseException("Fant ikke adresse for personen i PDL", NOT_FOUND);
	}

	// Implementerer regler 1,2
	private static Optional<Kontaktadresse> getKontaktadresse(HentPerson hentPerson) {
		if (isNull(hentPerson.getKontaktadresse())) {
			return empty();
		}
		return hentPerson.getKontaktadresse().stream()
				// Regel 1. Kontaktadresse med master PDL
				.filter(Kontaktadresse::isGyldigPdlKilde)
				.findFirst()
				.or(() -> hentPerson.getKontaktadresse().stream()
						.filter(Objects::nonNull)
						.filter(Kontaktadresse::isGyldigFregKilde)
						// Regel 2. Kontaktadresse fra Freg med nyeste registreringsdato (det er mulig med to)
						// Sorteres naturlig etter kontaktadresse.gyldigFraOgMed
						.max(Comparator.naturalOrder()));
	}

	private Optional<PdlMottakerInfo> mapKontaktadresse(HentPerson hentPerson, Kontaktadresse kontaktadresse) {
		String coAdressenavn = hentPerson.getKontaktadresse().stream()
				.filter(Objects::nonNull)
				.map(Kontaktadresse::getCoAdressenavn)
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
		return mapPostadresseFraKontaktadresse(kontaktadresse, coAdressenavn)
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

	private Optional<PostadresseTo> mapPostadresseFraKontaktadresse(Kontaktadresse kontaktadresse, String coAdressenavn) {
		if (POSTADRESSE_INNLAND.equalsIgnoreCase(kontaktadresse.getType())) {
			return norskAdresseService.mapNorskPostAdresse(kontaktadresse, coAdressenavn);
		} else if (POSTADRESSE_UTLAND.equalsIgnoreCase(kontaktadresse.getType())) {
			return mapUtenlandskPostAdresse(kontaktadresse, coAdressenavn);
		}
		return empty();
	}

	private Optional<PostadresseTo> mapPostadresseFraBostedsadresse(Bostedsadresse bostedsadresse, String serviceCode) {
		if (harBostedsadresse(bostedsadresse)) {
			return getValidAdresse(bostedsadresse.getVegadresse(), bostedsadresse.getUtenlandskAdresse(),
					bostedsadresse.getMatrikkeladresse(),
					bostedsadresse.getUkjentBosted(), bostedsadresse.getCoAdressenavn(), serviceCode, BOSTEDSADRESSE);
		}
		return empty();
	}

	private Optional<PostadresseTo> mapPostadresseFraOppholdsadresse(Oppholdsadresse oppholdsadresse, String serviceCode) {
		if (harOppholdsadresse(oppholdsadresse)) {
			return getValidAdresse(oppholdsadresse.getVegadresse(), oppholdsadresse.getUtenlandskAdresse(),
					oppholdsadresse.getMatrikkeladresse(), null,
					oppholdsadresse.getCoAdressenavn(), serviceCode, OPPHOLDSADRESSE);
		}
		return empty();
	}

	private boolean harBostedsadresse(Bostedsadresse bostedsadresse) {
		return (nonNull(bostedsadresse.getMatrikkeladresse()) || nonNull(bostedsadresse.getVegadresse())
				|| nonNull(bostedsadresse.getUtenlandskAdresse()) || nonNull(bostedsadresse.getUkjentBosted()));
	}

	private boolean harOppholdsadresse(Oppholdsadresse oppholdsadresse) {
		return (nonNull(oppholdsadresse.getMatrikkeladresse()) || nonNull(oppholdsadresse.getVegadresse())
				|| nonNull(oppholdsadresse.getUtenlandskAdresse()));
	}

	private Optional<PostadresseTo> getValidAdresse(Vegadresse vegadresse, UtenlandskAdresse utenlandskAdresse,
													Matrikkeladresse matrikkeladresse, UkjentBosted ukjentBosted,
													String coAdressenavn, String serviceCode, AdresseKildeCode adresseKilde) {
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

	private static Optional<Oppholdsadresse> getOppholdsadresse(HentPerson hentPerson) {
		if (isNull(hentPerson.getOppholdsadresse())) {
			return empty();
		}
		return hentPerson.getOppholdsadresse().stream()
				.filter(Oppholdsadresse::isGyldigPdlKilde)
				.findAny().or(() ->
						hentPerson.getOppholdsadresse().stream()
								.filter(Oppholdsadresse::isGyldigFregKilde)
								.findAny());
	}

	private static boolean isInnlandAdresseTypeAndPostnummerNull(PdlMottakerInfo pdlMottakerInfo) {
		return isBlank(pdlMottakerInfo.getPostadresse().getPostnummer()) && POSTADRESSE_INNLAND.equals(pdlMottakerInfo.getPostadresse().getAdresseType());
	}

}