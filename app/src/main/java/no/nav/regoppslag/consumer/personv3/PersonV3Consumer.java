package no.nav.regoppslag.consumer.personv3;

import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_COUNTER;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_MISS;
import static no.nav.regoppslag.metrics.PrometheusLabels.PERSONV3;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestLatency;

import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.SamlTokenInterceptorException;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Informasjonsbehov;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personidenter;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
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
	private Histogram.Timer requestTimer;
	
	public static final String HENT_PERSON = "hentPerson";
	public static final String PERSON_IKKE_FUNNET = "PersonV3 - Person ikke funnet";
	public static final String SIKKERHETSBEGRENSNING = "PersonV3 - Sikkerhetsbegrensning";
	
	@Inject
	public PersonV3Consumer(PersonV3 personV3) {
		this.personV3 = personV3;
	}
	
	@Cacheable(value = HENT_PERSON, key = "#personidentifikator+'-'+#consumerId")
	@Retryable(include = RegOppslagTechnicalException.class, exclude = {RegOppslagFunctionalException.class }, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public Bruker hentPerson(final String personidentifikator, final String consumerId, final String serviceCode) throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		
		requestCounter.labels(serviceCode, HENT_PERSON, CACHE_COUNTER, consumerId, CACHE_MISS).inc();
		
		HentPersonRequest request = mapHentPersonRequest(personidentifikator);
		HentPersonResponse response;
		
		try {
			requestTimer = requestLatency.labels(serviceCode, PERSONV3, HENT_PERSON).startTimer();
			response = personV3.hentPerson(request);
		} catch (HentPersonPersonIkkeFunnet hentPersonPersonIkkeFunnet) {
			throw new RegOppslagFunctionalException("PersonV3.hentPerson fant ikke person med ident:" + personidentifikator + ", message=" + hentPersonPersonIkkeFunnet
					.getMessage(), hentPersonPersonIkkeFunnet, PERSON_IKKE_FUNNET);
		} catch (HentPersonSikkerhetsbegrensning hentPersonSikkerhetsbegrensning) {
			throw new RegOppslagSecurityException("PersonV3.hentPerson feiler på grunn av sikkerhetsbegresning. ConsumerId=" + consumerId + ", message=" + hentPersonSikkerhetsbegrensning
					.getMessage(), hentPersonSikkerhetsbegrensning, SIKKERHETSBEGRENSNING);
		} catch (Exception e) {
			//Kastes SoapFaultException som også kan kastes av andre grunner enn Interceptor feil
			if (e.getCause() instanceof SamlTokenInterceptorException){
				throw new RegOppslagFunctionalException(e.getMessage(), e, "PersonV3 - Mangler/Feil SAML token");
			}
			throw new RegOppslagTechnicalException("Noe gikk galt i kall til PersonV3.hentPerson. ConsumerId=" + consumerId + ", message=" + e
					.getMessage(), e, "PersonV3 - Teknisk feil");
		} finally {
			requestTimer.observeDuration();
		}
		if (response != null && response.getPerson() != null) {
			
			return (Bruker) response.getPerson();
		}
		return null;
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
		return request;
	}
}
