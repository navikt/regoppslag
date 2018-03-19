package no.nav.regoppslag.comptest;

import static no.nav.regoppslag.rest.RegisteroppslagRestController.KOMPLETTER_BREVDATA_URI_PATH;
import static no.nav.regoppslag.util.TestUtil.resourceUrlToString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.common.io.Resources;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.rest.RegisteroppslagRestController;
import no.nav.regoppslag.service.RegOppslagService;
import no.nav.regoppslag.treg001.RegOppslagResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URL;

/**
 * Komponenttester for å teste tekniske feil og funksjonelle feil gjennom applikasjonen som helhet.
 * Tekniske feil fra avhengigheter blir simulert ved bruk av mock.
 * Og en happypathtest tester at verdier blir returnert som forventet.
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class ValiderOgKompletterBrevdataCompTest {
	private URL request_Url = Resources.getResource("comptest/dummy_request.json");
	private String request = resourceUrlToString(request_Url);
	RegOppslagResponse response = new RegOppslagResponse("<ole>brumm</ole>");
	RegOppslagService regOppslagService = mock(RegOppslagService.class);  //TODO fjern midlertidig mock av service. Bytt ut med wiremock av endepunktene som plugin kjører mot
	RegisteroppslagRestController registeroppslagRestController = new RegisteroppslagRestController(regOppslagService);
	private MockMvc mvc;
	
	@Before
	public void setup() {
		mvc = MockMvcBuilders.standaloneSetup(registeroppslagRestController).build(); //TODO bytt ut med webAppContext
		//TODO: SecurityTestConfig med local authentication eller stubFor LdapConfig authentication eller mock bort ldapTemplate
	}
	
	@Test
	public void shouldKomplettereBrevdata() throws Exception {
		when(regOppslagService.hentBrevdataFraRegistre(any())).thenReturn(response);
		mvc.perform(post(KOMPLETTER_BREVDATA_URI_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(request))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("brevdata").value(response.getBrevdata()))
		;
	}
	
	@Test
	public void shouldThrowFunctionalException() throws Exception {
		String feilmelding = "feilmelding";
		when(regOppslagService.hentBrevdataFraRegistre(any())).thenThrow(new RegOppslagFunctionalException(feilmelding));
		mvc.perform(post(KOMPLETTER_BREVDATA_URI_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(request))
				.andExpect(status().isBadRequest())
		;
		//PS: man får ikke testet med mockmvc at feilmeldingen i responsen ser ut som den skal. Her må man stole på @ExceptionHandler.
	}
	
	@Test
	public void shouldThrowTechnicalException() throws Exception {
		String feilmelding = "feilmelding";
		when(regOppslagService.hentBrevdataFraRegistre(any())).thenThrow(new RegOppslagTechnicalException(feilmelding));
		
		mvc.perform(post(KOMPLETTER_BREVDATA_URI_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(request))
				.andDo(print())
				.andExpect(status().isInternalServerError());
	}
	
	}
