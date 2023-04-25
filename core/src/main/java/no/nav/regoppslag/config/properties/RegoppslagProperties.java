package no.nav.regoppslag.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.net.URI;

@Data
@Validated
@ConfigurationProperties("regoppslag")
public class RegoppslagProperties {

	private final Serviceuser serviceuser = new Serviceuser();
	private final Endpoints endpoints = new Endpoints();

	@Data
	@Validated
	public static class Serviceuser {
		@NotEmpty
		private String username;
		@NotEmpty
		private String password;
	}

	@Data
	@Validated
	public static class Endpoints {
		/**
		 * URL til PDL (Persondataløsningen).
		 */
		@NotNull
		private Oauth2SecuredEndpoint pdl;
		/**
		 * URL til dokmet.
		 */
		@NotNull
		private Oauth2SecuredEndpoint dokmet;
		/**
		 * URL til digdir-krr-proxy.
		 */
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

}
