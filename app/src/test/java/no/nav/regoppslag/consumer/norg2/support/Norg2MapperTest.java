package no.nav.regoppslag.consumer.norg2.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import no.nav.dok.metaforcemal.jaxb2.gen.NavEnhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.KontaktinformasjonForOrganisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.PostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Postnummer;
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
		NavEnhet navEnhet = createSimpleNavEnhet(NAV_ENHET_ID);
		norg2Mapper.mapPostadresse(createSimpleEnhet(NAV_ENHET_NAVN), navEnhet);
		assertThat(navEnhet.getEnhetsId(),is(NAV_ENHET_ID));
		assertThat(navEnhet.getEnhetsNavn(),is(NAV_ENHET_NAVN));
	}

	@Test
	public void shouldMapStedadresseNavEnhet() {
		NavEnhet navEnhet = createSimpleNavEnhet(NAV_ENHET_ID);
		norg2Mapper.mapPostadresse(createEnhetWithStedsadresse(NAV_ENHET_NAVN), navEnhet);
		assertThat(navEnhet.getEnhetsId(), is(NAV_ENHET_ID));
		assertThat(navEnhet.getEnhetsNavn(), is(NAV_ENHET_NAVN));

		assertThat(navEnhet.getAdresse().getAdresselinje1(), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat(navEnhet.getAdresse().getPostnummer(), is(POSTNR));
		assertThat(navEnhet.getAdresse().getPoststed(), is(POSTSTED));
	}

	@Test
	public void shouldMapPostboksadresseNavEnhet() {
		NavEnhet navEnhet = createSimpleNavEnhet(NAV_ENHET_ID);
		norg2Mapper.mapPostadresse(createWSEnhetWithPostbokssadresse(NAV_ENHET_NAVN), navEnhet);
		assertThat(navEnhet.getEnhetsId(), is(NAV_ENHET_ID));
		assertThat(navEnhet.getEnhetsNavn(), is(NAV_ENHET_NAVN));

		assertThat(navEnhet.getAdresse().getAdresselinje1(), is(POSTBOKSNUMMER + " " + POSTBOKSANLEGG));
		assertThat(navEnhet.getAdresse().getPostnummer(), is(POSTNR));
		assertThat(navEnhet.getAdresse().getPoststed(), is(POSTSTED));
	}

	@Test
	public void shouldMapBesokAdresseNavEnhet() {
		NavEnhet navEnhet = createSimpleNavEnhet(NAV_ENHET_ID);
		norg2Mapper.mapBesokadresse(createWSEnhetWithBesoksadresse(NAV_ENHET_NAVN), navEnhet);
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

	private Organisasjonsenhet createSimpleEnhet (String enhetNavn) {
		Organisasjonsenhet wsEnhet = new Organisasjonsenhet();
		wsEnhet.setEnhetNavn(enhetNavn);
		return wsEnhet;
	}

	private Organisasjonsenhet createEnhetWithStedsadresse (String enhetNavn) {
		Organisasjonsenhet wsEnhet = new Organisasjonsenhet();

		Gateadresse gateadresse = new Gateadresse();
		gateadresse.setGatenavn(GATENAVN);
		gateadresse.setHusnummer(HUSNR);
		gateadresse.setHusbokstav(HUSBOKSTAV);

		Postnummer postnummer = new Postnummer();
		postnummer.setValue(POSTNR);
		postnummer.setKodeverksRef(POSTSTED);
		gateadresse.setPoststed(postnummer);

		KontaktinformasjonForOrganisasjonsenhet kontaktinformasjon = new KontaktinformasjonForOrganisasjonsenhet();

		kontaktinformasjon.setPostadresse(gateadresse);

		wsEnhet.setKontaktinformasjon(kontaktinformasjon);
		wsEnhet.setEnhetNavn(enhetNavn);
		return wsEnhet;
	}

	private Organisasjonsenhet createWSEnhetWithPostbokssadresse (String enhetNavn) {
		Organisasjonsenhet wsEnhet = new Organisasjonsenhet();

		Postnummer postnummer = new Postnummer();
		postnummer.setValue(POSTNR);
		postnummer.setKodeverksRef(POSTSTED);

		PostboksadresseNorsk postboksadresse = new PostboksadresseNorsk();
		postboksadresse.setPoststed(postnummer);
		postboksadresse.setPostboksanlegg(POSTBOKSANLEGG);
		postboksadresse.setPostboksnummer (POSTBOKSNUMMER);

		KontaktinformasjonForOrganisasjonsenhet kontaktinformasjon = new KontaktinformasjonForOrganisasjonsenhet();

		kontaktinformasjon.setPostadresse(postboksadresse);

		wsEnhet.setKontaktinformasjon(kontaktinformasjon);
		wsEnhet.setEnhetNavn(enhetNavn);
		return wsEnhet;
	}

	private Organisasjonsenhet createWSEnhetWithBesoksadresse (String enhetNavn) {
		Organisasjonsenhet wsEnhet = new Organisasjonsenhet();

		Gateadresse gateadresse = new Gateadresse();
		gateadresse.setGatenavn(GATENAVN);
		gateadresse.setHusnummer(HUSNR);
		gateadresse.setHusbokstav(HUSBOKSTAV);

		Postnummer postnummer = new Postnummer();
		postnummer.setValue(POSTNR);
		postnummer.setKodeverksRef(POSTSTED);

		KontaktinformasjonForOrganisasjonsenhet kontaktinformasjon = new KontaktinformasjonForOrganisasjonsenhet();

		gateadresse.setPoststed(postnummer);
		kontaktinformasjon.setBesoeksadresse(gateadresse);

		wsEnhet.setKontaktinformasjon(kontaktinformasjon);
		wsEnhet.setEnhetNavn(enhetNavn);
		return wsEnhet;
	}
}
