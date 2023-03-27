package no.nav.regoppslag.consumer.azure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TokenResponse (
		@JsonProperty(value = "access_token", required = true) String accessToken
){}
