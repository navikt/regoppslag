package no.nav.regoppslag.treg002;

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
	
	@Inject
	public HentMottakerOgAdresseService(PersonV3Consumer personV3Consumer, PersonV3Mapper personV3Mapper, OrganisasjonV4Consumer organisasjonV4Consumer, OrganisasjonV4Mapper organisasjonV4Mapper) {
		this.personV3Consumer = personV3Consumer;
		this.personV3Mapper = personV3Mapper;
		this.organisasjonV4Consumer = organisasjonV4Consumer;
		this.organisasjonV4Mapper = organisasjonV4Mapper;
	}
	
	public HentMottakerOgAdresseResponse hentMottakerOgAdresseInfo(HentMottakerOgAdresseRequest request) throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		
		validateInput(request);
		try {
			Mottaker mottaker = new Mottaker();
			log.info(String.format("Mottat hentMottakerOgAdresse kall. Identifikator=%s, type=%s", request.getIdentifikator(), request
					.getType()));
			if (AktoerType.PERSON.name().equals(request.getType())) {
				Bruker bruker = personV3Consumer.hentPerson(request.getIdentifikator());
				personV3Mapper.map(bruker, mottaker);
			} else {
				Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(request.getIdentifikator());
				organisasjonV4Mapper.map(organisasjon, mottaker);
			}
			log.info(String.format("HentMottakerOgAdresse kall behandlet ferdig. Identifikator=%s, type=%s", request.getIdentifikator(), request
					.getType()));
			
			return HentMottakerOgAdresseResponse.builder()
					.identifikator(request.getIdentifikator())
					.navn(mottaker.getNavn())
					.adresse(AdresseMapper.map(mottaker))
					.build();
		} catch (Exception e) {
			logAndRethrowException(e);
		}
		
		return null;
	}
	
	private void validateInput(HentMottakerOgAdresseRequest request) throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		
		if (request==null){
			throw new RegOppslagFunctionalException("Input body er null");
		}
		
		if (request.getIdentifikator()==null){
			throw new RegOppslagFunctionalException("Identifikator kan ikke være null");
		}
		
		if (request.getType()==null) {
			throw new RegOppslagFunctionalException("Mottakertype kan ikke være null");
		} else if (!(AktoerType.PERSON.name().equals(request.getType())|| AktoerType.ORGANISASJON.name().equals(request.getType()))) {
			throw new RegOppslagFunctionalException(String.format("Mottakertype var %s. Det må være enten PERSON eller ORGANISASJON.",request.getType()));
		}
	}
	
	private void logAndRethrowException(Exception e) throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		if (e instanceof RegOppslagFunctionalException) {
			log.info("Funksjonell feil", e);
			throw (RegOppslagFunctionalException) e;
			
		} else {
			log.error("Technical exception", e);
			throw new RegOppslagTechnicalException(String.format("Technical exception: errorMsg=%s", e.getMessage()));
		}
	}
	
	
}
