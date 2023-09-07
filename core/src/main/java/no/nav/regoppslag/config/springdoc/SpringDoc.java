package no.nav.regoppslag.config.springdoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER;
import static io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Configuration
public class SpringDoc {

	public static final String jwtTokenInfo = "<p>Denne tjenesten krever STS token som authorization header.</p>";

	@Bean
	public OpenAPI api() {
		return new OpenAPI().info(new Info()
						.title("Regoppslag APIer")
						.version("1.0.0")
						.description("Dokumentasjon over api'er eksponert av Registeroppslag applikasjonen. Spørsmål? Vi svarer deg på Slack #team_dokumentløsninger"))
				.components(
						new Components()
								.addSecuritySchemes("JWT-Authorization",
										new SecurityScheme()
												.type(HTTP)
												.scheme("bearer")
												.bearerFormat("JWT")
												.in(HEADER)
												.description("Eksempel på verdi som skal inn i Value-feltet (Bearer trengs altså ikke å oppgis): 'eyAidH...'")
												.name(AUTHORIZATION)
								)

				)
				.addSecurityItem(
						new SecurityRequirement()
								.addList("JWT-Authorization")
				);
	}

}
