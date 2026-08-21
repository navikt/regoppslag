package no.nav.regoppslag.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode;
import no.nav.regoppslag.consumer.pdl.to.Bostedsadresse;
import no.nav.regoppslag.consumer.pdl.to.HentPerson;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.Matrikkeladresse;
import no.nav.regoppslag.consumer.pdl.to.Oppholdsadresse;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo.PdlMottakerInfoBuilder;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.to.UkjentBosted;
import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;
import no.nav.regoppslag.consumer.pdl.to.Vegadresse;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.BOSTEDSADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.OPPHOLDSADRESSE;
import static org.springframework.util.CollectionUtils.isEmpty;

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
 * NB! Dersom personen har en utenlandsk bostedsadresse som er nyere enn den adressen som velges ved prioriteringen ovenfor her, anbefaler vi at det er bostedsadressen som benyttes.
 */
@Slf4j
@Component
public class MapPDLResponse {

	// UKJENT_ADRESSE_REASON_CODE brukes til å sjekke feilmeldinger i andre applikasjoner, så innholdet i UKJENT_ADRESSE_REASON_CODE må ikke endres!
	public static final String UKJENT_ADRESSE_REASON_CODE = "ukjent_adresse";
	public static final String UGRADERT = "ugradert";
	public static final String FORTROLIG = "fortrolig";
	public static final String STRENGT_FORTROLIG = "strengt_fortrolig";
	public static final String STRENGT_FORTROLIG_UTLAND = "strengt_fortrolig_utland";

	private final DoedsboAdresseService doedsboAdresseService;
	private final Clock clock;

	public MapPDLResponse(DoedsboAdresseService doedsboAdresseService,
						  Clock clock) {
		this.doedsboAdresseService = doedsboAdresseService;
		this.clock = clock;
	}

