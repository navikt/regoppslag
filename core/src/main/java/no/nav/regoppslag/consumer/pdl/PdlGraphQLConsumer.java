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
import static no.nav.regoppslag.metrics.MetricLabels.DOK_CONSUMER;
import static no.nav.regoppslag.metrics.MetricLabels.HENT_NAVN;
import static no.nav.regoppslag.metrics.MetricLabels.HENT_PERSON;
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
	@Metrics(value = DOK_CONSUMER, extraTags = {PROCESS_CODE, HENT_PERSON}, percentiles = {0.5, 0.95}, histogram = true)
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
	@Metrics(value = DOK_CONSUMER, extraTags = {PROCESS_CODE, HENT_NAVN}, percentiles = {0.5, 0.95}, histogram = true)
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
		return PDLRequest.builder().query(query).variables(variables).build();
	}

	private String hentNavn = "query hentPerson($ident: ID!){\n" +
			"  hentPerson(ident: $ident){\n" +
			"    navn(historikk: false){\n" +
			"      fornavn\n" +
			"      mellomnavn\n" +
			"      etternavn\n" +
			"      forkortetNavn\n" +
			"    }\n" +
			"   \n" +
			"  }\n" +
			"}";

	private String hentPerson = "query hentPerson($ident: ID!){\n" +
			"  hentPerson(ident: $ident){\n" +
			"    adressebeskyttelse(historikk: false){\n" +
			"      gradering\n" +
			"    }\n" +
			"    doedsfall{\n" +
			"      doedsdato\n" +
			"    }\n" +
			"    foedsel{\n" +
			"      foedselsaar\n" +
			"      foedselsdato\n" +
			"    }\n" +
			"    navn(historikk: false){\n" +
			"      fornavn\n" +
			"      mellomnavn\n" +
			"      etternavn\n" +
			"      forkortetNavn\n" +
			"    }\n" +
			"    kontaktadresse(historikk: false){\n" +
			"      gyldigFraOgMed\n" +
			"      gyldigTilOgMed\n" +
			"      type\n" +
			"      coAdressenavn\n" +
			"      postboksadresse{\n" +
			"        postbokseier\n" +
			"        postboks\n" +
			"        postnummer\n" +
			"      }\n" +
			"      vegadresse{\n" +
			"        matrikkelId\n" +
			"        husnummer\n" +
			"        husbokstav\n" +
			"        bruksenhetsnummer\n" +
			"        adressenavn\n" +
			"        kommunenummer\n" +
			"        bydelsnummer\n" +
			"        tilleggsnavn\n" +
			"        postnummer\n" +
			"      }\n" +
			"      postadresseIFrittFormat{\n" +
			"        adresselinje1\n" +
			"        adresselinje2\n" +
			"        adresselinje3\n" +
			"        postnummer\n" +
			"      }\n" +
			"      utenlandskAdresse{\n" +
			"        adressenavnNummer\n" +
			"        bygningEtasjeLeilighet\n" +
			"        postboksNummerNavn\n" +
			"        postkode\n" +
			"        bySted\n" +
			"        regionDistriktOmraade\n" +
			"        landkode\n" +
			"      }\n" +
			"      utenlandskAdresseIFrittFormat{\n" +
			"        adresselinje1\n" +
			"        adresselinje2\n" +
			"        adresselinje3\n" +
			"        postkode\n" +
			"        byEllerStedsnavn\n" +
			"        landkode\n" +
			"      }\n" +
			"      metadata{\n" +
			"        opplysningsId\n" +
			"        master\n" +
			"        endringer{\n" +
			"          type\n" +
			"          registrert\n" +
			"          registrertAv\n" +
			"          systemkilde\n" +
			"          kilde\n" +
			"        }\n" +
			"        historisk\n" +
			"      }\n" +
			"    }\n" +
			"    oppholdsadresse(historikk: false){\n" +
			"      gyldigFraOgMed\n" +
			"      gyldigTilOgMed\n" +
			"      coAdressenavn\n" +
			"      utenlandskAdresse{\n" +
			"        adressenavnNummer\n" +
			"        bygningEtasjeLeilighet\n" +
			"        postboksNummerNavn\n" +
			"        postkode\n" +
			"        bySted\n" +
			"        regionDistriktOmraade\n" +
			"        landkode\n" +
			"      }\n" +
			"      vegadresse{\n" +
			"        matrikkelId\n" +
			"        husnummer\n" +
			"        husbokstav\n" +
			"        bruksenhetsnummer\n" +
			"        adressenavn\n" +
			"        kommunenummer\n" +
			"        bydelsnummer\n" +
			"        tilleggsnavn\n" +
			"        postnummer\n" +
			"      }\n" +
			"      matrikkeladresse{\n" +
			"        matrikkelId\n" +
			"        bruksenhetsnummer\n" +
			"        tilleggsnavn\n" +
			"        postnummer\n" +
			"        kommunenummer\n" +
			"      }\n" +
			"      oppholdAnnetSted\n" +
			"      metadata{\n" +
			"        opplysningsId\n" +
			"        master\n" +
			"      }\n" +
			"    }\n" +
			"    kontaktinformasjonForDoedsbo(historikk: false){\n" +
			"      skifteform\n" +
			"      attestutstedelsesdato\n" +
			"      personSomKontakt{\n" +
			"        foedselsdato\n" +
			"        personnavn{\n" +
			"          fornavn\n" +
			"          mellomnavn\n" +
			"          etternavn\n" +
			"        }\n" +
			"        identifikasjonsnummer\n" +
			"      }\n" +
			"      advokatSomKontakt{\n" +
			"        personnavn{\n" +
			"          fornavn\n" +
			"          mellomnavn\n" +
			"          etternavn\n" +
			"        }\n" +
			"        organisasjonsnavn\n" +
			"        organisasjonsnummer\n" +
			"      }\n" +
			"      organisasjonSomKontakt{\n" +
			"        kontaktperson{\n" +
			"          fornavn\n" +
			"          mellomnavn\n" +
			"          etternavn\n" +
			"        }\n" +
			"        organisasjonsnavn\n" +
			"        organisasjonsnummer\n" +
			"      }\n" +
			"      adresse{\n" +
			"        adresselinje1\n" +
			"        adresselinje2\n" +
			"        poststedsnavn\n" +
			"        postnummer\n" +
			"        landkode\n" +
			"      }\n" +
			"      metadata{\n" +
			"        opplysningsId\n" +
			"        master\n" +
			"      }\n" +
			"    }\n" +
			"    sikkerhetstiltak{\n" +
			"      tiltakstype\n" +
			"      beskrivelse\n" +
			"    }\n" +
			"    folkeregisteridentifikator(historikk: false){\n" +
			"      identifikasjonsnummer\n" +
			"      type\n" +
			"      status\n" +
			"    }\n" +
			"    bostedsadresse(historikk: false){\n" +
			"      angittFlyttedato\n" +
			"      gyldigFraOgMed\n" +
			"      gyldigTilOgMed\n" +
			"      coAdressenavn\n" +
			"      vegadresse{\n" +
			"        matrikkelId\n" +
			"        husnummer\n" +
			"        husbokstav\n" +
			"        bruksenhetsnummer\n" +
			"        adressenavn\n" +
			"        kommunenummer\n" +
			"        bydelsnummer\n" +
			"        tilleggsnavn\n" +
			"        postnummer\n" +
			"      }\n" +
			"      utenlandskAdresse{\n" +
			"        adressenavnNummer\n" +
			"        bygningEtasjeLeilighet\n" +
			"        postboksNummerNavn\n" +
			"        postkode\n" +
			"        bySted\n" +
			"        regionDistriktOmraade\n" +
			"        landkode\n" +
			"      },\n" +
			"      matrikkeladresse{\n" +
			"        matrikkelId\n" +
			"        bruksenhetsnummer\n" +
			"        tilleggsnavn\n" +
			"        postnummer\n" +
			"        kommunenummer\n" +
			"      },\n" +
			"      ukjentBosted{\n" +
			"        bostedskommune\n" +
			"      }\n" +
			"      metadata{\n" +
			"        opplysningsId\n" +
			"        master\n" +
			"      }\n" +
			"    }\n" +
			"    folkeregisterpersonstatus(historikk: false){\n" +
			"      status\n" +
			"      forenkletStatus\n" +
			"      folkeregistermetadata{\n" +
			"        kilde\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n";
}
