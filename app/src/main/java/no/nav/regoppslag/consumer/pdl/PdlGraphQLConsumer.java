package no.nav.regoppslag.consumer.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.pdl.pdlresponse.PDLHentPersonResponse;
import no.nav.regoppslag.consumer.stsrest.StsRestConsumer;
import no.nav.regoppslag.exceptions.PdlFunctionalException;
import no.nav.regoppslag.exceptions.PdlHentPersonTechnicalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.time.Duration;
import java.util.HashMap;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class PdlGraphQLConsumer {

    private static final String NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
    private static final String HEADER_PDL_TEMA = "Tema";

    private final RestTemplate restTemplate;
    private final StsRestConsumer stsConsumer;
    private final String pdlUrl;


    @Inject
    public PdlGraphQLConsumer(RestTemplateBuilder restTemplateBuilder, StsRestConsumer stsConsumer, @Value("${pdl.url}") String pdlUrl) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(2L))
                .setReadTimeout(Duration.ofSeconds(5L))
                .build();
        this.stsConsumer = stsConsumer;
        this.pdlUrl = pdlUrl;
    }

    @Retryable(include = HttpServerErrorException.class)
    public PDLHentPersonResponse hentPerson(final String aktoerId, final String tema) {
        try {
            final UriComponents uri = UriComponentsBuilder.fromHttpUrl(pdlUrl).build();
            final String serviceUserToken = "Bearer " + stsConsumer.getOidcToken();
            final RequestEntity<PDLRequest> requestEntity = RequestEntity.post(uri.toUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, serviceUserToken)
                    .header(NAV_CONSUMER_TOKEN, serviceUserToken)
                    .header(HEADER_PDL_TEMA, tema)
                    .body(mapRequest(aktoerId));

            log.debug("Henter personinfo for aktørId={}", aktoerId);

            final PDLHentPersonResponse response = requireNonNull(restTemplate.exchange(requestEntity, PDLHentPersonResponse.class).getBody());

            return null;
        } catch (HttpClientErrorException e) {
            throw new PdlFunctionalException("Kunne ikke hente person fra pdl.", e);
        } catch (HttpServerErrorException e) {
            throw new PdlHentPersonTechnicalException("Teknisk feil ved kall mot PDL.", e);
        }

    }


    private PDLRequest mapRequest(final String aktoerId) {
        final HashMap<String, Object> variables = new HashMap<>();
        variables.put("ident", aktoerId);

        return PDLRequest.builder().query("query hentPerson($ident: ID!){\n" +
                "  hentPerson(ident: $ident){\n" +
                "    adressebeskyttelse(historikk: false){\n" +
                "    \tgradering\n" +
                "    }\n" +
                "    doedsfall{\n" +
                "        doedsdato\n" +
                "      }\n" +
                "  foedsel{\n" +
                "    foedselsaar\n" +
                "    foedselsdato\n" +
                "  }\n" +
                "    navn(historikk: false){\n" +
                "      fornavn\n" +
                "      mellomnavn\n" +
                "      etternavn\n" +
                "      forkortetNavn\n" +
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
                "  }\n" +
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
                "    }\n" +
                "    kontaktinformasjonForDoedsbo(historikk: false){\n" +
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
                "    organisasjonSomKontakt{\n" +
                "    kontaktperson{\n" +
                "      fornavn\n" +
                "      mellomnavn\n" +
                "      etternavn\n" +
                "    }\n" +
                "    organisasjonsnavn\n" +
                "    organisasjonsnummer\n" +
                "    }\n" +
                "    adresse{\n" +
                "      adresselinje1\n" +
                "      adresselinje2\n" +
                "      poststedsnavn\n" +
                "      postnummer\n" +
                "      landkode\n" +
                "    }\n" +
                "  }\n" +
                "    sikkerhetstiltak{\n" +
                "      tiltakstype\n" +
                "      beskrivelse\n" +
                "    }\n" +
                "    folkeregisteridentifikator(historikk: false){\n" +
                "      identifikasjonsnummer\n" +
                "      type\n" +
                "      status\n" +
                "    }\n" +
                "    deltBosted(historikk: false){\n" +
                "      startdatoForKontrakt\n" +
                "      sluttdatoForKontrakt\n" +
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
                "      }\n" +
                "      ukjentBosted{\n" +
                "    \t\tbostedskommune\n" +
                "      }\n" +
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
                "      }\n" +
                "      ukjentBosted{\n" +
                "        bostedskommune\n" +
                "      }\n" +
                "    }\n" +
                "    folkeregisterpersonstatus(historikk: false){\n" +
                "      status\n" +
                "    \tforenkletStatus\n" +
                "    }\n" +
                "    \n" +
                "  tilrettelagtKommunikasjon{\n" +
                "     tegnspraaktolk{\n" +
                "    \tspraak\n" +
                "    }\n" +
                "     talespraaktolk{\n" +
                "    spraak\n" +
                "  }\n" +
                "  }\n" +
                "  }\n" +
                "}\n").variables(variables).build();
    }


}
