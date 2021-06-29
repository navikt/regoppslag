package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PDLHentPersonResponse {

    private List<PdlError> errors;
    private PDLHentPerson data;

    @Data
    @Builder
    public static class PDLHentPerson {
        private HentPerson hentPerson;
    }

    @Data
    public static class PdlError {
        private String message;
        private PdlErrorExtensionTo extensions;
    }

    @Data
    static class PdlErrorExtensionTo {
        private String code;
        private ErrorDetails details;
        private String classification;
    }

    @Data
    static class ErrorDetails {
        private String type;
        private String cause;
        private String policy;
    }
}
