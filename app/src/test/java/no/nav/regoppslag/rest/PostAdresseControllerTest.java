package no.nav.regoppslag.rest;

import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.rreg003.Adresse;
import no.nav.regoppslag.rreg003.PostadresseRequest;
import no.nav.regoppslag.rreg003.PostadresseResponse;
import no.nav.regoppslag.rreg003.PostadresseService;
import no.nav.regoppslag.treg001.KompletterBrevdataRequest;
import no.nav.regoppslag.treg001.KompletterBrevdataResponse;
import no.nav.regoppslag.treg001.KompletterBrevdataService;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostAdresseControllerTest {
	private PostadresseResponse response;
	PostadresseRequest postadresseRequest = mock(PostadresseRequest.class);
	PostadresseService postadresseService = mock(PostadresseService.class);
	PostAdresseController postadresseController = new PostAdresseController(postadresseService);

	@BeforeEach
	public void setUp() throws RegOppslagSecurityException {
		PostadresseResponse response = PostadresseResponse.builder()
				.adresse(new Adresse())
				.navn("Ola Nordmann")
				.build();
		this.response = response;
		when(postadresseService.postadresseInfo(postadresseRequest)).thenReturn(response);
	}


	@Test
	public void shouldGetPostadresse() throws RegOppslagSecurityException {
		ResponseEntity<PostadresseResponse> actualResponse = postadresseController.postadresse(postadresseRequest);
		assertEquals(response, actualResponse.getBody());
		Mockito.verify(postadresseService, Mockito.times(1)).postadresseInfo(any(PostadresseRequest.class));
	}
}
