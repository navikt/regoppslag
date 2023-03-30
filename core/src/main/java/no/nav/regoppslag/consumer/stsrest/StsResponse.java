package no.nav.regoppslag.consumer.stsrest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record StsResponse(String accessToken, String tokenType, String expiresIn) {

    @JsonCreator
    public StsResponse(@JsonProperty("access_token") String accessToken, @JsonProperty("token_type") String tokenType, @JsonProperty("expires_in") String expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }
}