	public PdlMottakerInfo mapHentPerson(HentPerson hentPerson, String serviceCode) {
		// sjekk at personen ikke er død
		if (hentPerson.isDoed()) {
			return doedsboAdresseService.mapFoerDoedsbo(hentPerson);
		}

		String debugLog = "";
		LocalDateTime now = LocalDateTime.now(clock);

		// regel "6": Bruk bostedsadresse om denne er nyere enn de andre.
		Bostedsadresse bostedsadresse = Adressevelger.velgBostedsadresse(hentPerson.getBostedsadresse(), now).orElse(null);
		Optional<PdlMottakerInfo> bostedsadresseOptional = empty();

		if (bostedsadresse != null) {
			PDLAdresseValidator.validerBostedsadresse(bostedsadresse);
			bostedsadresseOptional = safeMapBostedsadresse(hentPerson, serviceCode, bostedsadresse);

			if (bostedsadresseOptional.isPresent() && (bostedsadresse.getGyldigFraOgMedOrSisteEndring() != null)) {
				debugLog += generateDebugLog(BOSTEDSADRESSE.name(), bostedsadresse.getGyldigFraOgMed());
			}
		}

		PostadresseTo bostedsPostadresse = bostedsadresseOptional
				.map(PdlMottakerInfo::getPostadresse)
				.orElse(null);

		Optional<Kontaktadresse> kontaktadresseOptional = Adressevelger.velgAdresseEtterKildeOgGyldighetsperiode(hentPerson.getKontaktadresse(), now);

		// prøver å bruke kontaktadresse - regel 1 og 2
		if (kontaktadresseOptional.isPresent()) {
			Kontaktadresse kontaktadresse = kontaktadresseOptional.get();
			PDLAdresseValidator.validerKontaktadresse(kontaktadresse);
			Optional<PdlMottakerInfo> mottakerFraKontaktAdresse = safeMapKontaktadresse(hentPerson, kontaktadresse);

			if (mottakerFraKontaktAdresse.filter(mottaker -> mottaker.getPostadresse().erKomplettForDistribusjon()).isPresent()) {
				//Bruk bostedsadresse hvis denne er av nyere dato enn kontaktadresse
				if (Adressevelger.skalPrioritereUtenlandskBostedsadresse(bostedsadresse, bostedsPostadresse, kontaktadresse)) {
					generateAndLogDebugLog(KONTAKTADRESSE.name(), kontaktadresse.getGyldigFraOgMed(), debugLog, BOSTEDSADRESSE.name());
					return bostedsadresseOptional.get();
				} else {
					generateAndLogDebugLog(KONTAKTADRESSE.name(), kontaktadresse.getGyldigFraOgMed(), debugLog, KONTAKTADRESSE.name());
					return mottakerFraKontaktAdresse.get();
				}
			} else {
				log.info("Fant ikke kontaktadresse og søker etter oppholdsadresse for personen i PDL data");
			}
		}

		Optional<Oppholdsadresse> oppholdsadresseOptional = Adressevelger.velgAdresseEtterKildeOgGyldighetsperiode(hentPerson.getOppholdsadresse(), now);
		// prøver å bruke oppholdsadresse - regel 3 og 4
		if (oppholdsadresseOptional.isPresent()) {
			Oppholdsadresse oppholdsadresse = oppholdsadresseOptional.get();
			PDLAdresseValidator.validerOppholdsadresse(oppholdsadresse);
			Optional<PdlMottakerInfo> mottakerFraOppholdsadresse = mapOppholdsadresse(hentPerson, serviceCode, oppholdsadresse);

			if (mottakerFraOppholdsadresse.isPresent()) {
				//Bruk bostedsadresse hvis denne er av nyere dato enn oppholdsadresse
				if (Adressevelger.skalPrioritereUtenlandskBostedsadresse(bostedsadresse, bostedsPostadresse, oppholdsadresse)) {
					generateAndLogDebugLog(OPPHOLDSADRESSE.name(), oppholdsadresse.getGyldigFraOgMed(), debugLog, BOSTEDSADRESSE.name());
					return bostedsadresseOptional.get();
				} else {
					generateAndLogDebugLog(OPPHOLDSADRESSE.name(), oppholdsadresse.getGyldigFraOgMed(), debugLog, OPPHOLDSADRESSE.name());
					return mottakerFraOppholdsadresse.get();
				}
			} else {
				log.info("Fant ikke oppholdsadresse og søker etter bostedsadresse for personen i PDL data");
			}
		}

		// prøver bostedsadresse - regel 5
		if (bostedsadresse != null) {
			if (bostedsadresseOptional.isPresent()) {
				return bostedsadresseOptional.get();
			} else {
				throw new UkjentAdresseException("Fant ikke bostedsadresse for personen i PDL", UKJENT_ADRESSE_REASON_CODE);
			}
		}

		if (hentPerson.erUtflyttet()) {
			throw new UkjentAdresseException(format("Fant ikke adresse for personen i PDL, med status=utflyttet og kilde=%s",
					hentPerson.getFolkeregistermetadataKilde()), UKJENT_ADRESSE_REASON_CODE);
		}

		throw new UkjentAdresseException("Fant ikke adresse for personen i PDL", UKJENT_ADRESSE_REASON_CODE);
	}

	private void generateAndLogDebugLog(String adresseType, LocalDateTime gyldigFraOgMed, String debugLog, String nyesteAdresseType) {
		//Ønsker bare å logge info hvis erBostedsadresseGyldigMedDatoFra != false.
		//Sjekker det implisitt her ved å sjekke om stringen er tom da denne bare får en verdi om erBostedsadresseGyldigMedDatoFra == true.
		if (!debugLog.isBlank()) {
			debugLog += generateDebugLog(adresseType, gyldigFraOgMed);
			debugLog += nyesteAdresseType + " er den sist oppdaterte adressen. Returnerer adresseType " + nyesteAdresseType;
			log.info(debugLog);
		}
	}

	private String generateDebugLog(String adresseType, LocalDateTime gyldigFraOgMed) {
		return adresseType + (gyldigFraOgMed == null ? " har ingen gyldigFraOgMed. Bruker siste endring som gyldigFraOgMed\n" :
				" har satt gyldigFraOgMed. Bruker gyldigFraOgMed\n");
	}

	private Optional<PdlMottakerInfo> safeMapKontaktadresse(HentPerson hentPerson, Kontaktadresse kontaktadresse) {
		return mapPostadresseFraKontaktadresse(kontaktadresse)
				.map(postadresse -> mottakerInfoBuilder(hentPerson, postadresse)
						.doedsdato(hentPerson.getDoedsdato().orElse(null))
						.build());
	}

