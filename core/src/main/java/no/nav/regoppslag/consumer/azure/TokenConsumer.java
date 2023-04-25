package no.nav.regoppslag.consumer.azure;

public interface TokenConsumer {
	String getClientCredentialToken(String scope);
	String getOnBehalfOfToken(String scope, String token);
}
