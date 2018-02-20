package no.nav.regoppslag;

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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Paul Magne Lunde, Visma Consulting
 */
@Service
public class RegOppslagRoute extends SpringRouteBuilder {

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

                    try {
                        Document doc;
                        if (oldExchange.getIn().getBody(Document.class) == null) {
                            GenericFile file = (GenericFile) newExchange.getProperty(ORG_PAYLOAD);
                            doc = parseString2Document(file);
                        } else {
                            doc = oldExchange.getIn().getBody(Document.class);
                        }

                        // Get complete element from plugin
                        Document body = newExchange.getIn().getBody(Document.class);
                        if (body != null) {
                            Element newElem = body.getDocumentElement();

                            // Find element in original XML, only one of each supported
                            Node orgElem = doc.getElementsByTagName(newElem.getTagName()).item(0);
                            // Import into document to be able to copy children
                            Node importedNew = doc.importNode(newElem, true);

                            while (importedNew.getChildNodes().getLength() > 0) {
                                orgElem.appendChild(importedNew.getChildNodes().item(0));
                            }

                            // Just for verification
                            writeXml(doc);

                            oldExchange.getIn().setBody(doc);
                        }
                    } catch (IOException | ParserConfigurationException | SAXException | TransformerException e) {
                        e.printStackTrace();
                    }
                    return oldExchange;
                }).to("direct:a", "direct:b", "direct:c")
                .end();

        from("direct:a")
                .choice()
                    .when(xpath("//felles/signerendeSaksbehandler"))
                        .process(exchange -> {
                            String s = "<signerendeSaksbehandler><navn>Liam Samuelsen</navn><telefon>99887766</telefon><dummy>test</dummy></signerendeSaksbehandler>";

                            DocumentBuilderFactory b = DocumentBuilderFactory.newInstance();
                            b.setNamespaceAware(true);
                            DocumentBuilder db = b.newDocumentBuilder();
                            Document doc = db.parse(new InputSource(new ByteArrayInputStream(s.getBytes())));
                            exchange.getIn().setBody(doc, Document.class);
                        })
                    .otherwise()
                        .process(emptyProcessor());

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
                        })
                    .otherwise()
                        .process(emptyProcessor());

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
                        })
                    .otherwise()
                        .process(emptyProcessor());
    }

    private Processor emptyProcessor() {
        return exchange -> exchange.getIn().setBody(null);
    }

    private Document parseString2Document(GenericFile file) throws IOException, ParserConfigurationException, SAXException {
        Document doc;

        Path path = Paths.get(file.getAbsoluteFilePath());

        String org_xml = new String(Files.readAllBytes(path));
        DocumentBuilderFactory b = DocumentBuilderFactory.newInstance();
        b.setNamespaceAware(true);
        DocumentBuilder db = b.newDocumentBuilder();
        doc = db.parse(new InputSource(new ByteArrayInputStream(org_xml.getBytes())));
        return doc;
    }

    private void writeXml(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        //for pretty print
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);

        StreamResult console = new StreamResult(System.out);
        StreamResult file = new StreamResult(new File("C:\\brevdata_out\\completed.xml"));

        transformer.transform(source, console);
        transformer.transform(source, file);
    }
}
