package no.nav.regoppslag.consumer.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.config.properties.RegoppslagProperties;
import no.nav.regoppslag.config.properties.RegoppslagProperties.Oauth2SecuredEndpoint;
import no.nav.regoppslag.consumer.AzureFlowInterceptor;
import no.nav.regoppslag.consumer.azure.AzureTokenConsumer;
import no.nav.regoppslag.consumer.pdl.map.MapHentNavnResponse;
import no.nav.regoppslag.consumer.pdl.to.HentPerson;
import no.nav.regoppslag.consumer.pdl.to.PDLError;
import no.nav.regoppslag.consumer.pdl.to.PDLHentNavnResponse;
import no.nav.regoppslag.consumer.pdl.to.PDLHentPersonResponse;
import no.nav.regoppslag.consumer.pdl.to.PDLRequest;
import no.nav.regoppslag.exceptions.PdlFunctionalException;
import no.nav.regoppslag.exceptions.PdlHentPersonTechnicalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.exceptions.RegOppslagIngenTilgangException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static no.nav.regoppslag.util.MDCConstants.CALL_ID;
import static no.nav.regoppslag.util.NavHeaders.NAV_CALL_ID;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class PdlGraphQLConsumer {

	private static final String HEADER_PDL_TEMA = "Tema";
	// https://pdldocs-navno.msappproxy.net/ekstern/index.html#_dokumenter_hjemmel_vha_tema
	private static final String HEADER_PDL_BEHANDLINGSNUMMER = "behandlingsnummer";
	// https://behandlingskatalog.nais.adeo.no/process/purpose/ARKIVPLEIE/756fd557-b95e-4b20-9de9-6179fb8317e6
	private static final String ARKIVPLEIE_BEHANDLINGSNUMMER = "B315";
	private static final String PDL_ERROR_EXTENSION_CODE_NOT_FOUND = "not_found";
	private static final String PDL_ERROR_EXTENSION_CODE_UNAUTHORIZED = "unauthorized";

	private final RestTemplate restTemplate;
	private final MapHentNavnResponse mapHentNavnResponse;
	private final Oauth2SecuredEndpoint pdl;

	@Autowired
	public PdlGraphQLConsumer(RestTemplateBuilder restTemplateBuilder,
							  AzureTokenConsumer azureTokenConsumer,
							  RegoppslagProperties regoppslagProperties) {
		this.pdl = regoppslagProperties.getEndpoints().getPdl();
		this.restTemplate = restTemplateBuilder
				.setConnectTimeout(Duration.ofSeconds(5L))
				.setReadTimeout(Duration.ofSeconds(15L))
				.additionalInterceptors(new AzureFlowInterceptor(azureTokenConsumer, pdl.getScope()))
				.build();
		this.mapHentNavnResponse = new MapHentNavnResponse();
	}

	@Retryable(retryFor = RegOppslagTechnicalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public HentPerson hentPerson(final String aktoerId, final String tema) {
		try {
			RequestEntity<PDLRequest> requestEntity = createRequestEntity(aktoerId, tema, hentPerson);
			final PDLHentPersonResponse response = requireNonNull(restTemplate.exchange(requestEntity, PDLHentPersonResponse.class).getBody());
			handterPdlFunksjonellFeil(response);
			return nonNull(response.getData()) ? response.getData().getHentPerson() : null;
		} catch (HttpClientErrorException e) {
			throw new PdlFunctionalException("Kunne ikke hente person fra pdl.", e, e.getStatusCode());
		} catch (HttpServerErrorException e) {
			throw new PdlHentPersonTechnicalException("Teknisk feil ved kall mot PDL.", e);
		}
	}

	public PDLHentNavnResponse hentPersonnavn(final String aktoerId, final String tema) {
		RequestEntity<PDLRequest> requestEntity = createRequestEntity(aktoerId, tema, hentNavn);
		return requireNonNull(restTemplate.exchange(requestEntity, PDLHentNavnResponse.class).getBody());
	}

	@Retryable(retryFor = RegOppslagTechnicalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public String hentNavn(final String aktoerId, final String tema) {
		try {
			final PDLHentNavnResponse response = hentPersonnavn(aktoerId, tema);
			handterPdlFunksjonellFeil(response);
			return mapHentNavnResponse.mapNavn(response);
		} catch (HttpClientErrorException e) {
			throw new PdlFunctionalException("Kunne ikke hente person fra pdl.", e, e.getStatusCode());
		} catch (HttpServerErrorException e) {
			throw new PdlHentPersonTechnicalException("Teknisk feil ved kall mot PDL.", e);
		}
	}

	@Retryable(retryFor = RegOppslagTechnicalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public Optional<String> hentDoedsBoKontaktPersonnavn(final String aktoerId, final String tema) {
		try {
			final PDLHentNavnResponse response = hentPersonnavn(aktoerId, tema);
			handterPdlFunksjonellFeil(response);
			return Optional.ofNullable(mapHentNavnResponse.mapNavnForDoedsbo(response));
		} catch (HttpClientErrorException e) {
			return Optional.empty();
		} catch (HttpServerErrorException e) {
			throw new PdlHentPersonTechnicalException("Teknisk feil ved kall mot PDL.", e);
		}
	}

	private RequestEntity<PDLRequest> createRequestEntity(String aktoerId, String tema, String query) {
		final UriComponents uri = UriComponentsBuilder.fromHttpUrl(pdl.getUrl()).build();
		return RequestEntity.post(uri.toUri())
				.accept(APPLICATION_JSON)
				.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.header(HEADER_PDL_TEMA, tema)
				.header(HEADER_PDL_BEHANDLINGSNUMMER, ARKIVPLEIE_BEHANDLINGSNUMMER)
				.header(NAV_CALL_ID, MDC.get(CALL_ID))
				.body(mapRequest(aktoerId, query));
	}

	private PDLRequest mapRequest(final String aktoerId, String query) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("ident", aktoerId);
		return new PDLRequest(query, variables);
	}

	private void handterPdlFunksjonellFeil(PDLHentPersonResponse response) {
		List<PDLError> errors = response.getErrors();
		handterPdlFunksjonellFeil(errors);
	}

	private void handterPdlFunksjonellFeil(PDLHentNavnResponse response) {
		List<PDLError> errors = response.getErrors();
		handterPdlFunksjonellFeil(errors);
	}

	private void handterPdlFunksjonellFeil(List<PDLError> errors) {
		if (nonNull(errors) && !errors.isEmpty()) {
			Optional<PDLError> pdlUnauthorized = errors.stream()
					.filter(p -> PDL_ERROR_EXTENSION_CODE_UNAUTHORIZED.equals(p.getExtensions().getCode()))
					.findFirst();
			if (pdlUnauthorized.isPresent()) {
				PDLError pdlError = pdlUnauthorized.get();
				throw new RegOppslagIngenTilgangException("Ingen tilgang til å se data om person. Avvist av policy=" + pdlError.getExtensions().getDetails().getPolicy(), FORBIDDEN);
			}

			Optional<PDLError> pdlNotFound = errors.stream()
					.filter(p -> PDL_ERROR_EXTENSION_CODE_NOT_FOUND.equals(p.getExtensions().getCode()))
					.findFirst();
			if (pdlNotFound.isPresent()) {
				throw new RegOppslagIkkeFunnetException("Fant ikke person i PDL. " + pdlNotFound.get(), NOT_FOUND);
			}
			log.warn("Kunne ikke hente person fra Pdl. Feilmeldinger={}", errors);
			throw new PdlFunctionalException("Kunne ikke hente person fra Pdl " + errors, null);
		}
	}

	private final String hentNavn = """
			query hentPerson($ident: ID!){
			  hentPerson(ident: $ident){
			    navn(historikk: false){
			      fornavn
			      mellomnavn
			      etternavn
			      forkortetNavn
			      gyldigFraOgMed
			      metadata{
			        opplysningsId
			        master
			        endringer{
			          type
			          registrert
			        }
			      }
			    }
			  }
			}""";

	private final String hentPerson = """
			query hentPerson($ident: ID!){
			  hentPerson(ident: $ident){
			    adressebeskyttelse(historikk: false){
			      gradering
			    }
			    doedsfall{
			      doedsdato
			    }
			    navn(historikk: false){
			      fornavn
			      mellomnavn
			      etternavn
			      forkortetNavn
			      gyldigFraOgMed
			      metadata{
			        opplysningsId
			        master
			        endringer{
			          type
			          registrert
			        }
			      }
			    }
			    kontaktadresse(historikk: false){
			      gyldigFraOgMed
			      gyldigTilOgMed
			      type
			      coAdressenavn
			      postboksadresse{
			        postbokseier
			        postboks
			        postnummer
			      }
			      vegadresse{
			        matrikkelId
			        husnummer
			        husbokstav
			        bruksenhetsnummer
			        adressenavn
			        kommunenummer
			        bydelsnummer
			        tilleggsnavn
			        postnummer
			      }
			      postadresseIFrittFormat{
			        adresselinje1
			        adresselinje2
			        adresselinje3
			        postnummer
			      }
			      utenlandskAdresse{
			        adressenavnNummer
			        bygningEtasjeLeilighet
			        postboksNummerNavn
			        postkode
			        bySted
			        regionDistriktOmraade
			        landkode
			      }
			      utenlandskAdresseIFrittFormat{
			        adresselinje1
			        adresselinje2
			        adresselinje3
			        postkode
			        byEllerStedsnavn
			        landkode
			      }
			      metadata{
			        opplysningsId
			        master
			        endringer{
			          type
			          registrert
			          registrertAv
			          systemkilde
			          kilde
			        }
			        historisk
			      }
			    }
			    oppholdsadresse(historikk: false){
			      gyldigFraOgMed
			      gyldigTilOgMed
			      coAdressenavn
			      utenlandskAdresse{
			        adressenavnNummer
			        bygningEtasjeLeilighet
			        postboksNummerNavn
			        postkode
			        bySted
			        regionDistriktOmraade
			        landkode
			      }
			      vegadresse{
			        matrikkelId
			        husnummer
			        husbokstav
			        bruksenhetsnummer
			        adressenavn
			        kommunenummer
			        bydelsnummer
			        tilleggsnavn
			        postnummer
			      }
			      metadata{
			        opplysningsId
			        master
			        endringer{
			          type
			          registrert
			        }
			      }
			      matrikkeladresse{
			        matrikkelId
			        bruksenhetsnummer
			        tilleggsnavn
			        postnummer
			        kommunenummer
			      }
			      oppholdAnnetSted
			      metadata{
			        opplysningsId
			        master
			      }
			    }
			    kontaktinformasjonForDoedsbo(historikk: false){
			      skifteform
			      attestutstedelsesdato
			      personSomKontakt{
			        foedselsdato
			        personnavn{
			          fornavn
			          mellomnavn
			          etternavn
			        }
			        identifikasjonsnummer
			      }
			      advokatSomKontakt{
			        personnavn{
			          fornavn
			          mellomnavn
			          etternavn
			        }
			        organisasjonsnavn
			        organisasjonsnummer
			      }
			      organisasjonSomKontakt{
			        kontaktperson{
			          fornavn
			          mellomnavn
			          etternavn
			        }
			        organisasjonsnavn
			        organisasjonsnummer
			      }
			      adresse{
			        adresselinje1
			        adresselinje2
			        poststedsnavn
			        postnummer
			        landkode
			      }
			      metadata{
			        opplysningsId
			        master
			      }
			    }
			    folkeregisteridentifikator(historikk: false){
			      identifikasjonsnummer
			      type
			      status
			    }
			    bostedsadresse(historikk: false){
			      angittFlyttedato
			      gyldigFraOgMed
			      gyldigTilOgMed
			      coAdressenavn
			      vegadresse{
			        matrikkelId
			        husnummer
			        husbokstav
			        bruksenhetsnummer
			        adressenavn
			        kommunenummer
			        bydelsnummer
			        tilleggsnavn
			        postnummer
			      }
			      utenlandskAdresse{
			        adressenavnNummer
			        bygningEtasjeLeilighet
			        postboksNummerNavn
			        postkode
			        bySted
			        regionDistriktOmraade
			        landkode
			      },
			      matrikkeladresse{
			        matrikkelId
			        bruksenhetsnummer
			        tilleggsnavn
			        postnummer
			        kommunenummer
			      },
			      ukjentBosted{
			        bostedskommune
			      }
			      metadata{
			        opplysningsId
			        master
			        endringer{
			          type
			          registrert
			        }
			      }
			    }
			    folkeregisterpersonstatus(historikk: false){
			      status
			      forenkletStatus
			      folkeregistermetadata{
			        kilde
			      }
			    }
			  }
			}
			""";
}
