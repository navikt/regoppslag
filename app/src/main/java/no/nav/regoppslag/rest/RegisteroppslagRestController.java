package no.nav.regoppslag.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
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

import java.util.Set;

import static no.nav.regoppslag.config.springdoc.SpringDoc.jwtTokenInfo;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;
import static no.nav.regoppslag.util.SafeLoggingUtil.removeUnsafeChars;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
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
	public static final int DOKUMENTTYPE_ID_LENGTH = 6;
	private static final Set<String> GYLDIG_MOTTAKER_TYPE = Set.of("PERSON", "ORGANISASJON");

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
		try {
			String dokumentTypeId = removeUnsafeChars(requestBody.getDokumentTypeId());
			log.info("TREG001 Har mottatt kall om å komplettere brevdata. DokumenttypeId={}", dokumentTypeId);
			validateKompletterBrevdataRequest(requestBody);
			KompletterBrevdataResponse response = kompletterBrevdataService.hentBrevdataFraRegistre(requestBody);
			log.info("TREG001 Er ferdig med å komplettere brevdata. DokumenttypeId={}", dokumentTypeId);
			return response;
		} catch (MarshallerTechnicalException e) {
			//Logger error hvis retry ikke fungerer
			log.error("TREG001 Teknisk marshaller feil: {}", e.getMessage(), e);
			throw e;
		} catch (RegoppslagIllegalArgumentException e) {
			log.warn("TREG001 valideringsfeil={}", e.getMessage());
			throw e;
		} finally {
			SecurityContextHolder.clearContext();
			MDC.clear();
		}
	}

	private static void validateKompletterBrevdataRequest(KompletterBrevdataRequest requestBody) {
		String dokumentTypeId = requestBody.getDokumentTypeId();
		if (isBlank(dokumentTypeId)) {
			throw new RegoppslagIllegalArgumentException("dokumentTypeId er null eller blank", BAD_REQUEST);
		}
		if (!isNumeric(dokumentTypeId)) {
			throw new RegoppslagIllegalArgumentException("dokumentTypeId er ikke numerisk. dokumentTypeId=" + removeUnsafeChars(dokumentTypeId), BAD_REQUEST);
		}
		if (dokumentTypeId.length() > DOKUMENTTYPE_ID_LENGTH) {
			throw new RegoppslagIllegalArgumentException("dokumentTypeId er lengre enn " + DOKUMENTTYPE_ID_LENGTH + " tegn. dokumentTypeId=" + removeUnsafeChars(dokumentTypeId), BAD_REQUEST);
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
			String mottakerType = removeUnsafeChars(requestBody.getType());
			log.info("TREG002 Henter mottaker og addresse. MottakerType={}", mottakerType);
			validateHentMottakerOgAdresseRequest(requestBody);
			HentMottakerOgAdresseResponse response = hentMottakerOgAdresseService.hentMottakerOgAdresseInfo(requestBody);
			log.info("TREG002 Har hentet mottaker og adresse. MottakerType={}", mottakerType);
			return response;
		} catch (RegoppslagIllegalArgumentException e) {
			log.warn("TREG002 valideringsfeil={}", e.getMessage());
			throw e;
		} finally {
			SecurityContextHolder.clearContext();
			MDC.clear();
		}
	}

	private static void validateHentMottakerOgAdresseRequest(HentMottakerOgAdresseRequest request) {
		if (request == null) {
			throw new RegoppslagIllegalArgumentException("Request body er tom.", BAD_REQUEST);
		}

		if (request.getIdentifikator() == null) {
			throw new RegoppslagIllegalArgumentException("Identifikator kan ikke være null.", BAD_REQUEST);
		}

		if (!isNumeric(request.getIdentifikator())) {
			throw new RegoppslagIllegalArgumentException("Identifikator kan kun bestå av tall.", BAD_REQUEST);
		}

		if (request.getType() == null) {
			throw new RegoppslagIllegalArgumentException("Mottakertype kan ikke være null.", BAD_REQUEST);
		} else if (!GYLDIG_MOTTAKER_TYPE.contains(request.getType())) {
			throw new RegoppslagIllegalArgumentException("Mottakertype var " + removeUnsafeChars(request.getType()) + "." +
														 " Mottakertype må være PERSON eller ORGANISASJON.", BAD_REQUEST);
		}
	}
}
