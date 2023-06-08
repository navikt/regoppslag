package no.nav.regoppslag.consumer.azure;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Konfigurert av naiserator. https://doc.nais.io/security/auth/azure-ad/#runtime-variables-credentials
 */
@Data
@ConfigurationProperties("azure")
@Validated
public class AzureProperties {
	@NotEmpty
	private String openidConfigTokenEndpoint;
	@NotEmpty
	private String appClientId;
	@NotEmpty
	private String appClientSecret;
}
