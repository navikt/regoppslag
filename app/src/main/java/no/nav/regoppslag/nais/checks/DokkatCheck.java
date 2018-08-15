package no.nav.regoppslag.nais.checks;

import no.nav.regoppslag.config.fasit.DokumenttypeInfoV3Alias;
import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import no.nav.regoppslag.nais.selftest.AbstractDependencyCheck;
import no.nav.regoppslag.nais.selftest.ApplicationNotReadyException;
import no.nav.regoppslag.nais.selftest.DependencyType;
import no.nav.regoppslag.nais.selftest.Importance;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class DokkatCheck extends AbstractDependencyCheck {
	public static final String DOKKATV3_LABEL = "Dokkat_V3";
	private final RestTemplate restTemplate;
	
	@Inject
	public DokkatCheck(RestTemplateBuilder restTemplateBuilder,
					   HttpComponentsClientHttpRequestFactory requestFactory,
					   DokumenttypeInfoV3Alias dokumenttypeInfoV3Alias,
					   ServiceuserAlias serviceuserAlias) {
		super(DependencyType.REST, DOKKATV3_LABEL, dokumenttypeInfoV3Alias.getUrl(), Importance.WARNING);
		this.restTemplate = restTemplateBuilder
				.requestFactory(requestFactory)
				.rootUri(dokumenttypeInfoV3Alias.getUrl())
				.basicAuthorization(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.setConnectTimeout(dokumenttypeInfoV3Alias.getConnecttimeoutms())
				.setReadTimeout(dokumenttypeInfoV3Alias.getReadtimeoutms())
				.build();
	}
	
	@Override
	protected void doCheck() {
		try {
			restTemplate.getForObject("/ping", String.class);
		} catch (Exception e) {
			throw new ApplicationNotReadyException("Could not ping DokkatV3. ErrorMessage="+e.getMessage(), e);
		}
	}
}