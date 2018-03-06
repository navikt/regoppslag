package no.nav.regoppslag.consumer.personv3.support;

import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;

public class PersonV3Mapper {
	public void map(Person person, Mottaker mottaker) {
		//Spraakkode?
		mottaker.setNavn(person.getPersonnavn().getSammensattNavn());

//		Bruker bruker = (Bruker) person;

	}
}
