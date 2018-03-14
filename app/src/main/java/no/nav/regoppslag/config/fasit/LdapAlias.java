package no.nav.regoppslag.config.fasit;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Getter
@Setter
@ToString
@Validated
@Configuration
@ConfigurationProperties("ldap")
public class LdapAlias {

	@NotEmpty
	private String url;
	@NotEmpty
	private String basedn;
	@NotEmpty
	private String username;
	@NotEmpty
	private String password;
	@NotEmpty
	private String requiredroledn;

	@Valid
	private final Serviceuser serviceuser = new Serviceuser();

	@Getter
	@Setter
	@ToString
	public static class Serviceuser {
		@NotEmpty
		private String basedn;
	}
}
