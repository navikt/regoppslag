package no.nav.regoppslag.config.springdoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import no.nav.regoppslag.config.AppVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Configuration
public class SpringDoc {

	public static final String jwtTokenInfo = "<p>Denne tjenesten krever STS token som authorization header.</p>";

	@Bean
	public OpenAPI api(AppVersion appVersion) {
		return new OpenAPI().info(new Info()
						.title("Regoppslag APIer")
						.description("Dokumentasjon over api'er eksponert av Registeroppslag applikasjonen. Spørsmål? Vi svarer deg på Slack #team_dokumentløsninger"))
				.components(
						new Components()
								.addSecuritySchemes("JWT Authorization",
										new SecurityScheme()
												.type(SecurityScheme.Type.HTTP)
												.scheme("bearer")
												.bearerFormat("JWT")
												.in(SecurityScheme.In.HEADER)
												.description("Eksempel på verdi som skal inn i Value-feltet (Bearer trengs altså ikke å oppgis): 'eyAidH...'")
												.name(HttpHeaders.AUTHORIZATION)
								)

				)
				.addSecurityItem(
						new SecurityRequirement()
								.addList("JWT Authorization")
				);
	}

}
