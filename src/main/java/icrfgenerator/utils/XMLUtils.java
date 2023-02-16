/*
 * Copyright (c) 2019 VUmc/KWF TraIT2Health-RI
 *
 * This file is part of iCRFGenerator
 *
 * iCRFGenerator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * iCRFGenerator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with iCRFGenerator. If not, see <http://www.gnu.org/licenses/>
 */

package icrfgenerator.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

public class XMLUtils {
    private static final Logger logger = LogManager.getLogger(XMLUtils.class.getName());

    /**
     * cleans a string
     * currently: replacing the "&#160" that sometimes appears in the ART-DECOR XML, e.g. identifierName="SNOMED&#160;CT"
     * @param value string
     * @return cleaned string
     */
    private static String cleanString(String value){
        return value.replaceAll("\\u00A0", " ");
    }

    /**
     * returns the value of an xml attribute
     * @param element       element which contains the attribute
     * @param attributeName name of the attribute
     * @return the value of an xml attribute or empty string if the attribute does not exist;
     */
    public static String getAttributeValue(Element element, String attributeName){
        return cleanString(element.getAttribute(attributeName));
    }

    /**
     * checks whether this element is of interest for us in our codebook, which depends on the statuscode of the element;
     * we're only interested in items which are "draft" or "final"
     * @param element the element to check
     * @return true/false
     */
    public static boolean hasValidStatusCode(Element element){
        String statusCode = getAttributeValue(element, "statusCode");
        return statusCode.equalsIgnoreCase("draft") || statusCode.equalsIgnoreCase("final");
    }

    /**
     * returns the first child element with a certain name
     * @param element the parent element
     * @param name    name of the element we're looking for
     * @return the child element
     */
    public static Element getChildElementWithName(Element element, String name){
        NodeList nodeList = element.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && ((Element) node).getTagName().equals(name)) {
                return (Element) node;
            }
        }
        return null;
    }

    /**
     * returns a list of all child elements with a certain name
     * @param element parent element
     * @param name    name to look for
     * @return list of children
     */
    public static List<Element> getChildElementsWithName(Element element, String name){
        List<Element> elementsWithName = new ArrayList<>();
        NodeList nodeList = element.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && ((Element) node).getTagName().equals(name)) {
                elementsWithName.add((Element) node);
            }
        }
        return elementsWithName;
    }

    /**
     * returns a map with all the custom properties ("property") defined in ART-DECOR
     * @param element the element for which to find the custom property values
     * @return the map with the properties and their values
     */
    public static Map<String, String> getPropertyAttributeValues(Element element){
        Map<String, String> propertiesMap = new HashMap<>();
        List<Element> propertyElementsList = getChildElementsWithName(element, "property");
        for(Element propertyElement:propertyElementsList){
            String attributeValue = getAttributeValue(propertyElement, "name");
            propertiesMap.put(attributeValue, propertyElement.getTextContent().trim());
         }
        return propertiesMap;
    }

    /**
     * returns the textcontent of a parent's child with a certain tag name
     * @param parent parent element
     * @param tagName tag we're looking for
     * @return text value
     */
    public static String getElementValue(Element parent, String tagName){
        Element element = getChildElementWithName(parent, tagName);
        if(element!=null)
            return element.getTextContent();
        return null;
    }

    /**
     * checks whether an element is a group element
     * @param conceptElement the element to check
     * @return true/false
     */
    public static boolean isGroupElement(Element conceptElement){
        String elementType = getAttributeValue(conceptElement, "type");
        return elementType.equalsIgnoreCase("group");
    }

    /**
     * transform the XML string into a properly indented string for readability
     * @param xmlString the xml string
     * @param indent number of spaces of an indent
     * @return formatted XML
     */
    public static String formatXMLString(String xmlString, int indent) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(new StringReader(xmlString)));

            // transformer for the actual transformation
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            // collect the output stream in a string buffer and return it as a string
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
            return stringWriter.toString();

        } catch (ParserConfigurationException | TransformerException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return "Something went wrong...";
    }
}
