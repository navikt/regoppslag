package no.nav.regoppslag.config;

import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
public class TomcatConfig {
	// Kopiert fra https://github.com/spring-projects/spring-boot/issues/4657#issuecomment-422561557
	@Bean
	WebServerFactoryCustomizer<ConfigurableTomcatWebServerFactory> tomcatCustomizer() {
		return factory -> {
			if (factory instanceof TomcatServletWebServerFactory) {
				factory.addConnectorCustomizers(gracefulShutdown());
			}
		};
	}

	@Bean
	GracefulShutdown gracefulShutdown() {
		return new GracefulShutdown();
	}

	private static class GracefulShutdown implements TomcatConnectorCustomizer,
			ApplicationListener<ContextClosedEvent> {

		private static final Logger log = LoggerFactory.getLogger(GracefulShutdown.class);

		private volatile Connector connector;

		@Override
		public void customize(Connector connector) {
			this.connector = connector;
		}

		@Override
		public void onApplicationEvent(ContextClosedEvent event) {
			Executor executor = this.connector.getProtocolHandler().getExecutor();
			if (executor instanceof ThreadPoolExecutor) {
				try {
					log.info("Graceful shutdown initiated. 30s until kill.");
					ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
					threadPoolExecutor.shutdown();
					if (!threadPoolExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
						log.warn("Tomcat thread pool did not shut down gracefully within "
								+ "30 seconds. Proceeding with forceful shutdown");
					}
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
