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
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

import static no.nav.regoppslag.metrics.MetricLabels.COMPONENT;
import static no.nav.regoppslag.metrics.MetricLabels.DOK_REQUEST;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_RREG003;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;

@RestController
@RequestMapping(REST)
@Tag(name = "Postadresse", description = "Tjeneste for å hente postadresse")
@Slf4j
public class PostAdresseController {

	public static final String POSTADRESSE_URI_PATH = "postadresse";

	private final PostadresseService postadresseService;

	@Inject
	public PostAdresseController(PostadresseService postadresseService){
		this.postadresseService = postadresseService;
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
