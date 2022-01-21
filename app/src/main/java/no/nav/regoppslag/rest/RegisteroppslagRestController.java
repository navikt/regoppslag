package no.nav.regoppslag.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.metrics.Metrics;
import no.nav.regoppslag.rreg003.PostadresseRequest;
import no.nav.regoppslag.rreg003.PostadresseResponse;
import no.nav.regoppslag.rreg003.PostadresseService;
import no.nav.regoppslag.treg001.KompletterBrevdataRequest;
import no.nav.regoppslag.treg001.KompletterBrevdataResponse;
import no.nav.regoppslag.treg001.KompletterBrevdataService;
import no.nav.regoppslag.treg001.xmlenricher.exceptions.MarshallerTechnicalException;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseService;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

import static no.nav.regoppslag.config.springdoc.SpringDoc.samlTokenInfo;
import static no.nav.regoppslag.metrics.MetricLabels.COMPONENT;
import static no.nav.regoppslag.metrics.MetricLabels.DOK_REQUEST;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_RREG003;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */

@RestController
@RequestMapping(REST)
@Tag(name = "Registeroppslag", description = "Tjeneste for å hente postadresse og komplettere brevskjema")
@Slf4j
public class RegisteroppslagRestController {

	public static final String REST = "rest/";
	public static final String KOMPLETTER_BREVDATA_URI_PATH = "kompletterBrevdata";
	public static final String HENT_MOTTAKEROGADRESSE_URI_PATH = "hentMottakerOgAdresse";
	public static final String POSTADRESSE_URI_PATH = "postadresse";

	private final KompletterBrevdataService kompletterBrevdataService;
	private final HentMottakerOgAdresseService hentMottakerOgAdresseService;
	private final PostadresseService postadresseService;

	@Inject
	public RegisteroppslagRestController(KompletterBrevdataService kompletterBrevdataService,
										 HentMottakerOgAdresseService hentMottakerOgAdresseService,
										 PostadresseService postadresseService) {
		this.kompletterBrevdataService = kompletterBrevdataService;
		this.hentMottakerOgAdresseService = hentMottakerOgAdresseService;
		this.postadresseService = postadresseService;
	}

	@Operation(summary = "TREG001", description = "Denne tjenesten tar brevdata i XML format som input og beriker elementene med data fra registere ved å benytte Berikerplugins.<br/><br/>" + samlTokenInfo)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "400", description = "Ugyldig input. Denne feilen vil returneres hvis det feil i input verdiene, eller om det mangler SAML token når mottakertype=PERSON"),
			@ApiResponse(responseCode = "401", description = "Ingen tilgang til PersonV3"),
			@ApiResponse(responseCode = "500", description = "Teknisk feil")
	})
	@PostMapping(value = KOMPLETTER_BREVDATA_URI_PATH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Metrics(value = DOK_REQUEST, extraTags = {SERVICE, SERVICE_CODE_TREG001, COMPONENT, "kompletterBrevdata"}, percentiles = {0.5, 0.95}, histogram = true, countExceptions = true)
	public @ResponseBody
	KompletterBrevdataResponse kompletterBrevdata(@RequestBody KompletterBrevdataRequest requestBody)
			throws RegOppslagSecurityException {

		log.info(String.format("TREG001 Har mottatt kall om å komplettere brevdata. DokumenttypeId=%s", requestBody.getDokumentTypeId()));

		try {
			KompletterBrevdataResponse response = kompletterBrevdataService.hentBrevdataFraRegistre(requestBody);
			log.info(String.format("TREG001 Er ferdig med å komplettere brevdata. DokumenttypeId=%s", requestBody.getDokumentTypeId()));
			return response;

		} catch (MarshallerTechnicalException e) {
			//Logger error hvis retry ikke fungerer
			log.error("TREG001 Teknisk marshaller feil: " + e.getMessage(), e);
			throw e;
		} finally {
			SecurityContextHolder.clearContext();
			MDC.clear();
		}
	}

	@Operation(summary = "TREG002", description = "Dette er en domenetjeneste som kan brukes for å hente mottakernavn og adresse slik at konsumenter kun trenger å sende inn mottakerId.<br/><br/>" + samlTokenInfo)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "400", description = "Ugyldig input. Denne feilen vil returneres hvis det feil i input verdiene, eller om det mangler SAML token når type=PERSON"),
			@ApiResponse(responseCode = "401", description = "Ingen tilgang til PersonV3"),
			@ApiResponse(responseCode = "404", description = "Bruker har ukjent adresse"),
			@ApiResponse(responseCode = "410", description = "Person er død og har ukjent adresse"),
			@ApiResponse(responseCode = "500", description = "Teknisk feil")
	})
	@PostMapping(value = HENT_MOTTAKEROGADRESSE_URI_PATH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Metrics(value = DOK_REQUEST, extraTags = {SERVICE, SERVICE_CODE_TREG002, COMPONENT, "hentMottakerOgAdresse"}, percentiles = {0.5, 0.95}, histogram = true, countExceptions = true)
	public @ResponseBody
	HentMottakerOgAdresseResponse hentMottakerOgAdresse(@RequestBody HentMottakerOgAdresseRequest requestBody)
			throws RegOppslagSecurityException {

		try {
			log.info(String.format("TREG002 Henter mottaker og addresse. MottakerType=%s", requestBody.getType()));
			HentMottakerOgAdresseResponse response = hentMottakerOgAdresseService.hentMottakerOgAdresseInfo(requestBody);
			log.info(String.format("TREG002 Har hentet mottaker og adresse. MottakerType=%s", requestBody
					.getType()));
			return response;
		} finally {
			SecurityContextHolder.clearContext();
			MDC.clear();
		}
	}

	@Operation(summary = "RREG003", description = "Dette er en domenetjeneste som kan brukes for å hente postadresse slik at konsumenter kun trenger å sende inn mottakerId.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "400", description = "Ugyldig input. Denne feilen vil returneres hvis det feil i input verdiene, eller om det mangler SAML token når type=PERSON"),
			@ApiResponse(responseCode = "401", description = "Ingen tilgang til PersonV3"),
			@ApiResponse(responseCode = "404", description = "Bruker har ukjent adresse"),
			@ApiResponse(responseCode = "410", description = "Person er død og har ukjent adresse"),
			@ApiResponse(responseCode = "500", description = "Teknisk feil")
	})
	@PostMapping(value = POSTADRESSE_URI_PATH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Metrics(value = DOK_REQUEST, extraTags = {SERVICE, SERVICE_CODE_RREG003, COMPONENT, "postadresse"}, percentiles = {0.5, 0.95}, histogram = true, countExceptions = true)
	public @ResponseBody
	PostadresseResponse postadresse(@RequestBody PostadresseRequest requestBody)
			throws RegOppslagSecurityException {

		try {
			log.info("RREG003 Henter postaddresse.");
			PostadresseResponse response = postadresseService.postadresseInfo(requestBody);
			log.info("RREG003 Har hentet postadresse.");
			return response;
		} finally {
			SecurityContextHolder.clearContext();
			MDC.clear();
		}
	}
}