	private Optional<PdlMottakerInfo> safeMapBostedsadresse(HentPerson hentPerson, String serviceCode, Bostedsadresse bostedsadresse) {
		try {
			return mapPostadresseFraBostedsadresse(bostedsadresse, serviceCode)
					.map(adresse -> mottakerInfoBuilder(hentPerson, adresse).build());
		} catch (UkjentAdresseException _) {
			return empty();
		}
	}

	private Optional<PdlMottakerInfo> mapOppholdsadresse(HentPerson hentPerson, String serviceCode, Oppholdsadresse oppholdsadresse) {
		return mapPostadresseFraOppholdsadresse(oppholdsadresse, serviceCode)
				.map(adresse -> mottakerInfoBuilder(hentPerson, adresse).build());
	}

	private PdlMottakerInfoBuilder mottakerInfoBuilder(HentPerson hentPerson, PostadresseTo postadresse) {
		return PdlMottakerInfo.builder()
				.identifikasjonsnummer(hentPerson.getIdentifikasjonsnummer())
				.navn(hentPerson.getFulltnavn())
				.kortNavn(hentPerson.getForkortetNavn())
				.postadresse(postadresse)
				.adressebeskyttelseType(mapAdressebeskyttelse(hentPerson));
	}

	private Optional<PostadresseTo> mapPostadresseFraKontaktadresse(Kontaktadresse kontaktadresse) {
		if (kontaktadresse.erInnland()) {
			return NorskAdresseMapper.mapPostadresse(kontaktadresse);
		} else if (kontaktadresse.erUtland()) {
			return UtenlandskAdresseMapper.mapUtenlandskPostadresse(kontaktadresse);
		}
		return empty();
	}

	private Optional<PostadresseTo> mapPostadresseFraBostedsadresse(Bostedsadresse bostedsadresse, String serviceCode) {
		return velgOgMapPostadresse(
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
		return velgOgMapPostadresse(
				oppholdsadresse.getVegadresse(),
				oppholdsadresse.getUtenlandskAdresse(),
				oppholdsadresse.getMatrikkeladresse(),
				null,
				oppholdsadresse.getCoAdressenavn(),
				serviceCode,
				OPPHOLDSADRESSE
		);
	}

	private Optional<PostadresseTo> velgOgMapPostadresse(Vegadresse vegadresse,
														 UtenlandskAdresse utenlandskAdresse,
														 Matrikkeladresse matrikkeladresse,
														 UkjentBosted ukjentBosted,
														 String coAdressenavn,
														 String serviceCode,
														 AdresseKildeCode adresseKilde) {
		if (vegadresse != null) {
			return Optional.of(NorskAdresseMapper.mapVegadresse(vegadresse, coAdressenavn, adresseKilde));
		} else if (utenlandskAdresse != null) {
			return Optional.of(UtenlandskAdresseMapper.mapUtenlandskAdresse(utenlandskAdresse, coAdressenavn, adresseKilde));
		} else if (matrikkeladresse != null) {
			return Optional.of(NorskAdresseMapper.mapMatrikkeladresse(matrikkeladresse, adresseKilde));
		} else if (ukjentBosted != null) {
			throw new UkjentAdresseException(serviceCode + ": Kunne ikke mappe postadresse for UkjentBosted mottaker", UKJENT_ADRESSE_REASON_CODE);
		}

		return empty();
	}

	private Set<String> mapAdressebeskyttelse(HentPerson hentPerson) {
		if (isEmpty(hentPerson.getAdressebeskyttelse())) {
			return Set.of();
		}

		return hentPerson.getAdressebeskyttelse().stream()
				.map(HentPerson.Adressebeskyttelse::getGradering)
				.filter(Objects::nonNull)
				.map(beskyttelseGradering -> switch (beskyttelseGradering) {
					case STRENGT_FORTROLIG_UTLAND -> STRENGT_FORTROLIG_UTLAND;
					case STRENGT_FORTROLIG -> STRENGT_FORTROLIG;
					case FORTROLIG -> FORTROLIG;
					case UGRADERT -> UGRADERT;
				})
				.collect(Collectors.toSet());
	}
}
