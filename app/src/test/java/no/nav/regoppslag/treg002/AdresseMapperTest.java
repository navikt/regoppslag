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

import no.nav.regoppslag.common.Adresse;
import org.junit.Test;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class AdresseMapperTest {

	
	@Test
	public void shouldMapWithNorskPostAdresse(){
		Adresse adresse = AdresseMapper.map(createMottaker());
		
		assertThat(adresse.getAdresselinje1(), is(ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is(LANDKODE));
		assertThat(adresse.getPostnummer(), is(POSTNUMMER));
		assertThat(adresse.getPoststed(), is(POSTSTED));
	}
	
	@Test
	public void shouldMapWithUtenlandskPostAdresse(){
		Adresse adresse = AdresseMapper.map(createMottaker(false));
		
		assertThat(adresse.getAdresselinje1(), is(ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is(LANDKODE));
		assertThat(adresse.getPostnummer(), nullValue());
		assertThat(adresse.getPoststed(), nullValue());
	}

	
}