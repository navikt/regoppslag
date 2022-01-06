package no.nav.regoppslag.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.api.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.api.KompletterBrevdataRequest;
import no.nav.regoppslag.api.KompletterBrevdataResponse;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.metrics.Metrics;
import no.nav.regoppslag.treg001.KompletterBrevdataService;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseService;
import no.nav.regoppslag.xmlenricher.exceptions.MarshallerTechnicalException;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

import static no.nav.regoppslag.config.swagger.SwaggerConfig.samlTokenInfo;
import static no.nav.regoppslag.metrics.MetricLabels.COMPONENT;
import static no.nav.regoppslag.metrics.MetricLabels.DOK_REQUEST;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */

@RestController
@RequestMapping(REST)
@Api(value = "Registeroppslag")
@Slf4j
public class RegisteroppslagRestController {

	public static final String REST = "rest/";
	public static final String KOMPLETTER_BREVDATA_URI_PATH = "kompletterBrevdata";
	public static final String HENT_MOTTAKEROGADRESSE_URI_PATH = "hentMottakerOgAdresse";

	private final KompletterBrevdataService kompletterBrevdataService;
	private final HentMottakerOgAdresseService hentMottakerOgAdresseService;

	@Inject
	public RegisteroppslagRestController(KompletterBrevdataService kompletterBrevdataService, HentMottakerOgAdresseService hentMottakerOgAdresseService) {
		this.kompletterBrevdataService = kompletterBrevdataService;
		this.hentMottakerOgAdresseService = hentMottakerOgAdresseService;
	}

	@ApiOperation(value = "TREG001", notes = "Denne tjenesten tar brevdata i XML format som input og beriker elementene med data fra registere ved å benytte Berikerplugins.<br/><br/>" + samlTokenInfo)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 400, message = "Ugyldig input. Denne feilen vil returneres hvis det feil i input verdiene, eller om det mangler SAML token når mottakertype=PERSON"),
			@ApiResponse(code = 401, message = "Ingen tilgang til PersonV3"),
			@ApiResponse(code = 500, message = "Teknisk feil")
	})
	@PostMapping(value = KOMPLETTER_BREVDATA_URI_PATH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Metrics(value = DOK_REQUEST, extraTags = {SERVICE, SERVICE_CODE_TREG001, COMPONENT, "kompletterBrevdata"}, percentiles = {0.5, 0.95}, histogram = true, countExceptions = true)
	public @ResponseBody KompletterBrevdataResponse kompletterBrevdata(@RequestBody KompletterBrevdataRequest requestBody)
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

	@ApiOperation(value = "TREG002", notes = "Dette er en domenetjeneste som kan brukes for å hente mottakernavn og adresse slik at konsumenter kun trenger å sende inn mottakerId.<br/><br/>" + samlTokenInfo)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 400, message = "Ugyldig input. Denne feilen vil returneres hvis det feil i input verdiene, eller om det mangler SAML token når type=PERSON"),
			@ApiResponse(code = 401, message = "Ingen tilgang til PersonV3"),
			@ApiResponse(code = 404, message = "Bruker har ukjent adresse"),
			@ApiResponse(code = 410, message = "Person er død og har ukjent adresse"),
			@ApiResponse(code = 500, message = "Teknisk feil")
	})
	@PostMapping(value = HENT_MOTTAKEROGADRESSE_URI_PATH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Metrics(value = DOK_REQUEST, extraTags = {SERVICE, SERVICE_CODE_TREG002, COMPONENT, "hentMottakerOgAdresse"}, percentiles = {0.5, 0.95}, histogram = true, countExceptions = true)
	public @ResponseBody HentMottakerOgAdresseResponse hentMottakerOgAdresse(@RequestBody HentMottakerOgAdresseRequest requestBody)
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
}
