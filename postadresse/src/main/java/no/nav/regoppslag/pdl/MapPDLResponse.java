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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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
		Kontaktadresse kontaktadresse = getKontaktadresse(hentPerson);
		if (kanSendeTil(kontaktadresse)) {
			PdlMottakerInfo pdlMottakerInfo = mapKontaktadresse(hentPerson);
			if (!isNull(pdlMottakerInfo.getPostadresse()) && !isInnlandAdresseTypeAndPostnummerNull(pdlMottakerInfo)) {
				return pdlMottakerInfo;
			}
			log.info("Fant ikke kontaktadresse og søker etter oppholdsadresse for personen i PDL data");
		}

		// prøver å bruke oppholdsadresse
		Oppholdsadresse oppholdsadresse = getOppholdsadresse(hentPerson);
		if (kanSendeTil(oppholdsadresse)) {
			PdlMottakerInfo pdlMottakerInfo = mapOppholdsadresse(hentPerson, serviceCode);
			if (nonNull(pdlMottakerInfo.getPostadresse())) {
				return pdlMottakerInfo;
			}
			log.info("Fant ikke oppholdsadresse og søker etter bostedsadresse for personen i PDL data");
		}

		// prøver bostedsadresse
		Bostedsadresse bostedsadresse = hentPerson.getBostedsadresse();
		if (nonNull(bostedsadresse)) {
			return mapBostedsadresse(hentPerson, serviceCode);
		}

		if (PERSONSTATUS_UTFLYTTET.equalsIgnoreCase(hentPerson.getFolkeregisterstatus())) {
			throw new UkjentAdresseException(format("Fant ikke adresse for personen i PDL, med status=utflyttet og kilde=%s",
					hentPerson.getFolkeregistermetadataKilde()), NOT_FOUND);
		}

		throw new UkjentAdresseException("Fant ikke adresse for personen i PDL", NOT_FOUND);
	}

	private static boolean kanSendeTil(AdresseGyldigKilde kontaktadresse) {
		return nonNull(kontaktadresse) && (kontaktadresse.isGyldigPdlKilde() || kontaktadresse.isGyldigFregKilde());
	}


	// Implementerer regler 1,2
	private static Kontaktadresse getKontaktadresse(HentPerson hentPerson) {
		if (isNull(hentPerson.getKontaktadresse()) || hentPerson.getKontaktadresse().isEmpty()) {
			return null;
		}
		return hentPerson.getKontaktadresse().stream()
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

	private PdlMottakerInfo mapKontaktadresse(HentPerson hentPerson) {
		String coAdressenavn = hentPerson.getKontaktadresse().stream()
				.filter(Objects::nonNull)
				.map(Kontaktadresse::getCoAdressenavn)
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
		Kontaktadresse kontaktadresse = getKontaktadresse(hentPerson);
		PostadresseTo postadresse = mapPostadresseFraKontaktadresse(kontaktadresse, coAdressenavn);
		return PdlMottakerInfo.builder().identifikasjonsnummer(hentPerson.getIdentifikasjonsnummer())
				.navn(hentPerson.getFulltnavn())
				.kortNavn(hentPerson.getForkortetNavn())
				.foedselsdato(hentPerson.getFoedselsdato())
				.doedsdato(hentPerson.getDoedsdato().orElse(null))
				.postadresse(postadresse)
				.build();
	}

	private PdlMottakerInfo mapBostedsadresse(HentPerson hentPerson, String serviceCode) {
		return PdlMottakerInfo.builder()
				.identifikasjonsnummer(hentPerson.getIdentifikasjonsnummer())
				.navn(hentPerson.getFulltnavn())
				.foedselsdato(hentPerson.getFoedselsdato())
				.kortNavn(hentPerson.getForkortetNavn())
				.postadresse(mapPostadresseFraBostedsadresse(hentPerson.getBostedsadresse(), serviceCode))
				.build();
	}

	private PdlMottakerInfo mapOppholdsadresse(HentPerson hentPerson, String serviceCode) {
		return PdlMottakerInfo.builder()
				.identifikasjonsnummer(hentPerson.getIdentifikasjonsnummer())
				.navn(hentPerson.getFulltnavn())
				.kortNavn(hentPerson.getForkortetNavn())
				.foedselsdato(hentPerson.getFoedselsdato())
				.postadresse(mapPostadresseFraOppholdsadresse(hentPerson.getOppholdsadresse().stream()
						.filter(Objects::nonNull).findAny()
						.orElse(null), serviceCode))
				.build();
	}

	private PostadresseTo mapPostadresseFraKontaktadresse(Kontaktadresse kontaktadresse, String coAdressenavn) {
		if (isNull(kontaktadresse)) {
			return null;
		}
		if (POSTADRESSE_INNLAND.equalsIgnoreCase(kontaktadresse.getType())) {
			return norskAdresseService.mapNorskPostAdresse(kontaktadresse, coAdressenavn);
		} else if (POSTADRESSE_UTLAND.equalsIgnoreCase(kontaktadresse.getType()) || nonNull(kontaktadresse.getUtenlandskAdresse())
				|| nonNull(kontaktadresse.getUtenlandskAdresseIFrittFormat())) {
			return mapUtenlandskPostAdresse(kontaktadresse, coAdressenavn);
		}
		return null;
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
		if (isNull(oppholdsadresse) || !harOppholdsadresse(oppholdsadresse)) {
			return null;
		}
		return getValidAdresse(oppholdsadresse.getVegadresse(), oppholdsadresse.getUtenlandskAdresse(),
				oppholdsadresse.getMatrikkeladresse(), null,
				oppholdsadresse.getCoAdressenavn(), serviceCode, OPPHOLDSADRESSE);
	}

	private boolean harBostedsadresse(Bostedsadresse bostedsadresse) {
		return (nonNull(bostedsadresse.getMatrikkeladresse()) || nonNull(bostedsadresse.getVegadresse())
				|| nonNull(bostedsadresse.getUtenlandskAdresse()) || nonNull(bostedsadresse.getUkjentBosted()));
	}

	private boolean harOppholdsadresse(Oppholdsadresse oppholdsadresse) {
		return (nonNull(oppholdsadresse.getMatrikkeladresse()) || nonNull(oppholdsadresse.getVegadresse())
				|| nonNull(oppholdsadresse.getUtenlandskAdresse()));
	}

	private PostadresseTo getValidAdresse(Vegadresse vegadresse, UtenlandskAdresse utenlandskAdresse,
										  Matrikkeladresse matrikkeladresse, UkjentBosted ukjentBosted,
										  String coAdressenavn, String serviceCode, AdresseKildeCode adresseKilde) {
		if (nonNull(vegadresse)) {
			return norskAdresseService.mapVegadresse(vegadresse, coAdressenavn).adressekilde(adresseKilde).build();
		} else if (nonNull(utenlandskAdresse)) {
			return mapUtenlandskAdresse(utenlandskAdresse, coAdressenavn).adressekilde(adresseKilde).build();
		} else if (nonNull(matrikkeladresse)) {
			return norskAdresseService.mapMatrikkeladresse(matrikkeladresse, adresseKilde);
		} else if (nonNull(ukjentBosted)) {
			throw new UkjentAdresseException(serviceCode + ": Kunne ikke mappe postadresse for UkjentBosted mottaker", NOT_FOUND);
		}
		return null;
	}

	private Oppholdsadresse getOppholdsadresse(HentPerson hentPerson) {
		if (isNull(hentPerson.getOppholdsadresse())) {
			return null;
		}
		return hentPerson.getOppholdsadresse().stream()
				.filter(Oppholdsadresse::isGyldigPdlKilde)
				.findAny().orElse(
						hentPerson.getOppholdsadresse().stream()
								.filter(Oppholdsadresse::isGyldigFregKilde)
								.findAny().orElse(null));
	}

	private boolean isInnlandAdresseTypeAndPostnummerNull(PdlMottakerInfo pdlMottakerInfo) {
		return isBlank(pdlMottakerInfo.getPostadresse().getPostnummer()) && POSTADRESSE_INNLAND.equals(pdlMottakerInfo.getPostadresse().getAdresseType());
	}

}