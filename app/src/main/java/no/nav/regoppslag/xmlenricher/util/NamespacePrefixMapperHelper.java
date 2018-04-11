package no.nav.regoppslag.xmlenricher.util;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class NamespacePrefixMapperHelper extends NamespacePrefixMapper {
	private final RegisteroppslagNamespaceContext registeroppslagNamespaceContext;

	public NamespacePrefixMapperHelper(RegisteroppslagNamespaceContext registeroppslagNamespaceContext) {
		this.registeroppslagNamespaceContext = registeroppslagNamespaceContext;
	}

	@Override
	public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
		return registeroppslagNamespaceContext.getPrefix(namespaceUri);
	}

	@Override
	public String[] getPreDeclaredNamespaceUris() {
		return new String[0];
	}

	@Override
	public String[] getPreDeclaredNamespaceUris2() {
		return new String[0];
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
		return new String[0];
	}
}
