package no.nav.regoppslag.consumer.norg2.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import no.nav.dok.metaforcemal.jaxb2.gen.NavEnhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSGateadresse;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSKontaktinformasjonForOrganisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSOrganisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSPostboksadresse;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSPostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSPostnummer;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSStedsadresse;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSStedsadresseNorge;
import org.junit.Test;

public class Norg2MapperTest {
	private Norg2Mapper norg2Mapper = new Norg2Mapper();
	private static final String NAV_ENHET_ID = "123";
	private static final String NAV_ENHET_NAVN = "NAV Husnes";

	private static final String GATENAVN = "Gatenavn";
	private static final String	HUSNR = "13";
	private static final String HUSBOKSTAV = "X";
	private static final String POSTNR = "5460";
	private static final String POSTSTED = "Husnes";
	private static final String POSTBOKSANLEGG = "Postboksanlegg";
	private static final String POSTBOKSNUMMER = "Boksnummer 1";

	@Test
	public void shouldMapSimpleNavEnhet() {
		NavEnhet navEnhet = norg2Mapper.mapPostadresse(createSimpleWSEnhet(NAV_ENHET_NAVN), createSimpleNavEnhet(NAV_ENHET_ID));
		assertThat(navEnhet.getEnhetsId(),is(NAV_ENHET_ID));
		assertThat(navEnhet.getEnhetsNavn(),is(NAV_ENHET_NAVN));
	}

	@Test
	public void shouldMapStedadresseNavEnhet() {
		NavEnhet navEnhet = norg2Mapper.mapPostadresse(createWSEnhetWithStedsadresse(NAV_ENHET_NAVN), createSimpleNavEnhet(NAV_ENHET_ID));
		assertThat(navEnhet.getEnhetsId(), is(NAV_ENHET_ID));
		assertThat(navEnhet.getEnhetsNavn(), is(NAV_ENHET_NAVN));

		assertThat(navEnhet.getAdresse().getAdresselinje1(), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat(navEnhet.getAdresse().getPostnummer(), is(POSTNR));
		assertThat(navEnhet.getAdresse().getPoststed(), is(POSTSTED));
	}

	@Test
	public void shouldMapPostboksadresseNavEnhet() {
		NavEnhet navEnhet = norg2Mapper.mapPostadresse(createWSEnhetWithPostbokssadresse(NAV_ENHET_NAVN), createSimpleNavEnhet(NAV_ENHET_ID));
		assertThat(navEnhet.getEnhetsId(), is(NAV_ENHET_ID));
		assertThat(navEnhet.getEnhetsNavn(), is(NAV_ENHET_NAVN));

		assertThat(navEnhet.getAdresse().getAdresselinje1(), is(POSTBOKSNUMMER + " " + POSTBOKSANLEGG));
		assertThat(navEnhet.getAdresse().getPostnummer(), is(POSTNR));
		assertThat(navEnhet.getAdresse().getPoststed(), is(POSTSTED));
	}

	@Test
	public void shouldMapBesokAdresseNavEnhet() {
		NavEnhet navEnhet = norg2Mapper.mapBesokadresse(createWSEnhetWithBesoksadresse(NAV_ENHET_NAVN), createSimpleNavEnhet(NAV_ENHET_ID));
		assertThat(navEnhet.getEnhetsId(), is(NAV_ENHET_ID));
		assertThat(navEnhet.getEnhetsNavn(), is(NAV_ENHET_NAVN));

		assertThat(navEnhet.getAdresse().getAdresselinje1(), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat(navEnhet.getAdresse().getPostnummer(), is(POSTNR));
		assertThat(navEnhet.getAdresse().getPoststed(), is(POSTSTED));
	}

	private NavEnhet createSimpleNavEnhet (String enhetId) {
		NavEnhet enhet = new NavEnhet();
		enhet.setEnhetsId(enhetId);
		return enhet;
	}

	private WSOrganisasjonsenhet createSimpleWSEnhet (String enhetNavn) {
		WSOrganisasjonsenhet wsEnhet = new WSOrganisasjonsenhet();
		wsEnhet.setEnhetNavn(enhetNavn);
		return wsEnhet;
	}

	private WSOrganisasjonsenhet createWSEnhetWithStedsadresse (String enhetNavn) {
		WSOrganisasjonsenhet wsEnhet = new WSOrganisasjonsenhet();

		WSGateadresse gateadresse = new WSGateadresse();
		gateadresse.setGatenavn(GATENAVN);
		gateadresse.setHusnummer(HUSNR);
		gateadresse.setHusbokstav(HUSBOKSTAV);

		WSPostnummer postnummer = new WSPostnummer();
		postnummer.setValue(POSTNR);
		postnummer.setKodeverksRef(POSTSTED);

		WSStedsadresseNorge stedsadresseNorge = new WSStedsadresseNorge();
		stedsadresseNorge.setPoststed(postnummer);

		WSKontaktinformasjonForOrganisasjonsenhet kontaktinformasjon = new WSKontaktinformasjonForOrganisasjonsenhet();

		kontaktinformasjon.setPostadresse(stedsadresseNorge);
		kontaktinformasjon.setBesoeksadresse(gateadresse);

		wsEnhet.setKontaktinformasjon(kontaktinformasjon);
		wsEnhet.setEnhetNavn(enhetNavn);
		return wsEnhet;
	}

	private WSOrganisasjonsenhet createWSEnhetWithPostbokssadresse (String enhetNavn) {
		WSOrganisasjonsenhet wsEnhet = new WSOrganisasjonsenhet();

		WSPostnummer postnummer = new WSPostnummer();
		postnummer.setValue(POSTNR);
		postnummer.setKodeverksRef(POSTSTED);

		WSPostboksadresseNorsk postboksadresse = new WSPostboksadresseNorsk();
		postboksadresse.setPoststed(postnummer);
		postboksadresse.setPostboksanlegg(POSTBOKSANLEGG);
		postboksadresse.setPostboksnummer (POSTBOKSNUMMER);

		WSKontaktinformasjonForOrganisasjonsenhet kontaktinformasjon = new WSKontaktinformasjonForOrganisasjonsenhet();

		kontaktinformasjon.setPostadresse(postboksadresse);

		wsEnhet.setKontaktinformasjon(kontaktinformasjon);
		wsEnhet.setEnhetNavn(enhetNavn);
		return wsEnhet;
	}

	private WSOrganisasjonsenhet createWSEnhetWithBesoksadresse (String enhetNavn) {
		WSOrganisasjonsenhet wsEnhet = new WSOrganisasjonsenhet();

		WSGateadresse gateadresse = new WSGateadresse();
		gateadresse.setGatenavn(GATENAVN);
		gateadresse.setHusnummer(HUSNR);
		gateadresse.setHusbokstav(HUSBOKSTAV);

		WSPostnummer postnummer = new WSPostnummer();
		postnummer.setValue(POSTNR);
		postnummer.setKodeverksRef(POSTSTED);

		WSKontaktinformasjonForOrganisasjonsenhet kontaktinformasjon = new WSKontaktinformasjonForOrganisasjonsenhet();

		gateadresse.setPoststed(postnummer);
		kontaktinformasjon.setBesoeksadresse(gateadresse);

		wsEnhet.setKontaktinformasjon(kontaktinformasjon);
		wsEnhet.setEnhetNavn(enhetNavn);
		return wsEnhet;
	}
}
