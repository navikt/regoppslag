package no.nav.brevbestilling;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class ApplicationConfig {
	@Bean
	public WebMvcConfigurerAdapter dispatcherServletConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addResourceHandlers(ResourceHandlerRegistry registry) {
				// static content for selftest etc
				registry.addResourceHandler("/internal/*")
						.addResourceLocations("classpath:/web/static/css/");
			}
		};
	}
}
