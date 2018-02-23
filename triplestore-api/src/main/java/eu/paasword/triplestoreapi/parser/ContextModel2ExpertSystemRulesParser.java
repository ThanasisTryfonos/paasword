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
package eu.paasword.triplestoreapi.parser;

import java.io.FileReader;
import java.io.Reader;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.paasword.triplestoreapi.parser.drools.*;
import java.io.FileOutputStream;
import java.util.stream.Collectors;

/**
 * Created by ipatini on 22/02/2017.
 */
public class ContextModel2ExpertSystemRulesParser extends AbstractParser {

    private static final Logger logger = Logger.getLogger(ContextModel2ExpertSystemRulesParser.class.getName());
	public static final String DEFAULT_KIE_SESSION_NAME = "ksession-rules";

    // =========================================================================================================
    //  MAIN: Allows use of parser from command line. Useful during development
    // =========================================================================================================
    public static void main(String[] args) throws IOException {
		
        logger.log(Level.INFO, "** triplestore-api test - Context Model to Expert System Rules Parser (Extraction of knowledge) **");
        ContextModel2ExpertSystemRulesParser parser = new ContextModel2ExpertSystemRulesParser();

        int p = 0;
        boolean output = true;
        if (args[p].trim().equalsIgnoreCase("--no-output") || args[p].trim().equalsIgnoreCase("-no")) {
            setCreateOutput(false);
            p++;
        } else if (args[p].trim().equalsIgnoreCase("--output") || args[p].trim().equalsIgnoreCase("-o")) {
            setCreateOutput(true);
            p++;
        }

        boolean isJson = false;
        if (args[p].trim().equalsIgnoreCase("--is-json") || args[p].trim().equalsIgnoreCase("-json")) {
            isJson = true;
            p++;
        }

		// get input file (RDF or JSON)
        String inputFile = args[p].trim();

        try (Reader reader = new FileReader(inputFile)) {
            String inputStr = new java.util.Scanner(new java.io.File(inputFile)).useDelimiter("\\Z").next();

            // Initialize a prefixes hash map
            HashMap<String, String> prefixes = new HashMap<String, String>();

			if (!isJson) {
				logger.log(Level.INFO, "** parseRdf2Session()");
				parseRdf2Session(inputStr, DEFAULT_KIE_SESSION_NAME);
			} else {
				logger.log(Level.INFO, "** parseJson2Session()");
				parseJson2Session(inputStr, DEFAULT_KIE_SESSION_NAME);
			}
        }
    }

	// =========================================================================================================
    //  METHODS for setting up an in-memory RDF model from a JSON description string and then parsing it 
	//  into facts for KIE session. The RDF/TTL required is generated using ContextModel2RdfParser
    // =========================================================================================================
	
	public static void parseJson2Session(String jsonStr, String sessionName) throws IOException {
        String ttlStr = ContextModel2RdfParser.getRdfFromJson(jsonStr);
		writeOutput(genFileName("CM-from-JSON","ttl"), ttlStr);
		parseRdf2Session(ttlStr, sessionName);
	}
	
	public static void parseJson2Session(String jsonStr, KieSession kSession) throws IOException {
        String ttlStr = ContextModel2RdfParser.getRdfFromJson(jsonStr);
		writeOutput(genFileName("CM-from-JSON","ttl"), ttlStr);
		parseRdf2Session(ttlStr, kSession);
	}
	
	// =========================================================================================================
    //  METHODS for setting up an in-memory RDF model and then parsing it into facts for KIE session
	//  Helper functions are used to parse Class/Property definitions and class instances into facts
    // =========================================================================================================

	public static void parseRdf2Session(String ttlStr, String sessionName) throws IOException {
		// load up the knowledge base
		KieServices ks = KieServices.Factory.get();
		KieContainer kContainer = ks.getKieClasspathContainer();
		KieSession kSession = kContainer.newKieSession(sessionName);
		
		// parse RDF into Drools facts
		parseRdf2Session(ttlStr, kSession);
	}
	
	public static void parseRdf2Session(String ttlStr, KieSession kSession) throws IOException {
		Model model = _createModel(ttlStr, "N3");
		
		HashMap<String,Clazz> hmClasses = createRdfClassFacts(model, kSession);
		HashMap<String,ObjectProperty> hmProperties = createRdfPropertyFacts(model, kSession, hmClasses);
		createRdfInstanceFacts(model, kSession, hmClasses, hmProperties);
		
		writeOutput(genFileName("FACTS-from-CM","txt"), kSession.getObjects());
	}

