package no.nav.regoppslag.consumer.azure;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.User;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.config.properties.RegoppslagProperties;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import okhttp3.Request;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static no.nav.regoppslag.config.cache.CacheConfig.HENT_NAV_ANSATT_NAVN;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@Slf4j
public class MsGraphConsumer {
	static final String NAVIDENT_REGEX = "^[a-zA-Z]\\d{6}$";
	static final Pattern NAVIDENT_PATTERN = Pattern.compile(NAVIDENT_REGEX);
	private final GraphServiceClient<Request> graphClient;

	public MsGraphConsumer(AzureProperties azureProperties,
						   RegoppslagProperties regoppslagProperties) {
		this.graphClient = GraphServiceClient.builder()
				.authenticationProvider(new TokenCredentialAuthProvider(new ClientSecretCredentialBuilder()
						.tenantId(azureProperties.appTenantId())
						.clientId(azureProperties.appClientId())
						.clientSecret(azureProperties.appClientSecret())
						.build()))
				.buildClient();
		String overrideMsGraphUrl = regoppslagProperties.getEndpoints().getOverrideMsGraphUrl();
		if (overrideMsGraphUrl != null) {
			this.graphClient.setServiceRoot(overrideMsGraphUrl);
		}
	}

	@Cacheable(value = HENT_NAV_ANSATT_NAVN, key = "#navIdent")
	@Retryable(include = ClientException.class, exclude = {RegOppslagFunctionalException.class}, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public String hentFulltNavn(String navIdent) {
		if (!NAVIDENT_PATTERN.matcher(navIdent).matches()) {
			throw new RegoppslagIllegalArgumentException("navIdent=" + navIdent + " matcher ikke gyldig pattern for NAV ident.", BAD_REQUEST);
		}
		List<User> res = graphClient
				.users()
				.buildRequest(List.of(
						new HeaderOption("ConsistencyLevel", "eventual"),
						new QueryOption("$filter", "onPremisesSamAccountName eq '" + navIdent + "'")
				))
				.count()
				.select("givenName,surname")
				.get().getCurrentPage();

		if (res.size() != 1) {
			throw new RegOppslagIkkeFunnetException(format("Microsoft Entra finner ikke NAV ansatt med navIdent=%s", navIdent), NOT_FOUND);
		}

		return fulltNavn(res);
	}

	private static String fulltNavn(List<User> res) {
		return res.get(0).givenName + " " + res.get(0).surname;
	}
}
