package no.nav.regoppslag.personv3;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.AktoerHarNavn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Feil;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkResponse;
import org.junit.Test;

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

	@Test
	public void shouldHentPersonnavn() throws Exception{
		when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(defaultResponse());

		Person person = personV3Consumer.hentPerson(FNR);

		assertThat(person.getPersonnavn().getSammensattNavn(), is("TOM MARVOLO RIDDLE"));
	}

	@Test
	public void shouldHentPersonNavnWhenMissingMellomnavn() throws Exception{
		when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(createResponse(FORNAVN, null, ETTERNAVN));

		Person person = personV3Consumer.hentPerson(FNR);

		assertThat(person.getPersonnavn().getSammensattNavn(), is("TOM RIDDLE"));
	}

	@Test
	public void shouldReturnNullWhenNavnInResponse() throws Exception{
		HentPersonResponse response = defaultResponse();
		response.setPerson(null);
		when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(response);

		Person person = personV3Consumer.hentPerson(FNR);

		assertThat(person, nullValue());
	}

	@Test
	public void shouldReturnNullWhenNameNotInResponse() throws Exception{
		HentPersonResponse response = defaultResponse();
		response.getPerson().setPersonnavn(null);
		when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(response);

		Person person = personV3Consumer.hentPerson(FNR);

		assertThat(person.getPersonnavn(), nullValue());
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
		Person person = new Person();
		person.setPersonnavn(personnavn);
		response.setPerson(person);
		return response;
	}
}
