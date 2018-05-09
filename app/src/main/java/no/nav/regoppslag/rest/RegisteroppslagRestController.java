package no.nav.regoppslag.rest;

import static no.nav.regoppslag.config.swagger.SwaggerConfig.samlTokenInfo;
import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_FUNCTIONAL_EXCEPTION;
import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_SECURITY_EXCEPTION;
import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_TECHNICAL_EXCEPTION;
import static no.nav.regoppslag.metrics.PrometheusLabels.PROCESSED_OK;
import static no.nav.regoppslag.metrics.PrometheusLabels.RECEIVED;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestExceptionCounter;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestLatency;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;

import io.prometheus.client.Histogram;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.nav.regoppslag.common.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.common.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.common.KompletterBrevdataRequest;
import no.nav.regoppslag.common.KompletterBrevdataResponse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.PrometheusLabels;
import no.nav.regoppslag.treg001.KompletterBrevdataService;
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

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */

@RestController
@RequestMapping(REST)
@Api(value = "Registeroppslag")
public class RegisteroppslagRestController {
	
	
	public static final String REST = "rest/";
	public static final String KOMPLETTER_BREVDATA_URI_PATH = "kompletterBrevdata";
	public static final String HENT_MOTTAKEROGADRESSE_URI_PATH = "hentMottakerOgAdresse";
	
	private final KompletterBrevdataService kompletterBrevdataService;
	private final HentMottakerOgAdresseService hentMottakerOgAdresseService;
	private Histogram.Timer requestTimer;
	
	@Inject
	public RegisteroppslagRestController(KompletterBrevdataService kompletterBrevdataService, HentMottakerOgAdresseService hentMottakerOgAdresseService) {
		this.kompletterBrevdataService = kompletterBrevdataService;
		this.hentMottakerOgAdresseService = hentMottakerOgAdresseService;
	}
	
	@ApiOperation(value = "TREG001", notes = "Denne tjenesten tar brevdata i XML format som input og beriker elementene med data fra registere ved å benytte Berikerplugins.<br/><br/>" + samlTokenInfo)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 401, message = "Ingen tilgang til PersonV3"),
			@ApiResponse(code = 400, message = "Ugyldig input. Denne feilen vil returneres hvis det feil i input verdiene, eller om det mangler SAML token når mottakertype=PERSON"),
			@ApiResponse(code = 500, message = "Teknisk feil")
	})
	@PostMapping(value = KOMPLETTER_BREVDATA_URI_PATH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	KompletterBrevdataResponse validerOgKompletterBrevdata(@RequestBody KompletterBrevdataRequest requestBody)
			throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		
		requestTimer = requestLatency.labels(SERVICE_CODE_TREG001, SERVICE_CODE_TREG001, "validerOgKompletterBrevdata")
				.startTimer();
		
		try {
			requestCounter.labels(SERVICE_CODE_TREG001, SERVICE_CODE_TREG001, PrometheusLabels.REST, getConsumerId(), RECEIVED)
					.inc();
			KompletterBrevdataResponse response = kompletterBrevdataService.hentBrevdataFraRegistre(requestBody);
			requestCounter.labels(SERVICE_CODE_TREG001, SERVICE_CODE_TREG001, PrometheusLabels.REST, getConsumerId(), PROCESSED_OK)
					.inc();
			return response;
		} catch (Exception e) {
			incrementExceptionMetrics(e, SERVICE_CODE_TREG001);
			throw e;
		} finally {
			requestTimer.observeDuration();
			SecurityContextHolder.clearContext();
			MDC.clear();
		}
	}
	
	@ApiOperation(value = "TREG002", notes = "Dette er en domenetjeneste som kan brukes for å hente mottakernavn og adresse slik at konsumenter kun trenger å sende inn mottakerId.<br/><br/>" + samlTokenInfo)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 401, message = "Ingen tilgang til PersonV3"),
			@ApiResponse(code = 400, message = "Ugyldig input. Denne feilen vil returneres hvis det feil i input verdiene, eller om det mangler SAML token når type=PERSON"),
			@ApiResponse(code = 500, message = "Teknisk feil")
	})
	@PostMapping(value = HENT_MOTTAKEROGADRESSE_URI_PATH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	HentMottakerOgAdresseResponse hentMottakerOgAdresse(@RequestBody HentMottakerOgAdresseRequest requestBody)
			throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		
		requestTimer = requestLatency.labels(SERVICE_CODE_TREG002, SERVICE_CODE_TREG002, "hentMottakerOgAdresse").startTimer();
		
		try {
			requestCounter.labels(SERVICE_CODE_TREG002, SERVICE_CODE_TREG002, PrometheusLabels.REST, getConsumerId(), RECEIVED)
					.inc();
			HentMottakerOgAdresseResponse response = hentMottakerOgAdresseService.hentMottakerOgAdresseInfo(requestBody);
			requestCounter.labels(SERVICE_CODE_TREG002, SERVICE_CODE_TREG002, PrometheusLabels.REST, getConsumerId(), PROCESSED_OK)
					.inc();
			return response;
		} catch (Exception e) {
			incrementExceptionMetrics(e, SERVICE_CODE_TREG002);
			throw e;
		} finally {
			requestTimer.observeDuration();
			SecurityContextHolder.clearContext();
			MDC.clear();
		}
	}
	
	private void incrementExceptionMetrics(Exception e, String serviceCode) {
		if (e instanceof RegOppslagFunctionalException) {
			requestExceptionCounter.labels(serviceCode, LABEL_FUNCTIONAL_EXCEPTION, e.getClass()
					.getSimpleName(), ((RegOppslagFunctionalException) e).getShortDescription()).inc();
		} else if (e instanceof RegOppslagSecurityException) {
			requestExceptionCounter.labels(serviceCode, LABEL_SECURITY_EXCEPTION, e.getClass()
					.getSimpleName(), ((RegOppslagSecurityException) e).getShortDescription()).inc();
		} else if (e instanceof RegOppslagTechnicalException) {
			requestExceptionCounter.labels(serviceCode, LABEL_TECHNICAL_EXCEPTION, e.getClass()
					.getSimpleName(), ((RegOppslagTechnicalException) e).getShortDescription()).inc();
		} else {
			requestExceptionCounter.labels(serviceCode, LABEL_TECHNICAL_EXCEPTION, e.getClass().getSimpleName(), e.getClass()
					.getSimpleName()).inc();
			
		}
	}
}
