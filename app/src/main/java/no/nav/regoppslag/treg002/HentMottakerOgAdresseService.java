package no.nav.regoppslag.treg002;

import static no.nav.dok.metaforcemal.jaxb2.gen.AktoerType.ORGANISASJON;
import static no.nav.dok.metaforcemal.jaxb2.gen.AktoerType.PERSON;
import static no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer.HENT_ORGANISASJON;
import static no.nav.regoppslag.consumer.personv3.PersonV3Consumer.HENT_PERSON;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_TOTAL;
import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_CACHE_COUNTER;
import static no.nav.regoppslag.metrics.PrometheusLabels.MOTTAKERTYPE;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.metaforcemal.jaxb2.gen.AktoerType;
import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.regoppslag.common.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.common.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.support.OrganisasjonV4Mapper;
import no.nav.regoppslag.consumer.personv3.PersonV3Consumer;
import no.nav.regoppslag.consumer.personv3.support.PersonV3Mapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
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
	
	@Inject
	public HentMottakerOgAdresseService(PersonV3Consumer personV3Consumer, PersonV3Mapper personV3Mapper, OrganisasjonV4Consumer organisasjonV4Consumer, OrganisasjonV4Mapper organisasjonV4Mapper, AdresseMapper adresseMapper) {
		this.personV3Consumer = personV3Consumer;
		this.personV3Mapper = personV3Mapper;
		this.organisasjonV4Consumer = organisasjonV4Consumer;
		this.organisasjonV4Mapper = organisasjonV4Mapper;
		this.adresseMapper = adresseMapper;
	}
	
	public HentMottakerOgAdresseResponse hentMottakerOgAdresseInfo(HentMottakerOgAdresseRequest request) throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		
		validateInput(request);
		try {
			Mottaker mottaker = new Mottaker();
			log.info(String.format("Mottat hentMottakerOgAdresse kall. MottakerType=%s, ConsumerId=%s", request
					.getType(), getConsumerId()));
			if (PERSON.name().equals(request.getType())) {
				requestCounter.labels(HENT_PERSON, LABEL_CACHE_COUNTER, getConsumerId(), CACHE_TOTAL).inc();
				Bruker bruker = personV3Consumer.hentPerson(request.getIdentifikator(), getConsumerId(), SERVICE_CODE_TREG002);
				personV3Mapper.map(bruker, mottaker);
				requestCounter.labels(SERVICE_CODE_TREG002, MOTTAKERTYPE, getConsumerId(), PERSON.name());
			} else {
				requestCounter.labels(HENT_ORGANISASJON, LABEL_CACHE_COUNTER, getConsumerId(), CACHE_TOTAL).inc();
				Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(request.getIdentifikator(), SERVICE_CODE_TREG002);
				organisasjonV4Mapper.map(organisasjon, mottaker);
				requestCounter.labels(SERVICE_CODE_TREG002, MOTTAKERTYPE, getConsumerId(), ORGANISASJON.name());
			}
			log.info(String.format("HentMottakerOgAdresse kall behandlet ferdig. MottakerType=%s, ConsumerId=%s", request
					.getType(), getConsumerId()));
			
			return HentMottakerOgAdresseResponse.builder()
					.identifikator(request.getIdentifikator())
					.navn(mottaker.getNavn())
					.adresse(adresseMapper.map(mottaker))
					.build();
		} catch (Exception e) {
			logAndRethrowException(e);
		}
		
		return null;
	}
	
	private void validateInput(HentMottakerOgAdresseRequest request) throws RegOppslagFunctionalException {
		
		if (request == null) {
			throw new RegOppslagFunctionalException("Input body er null");
		}
		
		if (request.getIdentifikator() == null) {
			throw new RegOppslagFunctionalException("Identifikator kan ikke være null");
		}
		
		if (request.getType() == null) {
			throw new RegOppslagFunctionalException("Mottakertype kan ikke være null");
		} else if (!(PERSON.name().equals(request.getType()) || AktoerType.ORGANISASJON.name()
				.equals(request.getType()))) {
			throw new RegOppslagFunctionalException(String.format("Mottakertype var %s. Det må være PERSON eller ORGANISASJON.", request
					.getType()));
		}
	}
	
	private void logAndRethrowException(Exception e) throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		if (e instanceof RegOppslagFunctionalException) {
			log.info("Funksjonell feil", e);
			throw (RegOppslagFunctionalException) e;
		} else if (e instanceof RegOppslagSecurityException) {
			log.info("Sikkerhetsfeil", e);
			throw (RegOppslagSecurityException) e;
		} else {
			log.error("Teknisk feil", e);
			throw new RegOppslagTechnicalException(String.format("Teknisk feil: feilmelding=%s", e.getMessage()));
		}
	}
	
	
}
