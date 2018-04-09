package no.nav.regoppslag.config.cxf;

import no.nav.regoppslag.config.sts.STSConfig;
import org.apache.cxf.Bus;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import javax.xml.namespace.QName;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract helper class for Cxf Endpoints
 *
 * @author Andreas Skomedal, Visma Consulting.
 */
@Configuration
public abstract class AbstractCxfEndpointConfig {
	public static final int DEFAULT_TIMEOUT = 30_000;

	@Inject
	private Bus bus;
	
	@Inject
	private STSConfig stsConfig;

	private int receiveTimeout = DEFAULT_TIMEOUT;
	private int connectTimeout = DEFAULT_TIMEOUT;
	private final JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();

	public AbstractCxfEndpointConfig() {
		factoryBean.setProperties(new HashMap<>());
		factoryBean.setBus(bus);
	}

	protected void setAdress(String aktoerUrl) {
		factoryBean.setAddress(aktoerUrl);
	}

	protected void setWsdlUrl(String classPathResourceWsdlUrl) {
		factoryBean.setWsdlURL(getUrlFromClasspathResource(classPathResourceWsdlUrl));
	}

	protected void setEndpointName(QName endpointName) {
		factoryBean.setEndpointName(endpointName);
	}

	protected void setServiceName(QName serviceName) {
		factoryBean.setServiceName(serviceName);
	}

	protected void addProperties(Map<String, Object> properties) {
		factoryBean.getProperties().putAll(properties);
	}

	protected void addFeature(Feature feature) {
		factoryBean.getFeatures().add(feature);
	}

	protected void addOutInterceptor(Interceptor<? extends Message> interceptor) {
		factoryBean.getOutInterceptors().add(interceptor);
	}

	protected void addInInterceptor(Interceptor<? extends Message> interceptor) {
		factoryBean.getInInterceptors().add(interceptor);
	}

	protected void addHandler(javax.xml.ws.handler.Handler handler) {
		factoryBean.getHandlers().add(handler);
	}

	protected <T> T createPort(Class<T> portType) {
		factoryBean.getFeatures().add(new TimeoutFeature(receiveTimeout, connectTimeout));
		return factoryBean.create(portType);
	}

	private static String getUrlFromClasspathResource(String classpathResource) {
		URL url = AbstractCxfEndpointConfig.class.getClassLoader().getResource(classpathResource);
		if (url != null) {
			return url.toString();
		}
		throw new IllegalStateException("Failed to find resource: " + classpathResource);
	}

	protected void enableMtom() {
		factoryBean.getProperties().put("mtom-enabled", true);
	}

	protected void setReceiveTimeout(int receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	public void configureSTSSamlToken(Object port){
		stsConfig.configureSTS(port);
	}
}