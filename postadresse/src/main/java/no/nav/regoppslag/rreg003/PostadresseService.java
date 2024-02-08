package no.nav.regoppslag.rreg003;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.ereg.EregConsumer;
import no.nav.regoppslag.consumer.ereg.MottakerTo;
import no.nav.regoppslag.consumer.ereg.support.Organisasjon;
import no.nav.regoppslag.consumer.ereg.support.OrganisasjonEregMapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.exceptions.RegOppslagIngenTilgangException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoedException;
import no.nav.regoppslag.pdl.MapPDLResponse;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer.ARKIVPLEIE_BEHANDLINGSNUMMER;
import static no.nav.regoppslag.util.DomainConstants.SERVICE_CODE_RREG003;
import static org.apache.commons.lang3.StringUtils.isAllUpperCase;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@Slf4j
public class PostadresseService {

	private final EregConsumer eregConsumer;
	private final AdresseMapper adresseMapper;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private final MapPDLResponse mapPDLResponse;
	private final OrganisasjonEregMapper organisasjonEregMapper;

	private static final String UGYLDIG_INPUT = "Ugyldig input";
	private static final String RREG003_FUNK_FEIL = "RREG003 Funksjonell feil: {}";
	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
	private static final Pattern BEHANDLINGSNUMMER_PATTERN = Pattern.compile("^[A-Z]\\d{3}$");

	public PostadresseService(AdresseMapper adresseMapper,
							  PdlGraphQLConsumer pdlGraphQLConsumer,
							  MapPDLResponse mapPDLResponse,
							  EregConsumer eregConsumer,
							  OrganisasjonEregMapper organisasjonEregMapper) {
		this.adresseMapper = adresseMapper;
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
		this.mapPDLResponse = mapPDLResponse;
		this.eregConsumer = eregConsumer;
		this.organisasjonEregMapper = organisasjonEregMapper;
	}

	public PostadresseResponse postadresseInfo(PostadresseRequest request, String behandlingsnummer) throws RegOppslagSecurityException {
		try {
			validateInput(request, behandlingsnummer);

			if (request.getIdent().length() == 9) { //organisasjon har alltid ident lengde 9
				return postadresseForOrg(request);
			} else {
				return postadresseForPerson(request, behandlingsnummer);
			}

		} catch (Exception e) {
			logAndRethrowException(e);
		}

		return null;
	}

	private PostadresseResponse postadresseForPerson(PostadresseRequest request, String input_behandlingsnummer) {
		String behandlingsnummer = input_behandlingsnummer == null ? ARKIVPLEIE_BEHANDLINGSNUMMER : input_behandlingsnummer;
		var personFraPdl = pdlGraphQLConsumer.hentPerson(request.getIdent(), behandlingsnummer);

		PdlMottakerInfo pdlMottakerInfo = mapPDLResponse.mapHentPerson(personFraPdl, SERVICE_CODE_RREG003);

		return PostadresseResponse.builder()
				.navn(pdlMottakerInfo.getNavn())
				.adresse(adresseMapper.mapFraPdl(pdlMottakerInfo))
				.build();
	}

	private PostadresseResponse postadresseForOrg(PostadresseRequest request) {
		Organisasjon organisasjon = eregConsumer.hentOrganisasjon(request.getIdent());

		MottakerTo mottakerTo = organisasjonEregMapper.map(request.getIdent(), organisasjon, SERVICE_CODE_RREG003);
		return PostadresseResponse.builder()
				.navn(mottakerTo.getMottaker().getNavn())
				.adresse(adresseMapper.map(mottakerTo))
				.build();
	}

	private void validateInput(PostadresseRequest request, String behandlingsnummer) {

		if (request == null) {
			throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT + " Request body er tom.", BAD_REQUEST);
		}

		if (request.getIdent() == null) {
			throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT + " Ident kan ikke være null.", BAD_REQUEST);
		}

		if (!NUMBER_PATTERN.matcher(request.getIdent()).matches()) {
			throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT + " Ident kan kun bestå av tall.", BAD_REQUEST);
		}

		if (!asList(9, 11, 13).contains(request.getIdent().length())) {
			throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT + " Ident må ha lengde på 9, 11 eller 13 siffer.", BAD_REQUEST);
		}

		if (!isEmpty(behandlingsnummer) && !BEHANDLINGSNUMMER_PATTERN.matcher(behandlingsnummer).matches()) {
			throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT + " Behandlingsnummer må bestå av en stor bokstav og tre etterfølgende siffer. Eks B123 ", BAD_REQUEST);
		}
	}

	private void logAndRethrowException(Exception e) throws RegOppslagSecurityException {
		if (e instanceof UkjentAdressePersonErDoedException err) {
			log.info("RREG003: {}", e.getMessage());
			throw err;
		} else if (e instanceof UkjentAdresseException err) {
			log.warn(RREG003_FUNK_FEIL, e.getMessage());
			throw err;
		} else if (e instanceof RegOppslagSecurityException err) {
			log.warn(RREG003_FUNK_FEIL, e.getMessage());
			throw err;
		} else if (e instanceof RegOppslagIngenTilgangException err) {
			log.warn(RREG003_FUNK_FEIL, e.getMessage());
			throw err;
		} else if (e instanceof RegOppslagFunctionalException err) {
			log.warn(RREG003_FUNK_FEIL, e.getMessage());
			if (NOT_FOUND.equals(((RegOppslagFunctionalException) e).getHttpStatusCode())) {
				throw new RegOppslagIkkeFunnetException(err.getLocalizedMessage(), err, err.getHttpStatusCode());
			}
			throw new RegoppslagIllegalArgumentException(e.getLocalizedMessage(), e, ((RegOppslagFunctionalException) e).getHttpStatusCode());
		} else {
			log.error("RREG003 Teknisk feil: {}", e.getMessage(), e);
			throw new RegOppslagTechnicalException(format("Teknisk feil: feilmelding=%s", e.getMessage()), e);
		}
	}
}
