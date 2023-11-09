package no.nav.regoppslag.rreg003;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.ereg.EregConsumer;
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
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoed;
import no.nav.regoppslag.pdl.MapPDLResponse;
import no.nav.regoppslag.consumer.ereg.MottakerTo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static java.lang.String.format;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_RREG003;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GONE;
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

	@Autowired
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

	public PostadresseResponse postadresseInfo(PostadresseRequest request) throws RegOppslagSecurityException {

		try {
			validateInput(request);

			if (request.getIdent().length() == 9) { //organisasjon har alltid ident lengde 9
				return postadresseForOrg(request);
			} else {
				return postadresseForPerson(request);
			}

		} catch (Exception e) {
			logAndRethrowException(e);
		}

		return null;
	}

	private PostadresseResponse postadresseForPerson(PostadresseRequest request) {
		var personFraPdl = pdlGraphQLConsumer.hentPerson(request.getIdent(), request.getTema());

		PdlMottakerInfo pdlMottakerInfo = mapPDLResponse.mapHentPerson(personFraPdl, SERVICE_CODE_RREG003, request.getTema());

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

	private void validateInput(PostadresseRequest request) {

		if (request == null) {
			throw new RegoppslagIllegalArgumentException("Request body er tom. " + UGYLDIG_INPUT, BAD_REQUEST);
		}

		if (request.getIdent() == null) {
			throw new RegoppslagIllegalArgumentException("Ident kan ikke være null. " + UGYLDIG_INPUT, BAD_REQUEST);
		}

		if (request.getTema() == null) {
			throw new RegoppslagIllegalArgumentException("Tema kan ikke være null. " + UGYLDIG_INPUT, BAD_REQUEST);
		}

		if (!StringUtils.isAllUpperCase(request.getTema()) && request.getTema().length() != 3) {
			throw new RegoppslagIllegalArgumentException("Tema må være 3 store bokstaver. " + UGYLDIG_INPUT, BAD_REQUEST);
		}

		//Identifikator må være 9, 11 eller 13 karakterer lang for å være en gyldig ident
		if (!Arrays.asList(9, 11, 13).contains(request.getIdent().length()) || !StringUtils.isNumeric(request.getIdent())) {
			throw new RegoppslagIllegalArgumentException("Identifikator er feilformatert. " + UGYLDIG_INPUT, BAD_REQUEST);
		}

	}


	private void logAndRethrowException(Exception e) throws RegOppslagSecurityException {
		if (e instanceof RegOppslagFunctionalException && GONE.equals(((RegOppslagFunctionalException) e).getHttpStatusCode())) {
			log.warn(format("RREG003 Funksjonell feil: %s", e.getMessage()), e);
			throw new UkjentAdressePersonErDoed(e.getMessage(), ((RegOppslagFunctionalException) e).getHttpStatusCode());
		} else if (e instanceof RegOppslagSecurityException) {
			log.warn(RREG003_FUNK_FEIL, e.getMessage());
			throw (RegOppslagSecurityException) e;
		} else if(e instanceof RegOppslagIngenTilgangException) {
			log.warn(RREG003_FUNK_FEIL, e.getMessage());
			throw (RegOppslagIngenTilgangException) e;
		} else if (e instanceof RegOppslagFunctionalException) {
			log.warn(RREG003_FUNK_FEIL, e.getMessage());
			if (NOT_FOUND.equals(((RegOppslagFunctionalException) e).getHttpStatusCode())) {
				throw new RegOppslagIkkeFunnetException(e.getLocalizedMessage(), e, "RREG003", ((RegOppslagFunctionalException) e).getHttpStatusCode());
			}
			throw new RegoppslagIllegalArgumentException(e.getLocalizedMessage(), e, "RREG003", ((RegOppslagFunctionalException) e).getHttpStatusCode());
		} else {
			log.error(String.format("RREG003 Teknisk feil: %s", e.getMessage()), e);
			throw new RegOppslagTechnicalException(String.format("Teknisk feil: feilmelding=%s", e.getMessage()), e, e.getClass()
					.getSimpleName());
		}
	}
}
