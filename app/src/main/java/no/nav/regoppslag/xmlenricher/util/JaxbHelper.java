package no.nav.regoppslag.xmlenricher.util;

import no.nav.regoppslag.exceptions.MarshallerException;
import org.springframework.util.Assert;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class JaxbHelper<T>{

	private final Class<T> jaxbClass;

	public JaxbHelper(Class<T> jaxbClass) {
		this.jaxbClass = jaxbClass;
	}


	public T unmarshal(Node node) throws MarshallerException {
		try {
			JAXBContext context = JAXBContext.newInstance(jaxbClass);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			JAXBElement<T> unmarshal = unmarshaller.unmarshal(node, jaxbClass);
			return unmarshal.getValue();
		} catch (JAXBException | IllegalArgumentException e) {
			throw new MarshallerException("Feilet ved unmarshalling. Sjekk stacktrace for detaljer", e);
		}

	}

	public <T> Node marshal(T jaxbObject, Node node) throws MarshallerException {
		try {
			String contextPath = jaxbClass.getPackage().getName();
			JAXBContext context = JAXBContext.newInstance(contextPath);
			Marshaller marshaller = context.createMarshaller();
			JAXBElement<T> jaxbElement = (JAXBElement<T>) getJaxbElement(jaxbObject, jaxbClass);
			marshaller.marshal(jaxbElement, node);
			return node;
		} catch (JAXBException | IllegalArgumentException e) {
			throw new MarshallerException("Feilet ved marshalling. Sjekk stacktrace for detaljer", e);
		}

	}

	/**
	 * Return object in JAXBElement wrapper
	 *
	 * @param object the object
	 * @param tClass the class of object
	 * @param <T>    type of object to be returned
	 * @return the wrapped object
	 */
	@SuppressWarnings("unchecked")
	public static <T> JAXBElement<T> getJaxbElement(Object object, Class<T> tClass) {
		Assert.isTrue(isObjectClassOf(object, tClass), "object is instance of class " + object.getClass() +
				", expected " + tClass);
		String name = tClass.getAnnotation(XmlType.class).name();
		String namespace = tClass.getPackage().getAnnotation(XmlSchema.class).namespace();
		return new JAXBElement<>(new QName(namespace, name), tClass, (T) object);
	}

	/**
	 * Test if an object is an instance of a list of classes
	 *
	 * @param payLoad      the object
	 * @param validClasses the classes to check against
	 * @return true if object is instance of any of the given classes, else true
	 */
	public static boolean isObjectClassOf(Object payLoad, Class... validClasses) {
		boolean validClass = false;
		for (Class clazz : validClasses) {
			if (clazz.isInstance(payLoad)) {
				validClass = true;
			}
		}
		return validClass;
	}
}
