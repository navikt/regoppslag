package no.nav.regoppslag.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.rreg003.PostadresseRequest;
import no.nav.regoppslag.rreg003.PostadresseResponse;
import no.nav.regoppslag.rreg003.PostadresseService;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.regoppslag.config.springdoc.SpringDoc.jwtTokenInfo;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(REST)
@Tag(name = "Postadresse", description = "Tjeneste for å hente postadresse. Krever JWT Authorization")
@Slf4j
@Protected
public class PostAdresseController {

	public static final String POSTADRESSE_URI_PATH = "postadresse";

	private final PostadresseService postadresseService;

	public PostAdresseController(PostadresseService postadresseService) {
		this.postadresseService = postadresseService;
	}

	@Operation(summary = "RREG003", description = "Dette er en domenetjeneste som kan brukes for å hente postadresse slik at konsumenter kun trenger å sende inn mottakerId.<br/><br/>" + jwtTokenInfo)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "400", description = "Ugyldig input. Denne feilen vil returneres hvis det feil i input verdiene.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Ingen tilgang til postadresse tjenesten.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Tilgang til å hente postadresse avvist", content = @Content),
			@ApiResponse(responseCode = "404", description = "Person / organisasjon har ukjent adresse.", content = @Content),
			@ApiResponse(responseCode = "410", description = "Person er død og har ukjent adresse.", content = @Content),
			@ApiResponse(responseCode = "500", description = "Intern teknisk feil i postadresse tjenesten.", content = @Content)
	})
	@PostMapping(value = POSTADRESSE_URI_PATH, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<PostadresseResponse> postadresse(@RequestBody PostadresseRequest requestBody) throws RegOppslagSecurityException {
		try {
			log.info("RREG003 Henter postaddresse.");

			PostadresseResponse response = postadresseService.postadresseInfo(requestBody);
			log.info("RREG003 Har hentet postadresse.");

			return ResponseEntity.ok(response);
		} finally {
			SecurityContextHolder.clearContext();
			MDC.clear();
		}
	}
}
