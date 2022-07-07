package no.nav.regoppslag.consumer.azure;

public interface TokenConsumer {
	TokenResponse getClientCredentialToken(String token);
}
