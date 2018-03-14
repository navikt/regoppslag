package no.nav.regoppslag.comptest;

import static no.nav.regoppslag.rest.RegisteroppslagRestController.KOMPLETTER_BREVDATA_URI_PATH;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.common.io.Resources;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.rest.RegisteroppslagRestController;
import no.nav.regoppslag.service.RegOppslagService;
import no.nav.regoppslag.treg001.RegOppslagResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class ValiderOgKompletterBrevdataCompTest {
	URL request_Url = Resources.getResource("comptest/dummy_request.json");
	RegOppslagResponse response = new RegOppslagResponse("<ole>brumm</ole>");
	RegOppslagService regOppslagService = mock(RegOppslagService.class);  //TODO fjern midlertidig mock av service. Bytt ut med wiremock av endepunktene som plugin kjører mot
	RegisteroppslagRestController registeroppslagRestController = new RegisteroppslagRestController(regOppslagService);
	private MockMvc mvc;
	
	private static String resourceUrlToString(URL url) {
		try {
			return Resources.toString(url, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Could not convert url to String" + url);
		}
	}
	
	@Before
	public void setup() {
		mvc = MockMvcBuilders.standaloneSetup(registeroppslagRestController).build(); //TODO bytt ut med webAppContext
	}
	
	@Test
	public void shouldKomplettereBrevdata() throws Exception {
		when(regOppslagService.hentBrevdataFraRegistre(any())).thenReturn(response);
		
		mvc.perform(post(KOMPLETTER_BREVDATA_URI_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(resourceUrlToString(request_Url)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("brevdata").value(response.getBrevdata()))
		;
	}
	
	@Test
	@Ignore("Exceptions blir behandlet ulikt av mockMvc relativt spring boot server")
	public void shouldThrowFunctionalException() throws Exception {
		String feilmelding = "feilmelding";
			RegOppslagFunctionalException functionalException = new RegOppslagFunctionalException(feilmelding, new Exception("the cause"));
		when(regOppslagService.hentBrevdataFraRegistre(any())).thenThrow(functionalException);
		
		MvcResult mvcResult= mvc.perform(post(KOMPLETTER_BREVDATA_URI_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(resourceUrlToString(request_Url)))
				.andDo(print()).andExpect(status().isBadRequest()).andReturn();
		assertEquals(feilmelding, mvcResult.getResponse().getErrorMessage()); //FIXME: Hvorfor er error message null? Fordi mockMvc ikke oppretter en container slik som en spring boot servlet gjør. Derfor behandles feilmeldingene ulikt ved bruk av @ExceptionHandler på rest-tjenestene, av mockMvc enn en server behandler dem.
	}
	
}
