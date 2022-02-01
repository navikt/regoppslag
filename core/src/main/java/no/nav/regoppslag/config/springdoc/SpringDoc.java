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
	public static final String samlTokenInfo = "<p>Ved input som krever oppslag i personopplysninger så krever denne tjenesten SAML assertion token som authorization header. Tokenet blir da brukt ved kall mot PersonV3, og hvis brukeren ikke har tilgang vil kallet mot PersonV3 returnere sikkerhetsfeil. " +
			"SAML assertion tokenet er del av SAML authentication headeret som starter og slutter med \"saml2:Assertion\". Dette tokenet må konverteres til BASE64 og legges som header i formatet: Key=Authorization, Value=SAML \"SAML assertion token konvertert til BASE64\"</p>";

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
								.addSecuritySchemes("SAML Authorization",
										new SecurityScheme()
												.type(SecurityScheme.Type.APIKEY)
												.in(SecurityScheme.In.HEADER)
												.description("Eksempel på verdi som skal inn i Value-feltet: 'SAML PHNhb...'")
												.name(HttpHeaders.AUTHORIZATION)
								)

				)
				.addSecurityItem(
						new SecurityRequirement()
								.addList("JWT Authorization")
								.addList("SAML Authorization")
				);
	}

}
