package no.nav.regoppslag.config;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@Getter
@Setter
@ConfigurationProperties("dokumenttypeinfo")
@Validated
public class DokumenttypeInfoProperties {
	@NotEmpty
	private String url;
	@Min(1)
	private int readtimeoutms;
	@Min(1)
	private int connecttimeoutms;
}