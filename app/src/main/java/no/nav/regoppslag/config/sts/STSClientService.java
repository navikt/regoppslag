package no.nav.regoppslag.config.sts;

import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.OrganisasjonEnhetKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personidenter;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Service
public class STSClientService  {
	
	public static String REQUEST="REGOPPSLAG SAML TOKEN";
	
	@Inject
	OrganisasjonEnhetKontaktinformasjonV1 v1;
	
	@Inject
	OrganisasjonV4 organisasjonV4;
	
	@Inject
	PersonV3 personV3;
	
	public String echo() {
		try {
			v1.ping();
			organisasjonV4.ping();
			personV3.ping();
			HentPersonRequest request = new HentPersonRequest();
			request.setAktoer(new PersonIdent());
			PersonIdent personIdent = new PersonIdent();
			NorskIdent norskIdent = new NorskIdent();
			norskIdent.setIdent("20096828390");
			Personidenter personidenter = new Personidenter();
			personidenter.setValue("PERSON");
			norskIdent.setType(personidenter);
			personIdent.setIdent(norskIdent);
			request.setAktoer(personIdent);
			HentPersonResponse response = personV3.hentPerson(request);
			return response.getPerson().getPersonnavn().getFornavn();
		}catch (Exception e){
			throw new RuntimeException(e.getMessage());
		}
	}
	
}