    private static Model _createModel(String strDataset, String strDatasetLang) {
        // Read initial model from string
        Model model = ModelFactory.createDefaultModel();
        model.read(new java.io.StringReader(strDataset), "", strDatasetLang);
        return model;
    }
    
	// =========================================================================================================
    //  METHODS for querying in-memory RDF model to extract Class/Property definitions or class insatnces
	//  and then insert them into KIE session
    // =========================================================================================================
	
	private static final String queryForClasses =
			  "SELECT ?subject ?parentClass "
			+ "WHERE "
			+ "{ "
			+ "	{ "
			+ "		SELECT ?subject ?parentClass "
			+ "		WHERE { "
			+ "		  ?subject a <http://www.w3.org/2000/01/rdf-schema#Class> . "
			+ "		  ?subject <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parentClass . "
			+ "		} "
			+ "	} UNION { "
			+ "		SELECT ?subject ?parentClass "
			+ "		WHERE { "
			+ "		  ?subject a <http://www.w3.org/2000/01/rdf-schema#Class> . "
			+ "		  FILTER NOT EXISTS { ?subject <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?x } . "
			+ "		  BIND ( '' as ?parentClass ) "
			+ "		} "
			+ "	} "
			+ "} "
			+ "ORDER BY ?subject " ;
			
