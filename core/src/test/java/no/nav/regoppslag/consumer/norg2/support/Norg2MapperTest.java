package no.nav.regoppslag.consumer.norg2.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import no.nav.dok.brevdata.felles.v1.navfelles.NavEnhet;
import no.nav.dok.brevdata.felles.v1.navfelles.Postadresse;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.KontaktinformasjonForOrganisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.PostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Postnummer;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Norg2MapperTest {

	private PostnummerService postnummerService;
	private Norg2Mapper norg2Mapper;
	private static final String NAV_ENHET_ID = "123";
	private static final String NAV_ENHET_NAVN = "NAV Husnes";

	private static final String GATENAVN = "Gatenavn";
	private static final String	HUSNR = "13";
	private static final String HUSBOKSTAV = "X";
	private static final String POSTNR = "5460";
	private static final String POSTSTED = "HUSNES";
	private static final String POSTBOKSANLEGG = "Postboksanlegg";
	private static final String POSTBOKSNUMMER = "1";

	@BeforeEach
	public	void initPostnummer() throws Exception {
		postnummerService = new PostnummerService();
		norg2Mapper = new Norg2Mapper(postnummerService);
	}

	@Test
	public void shouldMapNavEnhetNavn() {
		NavEnhet enhet = new NavEnhet();
		enhet.setEnhetsId(NAV_ENHET_ID);
		norg2Mapper.mapEnhetNavn(createEnhetWithStedsadresse(NAV_ENHET_NAVN), enhet);
		MatcherAssert.assertThat(enhet.getEnhetsId(), is(NAV_ENHET_ID));
		MatcherAssert.assertThat(enhet.getEnhetsNavn(), is(NAV_ENHET_NAVN));
	}

	@Test
	public void shouldMapStedadresseNavEnhet() {
		Postadresse postadresse = createPostadresse(NAV_ENHET_ID);
		norg2Mapper.mapPostadresse(createEnhetWithStedsadresse(NAV_ENHET_NAVN), postadresse);
		MatcherAssert.assertThat(postadresse.getEnhetsId(), is(NAV_ENHET_ID));
		MatcherAssert.assertThat(postadresse.getEnhetsNavn(), is(NAV_ENHET_NAVN));

		MatcherAssert.assertThat(postadresse.getAdresse().getAdresselinje1(), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		MatcherAssert.assertThat(postadresse.getAdresse().getPostnummer(), is(POSTNR));
		MatcherAssert.assertThat(postadresse.getAdresse().getPoststed(), is(POSTSTED));
	}

	@Test
	public void shouldMapPostboksadresseNavEnhet() {
		Postadresse postadresse = createPostadresse(NAV_ENHET_ID);
		norg2Mapper.mapPostadresse(createEnhetWithPostbokssadresse(NAV_ENHET_NAVN), postadresse);
		MatcherAssert.assertThat(postadresse.getEnhetsId(), is(NAV_ENHET_ID));
		MatcherAssert.assertThat(postadresse.getEnhetsNavn(), is(NAV_ENHET_NAVN));

		MatcherAssert.assertThat(postadresse.getAdresse().getAdresselinje1(), is("Postboks " + POSTBOKSNUMMER + " " + POSTBOKSANLEGG));
		MatcherAssert.assertThat(postadresse.getAdresse().getPostnummer(), is(POSTNR));
		MatcherAssert.assertThat(postadresse.getAdresse().getPoststed(), is(POSTSTED));
	}

	@Test
	public void shouldMapBesokAdresseNavEnhet() {
		Postadresse postadresse = createPostadresse(NAV_ENHET_ID);
		norg2Mapper.mapBesokadresse(createWSEnhetWithBesoksadresse(NAV_ENHET_NAVN), postadresse);
		MatcherAssert.assertThat(postadresse.getEnhetsId(), is(NAV_ENHET_ID));

		MatcherAssert.assertThat(postadresse.getAdresse().getAdresselinje1(), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		MatcherAssert.assertThat(postadresse.getAdresse().getPostnummer(), is(POSTNR));
		MatcherAssert.assertThat(postadresse.getAdresse().getPoststed(), is(POSTSTED));
	}

	private Postadresse createPostadresse (String enhetId) {
		Postadresse enhet = new Postadresse();
		enhet.setEnhetsId(enhetId);
		return enhet;
	}

	private Organisasjonsenhet createEnhetWithStedsadresse (String enhetNavn) {
		Organisasjonsenhet wsEnhet = new Organisasjonsenhet();
		wsEnhet.setEnhetNavn(enhetNavn);

		Gateadresse gateadresse = new Gateadresse();
		gateadresse.setGatenavn(GATENAVN);
		gateadresse.setHusnummer(HUSNR);
		gateadresse.setHusbokstav(HUSBOKSTAV);

		Postnummer postnummer = new Postnummer();
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTNR);
		gateadresse.setPoststed(postnummer);

		KontaktinformasjonForOrganisasjonsenhet kontaktinformasjon = new KontaktinformasjonForOrganisasjonsenhet();

		kontaktinformasjon.setPostadresse(gateadresse);

		wsEnhet.setKontaktinformasjon(kontaktinformasjon);
		return wsEnhet;
	}

	private Organisasjonsenhet createEnhetWithPostbokssadresse (String enhetNavn) {
		Organisasjonsenhet wsEnhet = new Organisasjonsenhet();
		wsEnhet.setEnhetNavn(enhetNavn);

		Postnummer postnummer = new Postnummer();
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTNR);

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
		wsEnhet.setEnhetNavn(enhetNavn);

		Gateadresse gateadresse = new Gateadresse();
		gateadresse.setGatenavn(GATENAVN);
		gateadresse.setHusnummer(HUSNR);
		gateadresse.setHusbokstav(HUSBOKSTAV);

		Postnummer postnummer = new Postnummer();
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTNR);

		KontaktinformasjonForOrganisasjonsenhet kontaktinformasjon = new KontaktinformasjonForOrganisasjonsenhet();

		gateadresse.setPoststed(postnummer);
		kontaktinformasjon.setBesoeksadresse(gateadresse);

		wsEnhet.setKontaktinformasjon(kontaktinformasjon);
		wsEnhet.setEnhetNavn(enhetNavn);
		return wsEnhet;
	}
}
