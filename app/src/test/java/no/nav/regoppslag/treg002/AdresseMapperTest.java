package no.nav.regoppslag.treg002;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE1;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE2;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE3;
import static no.nav.regoppslag.util.TestDataUtil.LANDKODE;
import static no.nav.regoppslag.util.TestDataUtil.POSTNUMMER;
import static no.nav.regoppslag.util.TestDataUtil.POSTSTED;
import static no.nav.regoppslag.util.TestDataUtil.SVENSK_LANDKODE;
import static no.nav.regoppslag.util.TestDataUtil.UTENLANDSK_ADRESSELINJE1;
import static no.nav.regoppslag.util.TestDataUtil.UTENLANDSK_ADRESSELINJE2;
import static no.nav.regoppslag.util.TestDataUtil.UTENLANDSK_ADRESSELINJE3;
import static no.nav.regoppslag.util.TestDataUtil.createMottaker;
import static no.nav.regoppslag.util.TestDataUtil.createNorskPostadresse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ExtendWith(MockitoExtension.class)
public class AdresseMapperTest {

    @Mock
    private LandkodeService landkodeService;

    @Mock
    private MicrometerMetrics metrics;

    @InjectMocks
    private AdresseMapper adresseMapper;

    @BeforeEach
    public void setUp() {
        when(landkodeService.finnLandkode(any())).thenReturn(LANDKODE);
    }

    @Test
    public void shouldMapWithNorskPostAdresse() {
        HentMottakerOgAdresseResponse.Adresse adresse = adresseMapper.map(createMottaker());
        assertThat(adresse.getAdresselinje1(), is(ADRESSELINJE1));
        assertThat(adresse.getAdresselinje2(), is(ADRESSELINJE2));
        assertThat(adresse.getAdresselinje3(), is(ADRESSELINJE3));
        assertThat(adresse.getLandkode(), is(LANDKODE));
        assertThat(adresse.getPostnummer(), is(POSTNUMMER));
        assertThat(adresse.getPoststed(), is(POSTSTED));
    }

    @Test
    public void shouldMapWithUtenlandskPostAdresse() {
        when(landkodeService.finnLandkode(any())).thenReturn(SVENSK_LANDKODE);
        HentMottakerOgAdresseResponse.Adresse adresse = adresseMapper.map(createMottaker(false));

        assertThat(adresse.getAdresselinje1(), is(UTENLANDSK_ADRESSELINJE1));
        assertThat(adresse.getAdresselinje2(), is(UTENLANDSK_ADRESSELINJE2));
        assertThat(adresse.getAdresselinje3(), is(UTENLANDSK_ADRESSELINJE3));
        assertThat(adresse.getLandkode(), is(SVENSK_LANDKODE));
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