	private static HashMap<String,Clazz> createRdfClassFacts(Model model, KieSession kSession) {

		// Query model to list RDF classes and their parent classes and create Clazz instances for them.
		// Store class-parent URI pairs in a temporary hash map and class URI-Clazz instance pairs into 
		// hmClasses
		HashMap<String,String> temp = new HashMap<String,String>();		// stores classUri-parentUri pairs
		HashMap<String,Clazz> hmClasses = new HashMap<String,Clazz>();	// stores classUri-Clazz pairs
		
		Query query = QueryFactory.create( queryForClasses );			// QUERY MODEL
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				// Extract solution (row) data
				QuerySolution soln = results.next();
				
				// Retrieve class and parent URIs
				RDFNode clazzNode = soln.get("subject");
				RDFNode parentNode  = soln.get("parentClass");
				
				// Add URIs
				String clazzUri = clazzNode.toString();
				String parentUri  = (parentNode!=null) ? parentNode.toString() : null;
				
				// Add in hash maps
				temp.put(clazzUri, parentUri);
				hmClasses.put(clazzUri, new Clazz(clazzUri, null));
			}
		}
		
		// Set clazz parent and add it as fact to Kie session
		//int i=0;
		for (String clazzUri : hmClasses.keySet()) {
			Clazz clazz = hmClasses.get(clazzUri);
			String parentUri = temp.get(clazzUri);
			if (parentUri!=null) {
				Clazz parent = hmClasses.get( parentUri );
				clazz.setParent(parent);
			}
			// Add class as fact into Kie session
			kSession.insert(clazz);
			//logger.finer("Added class "+clazz);
			//i++;
		}
		//logger.finer("Class facts added "+i);
		
		return hmClasses;
	}
	
	private static final String queryForProperties =
			  "SELECT ?subject ?parentProp ?domainClass ?rangeClass ?transitive "
			+ "WHERE "
			+ "{ "
			+ "	{ "
			+ "		SELECT ?subject ?parentProp "
			+ "		WHERE { "
			+ "		  ?subject a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . "
			+ "		  ?subject <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parentProp . "
			+ "		} "
			+ "	} UNION { "
			+ "		SELECT ?subject ?parentProp "
			+ "		WHERE { "
			+ "		  ?subject a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . "
			+ "		  FILTER NOT EXISTS { ?subject <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> ?x } . "
			+ "		  BIND ( '' as ?parentProp ) "
			+ "		} "
			+ "	} . "
			+ "    ?subject <http://www.w3.org/2000/01/rdf-schema#domain> ?domainClass . "
			+ "    ?subject <http://www.w3.org/2000/01/rdf-schema#range> ?rangeClass . "
			+ "	{ "
			+ "		SELECT ?subject ?transitive "
			+ "		WHERE { "
			+ "		  ?subject a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . "
			+ "		  ?subject a <http://www.zzzz.com/TransitiveProperty> . "
			+ "		  BIND ( 'true' as ?transitive ) "
			+ "		} "
			+ "	} UNION { "
			+ "		SELECT ?subject ?transitive "
			+ "		WHERE { "
			+ "		  ?subject a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . "
			+ "		  FILTER NOT EXISTS { ?subject a <http://www.zzzz.com/TransitiveProperty> } . "
			+ "		  BIND ( 'false' as ?transitive ) "
			+ "		} "
			+ "	} . "
			+ "} "
			+ "ORDER BY ?subject " ;
			
	private static HashMap<String,ObjectProperty> createRdfPropertyFacts(Model model, KieSession kSession, HashMap<String,Clazz> hmClasses) {

		// Query model to list RDF properties and their parent properties and create ObjectProperty instances for them.
		// Store property-parent URI pairs in a temporary hash map and property URI-ObjectProperty instance pairs into 
		// hmProperties
		HashMap<String,String> temp = new HashMap<String,String>();		// stores propertyUri-parentUri pairs
		HashMap<String,ObjectProperty> hmProperties = new HashMap<String,ObjectProperty>();	// stores propertyUri-ObjectProperty pairs
		
		Query query = QueryFactory.create( queryForProperties );		// QUERY MODEL
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				// Extract solution (row) data
				QuerySolution soln = results.next();
				
				// Retrieve class and parent URIs
				RDFNode propertyNode = soln.get("subject");
				RDFNode parentNode  = soln.get("parentProp");
				RDFNode domainNode  = soln.get("domainClass");
				RDFNode rangeNode   = soln.get("rangeClass");
				RDFNode transNode   = soln.get("transitive");
				
				// Get URIs
				String propertyUri = propertyNode.toString();
				String parentUri  = (parentNode!=null) ? parentNode.toString() : null;
				String domainUri  = domainNode.toString();
				String rangeUri   = rangeNode.toString();
				boolean transitive = transNode.toString().equals("true");
				
				// Add basic datatypes as classes, if absent
				Clazz domain = hmClasses.get(domainUri);
				if (domain==null && domainUri.startsWith("http://www.w3.org/2001/XMLSchema#")) {
					domain = new Clazz(domainUri);
					hmClasses.put(domainUri, domain);
				}
				Clazz range = hmClasses.get(rangeUri);
				if (range==null && rangeUri.startsWith("http://www.w3.org/2001/XMLSchema#")) {
					range = new Clazz(rangeUri);
					hmClasses.put(rangeUri, range);
				}
				
				// Add in hash maps
				temp.put(propertyUri, parentUri);
				hmProperties.put(propertyUri, new ObjectProperty(propertyUri, domain, range, transitive));
			}
		}
		
		// Set property parent and add it as fact to Kie session
		//int i=0;
		for (String propertyUri : hmProperties.keySet()) {
			ObjectProperty property = hmProperties.get(propertyUri);
			String parentUri = temp.get(propertyUri);
			if (parentUri!=null) {
				ObjectProperty parent = hmProperties.get( parentUri );
				property.setParent(parent);
			}
			// Add property as fact into Kie session
			kSession.insert(property);
			//logger.finer("Added property "+property);
			//i++;
		}
		//logger.finer("Property facts added "+i);
		
		return hmProperties;
	}

	private static final String queryForInstances =
			  "SELECT ?subject ?predicate ?object "
			+ "WHERE { "
			+ "  ?subject ?predicate ?object . "
			+ "  FILTER EXISTS { "
			+ "		?subject a ?subjectClass . "
			+ "  	FILTER ( ?subjectClass != <http://www.w3.org/2000/01/rdf-schema#Class> ) . "
			+ "  	FILTER ( ?subjectClass != <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ) . "
			+ "  	FILTER ( ?predicate != <http://purl.org/dc/terms/identifier> ) . "
			+ "  	FILTER ( ?predicate != <http://www.w3.org/2000/01/rdf-schema#label> ) . "
			+ "	} "
			+ "} "
			+ "ORDER BY ?subject " ;
	
	private static void createRdfInstanceFacts(Model model, KieSession kSession, HashMap<String,Clazz> hmClasses, HashMap<String,ObjectProperty> hmProperties) {
		
		// Query model to list RDF class instances and their property values and create InstanceOfClazz instances for them.
		// Store / Cache instance-RDF triple URI pairs in a temporary hash map and instance URI-InstanceOfClazz instance pairs 
		// into hmInstances
		HashMap<String,String[]> temp = new HashMap<String,String[]>();		// temporarily stores instanceUri-RDF_triple pairs
		HashMap<String,InstanceOfClazz> hmInstances = new HashMap<String,InstanceOfClazz>();	// stores instanceUri-InstanceOfClazz pairs
		
		Query query = QueryFactory.create( queryForInstances );		// QUERY MODEL
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				// Extract solution (row) data
				QuerySolution soln = results.next();
				
				// Retrieve nodes
				RDFNode subjectNode = soln.get("subject");
				RDFNode predicateNode = soln.get("predicate");
				RDFNode objectNode = soln.get("object");
				
				// Get URIs
				String subjectUri = subjectNode.toString();
				String predicateUri = predicateNode.toString();
				String objectUri = objectNode.toString();
				
				// Add in hash maps
				if (predicateUri.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
					// Instantiation (rdf:type) property
					// create class instance
					Clazz clazz = hmClasses.get(objectUri);
					hmInstances.put(subjectUri, new InstanceOfClazz(subjectUri, clazz));
				} else {
					// Plain property
					// cache triple
					String[] triple = new String[2];
					triple[0] = predicateUri;
					triple[1] = objectUri;
					temp.put(subjectUri, triple);
				}
			}
		}
		
		// Add class instances as facts to Kie session
		//int i=0;
		for (InstanceOfClazz instance : hmInstances.values()) {
			// Add instance as fact into Kie session
			kSession.insert(instance);
			//logger.finer("Added instance "+instance);
			//i++;
		}
		//logger.finer("Instance facts added "+i);
		
		// Add instance property values (cached in temp) as facts to Kie session
		//int i=0;
		for (String subjectUri : temp.keySet()) {
			// Add instance property value as fact into Kie session
			String[] triple = temp.get(subjectUri);
			if (triple==null) {
				//logger.fine("No instance property value triple found for "+subjectUri);
				continue;
			}
			InstanceOfClazz subject = hmInstances.get(subjectUri);
			ObjectProperty predicate = hmProperties.get(triple[0]);
			InstanceOfClazz object = hmInstances.get(triple[1]);
			if (object==null && predicate!=null && predicate.getRange().getName().startsWith("http://www.w3.org/2001/XMLSchema#")) {
				Clazz clazz = predicate.getRange();
				object = new InstanceOfClazz(triple[1], clazz);
				hmInstances.put(triple[1], object);
			} else
			if (subject==null || predicate==null || object==null) {
				/*logger.fine("Skipped fact creation for instance property value :"+
								"\n\tsubjectUri="+subjectUri+
								"\n\tsubject="+subject+
								"\n\tpredicateUri="+triple[0]+
								"\n\tpredicate="+predicate+
								"\n\tobjectUri="+triple[1]+
								"\n\tobject="+object);*/
				continue;
			}

			KnowledgeTriple instanceValues = new KnowledgeTriple(subject, predicate, object);
			kSession.insert(instanceValues);
			//logger.finer("Added instance values "+instanceValues);
			//i++;
		}
		//logger.finer("Instance value facts added "+i);
	}
	
	
    // =========================================================================================================
    //  Output related methods
    // =========================================================================================================
    
	private static boolean createOutput = false;
	public static boolean getCreateOutput() { return createOutput; }
	public static void setCreateOutput(boolean b) { createOutput = b; }
	
    public static void writeOutput(String fileName, String fileContents) throws IOException {
        if (createOutput) {
            logger.info("Creating file "+fileName+"...");

            String current_dir = System.getProperty("user.dir");
            //logger.fine("current_dir " + current_dir);

            FileOutputStream out = new FileOutputStream(current_dir + "/" + fileName);
            out.write(fileContents.getBytes());
            out.close();
        }
    }
    
	public static void writeOutput(String fileName, List list) throws IOException {
        if (createOutput) {
            logger.info("Generating JSON...");

            Gson gson = new GsonBuilder().create();
            String jsonStr = gson.toJson(list);
			writeOutput(fileName, jsonStr);
        }
    }
    
	public static void writeOutput(String fileName, Collection collection) throws IOException {
        if (createOutput) {
            logger.info("Generating collection...");

            Gson gson = new GsonBuilder().create();
            String jsonStr = gson.toJson(collection);
			writeOutput(fileName, jsonStr);
        }
    }
	
	private static Format tsFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
	
	public static String genFileName(String prefix, String ext) {
		String ts = tsFormatter.format(new Date());
		return String.format("%s-%s.%s", prefix, ts, ext);
	}
}
