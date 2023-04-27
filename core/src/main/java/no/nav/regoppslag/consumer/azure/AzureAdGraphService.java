package no.nav.regoppslag.consumer.azure;

import com.microsoft.graph.models.User;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import okhttp3.Request;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static no.nav.regoppslag.config.cache.CacheConfig.HENT_NAV_ANSATT_NAVN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@Slf4j
public class AzureAdGraphService {

	public static final String BRUKER_IKKE_FUNNET = "Azure AD - Bruker ikke funnet";
	public static final String MICROSOFT_GRAPH_SCOPE_V2 = "https://graph.microsoft.com/";
	public static final String MICROSOFT_GRAPH_SCOPE_APP = MICROSOFT_GRAPH_SCOPE_V2 + ".default";

	private final AzureTokenConsumer azureTokenConsumer;

	public AzureAdGraphService(AzureTokenConsumer azureTokenConsumer) {
		this.azureTokenConsumer = azureTokenConsumer;
	}

	@Cacheable(value = HENT_NAV_ANSATT_NAVN, key = "#navIdent")
	@Retryable(include = Exception.class, exclude = {RegOppslagFunctionalException.class}, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public String hentFulltNavn(String navIdent) {
		LinkedList<Option> requestOptions = new LinkedList<>();
		requestOptions.add(new HeaderOption("ConsistencyLevel", "eventual"));
		requestOptions.add(new QueryOption("$filter", "onPremisesSamAccountName eq '" + navIdent + "'"));

		List<User> res = getGraphClient(getUserToken())
				.users()
				.buildRequest(requestOptions)
				.count(true)
				.select("givenname, surname")
				.get().getCurrentPage();

		if (res.size() != 1) {
			throw new RegOppslagIkkeFunnetException(String.format("Azure AD finner ikke bruker med ident=%s. %s", navIdent, BRUKER_IKKE_FUNNET), NOT_FOUND);
		}

		return res.get(0).givenName + " " + res.get(0).surname;
	}


	private String getUserToken() {
		return azureTokenConsumer.getClientCredentialToken(MICROSOFT_GRAPH_SCOPE_APP);
	}

	GraphServiceClient<Request> getGraphClient(String accessToken) {
		return GraphServiceClient.builder()
				.authenticationProvider(url -> CompletableFuture.completedFuture(accessToken))
				.buildClient();
	}

}
