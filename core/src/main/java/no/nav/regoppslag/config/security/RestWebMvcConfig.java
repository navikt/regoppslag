package no.nav.regoppslag.config.security;

import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Configuration
public class RestWebMvcConfig implements WebMvcConfigurer {

	private final TokenValidationContextHolder tokenValidationContextHolder;

	public RestWebMvcConfig(TokenValidationContextHolder tokenValidationContextHolder) {
		this.tokenValidationContextHolder = tokenValidationContextHolder;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new MDCHandlerInterceptor(tokenValidationContextHolder))
				.addPathPatterns("/rest/**");
		registry.addInterceptor(new SecurityContextHandlerInterceptor(tokenValidationContextHolder))
				.addPathPatterns("/rest/**");
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.defaultContentType(APPLICATION_JSON);
	}
}
