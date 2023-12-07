package no.nav.regoppslag.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.treg001.KompletterBrevdataRequest;
import no.nav.regoppslag.treg001.KompletterBrevdataResponse;
import no.nav.regoppslag.treg001.KompletterBrevdataService;
import no.nav.regoppslag.treg001.xmlenricher.exceptions.MarshallerTechnicalException;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseService;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.String.format;
import static no.nav.regoppslag.config.springdoc.SpringDoc.jwtTokenInfo;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(REST)
@Tag(name = "Registeroppslag", description = "<b><h3>TIL INTERN BRUK: (skal ikke benyttes av eksterne konsumenter)</h3></b> Tjeneste for å hente mottakeradresse og komplettere brevskjema. Krever JWT Authorization.")
@Slf4j
@Protected
public class RegisteroppslagRestController {

	public static final String REST = "rest/";
	public static final String KOMPLETTER_BREVDATA_URI_PATH = "kompletterBrevdata";
	public static final String HENT_MOTTAKEROGADRESSE_URI_PATH = "hentMottakerOgAdresse";

	private final KompletterBrevdataService kompletterBrevdataService;
	private final HentMottakerOgAdresseService hentMottakerOgAdresseService;

	public RegisteroppslagRestController(KompletterBrevdataService kompletterBrevdataService,
										 HentMottakerOgAdresseService hentMottakerOgAdresseService) {
		this.kompletterBrevdataService = kompletterBrevdataService;
		this.hentMottakerOgAdresseService = hentMottakerOgAdresseService;
	}

	@Operation(summary = "TREG001", description = "Denne tjenesten tar brevdata i XML format som input og beriker elementene med data fra registere ved å benytte Berikerplugins.<br/><br/>" + jwtTokenInfo)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "400", description = "Ugyldig input. Denne feilen vil returneres hvis det er feil i inputverdiene", content = @Content),
			@ApiResponse(responseCode = "401", description = "Ingen tilgang til PersonV3", content = @Content),
			@ApiResponse(responseCode = "500", description = "Teknisk feil", content = @Content)
	})
	@PostMapping(value = KOMPLETTER_BREVDATA_URI_PATH, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	KompletterBrevdataResponse kompletterBrevdata(@RequestBody KompletterBrevdataRequest requestBody) throws RegOppslagSecurityException {

		log.info(format("TREG001 Har mottatt kall om å komplettere brevdata. DokumenttypeId=%s", requestBody.getDokumentTypeId()));

		try {
			KompletterBrevdataResponse response = kompletterBrevdataService.hentBrevdataFraRegistre(requestBody);
			log.info(format("TREG001 Er ferdig med å komplettere brevdata. DokumenttypeId=%s", requestBody.getDokumentTypeId()));
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

	@Operation(summary = "TREG002", description = "Dette er en domenetjeneste som kan brukes for å hente mottakernavn og adresse slik at konsumenter kun trenger å sende inn mottakerId.<br/><br/>" + jwtTokenInfo)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "400", description = "Ugyldig input. Denne feilen vil returneres hvis det er feil i inputverdiene", content = @Content),
			@ApiResponse(responseCode = "401", description = "Ingen tilgang til PersonV3", content = @Content),
			@ApiResponse(responseCode = "404", description = "Bruker har ukjent adresse", content = @Content),
			@ApiResponse(responseCode = "410", description = "Person er død og har ukjent adresse", content = @Content),
			@ApiResponse(responseCode = "500", description = "Teknisk feil", content = @Content)
	})
	@PostMapping(value = HENT_MOTTAKEROGADRESSE_URI_PATH, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	HentMottakerOgAdresseResponse hentMottakerOgAdresse(@RequestBody HentMottakerOgAdresseRequest requestBody) throws RegOppslagSecurityException {

		try {
			log.info(format("TREG002 Henter mottaker og addresse. MottakerType=%s", requestBody.getType()));
			HentMottakerOgAdresseResponse response = hentMottakerOgAdresseService.hentMottakerOgAdresseInfo(requestBody);
			log.info(format("TREG002 Har hentet mottaker og adresse. MottakerType=%s", requestBody.getType()));
			return response;
		} finally {
			SecurityContextHolder.clearContext();
			MDC.clear();
		}
	}


}
