package no.nav.regoppslag.consumer.stsrest;

import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.StsTechnicalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static java.util.Objects.requireNonNull;
import static no.nav.regoppslag.metrics.MetricLabels.RESTSTS_CACHE_NAME;

@Component
public class StsRestConsumer {

	private final RestTemplate restTemplate;
	private final String stsUrl;
	public static final int DELAY_SHORT = 300;
	public static final int MULTIPLIER_SHORT = 2;

	@Autowired
	public StsRestConsumer(@Value("${security-token-service-token.url}") String stsUrl,
						   RestTemplateBuilder restTemplateBuilder,
						   final ServiceuserAlias serviceuserAlias) {
		this.stsUrl = stsUrl;
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.build();
	}

	@Retryable(include = RegOppslagTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Cacheable(RESTSTS_CACHE_NAME)
	public String getOidcToken() {
		try {
			return requireNonNull(restTemplate.getForObject(stsUrl + "?grant_type=client_credentials&scope=openid", StsResponse.class))
					.getAccessToken();
		} catch (HttpStatusCodeException e) {
			throw new StsTechnicalException(String.format("Kall mot STS feilet med status=%s feilmelding=%s.", e.getStatusCode(), e
					.getMessage()), e);
		}
	}
}

