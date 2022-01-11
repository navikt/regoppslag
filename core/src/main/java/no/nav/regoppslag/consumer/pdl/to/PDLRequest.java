package no.nav.regoppslag.consumer.pdl.to;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class PDLRequest {

    private final String query;
    private final Map<String, Object> variables;

    @JsonCreator
    public PDLRequest(@JsonProperty("query") String query, @JsonProperty("variables") Map<String, Object> variables) {
        this.query = query;
        this.variables = variables;
    }
}
