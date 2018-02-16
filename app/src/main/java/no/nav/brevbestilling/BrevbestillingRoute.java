package no.nav.brevbestilling;

import no.nav.brevbestilling.config.CamelUri;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Paul Magne Lunde, Visma Consulting
 */
@SpringBootApplication
public class BrevbestillingRoute extends SpringRouteBuilder {

    public static final CamelUri BREVBESTILLING_V1 = CamelUri.builder().uri("cxf:bean:brevbestilling").routeId("Brevbestilling_v1").build();
    private static final CamelUri PING = CamelUri.builder().uri("direct:ping").routeId("ping").build();

    private static final String RECIPIENT_LIST = "recipientList";

    public static void main(String[] args) {
        SpringApplication.run(BrevbestillingRoute.class, args);
    }


    @Override
    public void configure() throws Exception {

        from("file:src/brevdata?noop=true")
                .multicast()
                .parallelProcessing()
                .aggregationStrategy(new AggregationStrategy() {
                    @Override
                    public Exchange aggregate(Exchange exchange, Exchange exchange1) {
                        return null;
                    }
                }).to("direct:a", "direct:b", "direct:c")
                .end();

        from("direct:a")
                .choice()
                .when(xpath("//felles/signerendeSaksbehandler"))
                    .setBody(simple("<navn>Liam Samuelsen</navn>", String.class));

        from("direct:b")
                .choice()
                .when(xpath("//felles/signerendeBeslutter"))
                    .setBody(simple("<navn>Clark Kent</navn>", String.class));

        from("direct:c")
                .choice()
                .when(xpath("//felles/besluttersEnhet"))
                    .setBody(simple("<enhetsnavn>NAV Sentrum i bygda</enhetsnavn>", String.class));

    }
}
