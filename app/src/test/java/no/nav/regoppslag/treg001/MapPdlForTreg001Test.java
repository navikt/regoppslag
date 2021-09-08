package no.nav.regoppslag.treg001;


import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.Person;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.support.OrganisasjonV4Mapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.map.MapPDLResponse;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.FULLT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.KORT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.LAND_UTENLANDSK;
import static no.nav.regoppslag.util.PDLResponseUtil.PERSON_IDENT;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTBOKSNUMMERNAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTKODE_AND_BYSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.SWEDEN_UTENLANDSK;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonUtenlandskAdresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithBostedsadresse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapPdlForTreg001Test {

	private static final String TEMA = "PEN";
	private PdlGraphQLConsumer pdlGraphQLConsumer;
	private OrganisasjonV4Consumer organisasjonV4Consumer;
	private OrganisasjonV4Mapper organisasjonV4Mapper;
	private LandkodeService landkodeService;
	@InjectMocks
	private MapPDLResponse mapPDLResponse;
	private PostnummerService postnummerService;
	@InjectMocks
	private MapPdlForTreg001 pdlForTreg001;
	private MicrometerMetrics micrometerMetrics;


	@BeforeEach
	public void setUp() throws IOException {
		landkodeService = new LandkodeService();
		postnummerService = new PostnummerService();
		pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
		mapPDLResponse = new MapPDLResponse(postnummerService, landkodeService);
		organisasjonV4Consumer = mock(OrganisasjonV4Consumer.class);
		organisasjonV4Mapper = mock(OrganisasjonV4Mapper.class);
		pdlForTreg001 = new MapPdlForTreg001(pdlGraphQLConsumer, mapPDLResponse, landkodeService, organisasjonV4Consumer, organisasjonV4Mapper);

	}

	@Test
	public void shouldMapTreg001MottakerAdresseFraPdl() {
		when(pdlGraphQLConsumer.hentPerson(PERSON_IDENT, TEMA)).thenReturn(createPdlHentPersonWithBostedsadresse());
		Mottaker mottaker = pdlForTreg001.getMottakerFraPdl(TEMA, createMottaker());
		NorskPostadresse adresse = (NorskPostadresse) mottaker.getMottakeradresse();

		assertEquals(PERSON_IDENT, mottaker.getId());
		assertEquals(KORT_NAVN, mottaker.getKortNavn());
		assertEquals(FULLT_NAVN, mottaker.getNavn());
		assertEquals(ADRESSENAVN_1, adresse.getAdresselinje1());
		assertEquals(POSTSTED, adresse.getPoststed());
		assertEquals(POSTNUMMER, adresse.getPostnummer());
	}

	@Test
	public void shouldMapUtenlandiskAdresseFraPdl() {
		when(pdlGraphQLConsumer.hentPerson(PERSON_IDENT, TEMA)).thenReturn(createPdlHentPersonUtenlandskAdresse());
		Mottaker mottaker = pdlForTreg001.getMottakerFraPdl(TEMA, createMottaker());
		UtenlandskPostadresse adresse = (UtenlandskPostadresse) mottaker.getMottakeradresse();

		assertEquals(PERSON_IDENT, mottaker.getId());
		assertEquals(KORT_NAVN, mottaker.getKortNavn());
		assertEquals(FULLT_NAVN, mottaker.getNavn());
		assertEquals(POSTBOKSNUMMERNAVN, adresse.getAdresselinje1());
		assertEquals(POSTKODE_AND_BYSTED, adresse.getAdresselinje2());
		assertEquals(SWEDEN_UTENLANDSK, adresse.getLand());
	}

	private Mottaker createMottaker() {
		Mottaker mottaker = new Person();
		mottaker.setId(PERSON_IDENT);
		mottaker.setBerik(true);
		mottaker.setSpraakkode(Spraakkode.NB);
		mottaker.setTypeKode(AktoerType.PERSON);
		return mottaker;
	}
}