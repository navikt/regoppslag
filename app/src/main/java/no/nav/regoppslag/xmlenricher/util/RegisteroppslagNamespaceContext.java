package no.nav.regoppslag.xmlenricher.util;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public  class RegisteroppslagNamespaceContext implements NamespaceContext {
	private Map<String, String> prefix2Uri = null;
	private Map<String, String> uri2Prefix = null;
	private final List<String> decls = new ArrayList<>();

	private void init() {
		prefix2Uri = new HashMap();
		prefix2Uri.put("schema", "http://www.w3.org/2001/XMLSchema-instance");
		prefix2Uri.put("felles", "http://nav.no/dok/felles/v1/NavFelles");
		prefix2Uri.put("mottaker", "http://nav.no/dok/felles/v1/Mottaker");
		prefix2Uri.put("navEnhet", "http://nav.no/dok/felles/v1/NavEnhet");
		prefix2Uri.put("saksbehandler", "http://nav.no/dok/felles/v1/Saksbehandler");
		prefix2Uri.put("behandlendeEnhet", "http://nav.no/dok/felles/v1/BehandlendeEnhet");
		prefix2Uri.put("sakspart", "http://nav.no/dok/felles/v1/Sakspart");
		prefix2Uri.put("navAnsatt", "http://nav.no/dok/felles/v1/NavAnsatt");
		prefix2Uri.put("kontaktinformasjon", "http://nav.no/dok/felles/v1/Kontaktinformasjon");
		prefix2Uri.put("aktoer", "http://nav.no/dok/felles/v1/Aktoer");
		prefix2Uri.put("utenlandskPostadresse", "http://nav.no/dok/felles/v1/UtenlandskPostadresse");
		prefix2Uri.put("postadresse", "http://nav.no/dok/felles/v1/Postadresse");
		prefix2Uri.put("adresseenhet", "http://nav.no/dok/felles/v1/AdresseEnhet");
		prefix2Uri.put("norskPostadresse", "http://nav.no/dok/felles/v1/NorskPostadresse");

		uri2Prefix = new HashMap();
		for (String prefix : prefix2Uri.keySet()) {
			uri2Prefix.put(prefix2Uri.get(prefix), prefix);
			decls.add(prefix);
			decls.add(prefix2Uri.get(prefix));
		}
	}

	/**
	 * This method is called by XPath.
	 *
	 * @param prefix to search for
	 * @return uri
	 */
	public String getNamespaceURI(String prefix) {
		if (prefix2Uri == null) {
			init();
		}
		return prefix2Uri.get(prefix);
	}

	/*
	 * This method is not needed in this context, but can be implemented in a
	 * similar way.
	*/
	public String getPrefix(String namespaceURI) {
		if (prefix2Uri == null) {
			init();
		}
		return uri2Prefix.get(namespaceURI);
	}

	public Iterator getPrefixes(String namespaceURI) {
		// Not implemented
		return null;
	}
}