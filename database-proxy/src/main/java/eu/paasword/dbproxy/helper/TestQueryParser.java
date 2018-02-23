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
package eu.paasword.dbproxy.helper;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class to retrieve test queries to the adapter.
 *
 * The xml document that is put into {@link #loadConfig(String)} has to be
 * well formed and it's content must look like:
 *
 * <queries>
 *     ....
 *     <query name="myQuery1"> SELECT * FROM table</query>
 *     <query name="myQuery2"> INSERT INTO ....</query>
 *     ....
 * </queries>
 * @author valentin
 */
public class TestQueryParser {

    private static TestQueryParser self = null;
    private static Map<String, String> queryMap = null;
    private static Map<String, HashMap<String, String>> schemes;
    private Logger logger = Logger.getLogger("prototype.queryparser");
    private static boolean xml = false;

    /**
     * Private constructor for Parser
     */
    private TestQueryParser() {
    }

    /**
     * Load the config file by filepath. This has to be executed before querying
     * config information
     *
     * @param filepath
     *            config file location
     * @return the singleton instance
     * @throws FileNotFoundException
     *             in case of unreadable config file
     */
    public void loadConfig(String filepath) throws FileNotFoundException {
        if (filepath.endsWith("xml")) { // Compatibility mode
            xml = true;
            schemes = new HashMap<String, HashMap<String, String>>();
            DocumentBuilderFactory DOMfactory = DocumentBuilderFactory
                    .newInstance();
            DOMfactory.setCoalescing(true);
            DocumentBuilder DOMbuilder;
            Document doc = null;
            try {
                DOMbuilder = DOMfactory.newDocumentBuilder();
                doc = DOMbuilder.parse(new File(filepath));
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            queryMap =  getXMLRootMapping(doc);
        }
    }

    /**
     * Public accessor
     *
     * @return the instance itself
     * @throws FileNotFoundException
     */
    public static TestQueryParser getInstance() {
        if (self == null) {
            self = new TestQueryParser();
        }
        return self;
    }


    /**
     * Return the text content of the query node that is identified by the name attribute.
     * @param queryName The value of the name attribute of the query.
     * @return The text content of the query node.
     */
    public String getQuery(String queryName){
        return queryMap.get(queryName);
    }

    /**
     * Return all queries that where loaded from the configuration file
     * @return A map containing all queries.
     */
    public Map<String, String> getAllQueries(){
        return queryMap;
    }

    /**
     * Parses the XML to the Mapping-structure
     * @param doc the xml DOM-Document
     * @return a complete Mapping of the configuration analogous to the yaml-version
     */
    private Map<String, String> getXMLRootMapping(Document doc) {
        HashMap<String, String> map = new HashMap<String, String>();
        Node test = doc.getDocumentElement();
        NodeList rootelements = test.getChildNodes();
        for (int i = 0; i < rootelements.getLength(); i++) {
            Node node = rootelements.item(i);
            if (!node.getNodeName().equalsIgnoreCase("#text") && !node.getNodeName().equalsIgnoreCase("#comment")) { // skip unneccessary
                // textnodes
                TestQueryParser.XMLElements e = TestQueryParser.XMLElements.valueOf(node.getNodeName()
                        .toUpperCase());
                switch (e) {
                    case QUERY:
                        map.put(node.getAttributes().getNamedItem("name").getNodeValue(), node.getTextContent().trim());
                        break;
                    default:
                        logger.log(Level.INFO, "Error: Xml entity+ '" + e + "'' unkown");
                }
            }
        }
        return map;
    }

    /**
     * List of all valid XML-Entities
     */
    private enum XMLElements {
        QUERY
    }
}
