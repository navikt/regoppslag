package no.nav.regoppslag.service;

import static org.mockito.Mockito.mock;

import no.nav.regoppslag.api.KompletterBrevdataRequest;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.treg001.KompletterBrevdataService;
import no.nav.regoppslag.xmlenricher.ElementEnricher;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@RunWith(Parameterized.class)
public class ParseToXMLInService {
	
	ExpectedException exception = ExpectedException.none();
	ElementEnricher elementEnricher = mock(ElementEnricher.class);
	
	private String brevdata = "<ole>brumm</ole>";
	private KompletterBrevdataRequest request = KompletterBrevdataRequest.builder()
			.dokumentTypeId("123")
			.brevdata(brevdata)
			.build();
	private KompletterBrevdataService kompletterBrevdataService = new KompletterBrevdataService(elementEnricher);
	
	@Parameterized.Parameter
	public String brevdataFeilFormat;
	@Parameterized.Parameters
	public static Collection parameters() {
		return Arrays.asList(new String[]{"", "<ole>brumm<ole>", "<ole>brumm/ole>", "\"<ole>brumm<ole>", "<ole><idolet>brumm</ole>"});
	}
	
	/**
	 * HVIS parsing av brevdata til xml- fra streng-format feiler, SÅ skal funksjonell feil kastes
	 */
	@Test
	public void shouldHandleSaxParserException() throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		exception.expect(RegOppslagFunctionalException.class);
		String brevdataFeilFormat = "<ole>brumm<ole>";
		KompletterBrevdataRequest.builder().dokumentTypeId("123").brevdata(brevdataFeilFormat).build();
		kompletterBrevdataService.hentBrevdataFraRegistre(request);
	}
}
