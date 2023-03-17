package no.nav.regoppslag.config;

import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import static no.nav.regoppslag.util.MDCConstants.CALL_ID;
import static no.nav.regoppslag.util.NavHeaders.NAV_CALLID;

public record NavHeaderFilter() implements ExchangeFilterFunction {
	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {

		if (MDC.get(CALL_ID) != null) {
			return next.exchange(ClientRequest.from(request).headers((headers) -> headers.set(NAV_CALLID, MDC.get(CALL_ID))).build());
		}
		return next.exchange(request);
	}
}