package no.nav.regoppslag.treg002;

import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE1;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE2;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE3;
import static no.nav.regoppslag.util.TestDataUtil.LANDKODE;
import static no.nav.regoppslag.util.TestDataUtil.POSTNUMMER;
import static no.nav.regoppslag.util.TestDataUtil.POSTSTED;
import static no.nav.regoppslag.util.TestDataUtil.createMottaker;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import no.nav.regoppslag.common.Adresse;
import no.nav.regoppslag.service.LandkodeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class AdresseMapperTest {

	@Mock
	private LandkodeService landkodeService;
	
	@InjectMocks
	private AdresseMapper adresseMapper;
	
	@Before
	public void setUp(){
		when(landkodeService.finnLandkode(any())).thenReturn(LANDKODE);
	}
	
	@Test
	public void shouldMapWithNorskPostAdresse(){
		Adresse adresse = adresseMapper.map(createMottaker());
		
		assertThat(adresse.getAdresselinje1(), is(ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is(LANDKODE));
		assertThat(adresse.getPostnummer(), is(POSTNUMMER));
		assertThat(adresse.getPoststed(), is(POSTSTED));
	}
	
	@Test
	public void shouldMapWithUtenlandskPostAdresse(){
		Adresse adresse = adresseMapper.map(createMottaker(false));
		
		assertThat(adresse.getAdresselinje1(), is(ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is(LANDKODE));
		assertThat(adresse.getPostnummer(), nullValue());
		assertThat(adresse.getPoststed(), nullValue());
	}

	
}