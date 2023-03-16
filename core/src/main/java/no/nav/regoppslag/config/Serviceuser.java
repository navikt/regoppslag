package no.nav.regoppslag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@ConfigurationProperties("serviceuser")
@Validated
public class Serviceuser {
	@NotEmpty
	private String username;
	@NotEmpty
	private String password;
}
