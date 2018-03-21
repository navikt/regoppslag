package no.nav.regoppslag.service;

import static no.nav.regoppslag.util.TestUtil.stringToDocument;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.treg001.Orchestrator;
import no.nav.regoppslag.treg001.RegOppslagRequest;
import no.nav.regoppslag.treg001.RegOppslagResponse;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;
import no.nav.regoppslag.xmlenricher.exceptions.MultiExceptionHolder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class RegOppslagServiceTest {
	private String brevdata = "<ole>brumm</ole>";
	private String brevdataUtfylt = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ole>brumm</ole>";
	
	private RegOppslagRequest request = RegOppslagRequest.builder().dokumentTypeId("123").brevdata(brevdata).build();
	Orchestrator orchestrator = mock(Orchestrator.class);
	private RegOppslagService regOppslagService = new RegOppslagService(orchestrator);
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	/**HVIS request inneholder gyldige verdier, SÅ skal orchestrator kalles og metoden returnere ferdig utfylt/beriket brevdata.*/
	@Test
	public void shouldValiderOgKompletterBrevdata() throws MultiExceptionHolder, XPathExpressionException, MissingPluginException, RegOppslagFunctionalException, RegOppslagTechnicalException, IOException, SAXException, ParserConfigurationException {
		when(orchestrator.process(any(),any())).thenReturn(stringToDocument(brevdataUtfylt));
		RegOppslagResponse actualResponse = regOppslagService.hentBrevdataFraRegistre(request);
		assertEquals(brevdataUtfylt, actualResponse.getBrevdata());
		Mockito.verify(orchestrator, Mockito.times(1)).process(any(),any());
	}
	
	/** HVIS Plugin mangler, SÅ skal teknisk feil kastes */
	@Test
	public void shouldHandleMissingPluginException() throws MultiExceptionHolder, XPathExpressionException, MissingPluginException, RegOppslagFunctionalException, RegOppslagTechnicalException {
		exception.expect(RegOppslagTechnicalException.class);
		when(orchestrator.process(any(),any())).thenThrow(MissingPluginException.class);
		regOppslagService.hentBrevdataFraRegistre(request);
	}
	
	/** HVIS XPathExpression feiler i behandling av brevdata, SÅ skal funksjonell feil kastes */
	@Test
	public void shouldHandleXPathExpressionException() throws RegOppslagFunctionalException, RegOppslagTechnicalException, MultiExceptionHolder, XPathExpressionException, MissingPluginException {
		exception.expect(RegOppslagFunctionalException.class);
		when(orchestrator.process(any(),any())).thenThrow(XPathExpressionException.class);
		regOppslagService.hentBrevdataFraRegistre(request);
	}
	
	/** HVIS parsing av brevdata fra xml- til streng-format feiler, SÅ skal funksjonell feil kastes */
	@Test
	@Ignore("Hvordan trigger jeg TransformerException-feilen?")
	public void shouldHandleTransformerException() throws MultiExceptionHolder, XPathExpressionException, MissingPluginException, RegOppslagFunctionalException, RegOppslagTechnicalException, IOException, SAXException, ParserConfigurationException {
		exception.expect(RegOppslagTechnicalException.class);
		Document document= null;
		when(orchestrator.process(any(),any())).thenReturn(document);
		regOppslagService.hentBrevdataFraRegistre(request);
	}
	
	/** Testbetingelser:
	 * - HVIS både teknisk og funksjonell feil kastes, SÅ skal funksjonell feil kastes til bruker
	 * - HVIS det oppstår en teknisk feil for et   brevdataelement i en berikerplugin SÅ oppdater feillogg teknisk feil OG   fortsett til neste brevdataelement
	 * -HVIS det oppstår en funksjonell feil for   et brevdataelement i en berikerplugin SÅ oppdater feillogg funksjonelle feil   OG fortsett til neste brevdataelement
	 * - HVIS det er opprettet en feillogg funksjonelle feil SÅ SKAL loggen returneres
	 */
	@Test
	public void shouldHandleMultiExceptionHolder() throws RegOppslagFunctionalException, RegOppslagTechnicalException, MultiExceptionHolder, XPathExpressionException, MissingPluginException {
		exception.expect(RegOppslagFunctionalException.class);
		MultiExceptionHolder exceptionHolder = new MultiExceptionHolder("registeroppslag feilet");
		exceptionHolder.setUnhandledErrors(Arrays.asList(new RegOppslagFunctionalException("feil 1"),new RegOppslagTechnicalException("feil 2")));
		when(orchestrator.process(any(),any())).thenThrow(exceptionHolder);
		regOppslagService.hentBrevdataFraRegistre(request);
	}
	
}

