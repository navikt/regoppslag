package no.nav.regoppslag.xmlenricher.util;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class NamespacePrefixMapperHelper extends NamespacePrefixMapper {
	public static final String NAMESPACES_FELLES = "http://nav.no/dok/pesysbrev/felles/v1/";
	private NamespaceContext namespaceCache;
	private String[] contextualPrefixes;

	public NamespacePrefixMapperHelper(UniversalNamespaceCache namespaceCache) {
		this.namespaceCache = namespaceCache;
		List<String> prefixNamespacePairs = new ArrayList<>();
		Iterator<String> prefixIterator = namespaceCache.getPrefixIterator();
		while (prefixIterator.hasNext()) {
			String prefix = prefixIterator.next();
			String namespaceURI = namespaceCache.getNamespaceURI(prefix);
			// Ignore namespaces not used in <felles>
			if (namespaceURI.startsWith(NAMESPACES_FELLES) || namespaceURI.equals("http://www.w3.org/2001/XMLSchema-instance")) {
				prefixNamespacePairs.add(prefix);
				prefixNamespacePairs.add(namespaceURI);
			}
		};
		contextualPrefixes = prefixNamespacePairs.toArray(new String[prefixNamespacePairs.size()]);
	}

	@Override
	public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
		return namespaceCache.getPrefix(namespaceUri);
	}

	@Override
	public String[] getPreDeclaredNamespaceUris2() {
		return contextualPrefixes;
	}

	/**
	 * If we assume all namespaces are declared outside the element to be processed, we do not need namespace-declarations inside the element.
	 * In that case we may return contextualPrefixes in this call.
	 * The downside is that the element will not be marshallable out of context of the whole document, since the element will miss the necessary
	 * namespace declarations to stand alone.
	 * @return
	 */
	@Override
	public String[] getContextualNamespaceDecls() {
//		return contextualPrefixes;
		return new String[0];
	}
}
