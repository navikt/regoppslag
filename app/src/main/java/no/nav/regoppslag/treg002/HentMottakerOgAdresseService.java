package no.nav.regoppslag.treg002;

import static no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType.PERSON;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.metrics.MicrometerMetrics.getUserId;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType;
import no.nav.regoppslag.api.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.support.OrganisasjonV4Mapper;
import no.nav.regoppslag.consumer.personv3.PersonV3Consumer;
import no.nav.regoppslag.consumer.personv3.support.PersonV3Mapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.treg001.to.MottakerTo;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

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

	private static final String UGYLDIG_INPUT = "Ugyldig input";

	@Inject
	public HentMottakerOgAdresseService(PersonV3Consumer personV3Consumer, PersonV3Mapper personV3Mapper, OrganisasjonV4Consumer organisasjonV4Consumer, OrganisasjonV4Mapper organisasjonV4Mapper, AdresseMapper adresseMapper) {
		this.personV3Consumer = personV3Consumer;
		this.personV3Mapper = personV3Mapper;
		this.organisasjonV4Consumer = organisasjonV4Consumer;
		this.organisasjonV4Mapper = organisasjonV4Mapper;
		this.adresseMapper = adresseMapper;
	}

	public HentMottakerOgAdresseResponse hentMottakerOgAdresseInfo(HentMottakerOgAdresseRequest request) throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {

		try {
			validateInput(request);
			MottakerTo mottakerTo;
			if (PERSON.name().equals(request.getType())) {
				Bruker bruker = personV3Consumer.hentPerson(request.getIdentifikator());
				mottakerTo = personV3Mapper.map(bruker, SERVICE_CODE_TREG002);
			} else {
				Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(request.getIdentifikator());
				mottakerTo = organisasjonV4Mapper.map(request.getIdentifikator(), organisasjon, SERVICE_CODE_TREG002);
			}
			return HentMottakerOgAdresseResponse.builder()
					.identifikator(request.getIdentifikator())
					.navn(mottakerTo.getMottaker().getNavn())
					.adresse(adresseMapper.map(mottakerTo.getMottaker()))
					.build();
		} catch (Exception e) {
			logAndRethrowException(e);
		}

		return null;
	}

	private void validateInput(HentMottakerOgAdresseRequest request) throws RegOppslagFunctionalException {

		if (request == null) {
			throw new RegOppslagFunctionalException("Request body er tom", UGYLDIG_INPUT);
		}

		if (request.getIdentifikator() == null) {
			throw new RegOppslagFunctionalException("Identifikator kan ikke være null", UGYLDIG_INPUT);
		}

		if (request.getType() == null) {
			throw new RegOppslagFunctionalException("Mottakertype kan ikke være null", UGYLDIG_INPUT);
		} else if (!(PERSON.name().equals(request.getType()) || AktoerType.ORGANISASJON.name()
				.equals(request.getType()))) {
			throw new RegOppslagFunctionalException(String.format("Mottakertype var %s. Det må være PERSON eller ORGANISASJON.", request
					.getType()), UGYLDIG_INPUT);
		}
	}

	private void logAndRethrowException(Exception e) throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		if (e instanceof RegOppslagFunctionalException) {
			log.warn(String.format("TREG002 Funksjonell feil: %s", e.getMessage()));
			throw (RegOppslagFunctionalException) e;
		} else if (e instanceof RegOppslagSecurityException) {
			log.warn(String.format("TREG002 Sikkerhetsfeil: %s", e.getMessage()));
			throw (RegOppslagSecurityException) e;
		} else if (e instanceof RegOppslagTechnicalException) {
			log.error(String.format("TREG002 Teknisk feil: %s", e.getMessage()), e);
			throw new RegOppslagTechnicalException(String.format("Teknisk feil: feilmelding=%s", e.getMessage()), e, ((RegOppslagTechnicalException) e)
					.getMetricMessage());
		} else {
			log.error(String.format("TREG002 Teknisk feil: %s", e.getMessage()), e);
			throw new RegOppslagTechnicalException(String.format("Teknisk feil: feilmelding=%s", e.getMessage()), e, e.getClass()
					.getSimpleName());
		}
	}
}
