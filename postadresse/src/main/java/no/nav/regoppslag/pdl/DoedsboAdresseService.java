package no.nav.regoppslag.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.to.HentPerson;
import no.nav.regoppslag.consumer.pdl.to.KontaktinformasjonForDoedsbo;
import no.nav.regoppslag.consumer.pdl.to.KontaktinformasjonForDoedsbo.AdvokatSomKontakt;
import no.nav.regoppslag.consumer.pdl.to.KontaktinformasjonForDoedsbo.KontaktAdresse;
import no.nav.regoppslag.consumer.pdl.to.KontaktinformasjonForDoedsbo.OrganisasjonSomKontakt;
import no.nav.regoppslag.consumer.pdl.to.KontaktinformasjonForDoedsbo.PersonSomKontakt;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoedException;
import no.nav.regoppslag.service.PostnummerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTINFORMASJONFORDØDSBO;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.pdl.MapPDLUtils.getAlpha2Landkode;
import static no.nav.regoppslag.pdl.MapPDLUtils.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.GONE;

@Slf4j
@Component
public class DoedsboAdresseService {

	private static final String ALPHA2_NORGE_LANDKODE = "NO";
	private static final String ALPHA3_NORGE_LANDKODE = "NOR";
	private static final String ERROR_MELDING = "Feltet %s kan ikke være null eller tomt";
	private static final String CARE_OF = "C/O ";
	private static final String MOTTAKER_DOED = "Person er død og har ingen registrerte kontaktsopplysninger for dødsbo";
	private static final String POSTNUMMER = "postnummer";
	private static final Logger secureLog = LoggerFactory.getLogger("secureLog");

	private final PostnummerService postnummerService;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;

	public DoedsboAdresseService(PostnummerService postnummerService, PdlGraphQLConsumer pdlGraphQLConsumer) {
		this.postnummerService = postnummerService;
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
	}

	PdlMottakerInfo mapFoerDoedsbo(HentPerson hentPerson) {
		return PdlMottakerInfo.builder()
				.identifikasjonsnummer(hentPerson.getIdentifikasjonsnummer())
				.navn(hentPerson.getFulltnavn() + " DOEDSBO")
				.kortNavn(hentPerson.getForkortetNavn())
				.doedsdato(hentPerson.getDoedsdato().orElse(null))
				.postadresse(mapKontaktinformasjonForDoedsbo(getKontaktForDoedsbo(hentPerson)))
				.build();
	}

	private PostadresseTo mapKontaktinformasjonForDoedsbo(Optional<KontaktinformasjonForDoedsbo> kontaktinformasjon) {
		return kontaktinformasjon
				.filter(DoedsboAdresseService::isDoedPersonValidKontaktAdresse)
				.map(this::mapAndValidateKontaktinformasjonForDoeds)
				.orElseThrow(() -> new UkjentAdressePersonErDoedException(MOTTAKER_DOED, GONE));
	}

	private static boolean isDoedPersonValidKontaktAdresse(KontaktinformasjonForDoedsbo kontaktinformasjon) {
		return nonNull(kontaktinformasjon.getAttestutstedelsesdato()) && ((nonNull(kontaktinformasjon.getOrganisasjonSomKontakt()) ||
				nonNull(kontaktinformasjon.getAdvokatSomKontakt()) || nonNull(kontaktinformasjon.getPersonSomKontakt())));
	}

	private PostadresseTo mapAndValidateKontaktinformasjonForDoeds(KontaktinformasjonForDoedsbo kontaktinformasjonForDoedsbo) {
		KontaktAdresse kontaktAdresse = kontaktinformasjonForDoedsbo.getAdresse();

		if (nonNull(kontaktinformasjonForDoedsbo.getAdvokatSomKontakt())) {
			AdvokatSomKontakt advokatSomKontakt = kontaktinformasjonForDoedsbo.getAdvokatSomKontakt();
			secureLog.info("Hentet KONTAKTINFORMASJONFORDØDSBO med advokat som kontakt");
			return mapMidlertidigPostboksadresse(kontaktAdresse, getAdvokatOrOrgKontaktNavn(advokatSomKontakt.getPersonnavn(), advokatSomKontakt.getOrganisasjonsnavn()));
		} else if (nonNull(kontaktinformasjonForDoedsbo.getPersonSomKontakt())) {
			PersonSomKontakt personSomKontakt = kontaktinformasjonForDoedsbo.getPersonSomKontakt();
			secureLog.info("Hentet KONTAKTINFORMASJONFORDØDSBO med person som kontakt");
			return mapMidlertidigPostboksadresse(kontaktAdresse, getPersonSomKontaktNavn(personSomKontakt));
		} else if (nonNull(kontaktinformasjonForDoedsbo.getOrganisasjonSomKontakt()) && nonNull(kontaktAdresse)) {
			OrganisasjonSomKontakt organisasjonSomKontakt = kontaktinformasjonForDoedsbo.getOrganisasjonSomKontakt();
			secureLog.info("Hentet KONTAKTINFORMASJONFORDØDSBO med organisasjon som kontakt");
			return mapOrganisasjonSomKontaktAdresse(kontaktAdresse, getAdvokatOrOrgKontaktNavn(organisasjonSomKontakt.getKontaktperson(), organisasjonSomKontakt.getOrganisasjonsnavn()));
		}
		return null;
	}

