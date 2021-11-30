package no.nav.regoppslag.treg002;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType;
import no.nav.regoppslag.api.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.support.OrganisasjonV4Mapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.map.MapPDLResponse;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.personv3.PersonV3Consumer;
import no.nav.regoppslag.consumer.personv3.support.PersonV3Mapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoed;
import no.nav.regoppslag.treg001.to.MottakerTo;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static java.lang.String.format;
import static no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType.PERSON;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Slf4j
public class HentMottakerOgAdresseService {

	private final PersonV3Consumer personV3Consumer;
	private final PersonV3Mapper personV3Mapper;
	private final OrganisasjonV4Consumer organisasjonV4Consumer;
	private final OrganisasjonV4Mapper organisasjonV4Mapper;
	private final AdresseMapper adresseMapper;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private final MapPDLResponse mapPDLResponse;

	private static final String UGYLDIG_INPUT = "Ugyldig input";
	private static final String TREG002_FUNK_FEIL = "TREG002 Funksjonell feil: {}";

	@Inject
	public HentMottakerOgAdresseService(PersonV3Consumer personV3Consumer, PersonV3Mapper personV3Mapper,
										OrganisasjonV4Consumer organisasjonV4Consumer, OrganisasjonV4Mapper organisasjonV4Mapper,
										AdresseMapper adresseMapper, PdlGraphQLConsumer pdlGraphQLConsumer, MapPDLResponse mapPDLResponse) {
		this.personV3Consumer = personV3Consumer;
		this.personV3Mapper = personV3Mapper;
		this.organisasjonV4Consumer = organisasjonV4Consumer;
		this.organisasjonV4Mapper = organisasjonV4Mapper;
		this.adresseMapper = adresseMapper;
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
		this.mapPDLResponse = mapPDLResponse;
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
		if (isBlank(request.getTema()) && PERSON.name().equals(request.getType())) {
			log.info("hentPersonV3 fra HentMottakerOgAdresseService"); //TODO: remove this log when is ready MMA-5754
			Bruker bruker = personV3Consumer.hentPerson(request.getIdentifikator(), SERVICE_CODE_TREG002);
			MottakerTo mottakerTo = personV3Mapper.map(bruker, SERVICE_CODE_TREG002);
			return HentMottakerOgAdresseResponse.builder()
					.identifikator(request.getIdentifikator())
					.navn(mottakerTo.getMottaker().getNavn())
					.adresse(adresseMapper.map(mottakerTo.getMottaker()))
					.build();
		}

		log.info("Treg002 hentMottakerOgAdresse bruker PDL PersonV3. Tema er satt.");
		PdlMottakerInfo pdlMottakerInfo = mapPDLResponse.mapHentPerson(pdlGraphQLConsumer.hentPerson(request.getIdentifikator(), request.getTema()), SERVICE_CODE_TREG002, request.getTema());
		return HentMottakerOgAdresseResponse.builder()
				.identifikator(request.getIdentifikator())
				.navn(pdlMottakerInfo.getNavn())
				.adresse(adresseMapper.mapFraPdl(pdlMottakerInfo))
				.build();
	}

	private HentMottakerOgAdresseResponse hentMottakerOgAdresseForOrg(HentMottakerOgAdresseRequest request) {
		Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(request.getIdentifikator());
		MottakerTo mottakerTo = organisasjonV4Mapper.map(request.getIdentifikator(), organisasjon, SERVICE_CODE_TREG002);
		return HentMottakerOgAdresseResponse.builder()
				.identifikator(request.getIdentifikator())
				.navn(mottakerTo.getMottaker().getNavn())
				.adresse(adresseMapper.map(mottakerTo.getMottaker()))
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
		} else if (!(PERSON.name().equals(request.getType()) || AktoerType.ORGANISASJON.name()
				.equals(request.getType()))) {
			throw new RegoppslagIllegalArgumentException(format("Mottakertype var %s. Det må være PERSON eller ORGANISASJON.", request
					.getType()) + UGYLDIG_INPUT, BAD_REQUEST);
		}
	}


	private void logAndRethrowException(Exception e) throws RegOppslagSecurityException {
		if (e instanceof RegOppslagFunctionalException && GONE.equals(((RegOppslagFunctionalException) e).getHttpStatus())) {
			log.error(format("TREG002 Funksjonell feil: %s", e.getMessage()), e);
			throw new UkjentAdressePersonErDoed(e.getMessage(), ((RegOppslagFunctionalException) e).getHttpStatus());
		} else if (e instanceof RegOppslagSecurityException) {
			log.warn(TREG002_FUNK_FEIL, e.getMessage());
			throw (RegOppslagSecurityException) e;
		} else if (e instanceof RegOppslagFunctionalException) {
			log.warn(TREG002_FUNK_FEIL, e.getMessage());
			if (NOT_FOUND.equals(((RegOppslagFunctionalException) e).getHttpStatus())) {
				throw new RegOppslagIkkeFunnetException(e.getLocalizedMessage(), e, "TREG002", ((RegOppslagFunctionalException) e).getHttpStatus());
			}
			throw new RegoppslagIllegalArgumentException(e.getLocalizedMessage(), e, "TREG002", ((RegOppslagFunctionalException) e).getHttpStatus());
		} else {
			log.error(String.format("TREG002 Teknisk feil: %s", e.getMessage()), e);
			throw new RegOppslagTechnicalException(String.format("Teknisk feil: feilmelding=%s", e.getMessage()), e, e.getClass()
					.getSimpleName());
		}
	}
}
