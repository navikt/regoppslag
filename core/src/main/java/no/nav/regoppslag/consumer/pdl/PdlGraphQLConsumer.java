package no.nav.regoppslag.consumer.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.pdl.map.MapHentNavnResponse;
import no.nav.regoppslag.consumer.pdl.to.HentPerson;
import no.nav.regoppslag.consumer.pdl.to.PDLHentNavnResponse;
import no.nav.regoppslag.consumer.pdl.to.PDLHentPersonResponse;
import no.nav.regoppslag.consumer.pdl.to.PDLRequest;
import no.nav.regoppslag.consumer.stsrest.StsRestConsumer;
import no.nav.regoppslag.exceptions.PdlFunctionalException;
import no.nav.regoppslag.exceptions.PdlHentPersonTechnicalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.Metrics;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static no.nav.regoppslag.config.cache.CacheConfig.HENT_BRUKER_NAVN;
import static no.nav.regoppslag.config.cache.CacheConfig.HENT_BRUKER_PERSONDATA;
import static no.nav.regoppslag.metrics.MetricLabels.DOK_CONSUMER;
import static no.nav.regoppslag.metrics.MetricLabels.PROCESS_CODE;
import static no.nav.regoppslag.util.MDCConstants.CALL_ID;
import static no.nav.regoppslag.util.MDCConstants.NAV_CALL_ID;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class PdlGraphQLConsumer {

	private static final String NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
	private static final String HEADER_PDL_TEMA = "Tema";

	private final RestTemplate restTemplate;
	private final StsRestConsumer stsConsumer;
	private final String pdlUrl;
	private final MapHentNavnResponse mapHentNavnResponse;

	@Autowired
	public PdlGraphQLConsumer(RestTemplateBuilder restTemplateBuilder,
							  StsRestConsumer stsConsumer, @Value("${pdl.url}") String pdlUrl) {
		this.restTemplate = restTemplateBuilder
				.setConnectTimeout(Duration.ofSeconds(5L))
				.setReadTimeout(Duration.ofSeconds(15L))
				.build();
		this.stsConsumer = stsConsumer;
		this.pdlUrl = pdlUrl;
		this.mapHentNavnResponse = new MapHentNavnResponse();
	}

	@Retryable(include = RegOppslagTechnicalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	@Metrics(value = DOK_CONSUMER, extraTags = {PROCESS_CODE, HENT_BRUKER_PERSONDATA}, percentiles = {0.5, 0.95}, histogram = true)
	public HentPerson hentPerson(final String aktoerId, final String tema) {
		try {
			RequestEntity<PDLRequest> requestEntity = createRequestEntity(aktoerId, tema, hentPerson);

			final PDLHentPersonResponse response = requireNonNull(restTemplate.exchange(requestEntity, PDLHentPersonResponse.class).getBody());

			if (nonNull(response.getErrors()) && !response.getErrors().isEmpty()) {
				log.warn("Kunne ikke hente person fra Pdl. Feilmeldinger={}", response.getErrors());
				throw new PdlFunctionalException("Kunne ikke hente person fra Pdl" + response.getErrors(), null);
			}
			return nonNull(response.getData()) ? response.getData().getHentPerson() : null;
		} catch (HttpClientErrorException e) {
			throw new PdlFunctionalException("Kunne ikke hente person fra pdl.", e, "PDL", e.getStatusCode());
		} catch (HttpServerErrorException e) {
			throw new PdlHentPersonTechnicalException("Teknisk feil ved kall mot PDL.", e);
		}
	}


	public PDLHentNavnResponse hentPersonnavn(final String aktoerId, final String tema) {
		RequestEntity<PDLRequest> requestEntity = createRequestEntity(aktoerId, tema, hentNavn);
		return requireNonNull(restTemplate.exchange(requestEntity, PDLHentNavnResponse.class).getBody());
	}

	@Retryable(include = RegOppslagTechnicalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	@Metrics(value = DOK_CONSUMER, extraTags = {PROCESS_CODE, HENT_BRUKER_NAVN}, percentiles = {0.5, 0.95}, histogram = true)
	public String hentNavn(final String aktoerId, final String tema) {
		try {
			final PDLHentNavnResponse response = hentPersonnavn(aktoerId, tema);

			if (nonNull(response.getErrors()) && !response.getErrors().isEmpty()) {
				throw new PdlFunctionalException("Kunne ikke hente person fra Pdl" + response.getErrors(), null);
			}
			return mapHentNavnResponse.mapNavn(response);
		} catch (HttpClientErrorException e) {
			throw new PdlFunctionalException("Kunne ikke hente person fra pdl.", e, "PDL", e.getStatusCode());
		} catch (HttpServerErrorException e) {
			throw new PdlHentPersonTechnicalException("Teknisk feil ved kall mot PDL.", e);
		}
	}


	@Retryable(include = RegOppslagTechnicalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	@Metrics(value = DOK_CONSUMER, extraTags = {PROCESS_CODE, "hentDoedsBoKontaktPersonnavn"}, percentiles = {0.5, 0.95}, histogram = true)
	public Optional<String> hentDoedsBoKontaktPersonnavn(final String aktoerId, final String tema) {
		try {
			final PDLHentNavnResponse response = hentPersonnavn(aktoerId, tema);
			return (nonNull(response.getErrors()) && !response.getErrors().isEmpty()) ? null : Optional.ofNullable(mapHentNavnResponse.mapNavnForDoedsbo(response));
		} catch (HttpClientErrorException e) {
			return Optional.empty();
		} catch (HttpServerErrorException e) {
			throw new PdlHentPersonTechnicalException("Teknisk feil ved kall mot PDL.", e);
		}
	}

	private RequestEntity<PDLRequest> createRequestEntity(String aktoerId, String tema, String query) {
		final UriComponents uri = UriComponentsBuilder.fromHttpUrl(pdlUrl).build();
		final String serviceUserToken = "Bearer " + stsConsumer.getOidcToken();
		return RequestEntity.post(uri.toUri())
				.accept(APPLICATION_JSON)
				.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.header(AUTHORIZATION, serviceUserToken)
				.header(NAV_CONSUMER_TOKEN, serviceUserToken)
				.header(HEADER_PDL_TEMA, tema)
				.header(NAV_CALL_ID, MDC.get(CALL_ID))
				.body(mapRequest(aktoerId, query));
	}

	private PDLRequest mapRequest(final String aktoerId, String query) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("ident", aktoerId);
		return new PDLRequest(query, variables);
	}

	private final String hentNavn = """
			query hentPerson($ident: ID!){
			  hentPerson(ident: $ident){
			    navn(historikk: false){
			      fornavn
			      mellomnavn
			      etternavn
			      forkortetNavn
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
			    foedsel{
			      foedselsaar
			      foedselsdato
			    }
			    navn(historikk: false){
			      fornavn
			      mellomnavn
			      etternavn
			      forkortetNavn
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
			    sikkerhetstiltak{
			      tiltakstype
			      beskrivelse
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
