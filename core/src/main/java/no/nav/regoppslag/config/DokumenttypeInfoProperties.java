package no.nav.regoppslag.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

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