package no.nav.regoppslag.treg002;

import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE1;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE2;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE3;
import static no.nav.regoppslag.util.TestDataUtil.LANDKODE;
import static no.nav.regoppslag.util.TestDataUtil.POSTNUMMER;
import static no.nav.regoppslag.util.TestDataUtil.POSTSTED;
import static no.nav.regoppslag.util.TestDataUtil.createMottaker;
import static no.nav.regoppslag.util.TestDataUtil.createNorskPostadresse;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class AdresseMapperTest {

	@Mock
	private LandkodeService landkodeService;

	@Mock
	private MicrometerMetrics metrics;

	@InjectMocks
	private AdresseMapper adresseMapper;
	
	@Before
	public void setUp(){
		when(landkodeService.finnLandkode(any())).thenReturn(LANDKODE);
	}
	
	@Test
	public void shouldMapWithNorskPostAdresse(){
		HentMottakerOgAdresseResponse.Adresse adresse = adresseMapper.map(createMottaker());
		
		assertThat(adresse.getAdresselinje1(), is(ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is(LANDKODE));
		assertThat(adresse.getPostnummer(), is(POSTNUMMER));
		assertThat(adresse.getPoststed(), is(POSTSTED));
	}
	
	@Test
	public void shouldMapWithUtenlandskPostAdresse(){
		HentMottakerOgAdresseResponse.Adresse adresse = adresseMapper.map(createMottaker(false));
		
		assertThat(adresse.getAdresselinje1(), is(ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is(LANDKODE));
		assertThat(adresse.getPostnummer(), nullValue());
		assertThat(adresse.getPoststed(), nullValue());
	}

	@Test
	public void shouldMapWhenLandkodeIsNull() {
		when(landkodeService.finnLandkode(null)).thenReturn(null);
		Mottaker mottaker = createMottaker();
		NorskPostadresse norskPostadresse = createNorskPostadresse();
		norskPostadresse.setLand(null);
		mottaker.setMottakeradresse(norskPostadresse);
		HentMottakerOgAdresseResponse.Adresse adresse = adresseMapper.map(mottaker);

		assertThat(adresse.getAdresselinje1(), is(ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is("???"));
		assertThat(adresse.getPostnummer(), is(POSTNUMMER));
		assertThat(adresse.getPoststed(), is(POSTSTED));
	}

	
}