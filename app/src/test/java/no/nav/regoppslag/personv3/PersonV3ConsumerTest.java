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
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkResponse;
import org.junit.Test;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class PersonV3ConsumerTest {

	private static final String FNR = "99999999999";
	private static final String FORNAVN = " TOM";
	private static final String MELLOMNAVN = " MARVOLO ";
	private static final String ETTERNAVN = "RIDDLE ";

	private PersonV3 personV3 = mock(PersonV3.class);
	private PersonV3Consumer personV3Consumer = new PersonV3Consumer(personV3);

	@Test
	public void shouldHentPersonnavn() {
		when(personV3.hentPersonnavnBolk(any(HentPersonnavnBolkRequest.class))).thenReturn(defaultResponse());

		String personnavn = personV3Consumer.hentPersonnavn(FNR);

		assertThat(personnavn, is("TOM MARVOLO RIDDLE"));
	}

	@Test
	public void shouldHentPersonNavnWhenMissingMellomnavn() {
		when(personV3.hentPersonnavnBolk(any(HentPersonnavnBolkRequest.class))).thenReturn(createResponse(FORNAVN, null, ETTERNAVN));

		String personnavn = personV3Consumer.hentPersonnavn(FNR);

		assertThat(personnavn, is("TOM RIDDLE"));
	}

	@Test
	public void shouldReturnNullWhenNavnInResponse() {
		HentPersonnavnBolkResponse response = defaultResponse();
		response.getAktoerHarNavnListe().clear();
		when(personV3.hentPersonnavnBolk(any(HentPersonnavnBolkRequest.class))).thenReturn(response);

		String personnavn = personV3Consumer.hentPersonnavn(FNR);

		assertThat(personnavn, nullValue());
	}

	@Test
	public void shouldReturnNullWhenFeilListeInResponse() {
		HentPersonnavnBolkResponse response = defaultResponse();
		response.getAktoerHarNavnListe().clear();
		Feil feil = new Feil();
		PersonIdent personIdent = new PersonIdent();
		NorskIdent norskIdent = new NorskIdent();
		norskIdent.setIdent(FNR);
		personIdent.setIdent(norskIdent);
		feil.setAktoer(personIdent);
		feil.setFeilBeskrivelse("Brukeren finnes ikke");
		response.getFeilListe().add(feil);
		when(personV3.hentPersonnavnBolk(any(HentPersonnavnBolkRequest.class))).thenReturn(response);

		String personnavn = personV3Consumer.hentPersonnavn(FNR);

		assertThat(personnavn, nullValue());
	}

	@Test
	public void shouldReturnNullWhenNameNotInResponse() {
		HentPersonnavnBolkResponse response = defaultResponse();
		response.getAktoerHarNavnListe().get(0).setPersonnavn(null);
		when(personV3.hentPersonnavnBolk(any(HentPersonnavnBolkRequest.class))).thenReturn(response);

		String personnavn = personV3Consumer.hentPersonnavn(FNR);

		assertThat(personnavn, nullValue());
	}

	private HentPersonnavnBolkResponse defaultResponse() {
		return createResponse(FORNAVN, MELLOMNAVN, ETTERNAVN);
	}

	private HentPersonnavnBolkResponse createResponse(String fornavn, String mellomnavn, String etternavn) {
		HentPersonnavnBolkResponse response = new HentPersonnavnBolkResponse();
		AktoerHarNavn aktoerHarNavn = new AktoerHarNavn();
		Personnavn value = new Personnavn();
		value.setFornavn(fornavn);
		value.setMellomnavn(mellomnavn);
		value.setEtternavn(etternavn);
		aktoerHarNavn.setPersonnavn(value);
		response.getAktoerHarNavnListe().add(aktoerHarNavn);
		return response;
	}
}
