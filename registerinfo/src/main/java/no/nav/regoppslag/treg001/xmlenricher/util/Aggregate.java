package no.nav.regoppslag.treg001.xmlenricher.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.w3c.dom.Node;

@Getter
@AllArgsConstructor
public class Aggregate {
	private Node newNode;
	private Node origNode;
}
