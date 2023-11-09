package no.nav.regoppslag.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
		private String overrideMsGraphUrl;
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
