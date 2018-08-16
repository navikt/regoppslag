package no.nav.regoppslag.treg001;

import static no.nav.regoppslag.util.TestUtil.stringToDocument;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.regoppslag.api.KompletterBrevdataRequest;
import no.nav.regoppslag.api.KompletterBrevdataResponse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.xmlenricher.ElementEnricher;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class KompletterBrevdataServiceTest {
	private String brevdata = "<ole>brumm</ole>";
	private String brevdataUtfylt = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ole>brumm</ole>";
	
	private KompletterBrevdataRequest request = KompletterBrevdataRequest.builder()
			.dokumentTypeId("123")
			.brevdata(brevdata)
			.build();
	ElementEnricher elementEnricher = mock(ElementEnricher.class);
	private KompletterBrevdataService kompletterBrevdataService = new KompletterBrevdataService(elementEnricher);
	private KompletterBrevdataRequest illegalRequest = KompletterBrevdataRequest.builder()
			.dokumentTypeId("123")
			.brevdata("<ole>brumm</oleIllegal>")
			.build();

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	/**HVIS request inneholder gyldige verdier, SÅ skal elementEnricher kalles og metoden returnere ferdig utfylt brevdata.*/
	@Test
	public void shouldKompletterBrevdata() throws XPathExpressionException, MissingPluginException, RegOppslagFunctionalException, RegOppslagTechnicalException, IOException, SAXException, ParserConfigurationException, RegOppslagSecurityException {
		when(elementEnricher.process(any(),any())).thenReturn(stringToDocument(brevdataUtfylt));
		KompletterBrevdataResponse actualResponse = kompletterBrevdataService.hentBrevdataFraRegistre(request);
		assertEquals(brevdataUtfylt, actualResponse.getBrevdata());
		Mockito.verify(elementEnricher, Mockito.times(1)).process(any(),any());
	}
	
	/** HVIS Plugin mangler, SÅ skal teknisk feil kastes */
	@Test
	public void shouldHandleMissingPluginException() throws XPathExpressionException, MissingPluginException, RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		exception.expect(RegOppslagTechnicalException.class);
		when(elementEnricher.process(any(),any())).thenThrow(MissingPluginException.class);
		kompletterBrevdataService.hentBrevdataFraRegistre(request);
	}
	
	/** HVIS XPathExpression feiler i behandling av brevdata, SÅ skal funksjonell feil kastes */
	@Test
	public void shouldHandleXPathExpressionException() throws RegOppslagFunctionalException, RegOppslagTechnicalException, XPathExpressionException, MissingPluginException, RegOppslagSecurityException {
		exception.expect(RegOppslagFunctionalException.class);
		when(elementEnricher.process(any(),any())).thenThrow(XPathExpressionException.class);
		kompletterBrevdataService.hentBrevdataFraRegistre(request);
	}
	
	/** HVIS parsing av brevdata fra xml- til streng-format feiler, SÅ skal funksjonell feil kastes */
	@Test
	public void shouldHandleTransformerException() throws XPathExpressionException, MissingPluginException, RegOppslagFunctionalException, RegOppslagTechnicalException, IOException, SAXException, RegOppslagSecurityException {
		exception.expect(RegOppslagFunctionalException.class);
		exception.expectMessage("org.xml.sax.SAXParseException");
		Document document= null;
		when(elementEnricher.process(any(),any())).thenReturn(document);
		kompletterBrevdataService.hentBrevdataFraRegistre(illegalRequest);
	}
}

