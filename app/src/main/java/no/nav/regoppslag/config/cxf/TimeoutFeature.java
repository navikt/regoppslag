package no.nav.regoppslag.config.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.asyncclient.AsyncHTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

/**
 * Timeoutfeature for å konfigurer timeout på cxf-klient
 *
 * @author Paul Magne Lunde, Visma Consulting
 */
public class TimeoutFeature extends AbstractFeature {

	public static final int DEFAULT_RECEIVE_TIMEOUT = 10000;
	public static final int DEFAULT_CONNECTION_TIMEOUT = 10000;

	private int receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;
	private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

	public TimeoutFeature() {
		//Used for test
	}

	public TimeoutFeature(int receiveTimeout, int connectionTimeout) {
		this.receiveTimeout = receiveTimeout;
		this.connectionTimeout = connectionTimeout;
	}

	@Override
	public void initialize(Client client, Bus bus) {
		Conduit conduit = client.getConduit();
		if (conduit instanceof HTTPConduit) {
			HTTPConduit httpConduit = (HTTPConduit) conduit;
			if (httpConduit.getClient() == null) {
				HTTPClientPolicy policy = new HTTPClientPolicy();
				policy.setReceiveTimeout(receiveTimeout);
				policy.setConnectionTimeout(connectionTimeout);
				httpConduit.setClient(policy);
			} else {
				httpConduit.getClient().setReceiveTimeout(receiveTimeout);
				httpConduit.getClient().setConnectionTimeout(connectionTimeout);
			}
		}
		bus.setProperty(AsyncHTTPConduit.USE_ASYNC, Boolean.TRUE);
		super.initialize(client, bus);
	}
}