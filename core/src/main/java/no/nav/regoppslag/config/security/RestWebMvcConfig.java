package no.nav.regoppslag.config.security;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
	}
}
