package no.nav.regoppslag.xmlenricher.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import org.w3c.dom.Node;

@Getter
@AllArgsConstructor
public class Payload {
	private Node element;
	private ElementEnricherPlugin plugin;
	private Node orgNode;
}
