/*
 *  Copyright 2016 PaaSword Framework, http://www.paasword.eu/
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.paasword.util;

import eu.paasword.util.entities.XMLAttribute;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by smantzouratos on 31/08/16.
 */
public class XMLUtil {

    /**
     *
     * Initialize the XML of the Solr Document for a SES_DOCUMENT
     *
     * @return A Document object
     *
     */
    public static Document initializeXMLDocument() {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        Document doc;

        try {

            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XMLUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (Exception ex) {
            Logger.getLogger(XMLUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        return doc;
    } // EoM initializeXMLDocument

    /**
     *
     * Adds a root element to XML Document
     *
     * @param _doc XML Document
     *
     * @return A Document object
     *
     */
    public static Element rootElementADD(Document _doc) {

        Element rootElement = createRootElement(_doc, "databases");

        return rootElement;

    } // EoM rootElementADD

    /**
     *
     * Adds a root element to XML Document
     *
     * @param _doc XML Document
     *
     * @return An Element object
     *
     */
    public static Element rootElementSCHEMA(Document _doc) {

        List<XMLAttribute> listOfAttributes = new ArrayList<>();

        // Root Element : schema
        Element rootElement = createRootElement(_doc, "schema");

        if (listOfAttributes.size() > 0) {
            listOfAttributes.clear();
        }

        listOfAttributes.add(new XMLAttribute("name", "example"));
        listOfAttributes.add(new XMLAttribute("version", "1.1"));

        addListOfAttributesToElement(_doc, listOfAttributes, rootElement);

        listOfAttributes.clear();

        return rootElement;

    } // EoM rootElementSCHEMA

    /**
     *
     * Transforms an XML Document to Solr Document
     *
     * @param _doc The XML Document that is needed to be transformed
     *
     * @return A String object | Solr Document as String
     *
     */
    public static String transformXMLDocumentToString(Document _doc) {

        StringWriter sw = null;

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(_doc);

            sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            transformer.transform(source, result);

            return sw.toString();

        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(XMLUtil.class.getName()).log(Level.SEVERE, "Problem constructing XML Document.", ex);
            return null;
        } catch (TransformerException ex) {
            Logger.getLogger(XMLUtil.class.getName()).log(Level.SEVERE, "Problem constructing XML Document.", ex);
            return null;
        } catch (Exception ex) {
            Logger.getLogger(XMLUtil.class.getName()).log(Level.SEVERE, "Problem constructing XML Document.", ex);
            return null;
        }

    } // EoM transformXMLDocumentToSolrDocument

    /**
     *
     * Creates a root element with a specific Name
     *
     * @param _doc XML Document
     *
     * @param _elementName Element name that is needed to be created
     *
     * @return An Element object
     *
     */
    public static Element createRootElement(Document _doc, String _elementName) {

        Element rootElement = _doc.createElement(_elementName);
        _doc.appendChild(rootElement);

        if (null == rootElement) {
            return null;
        }

        return rootElement;
    } // EoM createRootElement

    /**
     *
     * Adds a list of XML attributes to specific XML Element
     *
     * @param _doc XML Document
     * @param _listOfAttributeObjects List of XML Attributes
     * @param _toElement Element to add the attributes
     *
     */
    public static void addListOfAttributesToElement(Document _doc, List<XMLAttribute> _listOfAttributeObjects, Element _toElement) {
        for (XMLAttribute tempAttr : _listOfAttributeObjects) {
            addAttributeToElement(_doc, tempAttr, _toElement);
        }
    } // EoM addListOfAttributesToElement

    /**
     *
     * Adds an XML attribute to specific XML Element
     *
     * @param _doc XML Document
     * @param _attributeObject An XML Attribute
     * @param _toElement Element to add the attributes
     *
     */
    public static void addAttributeToElement(Document _doc, XMLAttribute _attributeObject, Element _toElement) {
        Attr newAttribute = _doc.createAttribute(_attributeObject.getAttributeName());
        newAttribute.setValue(_attributeObject.getAttributeValue());
        _toElement.setAttributeNode(newAttribute);

    } // EoM addAttributeToElement

    /**
     *
     * Creates an XML element and append it to specific XML Document
     *
     * @param _doc XML Document
     * @param _elementName The name of the element to be added
     * @param _value The _value of the element
     *
     * @return An Element Object
     */
    public static Element createElement(Document _doc, String _elementName, String _value) {
        Element _element = _doc.createElement(_elementName);

        if (null != _value) {
            _element.appendChild(_doc.createTextNode(_value));
        }

        return _element;
    } // EoM createElement

    /**
     *
     * Adds a child to an XML element
     *
     * @param _childElement Child Element
     * @param _toElement Father Element
     *
     */
    public static void addChildToElement(Element _childElement, Element _toElement) {

        _toElement.appendChild(_childElement);

    } // EoM addChildToElement

}
