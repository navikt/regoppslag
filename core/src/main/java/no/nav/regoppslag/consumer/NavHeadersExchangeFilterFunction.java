package no.nav.regoppslag.consumer;

import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import static no.nav.regoppslag.util.MDCConstants.CALL_ID;


public class NavHeadersExchangeFilterFunction implements ExchangeFilterFunction {

	private final String callIdHeadername;

	public NavHeadersExchangeFilterFunction(String callIdHeadername) {
		this.callIdHeadername = callIdHeadername;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		return next.exchange(ClientRequest.from(request)
				.headers(httpHeaders -> httpHeaders.set(callIdHeadername, MDC.get(CALL_ID)))
				.build());
	}
}