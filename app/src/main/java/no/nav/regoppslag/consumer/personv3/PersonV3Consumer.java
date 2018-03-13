package no.nav.regoppslag.consumer.personv3;

import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestLatency;

import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Service
public class PersonV3Consumer {
	private final PersonV3 personV3;
	private Histogram.Timer requestTimer;

	@Inject
	public PersonV3Consumer(PersonV3 personV3) {
		this.personV3 = personV3;
	}

	public Bruker hentPerson(final String personidentifikator) {
		HentPersonRequest request = mapHentPersonRequest(personidentifikator);

		HentPersonResponse response = null;
		try {
			requestTimer = requestLatency.labels(SERVICE_CODE_TREG001, "PERSON_V3", "hentPerson").startTimer();
			response = personV3.hentPerson(request);
		} catch (HentPersonPersonIkkeFunnet hentPersonPersonIkkeFunnet) {
			hentPersonPersonIkkeFunnet.printStackTrace();
		} catch (HentPersonSikkerhetsbegrensning hentPersonSikkerhetsbegrensning) {
			hentPersonSikkerhetsbegrensning.printStackTrace();
		}finally {
			requestTimer.observeDuration();
		}
		if (response != null && response.getPerson()!= null) {
			return (Bruker) response.getPerson();
		}
		return null;
	}

	private HentPersonRequest mapHentPersonRequest(String personidentifikator) {
		HentPersonRequest request = new HentPersonRequest();

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
		request.setAktoer(personIdent);
		request.getInformasjonsbehov().add(Informasjonsbehov.ADRESSE);
		return request;
	}
}
