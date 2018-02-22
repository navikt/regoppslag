package no.nav.regoppslag;

import no.nav.regoppslag.util.RegoppslagConfigSetter;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.EndpointWebMvcAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.ManagementServerPropertiesAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.PublicMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpEncodingAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = {
		CxfAutoConfiguration.class,
		DispatcherServletAutoConfiguration.class,
		EmbeddedServletContainerAutoConfiguration.class,
		EndpointWebMvcAutoConfiguration.class,
		ErrorMvcAutoConfiguration.class,
		HttpEncodingAutoConfiguration.class,
		HttpMessageConvertersAutoConfiguration.class,
		JacksonAutoConfiguration.class,
		JmsAutoConfiguration.class,
		MultipartAutoConfiguration.class,
		ServerPropertiesAutoConfiguration.class,
		ValidationAutoConfiguration.class,
		WebClientAutoConfiguration.class,
		WebMvcAutoConfiguration.class,
		ManagementServerPropertiesAutoConfiguration.class,
		CamelAutoConfiguration.class,
		PublicMetricsAutoConfiguration.class,
		ApplicationConfig.class})
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		RegoppslagConfigSetter configSetter = new RegoppslagConfigSetter();
		configSetter.configureSsl();
		configSetter.setAppConfig();
		SpringApplication.run(Application.class, args);
	}

}
