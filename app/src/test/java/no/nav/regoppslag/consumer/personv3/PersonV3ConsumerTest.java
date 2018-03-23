package no.nav.regoppslag.consumer.personv3;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.feil.PersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.feil.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class PersonV3ConsumerTest {

	private static final String FNR = "99999999999";
	private static final String FORNAVN = "TOM";
	private static final String MELLOMNAVN = "MARVOLO";
	private static final String ETTERNAVN = "RIDDLE";

	private PersonV3 personV3 = mock(PersonV3.class);
	private PersonV3Consumer personV3Consumer = new PersonV3Consumer(personV3);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void shouldHentPersonnavn() throws Exception{
		when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(defaultResponse());

		Bruker person = personV3Consumer.hentPerson(FNR);

		assertThat(person.getPersonnavn().getSammensattNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
	}

	@Test
	public void shouldHentPersonNavnWhenMissingMellomnavn() throws Exception{
		when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(createResponse(FORNAVN, null, ETTERNAVN));

		Bruker person = personV3Consumer.hentPerson(FNR);

		assertThat(person.getPersonnavn().getSammensattNavn(), is(FORNAVN + " " + ETTERNAVN));
	}

	@Test
	public void shouldReturnNullWhenNavnInResponse() throws Exception{
		HentPersonResponse response = defaultResponse();
		response.setPerson(null);
		when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(response);

		Bruker person = personV3Consumer.hentPerson(FNR);

		assertThat(person, nullValue());
	}

	@Test
	public void shouldReturnNullWhenNameNotInResponse() throws Exception{
		HentPersonResponse response = defaultResponse();
		response.getPerson().setPersonnavn(null);
		when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(response);

		Bruker person = personV3Consumer.hentPerson(FNR);

		assertThat(person.getPersonnavn(), nullValue());
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenPersonIkkeFunnet() throws Exception {
		when(personV3.hentPerson(any(HentPersonRequest.class))).thenThrow(new HentPersonPersonIkkeFunnet("Fant ikke person", new PersonIkkeFunnet()));

		expectedException.expect(RegOppslagFunctionalException.class);
		expectedException.expectMessage("PersonV3.hentPerson fant ikke person med ident:" + FNR);
		Bruker person = personV3Consumer.hentPerson(FNR);
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenSikkerhetsbegrensning() throws Exception {
		when(personV3.hentPerson(any(HentPersonRequest.class))).thenThrow(new HentPersonSikkerhetsbegrensning("Ingen adgang", new Sikkerhetsbegrensning()));
		expectedException.expect(RegOppslagFunctionalException.class);
		expectedException.expectMessage("PersonV3.hentPerson feiler på grunn av sikkerhetsbegresning for ident: " + FNR);

		Bruker person = personV3Consumer.hentPerson(FNR);
	}

	private HentPersonResponse defaultResponse() {
		return createResponse(FORNAVN, MELLOMNAVN, ETTERNAVN);
	}

	private HentPersonResponse createResponse(String fornavn, String mellomnavn, String etternavn) {
		HentPersonResponse response = new HentPersonResponse();
		Personnavn personnavn = new Personnavn();
		personnavn.setFornavn(fornavn);
		if (mellomnavn != null) {
			personnavn.setMellomnavn(mellomnavn);
			personnavn.setSammensattNavn(fornavn + " " + mellomnavn + " " + etternavn);
		} else {
			personnavn.setSammensattNavn(fornavn + " " + etternavn);
		}
		personnavn.setEtternavn(etternavn);
		Bruker person = new Bruker();
		person.setPersonnavn(personnavn);
		response.setPerson(person);
		return response;
	}
}
