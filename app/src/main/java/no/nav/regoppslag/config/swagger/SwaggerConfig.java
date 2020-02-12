package no.nav.regoppslag.config.swagger;

import no.nav.regoppslag.config.AppVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.ModelRendering;
import springfox.documentation.swagger.web.OperationsSorter;
import springfox.documentation.swagger.web.TagsSorter;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
	public static final String samlTokenInfo = "<p>Ved input som krever oppslag i personopplysninger så krever denne tjenesten SAML assertion token som authorization header. Tokenet blir da brukt ved kall mot PersonV3, og hvis brukeren ikke har tilgang vil kallet mot PersonV3 returnere sikkerhetsfeil. " +
			"SAML assertion tokenet er del av SAML authentication headeret som starter og slutter med \"saml2:Assertion\". Dette tokenet må konverteres til BASE64 og legges som header i formatet: Key=Authorization, Value=SAML \"SAML assertion token konvertert til BASE64\"</p>";
	
	@Bean
	public Docket api(AppVersion appVersion) {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.ant("/rest/*"))
				.build()
				.useDefaultResponseMessages(false)
				.apiInfo(apiInfo(appVersion));
	}
	
	@Bean
	UiConfiguration uiConfig() {
		return UiConfigurationBuilder.builder()
				.deepLinking(true)
				.displayOperationId(false)
				.defaultModelsExpandDepth(1)
				.defaultModelExpandDepth(1)
				.defaultModelRendering(ModelRendering.EXAMPLE)
				.displayRequestDuration(false)
				.docExpansion(DocExpansion.NONE)
				.filter(false)
				.maxDisplayedTags(null)
				.operationsSorter(OperationsSorter.ALPHA)
				.showExtensions(false)
				.tagsSorter(TagsSorter.ALPHA)
				.validatorUrl(null)
				.build();
	}
	
	private ApiInfo apiInfo(AppVersion appVersion) {
		return new ApiInfo(
				"Registeroppslag",
				" ",
				appVersion.getVersion(),
				"",
				new Contact("Team Dokument", "", ""),
				"", "", Collections.EMPTY_LIST);
	}
}
