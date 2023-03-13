package no.nav.regoppslag.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.to.HentPerson;
import no.nav.regoppslag.consumer.pdl.to.KontaktinformasjonForDoedsbo;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoed;
import no.nav.regoppslag.service.PostnummerService;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTINFORMASJONFORDØDSBO;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.pdl.MapPDLUtils.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.springframework.http.HttpStatus.GONE;

@Slf4j
@Component
public class DoedsboAdresseService {

	private final PostnummerService postnummerService;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private static final String LANDKODE_NORGE = "NO";
	private static final String ERROR_MELDING = "Feltet %s kan ikke være null eller tomt";
	private static final String ON_BEHALF_OF = "v/ ";
	private static final String MOTTAKER_DOED = "Person er død og har ingen registrerte kontaktsopplysninger for dødsbo";
	private static final String POSTNUMMER = "postnummer";

	public DoedsboAdresseService(PostnummerService postnummerService, PdlGraphQLConsumer pdlGraphQLConsumer) {
		this.postnummerService = postnummerService;
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
	}

	PdlMottakerInfo mapFoerDoedsbo(HentPerson hentPerson, String tema) {
		return PdlMottakerInfo.builder()
				.identifikasjonsnummer(hentPerson.getIdentifikasjonsnummer())
				.navn(hentPerson.getFulltnavn())
				.kortNavn(hentPerson.getForkortetNavn())
				.foedselsdato(hentPerson.getFoedselsdato())
				.doedsdato(hentPerson.getDoedsdato().orElse(null))
				.postadresse(mapKontaktinformasjonForDoedsbo(getKontaktForDoedsbo(hentPerson), tema))
				.build();
	}

	private PostadresseTo mapKontaktinformasjonForDoedsbo(Optional<KontaktinformasjonForDoedsbo> kontaktinformasjon, String tema) {
		return kontaktinformasjon
				.filter(DoedsboAdresseService::isDoedPersonValidKontaktAdresse)
				.map(kinfo -> mapAndValidateKontaktinformasjonForDoeds(kinfo, tema))
				.orElseThrow(
						() -> {
							log.warn(MOTTAKER_DOED);
							return new UkjentAdressePersonErDoed(MOTTAKER_DOED, GONE);
						});
	}

	private static boolean isDoedPersonValidKontaktAdresse(KontaktinformasjonForDoedsbo kontaktinformasjon) {
		return nonNull(kontaktinformasjon.getAttestutstedelsesdato()) && ((nonNull(kontaktinformasjon.getOrganisasjonSomKontakt()) ||
				nonNull(kontaktinformasjon.getAdvokatSomKontakt()) || nonNull(kontaktinformasjon.getPersonSomKontakt())));
	}

	private PostadresseTo mapAndValidateKontaktinformasjonForDoeds(KontaktinformasjonForDoedsbo kontaktinformasjonForDoedsbo, String tema) {
		KontaktinformasjonForDoedsbo.KontaktAdresse kontaktAdresse = kontaktinformasjonForDoedsbo.getAdresse();

		if (nonNull(kontaktinformasjonForDoedsbo.getAdvokatSomKontakt())) {
			KontaktinformasjonForDoedsbo.AdvokatSomKontakt advokatSomKontakt = kontaktinformasjonForDoedsbo.getAdvokatSomKontakt();
			return mapMidlertidigPostboksadresse(kontaktAdresse, getAdvokatOrOrgKontaktNavn(advokatSomKontakt.getPersonnavn(), advokatSomKontakt.getOrganisasjonsnavn()));
		} else if (nonNull(kontaktinformasjonForDoedsbo.getPersonSomKontakt())) {
			KontaktinformasjonForDoedsbo.PersonSomKontakt personSomKontakt = kontaktinformasjonForDoedsbo.getPersonSomKontakt();
			return mapMidlertidigPostboksadresse(kontaktAdresse, getPersonSomKontaktNavn(personSomKontakt, tema));
		} else if (nonNull(kontaktinformasjonForDoedsbo.getOrganisasjonSomKontakt()) && nonNull(kontaktAdresse)) {
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

	private PostadresseTo mapMidlertidigPostboksadresse(KontaktinformasjonForDoedsbo.KontaktAdresse adresse, String navn) {
		if (adresse == null) {
			return null;
		}
		return PostadresseTo.builder()
				.adressekilde(KONTAKTINFORMASJONFORDØDSBO)
				.adresseType(POSTADRESSE_INNLAND)
				.adresselinje1(isBlank(navn) ? adresse.getAdresselinje1() : ON_BEHALF_OF + navn)
				.adresselinje2(isBlank(navn) ? adresse.getAdresselinje2() : adresse.getAdresselinje1())
				.adresselinje3(isBlank(navn) ? null : adresse.getAdresselinje2())
				.postnummer(requireNonNull(adresse.getPostnummer(), format(ERROR_MELDING, POSTNUMMER)))
				.poststed(isBlank(adresse.getPoststedsnavn()) ? postnummerService.finnPoststed(adresse.getPostnummer())
						: adresse.getPoststedsnavn())
				.landkode(LANDKODE_NORGE)
				.build();
	}

	private String getAdvokatOrOrgKontaktNavn(KontaktinformasjonForDoedsbo.Personnavn personnavn, String
			organisasjonsnavn) {
		return isNotBlank(getFulltnavnForDoedsbo(personnavn)) ? getFulltnavnForDoedsbo(personnavn) : organisasjonsnavn;
	}

	private String getPersonSomKontaktNavn(KontaktinformasjonForDoedsbo.PersonSomKontakt personSomKontakt, String
			tema) {
		if (nonNull(personSomKontakt.getPersonnavn()) && nonNull(personSomKontakt.getPersonnavn())) {
			return getFulltnavnForDoedsbo(personSomKontakt.getPersonnavn());
		}
		if (isBlank(personSomKontakt.getIdentifikasjonsnummer())) {
			return null;
		}
		return pdlGraphQLConsumer.hentDoedsBoKontaktPersonnavn(personSomKontakt.getIdentifikasjonsnummer(), tema).orElse(null);
	}

	public String getFulltnavnForDoedsbo(KontaktinformasjonForDoedsbo.Personnavn personnavn) {
		if (isNull(personnavn)) {
			return null;
		}
		return trim(getNavn(personnavn.getFornavn()) + getNavn(personnavn.getMellomnavn()) +
				getNavn(personnavn.getEtternavn()));
	}

	public static String getNavn(String navn) {
		return isBlank(navn) ? "" : navn + " ";
	}


	private String getAdresselinje(String adresselinje) {
		return isBlank(adresselinje) ? null : adresselinje;
	}

	private Optional<KontaktinformasjonForDoedsbo> getKontaktForDoedsbo(HentPerson hentPerson) {
		if (isNull(hentPerson.getKontaktinformasjonForDoedsbo())) {
			return Optional.empty();
		}
		return hentPerson.getKontaktinformasjonForDoedsbo().stream().filter(Objects::nonNull).findAny();
	}

}
