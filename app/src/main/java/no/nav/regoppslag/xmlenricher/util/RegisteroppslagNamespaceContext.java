package no.nav.regoppslag.xmlenricher.util;

import javax.xml.namespace.NamespaceContext;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public  class RegisteroppslagNamespaceContext implements NamespaceContext {

	private static final  Map<String, String> prefix2Uri = Stream.of(
			new SimpleEntry<>("felles", "http://nav.no/dok/pesysbrev/felles/v1/PesysFelles"),
			new SimpleEntry<>("mottaker", "http://nav.no/dok/pesysbrev/felles/v1/Mottaker"),
			new SimpleEntry<>("navEnhet", "http://nav.no/dok/pesysbrev/felles/v1/NavEnhet"),
			new SimpleEntry<>("saksbehandler", "http://nav.no/dok/pesysbrev/felles/v1/Saksbehandler"),
			new SimpleEntry<>("navAnsatt", "http://nav.no/dok/pesysbrev/felles/v1/NavAnsatt"),
			new SimpleEntry<>("kontaktinformasjon", "http://nav.no/dok/pesysbrev/felles/v1/Kontaktinformasjon"),
			new SimpleEntry<>("norskPostadresse", "http://nav.no/dok/pesysbrev/felles/v1/NorskPostadresse")
	).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

	private static final  Map<String, String> uri2Prefix = prefix2Uri.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

	/**
	 * This method is called by XPath. It returns the default namespace, if the
	 * prefix is null or "".
	 *
	 * @param prefix
	 *            to search for
	 * @return uri
	 */
	public String getNamespaceURI(String prefix) {
		return prefix2Uri.get(prefix);
	}

	/*
	 * This method is not needed in this context, but can be implemented in a
	 * similar way.
	*/
	public String getPrefix(String namespaceURI) {
		return uri2Prefix.get(namespaceURI);
	}

	public Iterator getPrefixes(String namespaceURI) {
		// Not implemented
		return null;
	}
}
