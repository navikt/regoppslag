package no.nav.regoppslag.fasit;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Getter
@Setter
@ToString
@ConfigurationProperties("VIRKSOMHET_ORGANISASJON_V4")
@Validated
public class OrganisasjonV4Alias {
	@NotEmpty
	private String endpointurl;
	private String description;
	@Min(1)
	private int readtimeoutms;
	@Min(1)
	private int connecttimeoutms;
}
