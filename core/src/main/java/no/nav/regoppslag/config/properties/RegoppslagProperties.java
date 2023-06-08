package no.nav.regoppslag.config.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("regoppslag")
public class RegoppslagProperties {

	private final Endpoints endpoints = new Endpoints();

	@Data
	@Validated
	public static class Endpoints {
		@NotNull
		private Endpoint norg2;
		@NotNull
		private Endpoint ereg;
		@NotNull
		private Oauth2SecuredEndpoint pdl;
		@NotNull
		private Oauth2SecuredEndpoint dokmet;
		@NotNull
		private Oauth2SecuredEndpoint digdirkrrproxy;
	}

	@Data
	@Validated
	public static class Oauth2SecuredEndpoint {
		@NotEmpty
		private String url;
		@NotEmpty
		private String scope;
	}

	@Data
	@Validated
	public static class Endpoint {
		@NotEmpty
		private String url;
	}

}