	private PostadresseTo mapOrganisasjonSomKontaktAdresse(KontaktAdresse kontaktAdresse, String fulltnavn) {

		if (isNorskadresse(kontaktAdresse.getLandkode())) {
			return PostadresseTo.builder()
					.adressekilde(KONTAKTINFORMASJONFORDØDSBO)
					.adresseType(POSTADRESSE_INNLAND)
					.adresselinje1(isBlank(fulltnavn) ? getAdresselinje(kontaktAdresse.getAdresselinje1()) : CARE_OF + fulltnavn)
					.adresselinje2(isBlank(fulltnavn) ? getAdresselinje(kontaktAdresse.getAdresselinje2()) : getAdresselinje(kontaktAdresse.getAdresselinje1()))
					.adresselinje3(isBlank(fulltnavn) ? null : getAdresselinje(kontaktAdresse.getAdresselinje2()))
					.postnummer(requireNonNull(kontaktAdresse.getPostnummer(), format(ERROR_MELDING, POSTNUMMER)))
					.poststed(requireNonNull(isBlank(kontaktAdresse.getPoststedsnavn()) ? postnummerService.finnPoststed(kontaktAdresse.getPostnummer()) : kontaktAdresse.getPoststedsnavn(), format(ERROR_MELDING, "poststed")))
					.landkode(ALPHA2_NORGE_LANDKODE)
					.build();
		}

		return mapDoedsboForUtenlandskAdresse(kontaktAdresse, fulltnavn);
	}

	private PostadresseTo mapMidlertidigPostboksadresse(KontaktAdresse adresse, String navn) {
		if (adresse == null) {
			return null;
		}

		if (isNorskadresse(adresse.getLandkode())) {
			return PostadresseTo.builder()
					.adressekilde(KONTAKTINFORMASJONFORDØDSBO)
					.adresseType(POSTADRESSE_INNLAND)
					.adresselinje1(isBlank(navn) ? adresse.getAdresselinje1() : CARE_OF + navn)
					.adresselinje2(isBlank(navn) ? adresse.getAdresselinje2() : adresse.getAdresselinje1())
					.adresselinje3(isBlank(navn) ? null : adresse.getAdresselinje2())
					.postnummer(requireNonNull(adresse.getPostnummer(), format(ERROR_MELDING, POSTNUMMER)))
					.poststed(isBlank(adresse.getPoststedsnavn()) ? postnummerService.finnPoststed(adresse.getPostnummer()) : adresse.getPoststedsnavn())
					.landkode(ALPHA2_NORGE_LANDKODE)
					.build();
		}

		return mapDoedsboForUtenlandskAdresse(adresse,navn);
	}

	private PostadresseTo mapDoedsboForUtenlandskAdresse(KontaktAdresse adresse, String navn) {
		if (Objects.isNull(adresse)) {
			return null;
		}

		return PostadresseTo.builder()
				.adressekilde(KONTAKTINFORMASJONFORDØDSBO)
				.adresseType(POSTADRESSE_UTLAND)
				.adresselinje1(isBlank(navn) ? adresse.getAdresselinje1() : CARE_OF + navn)
				.adresselinje2(isBlank(navn) ? adresse.getAdresselinje2() : concatenateAdresse(adresse.getAdresselinje1(), adresse.getAdresselinje2()))
				.adresselinje3(adresse.getPostnummer() + " " + adresse.getPoststedsnavn())
				.landkode(getAlpha2Landkode(adresse.getLandkode()))
				.build();
	}

	private String getAdvokatOrOrgKontaktNavn(KontaktinformasjonForDoedsbo.Personnavn personnavn, String organisasjonsnavn) {
		return personnavn != null && isNotBlank(personnavn.getFulltnavn()) ? personnavn.getFulltnavn() : organisasjonsnavn;
	}

	private String getPersonSomKontaktNavn(PersonSomKontakt personSomKontakt) {
		if (nonNull(personSomKontakt.getPersonnavn()) && isNotBlank(personSomKontakt.getPersonnavn().getFulltnavn())) {
			return personSomKontakt.getPersonnavn().getFulltnavn();
		}

		if (isBlank(personSomKontakt.getIdentifikasjonsnummer())) {
			return null;
		}

		return pdlGraphQLConsumer.hentDoedsBoKontaktPersonnavn(personSomKontakt.getIdentifikasjonsnummer()).orElse(null);
	}

	private String getAdresselinje(String adresselinje) {
		return isBlank(adresselinje) ? null : adresselinje;
	}

	private Optional<KontaktinformasjonForDoedsbo> getKontaktForDoedsbo(HentPerson hentPerson) {
		if (isNull(hentPerson.getKontaktinformasjonForDoedsbo())) {
			return Optional.empty();
		}

		return hentPerson.getKontaktinformasjonForDoedsbo().stream()
				.filter(Objects::nonNull)
				.findAny();
	}

	private boolean isNorskadresse(String landkode) {
		return ALPHA3_NORGE_LANDKODE.equals(landkode) || isBlank(landkode);
	}

	private String concatenateAdresse(String adresse1, String adresse2) {
		if (isNotBlank(adresse1) && isBlank(adresse2)) {
			return adresse1;
		} else if (isNotBlank(adresse1) && isNotBlank(adresse2)) {
			return adresse1 + ", " + adresse2;
		}  else {
			return adresse2;
		}
	}
}
