package no.nav.regoppslag.fasit;

import static no.nav.modig.security.ws.AbstractSAMLOutInterceptor.SYSTEM_PROPERTY_APPCERT_ALIAS;
import static no.nav.modig.security.ws.AbstractSAMLOutInterceptor.SYSTEM_PROPERTY_APPCERT_FILE;
import static no.nav.modig.security.ws.AbstractSAMLOutInterceptor.SYSTEM_PROPERTY_APPCERT_PASSWORD;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Getter
@Setter
@ToString
@ConfigurationProperties("SRVDOKMOTALTINN_CERT")
@Validated
public class NavAppCertAlias {
	@NotEmpty
	private String keystore;
	@NotEmpty
	private String keystorealias;
	@NotEmpty
	private String password;

	@PostConstruct
	public void postConstruct() {
		System.setProperty(SYSTEM_PROPERTY_APPCERT_FILE, keystore);
		System.setProperty(SYSTEM_PROPERTY_APPCERT_ALIAS, keystorealias);
		System.setProperty(SYSTEM_PROPERTY_APPCERT_PASSWORD, password);
	}
}