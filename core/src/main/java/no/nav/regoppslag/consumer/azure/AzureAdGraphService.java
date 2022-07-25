package no.nav.regoppslag.consumer.azure;

import com.microsoft.graph.models.User;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import no.nav.regoppslag.consumer.azure.digdir.AzureProperties;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import okhttp3.Request;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class AzureAdGraphService {


	public static final String HENT_FULLT_NAVN = "hentFulltNavn";
	private final TokenConsumer tokenConsumer;
	private final AzureProperties azureProperties;
	private final ServiceuserAlias serviceuserAlias;

	public static final String MICROSOFT_GRAPH_SCOPE_V2 = "https://graph.microsoft.com/";
	public static final String MICROSOFT_GRAPH_SCOPE_APP = MICROSOFT_GRAPH_SCOPE_V2 + ".default";

	public AzureAdGraphService(TokenConsumer tokenConsumer, AzureProperties azureProperties, ServiceuserAlias serviceuserAlias) {
		this.tokenConsumer = tokenConsumer;
		this.azureProperties = azureProperties;
		this.serviceuserAlias = serviceuserAlias;
	}

	@Cacheable(value = HENT_FULLT_NAVN, key = "#navIdent")
	@Retryable(include = Exception.class, exclude = {RegOppslagFunctionalException.class}, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public String hentFulltNavn(String navIdent) {
		LinkedList<Option> requestOptions = new LinkedList<Option>();
		requestOptions.add(new HeaderOption("ConsistencyLevel", "eventual"));
		requestOptions.add(new QueryOption("$filter", "onPremisesSamAccountName eq '" + navIdent + "'"));
		List<User> res = getGraphClient(getUserToken())
				.users()
				.buildRequest(requestOptions)
				.count(true)
				.select("givenname, surname")
				.get().getCurrentPage();
		if (res.size() != 1) {
			log.info("Did not find single user for navIdent {} ({})", navIdent, res.size());
			return null;
		}
		return res.get(0).givenName + " " + res.get(0).surname;
	}


	private String getUserToken() {
		return tokenConsumer.getClientCredentialToken(MICROSOFT_GRAPH_SCOPE_APP).getAccess_token();
	}

	GraphServiceClient<Request> getGraphClient(String accessToken) {
		return GraphServiceClient.builder()
				.authenticationProvider(url -> CompletableFuture.completedFuture(accessToken))
				.buildClient();
	}

}
