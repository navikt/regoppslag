package no.nav.regoppslag.config.fasit;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

/**
 * @author Ketill Fenne, Visma Consulting
 */
@Getter
@Setter
@ToString
@ConfigurationProperties("VIRKSOMHET_ORGANISASJON_KONTAKTINFORMASJON_V2")
@Validated
public class OrganisasjonEnhetKontaktinformasjonV1Alias {
	@NotEmpty
	private String endpointurl;
	private String description;
	@Min(1)
	private int readtimeoutms;
	@Min(1)
	private int connecttimeoutms;
}
