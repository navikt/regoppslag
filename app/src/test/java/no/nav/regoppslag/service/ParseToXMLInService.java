package no.nav.regoppslag.service;

import static org.mockito.Mockito.mock;

import no.nav.regoppslag.common.ValiderOgKompletterBrevdataRequest;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.treg001.KompletterBrevdataService;
import no.nav.regoppslag.treg001.Orchestrator;
import no.nav.regoppslag.xmlenricher.exceptions.MultiExceptionHolder;
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
	Orchestrator orchestrator = mock(Orchestrator.class);
	
	private String brevdata = "<ole>brumm</ole>";
	private ValiderOgKompletterBrevdataRequest request = ValiderOgKompletterBrevdataRequest.builder().dokumentTypeId("123").brevdata(brevdata).build();
	private KompletterBrevdataService kompletterBrevdataService = new KompletterBrevdataService(orchestrator);
	
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
	public void shouldHandleSaxParserException() throws RegOppslagFunctionalException, RegOppslagTechnicalException, MultiExceptionHolder {
		exception.expect(RegOppslagFunctionalException.class);
		String brevdataFeilFormat = "<ole>brumm<ole>";
		ValiderOgKompletterBrevdataRequest.builder().dokumentTypeId("123").brevdata(brevdataFeilFormat).build();
		kompletterBrevdataService.hentBrevdataFraRegistre(request);
	}
}
