package no.nav.regoppslag.consumer.personv3;

import static no.nav.regoppslag.metrics.MetricLabels.DOK_CONSUMER;
import static no.nav.regoppslag.metrics.MetricLabels.PERSONV3;
import static no.nav.regoppslag.metrics.MetricLabels.PERSON_DISKRESJONSKODE;
import static no.nav.regoppslag.metrics.MetricLabels.PROCESS_CODE;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.SamlTokenInterceptorException;
import no.nav.regoppslag.metrics.MetricLabels;
import no.nav.regoppslag.metrics.Metrics;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Informasjonsbehov;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personidenter;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 * @author Ketill Fenne, Visma Consulting AS
 */
@Slf4j
@Service
public class PersonV3Consumer {
	private final PersonV3 personV3;
	private MicrometerMetrics metrics;

	private static final String PERSON_IKKE_FUNNET = "PersonV3 - Person ikke funnet";
	private static final String SIKKERHETSBEGRENSNING = "PersonV3 - Sikkerhetsbegrensning";
	
	@Inject
	public PersonV3Consumer(PersonV3 personV3, MicrometerMetrics metrics) {
		this.personV3 = personV3;
		this.metrics = metrics;
	}

	@Cacheable(value = MetricLabels.HENT_PERSON, key = "#personidentifikator",
			unless = "#result != null && #result.diskresjonskode != null && #result.diskresjonskode.value != null && #result.diskresjonskode.value.length() > 0")
	@Retryable(include = RegOppslagTechnicalException.class, exclude = {RegOppslagFunctionalException.class }, maxAttempts = 5, backoff = @Backoff(delay = 200))
	@Metrics(value = DOK_CONSUMER, extraTags = {PROCESS_CODE, MetricLabels.HENT_PERSON}, percentiles = {0.5, 0.95}, histogram = true)
	public Bruker hentPerson(final String personidentifikator, String serviceCode) throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		metrics.cacheMiss(MetricLabels.HENT_PERSON);
		
		HentPersonRequest request = mapHentPersonRequest(personidentifikator);

		try {
			Person person = personV3.hentPerson(request).getPerson();
			if(person != null && person.getDiskresjonskode() != null && !isBlank(person.getDiskresjonskode().getValue())) {
				metrics.meter(serviceCode, PERSONV3, PERSON_DISKRESJONSKODE, PERSON_DISKRESJONSKODE);
			}
			return (Bruker)person;
		} catch (HentPersonPersonIkkeFunnet hentPersonPersonIkkeFunnet) {
			throw new RegOppslagFunctionalException(String.format("PersonV3.hentPerson fant ikke person med ident=%s, message=%s", personidentifikator, hentPersonPersonIkkeFunnet
					.getMessage()), hentPersonPersonIkkeFunnet, PERSON_IKKE_FUNNET);
		} catch (HentPersonSikkerhetsbegrensning hentPersonSikkerhetsbegrensning) {
			throw new RegOppslagSecurityException(String.format("PersonV3.hentPerson feiler på grunn av sikkerhetsbegresning. Message=%s", hentPersonSikkerhetsbegrensning
					.getMessage()), hentPersonSikkerhetsbegrensning, SIKKERHETSBEGRENSNING);
		} catch (Exception e) {
			if (e.getCause() instanceof SamlTokenInterceptorException){
				throw new RegOppslagFunctionalException(e.getMessage(), e, "PersonV3 - Mangler/Feil SAML token");
			}
			throw new RegOppslagTechnicalException(String.format("Noe gikk galt i kall til PersonV3.hentPerson. Message=%s", e
					.getMessage()), e, "PersonV3 - Teknisk feil");
		}
	}
	
	private HentPersonRequest mapHentPersonRequest(String personidentifikator) {

		Personidenter personidenter = new Personidenter();
		if (StringUtils.startsWithAny(personidentifikator, "0", "1", "2", "3")) {
			personidenter.setValue("FNR");
		} else {
			personidenter.setValue("DNR");
		}
		
		NorskIdent norskIdent = new NorskIdent();
		norskIdent.setType(personidenter);
		norskIdent.setIdent(personidentifikator);
		
		PersonIdent personIdent = new PersonIdent();
		personIdent.setIdent(norskIdent);
		HentPersonRequest request = new HentPersonRequest();
		request.setAktoer(personIdent);
		request.getInformasjonsbehov().add(Informasjonsbehov.ADRESSE);
		request.getInformasjonsbehov().add(Informasjonsbehov.KOMMUNIKASJON);
		return request;
	}
}
