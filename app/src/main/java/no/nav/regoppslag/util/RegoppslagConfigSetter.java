package no.nav.regoppslag.util;

import lombok.extern.slf4j.Slf4j;

import java.io.StringReader;
import java.util.Properties;

/**
 * Fetches linebreak seperated configs from env/system property regoppslagconfig_applicationProperties
 * Sets each property as a System property before Spring starts
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
public class RegoppslagConfigSetter {

	protected static final String PROPSOURCE = "regoppslagconfig_applicationProperties";
	public static final String TRUSTSTORE = "javax.net.ssl.trustStore";
	public static final String TRUSTSTOREPASSWORD = "javax.net.ssl.trustStorePassword";
	public static final String APP_TRUSTSTORE_PASSWORD = "APP_TRUSTSTORE_PASSWORD";

	public void configureSsl() {
		if(System.getProperty(TRUSTSTORE) == null) {
			System.setProperty(TRUSTSTORE, "/var/run/secrets/naisd.io/app_truststore_keystore");
		}
		if(System.getProperty(TRUSTSTOREPASSWORD) == null) {
			if(System.getenv(APP_TRUSTSTORE_PASSWORD) == null) {
				throw new IllegalStateException("ENV APP_TRUSTSTORE_PASSWORD was not supplied");
			}
			System.setProperty(TRUSTSTOREPASSWORD, System.getenv(APP_TRUSTSTORE_PASSWORD));
		}
	}

	public void setAppConfig() {
		String applicationPropertiesFromSystemProperties = System.getProperty(PROPSOURCE);
		setSystemProperties(applicationPropertiesFromSystemProperties);
		String applicationPropertiesFromEnv = System.getenv(PROPSOURCE);
		setSystemProperties(applicationPropertiesFromEnv);
	}

	private void setSystemProperties(String applicationProperties) {
		if (applicationProperties != null) {
			Properties properties = new Properties();
			try {
				properties.load(new StringReader(applicationProperties));
				for (Object propKey : properties.keySet()) {
					String oldPropValue = System.getProperty((String) propKey);
					String logSuffix = oldPropValue == null ? "" : ". Overriding existing value=" + oldPropValue;
					log.info("Setting System property={}, value={} from env source " + PROPSOURCE + logSuffix, propKey, properties.get(propKey));
					System.setProperty((String) propKey, properties.getProperty((String) propKey));
				}
			} catch (Exception e) {
				log.error("Unable to read " + PROPSOURCE + " env variable into System.properties", e);
				// continue, the properties could have been supplied from other sources
			}
		}
	}
}
