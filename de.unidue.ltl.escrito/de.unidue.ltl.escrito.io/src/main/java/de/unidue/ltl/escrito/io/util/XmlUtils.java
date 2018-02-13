package de.unidue.ltl.escrito.io.util;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;

public class XmlUtils {
	public static String getAttributeValue(Element e, String name) {
	    for (Object o : e.attributes()) {
	        Attribute attribute = (Attribute) o;  
	        if (name.equals(attribute.getName())) {
	            return attribute.getValue();
	        }
	    }
	    return null;
	}
	
	public static String getText(Element root, String xPath)
	        throws JaxenException
	{
	    final XPath xp = new Dom4jXPath(xPath);
	    
	    for (Object element : xp.selectNodes(root)) {
	        if (element instanceof Element) {
	            Element node = (Element) element;
	            return node.getText();
	        }
	    }
	    
	    return null;
	}
}
