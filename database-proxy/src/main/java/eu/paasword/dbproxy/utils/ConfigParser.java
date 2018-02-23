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
package eu.paasword.dbproxy.utils;

import eu.paasword.dbproxy.DBProxyOrchestrator;
import eu.paasword.dbproxy.database.utils.DatabaseTypes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses and encapsulates accessors for a YAML config file
 *
 * @author Maximilian Baritz
 *
 */
public class ConfigParser {

    private static Logger logger = Logger.getLogger(ConfigParser.class.getName());

    private static ConcurrentHashMap<String, ConfigParser> configmap = new ConcurrentHashMap<>();
    //private static ConfigParser self = null;
    private Map<String, Object> conf = null;
    private Map<String, HashMap<String, String>> schemes;
    private boolean xml = false;

    /**
     * Private constructor for Parser
     */
    private ConfigParser() {
    }

    /**
     * Load the config file by filepath. This has to be executed before querying
     * config information
     *
     * @param filepath config file location
     * @return the singleton instance
     * @throws FileNotFoundException in case of unreadable config file
     */
    private void loadConfiguration(String adapterid) throws FileNotFoundException {
        String filepath = DBProxyOrchestrator.getConfigurationFile(adapterid);
        logger.info("ConfigParser-->loadConfig from: " + filepath);
        if (filepath.endsWith("xml")) { // Compatibility mode
            xml = true;
            schemes = new HashMap<String, HashMap<String, String>>();
            DocumentBuilderFactory DOMfactory = DocumentBuilderFactory.newInstance();
            DOMfactory.setCoalescing(true);
            DocumentBuilder DOMbuilder;
            Document doc = null;
            try {
                DOMbuilder = DOMfactory.newDocumentBuilder();
                doc = DOMbuilder.parse(filepath);               ///media/ubuntu/disk2/workspace/paasword-framework/database-proxy/src/main/resources/config/ManualDistributionAdapterConfig.xml
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            conf = getXMLRootMapping(doc,adapterid);
            //store result
            configmap.put(filepath, this);
        }//if
    }//EoM

    /**
     * Public accessor
     *
     * @return the instance itself
     * @throws FileNotFoundException
     */
    public static ConfigParser getInstance(String adapterid) {

        ConfigParser config;
        if (configmap.get(adapterid) == null) {
            logger.info("ConfigParser --> Configuration ("+adapterid+") DOES NOT exist i will create it");
            config = new ConfigParser();
            try {
                config.loadConfiguration(adapterid);
                configmap.put(adapterid, config);
            } catch (FileNotFoundException ex) {
                logger.severe("Error loading file");
            }
        } else {
            logger.info("ConfigParser --> Configuration ("+adapterid+") already exists ");
            config = configmap.get(adapterid);
        }
        return config;
    }//EoM

    /**
     * Prevents cloning issues
     */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * @return the Configuration of the deamon
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getDeamonConfig() {
        return (Map<String, String>) conf.get("deamon");
    }

    /**
     *
     * @return the configuration of the local Database
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getLocalDatabase() {
        if (xml) {
            return (Map<String, String>) conf.get(DatabaseTypes.LOCAL.name());
        } else {
            Map<String, Object> databases = (Map<String, Object>) conf.get("databases");
            List<Map<String, String>> locals = (List<Map<String, String>>) databases.get(DatabaseTypes.LOCAL.name());
            return locals.get(0);
        }
    }

    /**
     *
     * @return the configurations of the remote databases
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getRemoteDatabases() {
        return getDatabases(DatabaseTypes.REMOTE.name());
    }

    /**
     *
     * @return the configurations of the remote index databases
     */
    public List<Map<String, String>> getRemoteIndexDatabases() {
        return getDatabases(DatabaseTypes.REMOTE_INDEX.name());
    }

    /**
     * Get the remote or remote_index data bases using their type
     *
     * @param pType The type of the data base (e.g. remote, remote_index)
     * @return A of configurations for the data bases.
     */
    private List<Map<String, String>> getDatabases(String pType) {
        if (xml) {
            return (List<Map<String, String>>) conf.get(pType);
        } else {
            Map<String, Object> databases = (Map<String, Object>) conf.get("databases");
            List<Map<String, String>> remotes = (List<Map<String, String>>) databases.get(pType);
            return remotes;
        }
    }

    /**
     *
     * @return the mapping scheme saved in the config
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getMappings() {
        return (List<Map<String, String>>) conf.get("mappings");
    }

    /**
     *
     * @return the configuration for the wibustick
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getWibuConfig() {
        return (Map<String, String>) conf.get("wibustick");
    }

    /**
     *
     * @return the configurations of the index e.g. type etc.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getIndexConfig() {
        return (Map<String, String>) conf.get("index");
    }

    /**
     *
     * @return global configurations like logging
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getGlobalConfig() {
        return (Map<String, String>) conf.get("global");
    }

    //parses a node to a general map
    private Map<String, String> parseHashMap(Node node) {
        HashMap<String, String> map = new HashMap<String, String>();
        NodeList childs = node.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            if (!child.getNodeName().equals("#text")) {
                map.put(childs.item(i).getNodeName(), childs.item(i)
                        .getTextContent());
            }
        }
        return map;

    }

    //parses the mapping scheme
    private List<Map<String, String>> parseMapping(Node node) {
        ArrayList<Map<String, String>> relations = new ArrayList<Map<String, String>>();
        NodeList childs = node.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            if (child.getNodeName().equals("relation")) {
                relations.add(parseHashMap(child));
            }
        }
        return relations;
    }

    //parses any type of database to a map
    private Map<String, String> parseDatabase(Node node, String databaseName) {
        HashMap<String, String> database = (HashMap<String, String>) parseHashMap(node);
        Element element = (Element) node;
        NodeList scheme = element.getElementsByTagName("scheme");
        if (scheme.getLength() != 0) {
            schemes.put(databaseName, (HashMap<String, String>) parseHashMap(scheme.item(0)));
            database.remove("scheme");
        }
        return database;
    }

    /**
     * Parses the XML to the Mapping-structure
     *
     * @param doc the xml DOM-Document
     * @return a complete Mapping of the configuration analogous to the
     * yaml-version
     */
    private Map<String, Object> getXMLRootMapping(Document doc, String adapterid) {
        Map<String, Object> map = new HashMap<String, Object>();
        Node test = doc.getDocumentElement();
        NodeList rootelements = test.getChildNodes();
        ArrayList<HashMap<String, String>> remoteDatabases = new ArrayList<HashMap<String, String>>();
        List<Map<String, String>> remoteIndexDatabases = new ArrayList<>();
        for (int i = 0; i < rootelements.getLength(); i++) {
            Node node = rootelements.item(i);
            if (!node.getNodeName().equals("#text")) { // skip unneccessary
                // textnodes
                XMLElements e = XMLElements.valueOf(node.getNodeName().toUpperCase());
                switch (e) {
                    case DATABASE:
                        //TODO: Get the nodes by name and not by their position to be safe
                        String databaseType = node.getChildNodes().item(1).getTextContent();
                        HashMap<String, String> databaseConfig = (HashMap<String, String>) parseDatabase(node, databaseType);
                        //sos add an extra key that will be read by the DatabaseLoader
                        databaseConfig.put("adapterid", adapterid);
                        if (databaseType.equalsIgnoreCase(DatabaseTypes.REMOTE.name())) {
                            remoteDatabases.add(databaseConfig);
                        } else if (databaseType.equalsIgnoreCase(DatabaseTypes.REMOTE_INDEX.name())) {
                            remoteIndexDatabases.add(databaseConfig);
                        } else if (databaseType.equalsIgnoreCase(DatabaseTypes.LOCAL.name())) {
                            map.put(DatabaseTypes.LOCAL.name(), databaseConfig);
                        }
                        break;
                    case MAPPING:
                        map.put("mappings", parseMapping(node));
                        break;
                    case WIBUSTICK:
                        map.put(node.getNodeName(), parseHashMap(node));
                        break;
                    case DEAMON:
                        map.put(node.getNodeName(), parseHashMap(node));
                        break;
                    case INDEX:
                        map.put(node.getNodeName(), parseHashMap(node));
                        break;
                    case GLOBAL:
                        map.put(node.getNodeName(), parseHashMap(node));
                        break;
                    default:
                        logger.log(Level.INFO, "Error: Xml Entity Unkown");
                }
            }
        }
        map.put(DatabaseTypes.REMOTE.name(), remoteDatabases);
        map.put(DatabaseTypes.REMOTE_INDEX.name(), remoteIndexDatabases);
        return map;
    }

    /**
     * List of all valid XML-Entities
     */
    private enum XMLElements {
        DATABASE, MAPPING, WIBUSTICK, DEAMON, INDEX, GLOBAL
    }
}
