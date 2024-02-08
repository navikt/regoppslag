package no.nav.regoppslag.treg001;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.treg001.xmlenricher.ElementEnricher;
import no.nav.regoppslag.treg001.xmlenricher.exceptions.MissingPluginException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

import static no.nav.regoppslag.util.TestUtil.stringToDocument;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KompletterBrevdataServiceTest {

	private static final  String brevdata = "<ole>brumm</ole>";
	private static final  String brevdataUtfylt = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ole>brumm</ole>";

	private final KompletterBrevdataRequest request = KompletterBrevdataRequest.builder()
			.dokumentTypeId("123")
			.brevdata(brevdata)
			.build();
	private final KompletterBrevdataRequest illegalRequest = KompletterBrevdataRequest.builder()
			.dokumentTypeId("123")
			.brevdata("<ole>brumm</oleIllegal>")
			.build();

	ElementEnricher elementEnricher;
	@InjectMocks
	private KompletterBrevdataService kompletterBrevdataService;


	@BeforeEach
	public void setUp() {
		elementEnricher = mock(ElementEnricher.class);
		kompletterBrevdataService = new KompletterBrevdataService(elementEnricher);
	}

	/**
	 * HVIS request inneholder gyldige verdier, SÅ skal elementEnricher kalles og metoden returnere ferdig utfylt brevdata.
	 */
	@Test
	public void shouldKompletterBrevdata() throws XPathExpressionException, MissingPluginException, IOException, SAXException, ParserConfigurationException, RegOppslagSecurityException {
		when(elementEnricher.process(any(), anyString())).thenReturn(stringToDocument(brevdataUtfylt));

		KompletterBrevdataResponse actualResponse = kompletterBrevdataService.hentBrevdataFraRegistre(request);

		assertNotNull(actualResponse.getBrevdata());
		verify(elementEnricher, times(1)).process(any(), any());
	}

	/**
	 * HVIS Plugin mangler, SÅ skal teknisk feil kastes
	 */
	@Test
	public void shouldHandleMissingPluginException() throws XPathExpressionException, MissingPluginException, RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		when(elementEnricher.process(any(), anyString())).thenThrow(MissingPluginException.class);

		assertThrows(RegOppslagFunctionalException.class,
				() -> kompletterBrevdataService.hentBrevdataFraRegistre(illegalRequest));
	}

	/**
	 * HVIS XPathExpression feiler i behandling av brevdata, SÅ skal funksjonell feil kastes
	 */
	@Test
	public void shouldHandleXPathExpressionException() throws RegOppslagFunctionalException, RegOppslagTechnicalException, XPathExpressionException, MissingPluginException, RegOppslagSecurityException {
		when(elementEnricher.process(any(), anyString())).thenThrow(XPathExpressionException.class);

		assertThrows(RegOppslagFunctionalException.class,
				() -> kompletterBrevdataService.hentBrevdataFraRegistre(illegalRequest));
	}

	/**
	 * HVIS parsing av brevdata fra xml- til streng-format feiler, SÅ skal funksjonell feil kastes
	 */
	@Test
	public void shouldHandleTransformerException() throws XPathExpressionException, MissingPluginException, RegOppslagSecurityException {
		Document document = null;
		when(elementEnricher.process(any(), anyString())).thenReturn(document);

		assertThrows(RegOppslagFunctionalException.class,
				() -> kompletterBrevdataService.hentBrevdataFraRegistre(illegalRequest), "org.xml.sax.SAXParseException");
	}
}

