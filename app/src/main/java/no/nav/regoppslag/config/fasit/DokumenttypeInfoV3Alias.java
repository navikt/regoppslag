package no.nav.regoppslag.config.fasit;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@Getter
@Setter
@ConfigurationProperties("DOKUMENTTYPEINFO_V3")
@Validated
public class DokumenttypeInfoV3Alias {
	@NotEmpty
	private String url;
	@Min(1)
	private int readtimeoutms;
	@Min(1)
	private int connecttimeoutms;
}