package no.nav.regoppslag.rest;

import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_FUNCTIONAL_EXCEPTION;
import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_TECHNICAL_EXCEPTION;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestExceptionCounter;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestLatency;

import io.prometheus.client.Histogram;
import no.nav.regoppslag.common.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.common.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataRequest;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataResponse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.treg001.KompletterBrevdataService;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseService;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */

@RestController
public class RegisteroppslagRestController {
	
	
	public static final String REST = "/rest/";
	public static final String KOMPLETTER_BREVDATA_URI_PATH = REST+"kompletterBrevdata";
	public static final String HENT_MOTTAKEROGADRESSE_URI_PATH = REST+"hentMottakerOgAdresse";
	
	private final KompletterBrevdataService kompletterBrevdataService;
	private final HentMottakerOgAdresseService hentMottakerOgAdresseService;
	private Histogram.Timer requestTimer;
	
	@Inject
	public RegisteroppslagRestController(KompletterBrevdataService kompletterBrevdataService, HentMottakerOgAdresseService hentMottakerOgAdresseService) {
		this.kompletterBrevdataService = kompletterBrevdataService;
		this.hentMottakerOgAdresseService=hentMottakerOgAdresseService;
	}

	@ExceptionHandler({RegOppslagFunctionalException.class, RegOppslagTechnicalException.class})
	@PostMapping(value = KOMPLETTER_BREVDATA_URI_PATH,consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	ValiderOgKompletterBrevdataResponse validerOgKompletterBrevdata(@RequestBody ValiderOgKompletterBrevdataRequest requestBody)
			throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
	
		
		requestTimer = requestLatency.labels(SERVICE_CODE_TREG001, SERVICE_CODE_TREG001, "validerOgKompletterBrevdata").startTimer();
		try {
			requestCounter.labels(SERVICE_CODE_TREG001, "controller", "received").inc();
			ValiderOgKompletterBrevdataResponse response = kompletterBrevdataService.hentBrevdataFraRegistre(requestBody);
			requestCounter.labels(SERVICE_CODE_TREG001, "controller", "processed_ok").inc();
			return response;
		} catch (Exception e){
			incrementExceptionMetrics(e, SERVICE_CODE_TREG001);
			throw e;
		} finally {
			requestTimer.observeDuration();
			SecurityContextHolder.clearContext();
		}
	}
	
	@PostMapping(value = HENT_MOTTAKEROGADRESSE_URI_PATH,consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody HentMottakerOgAdresseResponse hentMottakerOgAdresse(@RequestBody HentMottakerOgAdresseRequest requestBody)
			throws RegOppslagFunctionalException, RegOppslagTechnicalException{
		
		requestTimer = requestLatency.labels(SERVICE_CODE_TREG002, SERVICE_CODE_TREG002, "hentMottakerOgAdresse").startTimer();
		try {
			requestCounter.labels(SERVICE_CODE_TREG002, "controller", "received").inc();
			HentMottakerOgAdresseResponse response  = hentMottakerOgAdresseService.hentMottakerOgAdresseInfo(requestBody);
			requestCounter.labels(SERVICE_CODE_TREG002, "controller", "processed_ok").inc();
			return response;
		}catch (Exception e){
			incrementExceptionMetrics(e, SERVICE_CODE_TREG002);
			throw e;
		} finally {
			requestTimer.observeDuration();
			SecurityContextHolder.clearContext();
		}
	}
	
	private void incrementExceptionMetrics(Exception e, String serviceCode) {
		if (e instanceof RegOppslagFunctionalException){
			requestExceptionCounter.labels(serviceCode, LABEL_FUNCTIONAL_EXCEPTION, e.getClass().getSimpleName()).inc();
		} else {
			requestExceptionCounter.labels(serviceCode, LABEL_TECHNICAL_EXCEPTION, e.getClass().getSimpleName()).inc();
		}
	}
}
