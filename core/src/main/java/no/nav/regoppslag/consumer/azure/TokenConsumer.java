package no.nav.regoppslag.consumer.azure;

public interface TokenConsumer {
	String getClientCredentialToken(String token);
}
