package no.nav.regoppslag.consumer.azure.digdir;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Konfigurert av naiserator. https://doc.nais.io/security/auth/azure-ad/#runtime-variables-credentials
 */
@Data
@ConfigurationProperties("azure.app")
@Validated
public class AzureProperties {
	@NotEmpty
	private String tokenUrl;
	@NotEmpty
	private String clientId;
	@NotEmpty
	private String clientSecret;
	@NotEmpty
	private String tenantId;
	@NotEmpty
	private String wellKnownUrl;
	@NotEmpty
	private String scopeDigdirKrr;
}
