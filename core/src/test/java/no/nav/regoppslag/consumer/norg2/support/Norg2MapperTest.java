package no.nav.regoppslag.consumer.norg2.support;

import no.nav.dok.brevdata.felles.v1.navfelles.NavEnhet;
import no.nav.dok.brevdata.felles.v1.navfelles.Postadresse;
import no.nav.regoppslag.consumer.norg2.to.Adresse;
import no.nav.regoppslag.consumer.norg2.to.EnhetKontaktinformasjon;
import no.nav.regoppslag.consumer.norg2.to.EnhetNavn;
import no.nav.regoppslag.consumer.norg2.to.Stedsadresse;
import no.nav.regoppslag.service.PostnummerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static no.nav.regoppslag.consumer.norg2.support.Norg2Mapper.POSTBOKSADRESSE;
import static no.nav.regoppslag.consumer.norg2.support.Norg2Mapper.STEDSADRESSE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class Norg2MapperTest {

	private PostnummerService postnummerService;
	private Norg2Mapper norg2Mapper;
	private static final String NAV_ENHET_ID = "123";
	private static final String NAV_ENHET_NAVN = "NAV Husnes";

	private static final String GATENAVN = "Gatenavn";
	private static final String HUSNR = "13";
	private static final String HUSBOKSTAV = "X";
	private static final String POSTNR = "5460";
	private static final String POSTSTED = "HUSNES";
	private static final String POSTBOKSANLEGG = "Postboksanlegg";
	private static final String POSTBOKSNUMMER = "1";

	@BeforeEach
	public void initPostnummer() throws Exception {
		postnummerService = new PostnummerService();
		norg2Mapper = new Norg2Mapper(postnummerService);
	}

	@Test
	public void shouldMapNavEnhetNavn() {
		NavEnhet enhet = new NavEnhet();
		enhet.setEnhetsId(NAV_ENHET_ID);
		norg2Mapper.mapEnhetNavn(createEnhetNavn(NAV_ENHET_NAVN), enhet);
		assertThat(enhet.getEnhetsId(), is(NAV_ENHET_ID));
		assertThat(enhet.getEnhetsNavn(), is(NAV_ENHET_NAVN));
	}

	@Test
	public void shouldMapStedadresseNavEnhet() {
		Postadresse postadresse = createPostadresse(NAV_ENHET_ID);
		norg2Mapper.mapPostadresse(createEnhetNavn(NAV_ENHET_NAVN), createEnhetWithBesoksadresse(), postadresse);
		assertThat(postadresse.getEnhetsId(), is(NAV_ENHET_ID));
		assertThat(postadresse.getEnhetsNavn(), is(NAV_ENHET_NAVN));

		assertThat(postadresse.getAdresse().getAdresselinje1(), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat(postadresse.getAdresse().getPostnummer(), is(POSTNR));
		assertThat(postadresse.getAdresse().getPoststed(), is(POSTSTED));
	}

	@Test
	public void shouldMapPostboksadresseNavEnhet() {
		Postadresse postadresse = createPostadresse(NAV_ENHET_ID);
		norg2Mapper.mapPostadresse(createEnhetNavn(NAV_ENHET_NAVN), createEnhetWithPostboksadresse(NAV_ENHET_NAVN), postadresse);
		assertThat(postadresse.getEnhetsId(), is(NAV_ENHET_ID));
		assertThat(postadresse.getEnhetsNavn(), is(NAV_ENHET_NAVN));

		assertThat(postadresse.getAdresse().getAdresselinje1(), is("Postboks " + POSTBOKSNUMMER + " " + POSTBOKSANLEGG));
		assertThat(postadresse.getAdresse().getPostnummer(), is(POSTNR));
		assertThat(postadresse.getAdresse().getPoststed(), is(POSTSTED));
	}

	@Test
	public void shouldMapBesokAdresseNavEnhet() {
		Postadresse postadresse = createPostadresse(NAV_ENHET_ID);
		norg2Mapper.mapBesokadresse(createEnhetNavn(NAV_ENHET_NAVN), createEnhetWithBesoksadresse(), postadresse);
		assertThat(postadresse.getEnhetsId(), is(NAV_ENHET_ID));

		assertThat(postadresse.getAdresse().getAdresselinje1(), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat(postadresse.getAdresse().getPostnummer(), is(POSTNR));
		assertThat(postadresse.getAdresse().getPoststed(), is(POSTSTED));
	}

	@Test
	public void shouldMapWhenBesokAdresseIsNull() {
		Postadresse postadresse = createPostadresse(NAV_ENHET_ID);
		EnhetKontaktinformasjon enhetWithBesoksadresse = createEnhetWithBesoksadresse();
		enhetWithBesoksadresse.setBesoeksadresse(null);
		norg2Mapper.mapBesokadresse(createEnhetNavn(NAV_ENHET_NAVN), enhetWithBesoksadresse, postadresse);
		assertThat(postadresse.getEnhetsId(), is(NAV_ENHET_ID));

		assertThat(postadresse.getAdresse().getAdresselinje1(), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat(postadresse.getAdresse().getPostnummer(), is(POSTNR));
		assertThat(postadresse.getAdresse().getPoststed(), is(POSTSTED));
	}

	private Postadresse createPostadresse(String enhetId) {
		Postadresse enhet = new Postadresse();
		enhet.setEnhetsId(enhetId);
		return enhet;
	}

	private EnhetKontaktinformasjon createEnhetWithPostboksadresse(String enhetNavn) {
		return EnhetKontaktinformasjon.builder()
				.postadresse(Adresse.builder()
						.type(POSTBOKSADRESSE)
						.postboksanlegg(POSTBOKSANLEGG)
						.postboksnummer(POSTBOKSNUMMER)
						.postnummer(POSTNR)
						.build())
				.build();
	}

	private EnhetNavn createEnhetNavn(String enhetNavn) {
		return EnhetNavn.builder()
				.enhetNr(NAV_ENHET_ID)
				.navn(enhetNavn)
				.build();
	}

	private EnhetKontaktinformasjon createEnhetWithBesoksadresse() {
		return EnhetKontaktinformasjon.builder()
				.postadresse(Adresse.builder()
						.type(STEDSADRESSE)
						.gatenavn(GATENAVN)
						.husnummer(HUSNR)
						.husbokstav(HUSBOKSTAV)
						.postnummer(POSTNR)
						.poststed(POSTSTED)
						.build())
				.besoeksadresse(Stedsadresse.builder()
						.gatenavn(GATENAVN)
						.husnummer(HUSNR)
						.husbokstav(HUSBOKSTAV)
						.postnummer(POSTNR)
						.build())
				.build();
	}

}
