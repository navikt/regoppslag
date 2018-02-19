package no.nav.brevbestilling;

import no.nav.brevbestilling.config.CamelUri;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Paul Magne Lunde, Visma Consulting
 */
@Service
public class BrevbestillingRoute extends SpringRouteBuilder {

    public static final CamelUri BREVBESTILLING_V1 = CamelUri.builder().uri("cxf:bean:brevbestilling").routeId("Brevbestilling_v1").build();
    private static final CamelUri PING = CamelUri.builder().uri("direct:ping").routeId("ping").build();

    private static final String RECIPIENT_LIST = "recipientList";
    private static final String ORG_PAYLOAD = "org_payload";

    @Override
    public void configure() throws Exception {

        from("file:C:\\brevdata?noop=true")
                .log("Starting")
                .setProperty(ORG_PAYLOAD, simple("${body}"))
                .multicast()
                .parallelProcessing()
                .aggregationStrategy((oldExchange, newExchange) -> {
                    if (oldExchange == null) {
                        oldExchange = new DefaultExchange(this.getContext());
                    }

                    GenericFile property = (GenericFile) newExchange.getProperty(ORG_PAYLOAD);
                    Path path = Paths.get(property.getAbsoluteFilePath());

                    Element newElem = newExchange.getIn().getBody(Document.class).getDocumentElement();

                    try {
                        Document doc;
                        if (oldExchange.getIn().getBody(Document.class) == null) {
                            String org_xml = new String(Files.readAllBytes(path));
                            DocumentBuilderFactory b = DocumentBuilderFactory.newInstance();
                            b.setNamespaceAware(true);
                            DocumentBuilder db = b.newDocumentBuilder();
                            doc = db.parse(new InputSource(new ByteArrayInputStream(org_xml.getBytes())));
                        } else {
                            doc = oldExchange.getIn().getBody(Document.class);
                        }

                        Node orgElem = doc.getElementsByTagName(newElem.getTagName()).item(0);
                        Node importedNew = doc.importNode(newElem, true);

                        orgElem.appendChild(importedNew.getFirstChild());
                        oldExchange.getIn().setBody(doc);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }
                    return oldExchange;
                }).to("direct:a", "direct:b", "direct:c")
                .end();

        from("direct:a")
                .choice()
                .when(xpath("//felles/signerendeSaksbehandler"))
                    .process(exchange -> {
                        String s = "<signerendeSaksbehandler><navn>Liam Samuelsen</navn></signerendeSaksbehandler>";

                        DocumentBuilderFactory b = DocumentBuilderFactory.newInstance();
                        b.setNamespaceAware(true);
                        DocumentBuilder db = b.newDocumentBuilder();
                        Document doc = db.parse(new InputSource(new ByteArrayInputStream(s.getBytes())));
                        exchange.getIn().setBody(doc, Document.class);
                    });

        from("direct:b")
                .choice()
                .when(xpath("//felles/signerendeBeslutter"))
                    .process(exchange -> {
                        String s = "<signerendeBeslutter><navn>Clark Kent</navn></signerendeBeslutter>";

                        DocumentBuilderFactory b = DocumentBuilderFactory.newInstance();
                        b.setNamespaceAware(true);
                        DocumentBuilder db = b.newDocumentBuilder();
                        Document doc = db.parse(new InputSource(new ByteArrayInputStream(s.getBytes())));
                        exchange.getIn().setBody(doc, Document.class);
                    });

        from("direct:c")
                .choice()
                .when(xpath("//felles/besluttersEnhet"))
                    .process(exchange -> {
                        String s = "<besluttersEnhet><enhetsnavn>NAV Sentrum i bygda</enhetsnavn></besluttersEnhet>";

                        DocumentBuilderFactory b = DocumentBuilderFactory.newInstance();
                        b.setNamespaceAware(true);
                        DocumentBuilder db = b.newDocumentBuilder();
                        Document doc = db.parse(new InputSource(new ByteArrayInputStream(s.getBytes())));
                        exchange.getIn().setBody(doc, Document.class);
                    });
    }
}
