package no.nav.regoppslag.consumer.azure;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.ApiException;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.config.properties.RegoppslagProperties;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
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

	private static final String NAVIDENT_REGEX = "^[a-zA-Z]\\d{6}$";
	private static final Pattern NAVIDENT_PATTERN = Pattern.compile(NAVIDENT_REGEX);

	private final GraphServiceClient graphClient;

	public MsGraphConsumer(AzureProperties azureProperties,
						   RegoppslagProperties regoppslagProperties) {

		ClientSecretCredential tokenCredential = new ClientSecretCredentialBuilder()
				.tenantId(azureProperties.appTenantId())
				.clientId(azureProperties.appClientId())
				.clientSecret(azureProperties.appClientSecret())
				.build();
		this.graphClient = new GraphServiceClient(tokenCredential);

		String overrideMsGraphUrl = regoppslagProperties.getEndpoints().getOverrideMsGraphUrl();
		if (overrideMsGraphUrl != null) {
			this.graphClient.getRequestAdapter().setBaseUrl(overrideMsGraphUrl);
		}
	}

	@Cacheable(value = HENT_NAV_ANSATT_NAVN, key = "#navIdent")
	@Retryable(retryFor = ApiException.class, noRetryFor = RegOppslagFunctionalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public String hentFulltNavn(String navIdent) {

		if (!NAVIDENT_PATTERN.matcher(navIdent).matches()) {
			throw new RegoppslagIllegalArgumentException("navIdent=" + navIdent + " matcher ikke gyldig pattern for NAV ident.", BAD_REQUEST);
		}

		List<User> users = graphClient
				.users()
				.get(requestConfiguration -> {
					requestConfiguration.headers.add("ConsistencyLevel", "eventual");
					requestConfiguration.queryParameters.filter = "onPremisesSamAccountName eq '" + navIdent + "'";
					requestConfiguration.queryParameters.select = new String[]{"givenName", "surname"};
					requestConfiguration.queryParameters.count = true;
				})
				.getValue();

		if (users == null || users.size() != 1) {
			throw new RegOppslagIkkeFunnetException(format("Microsoft Entra finner ikke NAV ansatt med navIdent=%s", navIdent), NOT_FOUND);
		}

		return fulltNavn(users.getFirst());
	}

	private static String fulltNavn(User user) {
		return user.getGivenName() + " " + user.getSurname();
	}
}
