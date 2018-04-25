package no.nav.regoppslag.config.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	public static final AuthorizationScope[] AUTHORIZATION_SCOPES = new AuthorizationScope[]{
			new AuthorizationScope("SAML token i BASE64 format", "SAML tokenet blir propagert videre ved kall mot PersonV3 og kreves derfor ved all input hvor det kreves tilgang til PersonV3")};
	
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.ant("/rest/*"))
				.build()
				.useDefaultResponseMessages(true);
	}
	
	private ApiInfo apiInfo() {
		return new ApiInfo(
				"My REST API",
				"Some custom description of API.",
				"API TOS",
				"Terms of service",
				"s", "asd", "as");
	}
	
}
