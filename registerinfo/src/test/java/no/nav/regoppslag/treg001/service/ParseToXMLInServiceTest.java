package no.nav.regoppslag.treg001.service;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.treg001.KompletterBrevdataRequest;
import no.nav.regoppslag.treg001.KompletterBrevdataService;
import no.nav.regoppslag.treg001.xmlenricher.ElementEnricher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class ParseToXMLInServiceTest {

	private final ElementEnricher elementEnricher = mock(ElementEnricher.class);

	private final KompletterBrevdataService kompletterBrevdataService = new KompletterBrevdataService(elementEnricher);

	/**
	 * HVIS parsing av brevdata til xml- fra streng-format feiler, SÅ skal funksjonell feil kastes
	 */
	@ParameterizedTest
	@ValueSource(strings = {"", "<ole>brumm<ole>", "<ole>brumm/ole>", "\"<ole>brumm<ole>", "<ole><idolet>brumm</ole>"})
	public void shouldHandleSaxParserException(String brevdata) throws RegOppslagSecurityException {
		var request = KompletterBrevdataRequest.builder()
				.dokumentTypeId("123")
				.brevdata(brevdata)
				.build();

		String brevdataFeilFormat = "<ole>brumm<ole>";
		KompletterBrevdataRequest.builder().dokumentTypeId("123").brevdata(brevdataFeilFormat).build();
		assertThrows(RegOppslagFunctionalException.class, () -> kompletterBrevdataService.hentBrevdataFraRegistre(request));
	}
}
