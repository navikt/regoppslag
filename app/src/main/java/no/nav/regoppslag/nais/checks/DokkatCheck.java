package no.nav.regoppslag.nais.checks;

import no.nav.regoppslag.config.fasit.DokumenttypeInfoV3Alias;
import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import no.nav.regoppslag.nais.naiscontract.support.AbstractNaisIsReadyTest;
import no.nav.regoppslag.nais.naiscontract.support.ApplicationNotReadyException;
import no.nav.regoppslag.nais.naiscontract.support.Ping;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class DokkatCheck extends AbstractNaisIsReadyTest {
	public static final String DOKKATV3_LABEL = "DokkatV3";
	private final RestTemplate restTemplate;
	
	@Inject
	public DokkatCheck(RestTemplateBuilder restTemplateBuilder,
					   HttpComponentsClientHttpRequestFactory requestFactory,
					   DokumenttypeInfoV3Alias dokumenttypeInfoV3Alias,
					   ServiceuserAlias serviceuserAlias) {
		super(Ping.Type.REST, "DOKKAT", dokumenttypeInfoV3Alias.getUrl(), DOKKATV3_LABEL);
		this.restTemplate = restTemplateBuilder
				.requestFactory(requestFactory)
				.rootUri(dokumenttypeInfoV3Alias.getUrl())
				.basicAuthorization(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.setConnectTimeout(dokumenttypeInfoV3Alias.getConnecttimeoutms())
				.setReadTimeout(dokumenttypeInfoV3Alias.getReadtimeoutms())
				.build();
	}
	
	@Override
	public boolean isVital() {
		return true;
	}
	
	@Override
	protected void doCheck() {
		try {
			restTemplate.getForObject("/ping", String.class);
		} catch (Exception e) {
			throw new ApplicationNotReadyException("Could not ping DokkatV3", e);
		}
	}
}