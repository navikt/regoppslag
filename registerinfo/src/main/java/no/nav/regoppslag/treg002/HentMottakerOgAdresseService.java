package no.nav.regoppslag.treg002;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.ereg.EregConsumer;
import no.nav.regoppslag.consumer.ereg.MottakerTo;
import no.nav.regoppslag.consumer.ereg.support.Organisasjon;
import no.nav.regoppslag.consumer.ereg.support.OrganisasjonEregMapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoed;
import no.nav.regoppslag.pdl.MapPDLResponse;
import no.nav.regoppslag.rreg003.AdresseMapper;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType.ORGANISASJON;
import static no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType.PERSON;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.treg002.Treg002AdresseMapper.mapAdresseTilTreg002Adresse;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@Slf4j
public class HentMottakerOgAdresseService {

	private final AdresseMapper adresseMapper;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private final MapPDLResponse mapPDLResponse;
	private final EregConsumer eregConsumer;
	private final OrganisasjonEregMapper organisasjonEregMapper;

	private static final String UGYLDIG_INPUT = "Ugyldig input";
	private static final String TREG002_FUNK_FEIL = "TREG002 Funksjonell feil: {}";

	public HentMottakerOgAdresseService(AdresseMapper adresseMapper,
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

	public HentMottakerOgAdresseResponse hentMottakerOgAdresseInfo(HentMottakerOgAdresseRequest request) throws RegOppslagSecurityException {
		try {
			validateInput(request);
			return PERSON.name().equals(request.getType()) ? hentMottakerOgAdresseForPerson(request) : hentMottakerOgAdresseForOrg(request);
		} catch (Exception e) {
			logAndRethrowException(e);
		}

		return null;
	}

	private HentMottakerOgAdresseResponse hentMottakerOgAdresseForPerson(HentMottakerOgAdresseRequest request) {
		var person = pdlGraphQLConsumer.hentPerson(request.getIdentifikator(), request.getTema());

		PdlMottakerInfo pdlMottakerInfo = mapPDLResponse.mapHentPerson(person, SERVICE_CODE_TREG002, request.getTema());

		return HentMottakerOgAdresseResponse.builder()
				.identifikator(request.getIdentifikator())
				.navn(pdlMottakerInfo.getNavn())
				.adresse(mapAdresseTilTreg002Adresse(adresseMapper.mapFraPdl(pdlMottakerInfo)))
				.build();
	}

	private HentMottakerOgAdresseResponse hentMottakerOgAdresseForOrg(HentMottakerOgAdresseRequest request) {
		Organisasjon organisasjon = eregConsumer.hentOrganisasjon(request.getIdentifikator());
		MottakerTo mottakerTo = organisasjonEregMapper.map(request.getIdentifikator(), organisasjon, SERVICE_CODE_TREG002);

		return HentMottakerOgAdresseResponse.builder()
				.identifikator(request.getIdentifikator())
				.navn(mottakerTo.getMottaker().getNavn())
				.adresse(mapAdresseTilTreg002Adresse(adresseMapper.map(mottakerTo)))
				.build();
	}

	private void validateInput(HentMottakerOgAdresseRequest request) {
		if (request == null) {
			throw new RegoppslagIllegalArgumentException("Request body er tom. " + UGYLDIG_INPUT, BAD_REQUEST);
		}

		if (request.getIdentifikator() == null) {
			throw new RegoppslagIllegalArgumentException("Identifikator kan ikke være null. " + UGYLDIG_INPUT, BAD_REQUEST);
		}

		if (request.getType() == null) {
			throw new RegoppslagIllegalArgumentException("Mottakertype kan ikke være null. " + UGYLDIG_INPUT, BAD_REQUEST);
		} else if (!(PERSON.name().equals(request.getType()) || ORGANISASJON.name().equals(request.getType()))) {
			throw new RegoppslagIllegalArgumentException(format("Mottakertype var %s. Det må være PERSON eller ORGANISASJON.",
					request.getType()) + UGYLDIG_INPUT, BAD_REQUEST);
		}
	}

	private void logAndRethrowException(Exception e) throws RegOppslagSecurityException {
		if (e instanceof RegOppslagFunctionalException && GONE.equals(((RegOppslagFunctionalException) e).getHttpStatusCode())) {
			log.error(format("TREG002 Funksjonell feil: %s", e.getMessage()), e);
			throw new UkjentAdressePersonErDoed(e.getMessage(), ((RegOppslagFunctionalException) e).getHttpStatusCode());
		} else if (e instanceof RegOppslagSecurityException) {
			log.warn(TREG002_FUNK_FEIL, e.getMessage());
			throw (RegOppslagSecurityException) e;
		} else if (e instanceof RegOppslagFunctionalException) {
			log.warn(TREG002_FUNK_FEIL, e.getMessage());
			if (NOT_FOUND.equals(((RegOppslagFunctionalException) e).getHttpStatusCode())) {
				throw new RegOppslagIkkeFunnetException(e.getLocalizedMessage(), e, "TREG002", ((RegOppslagFunctionalException) e).getHttpStatusCode());
			}
			throw new RegoppslagIllegalArgumentException(e.getLocalizedMessage(), e, "TREG002", ((RegOppslagFunctionalException) e).getHttpStatusCode());
		} else {
			log.error(String.format("TREG002 Teknisk feil: %s", e.getMessage()), e);
			throw new RegOppslagTechnicalException(String.format("Teknisk feil: feilmelding=%s", e.getMessage()), e, e.getClass()
					.getSimpleName());
		}
	}
}
