package no.nav.regoppslag.consumer.azure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Konfigurert av naiserator. https://doc.nais.io/security/auth/azure-ad/#runtime-variables-credentials
 */
@ConfigurationProperties("azure")
@Validated
public record AzureProperties(
		@NotEmpty String appTenantId,
		@NotEmpty String appClientId,
		@NotEmpty String appClientSecret,
		@NotEmpty String openidConfigTokenEndpoint
) {
}
