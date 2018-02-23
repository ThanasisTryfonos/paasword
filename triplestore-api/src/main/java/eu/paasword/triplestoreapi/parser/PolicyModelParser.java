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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.paasword.triplestoreapi.client.PAPClientFactory;
import eu.paasword.triplestoreapi.client.TriplestoreClientFactory;
import eu.paasword.triplestoreapi.response.ParserResponse;
import eu.paasword.triplestoreapi.response.PolicyModelParserResponse;
import eu.paasword.validator.ValidatorClient;
import eu.paasword.validator.engine.ValidationReport;

import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by ipatini on 18/05/16.
 */
public class PolicyModelParser extends AbstractParser {

	private static final Logger logger = Logger.getLogger(PolicyModelParser.class.getName());
	
	// =========================================================================================================
	//  MAIN: Allows use of parser from command line. Useful during development
	// =========================================================================================================
	
	public static void main(String[] args) throws IOException {
		logger.log(Level.INFO, "** triplestore-api test - Parser 1 (Policy parser) **");
		PolicyModelParser parser = new PolicyModelParser();
		
		if (args[0].trim().equals("to")) {
			boolean validate = true;
			boolean output = true;
			int p = 1;
			if (args[p].trim().equalsIgnoreCase("--no-validate") || args[p].trim().equalsIgnoreCase("-nv")) { validate = false; p++; }
			else if (args[p].trim().equalsIgnoreCase("--validate") || args[p].trim().equalsIgnoreCase("-v")) { validate = true; p++; }
			if (args[p].trim().equalsIgnoreCase("--no-output") || args[p].trim().equalsIgnoreCase("-no")) { output = false; p++; }
			else if (args[p].trim().equalsIgnoreCase("--output") || args[p].trim().equalsIgnoreCase("-o")) { output = true; p++; }

			String jsonFile = args[p].trim();

			try(Reader reader = new FileReader(jsonFile)){
				String jsonStr = new java.util.Scanner(new java.io.File(jsonFile)).useDelimiter("\\Z").next();

				logger.log(Level.INFO, "** toTriplestore()");

				PolicyModelParserResponse resp = parser.toTriplestore(jsonStr, validate, output);
				if (resp.isSuccess()) {
					String rdf = resp.getRdf();
					//System.out.println(rdf);
				} else {
					System.err.println("** FAILURE **");
					System.err.println(resp.getMessage());
				}
			}
		} else
		if (args[0].trim().equals("from")) {
			System.out.println("** fromTriplestore()");
			String str = parser.fromTriplestore();
			System.out.println("Triplestore contents:\n"+str);
		}
	}
	
	// =========================================================================================================
	//  CONVENIENCE METHODS for generating/retrieving RDF/TTL, XACML policy files IN ONE CALL
	// =========================================================================================================
	
	public static PolicyModelParserResponse toTriplestore(String jsonStr) throws IOException {
		return toTriplestore(jsonStr, true, false);
	}
	
	public static PolicyModelParserResponse toTriplestore(String jsonStr, boolean validateInput, boolean createOutput) throws IOException {
		// Prepare RDF policies as string
		Gson gson = new GsonBuilder().create();
		JsonDefinition jd = gson.fromJson(jsonStr, JsonDefinition.class);
		String rdfStr = toRdf(jd);
		//logger.log(Level.INFO,rdfStr);
		
		// Prepare XACML policies as strings (if policy validation was successful)
		String xmlStr = toXACML(jd);
		//logger.log(Level.INFO,xmlStr);
		
		// Save RDF/XACML/HashMap strings into local files
		if (createOutput) {
			logger.info("Creating output files...");
			boolean b1 = TriplestoreClientFactory.getClientInstance( "eu.paasword.triplestoreapi.client.TriplestoreClientImpl" ).uploadToTriplestore( rdfStr );
			//logger.log(Level.INFO, "Upload to RDF triplestore: {0}", b1);
			boolean b2 = PAPClientFactory.getClientInstance().uploadToPAP( xmlStr );
			//logger.log(Level.INFO, "Upload to PAP: {0}", b2);
		} else {
			logger.info("No output files created");
		}

		// Create and return a response object
		PolicyModelParserResponse response = new PolicyModelParserResponse(ParserResponse.VALIDATION_RESULT.SUCCESS, null, rdfStr, xmlStr);
		return response;
	}
	
	//NOT IMPLEMENTED: It is not needed for the time being
	public static String fromTriplestore() {
		throw new RuntimeException("Method not implemented : fromTriplestore");
	}
	
	// =========================================================================================================
	//  BUILD XACML POLICY CONTENTS (can be uploaded in an XACML PAP)
	// =========================================================================================================

	private static long dummyIdCounter = -1;
	
	private static void prepareContextExpression(ContextExpressionDefinition cedef, String topLevelUri, HashMap<Long,ContextExpressionDefinition> hmExpressions, HashMap<Long,ContextExpressionDefinition> hmIndividuals, HashMap<String,String> classCache, HashMap<String,String> prefixes) {
		// store in hash map
		if (hmExpressions.containsKey(cedef.id)) throw new RuntimeException(PolicyModelParser.class.getName()+": ID already used for context expression definition: "+hmExpressions.get(cedef.id).name);
		if (cedef.id>0) hmExpressions.put(cedef.id, cedef); else hmExpressions.put(dummyIdCounter--, cedef);	// Assign a temporary id (it's a unique negative number from dummyIdCounter)
		if (cedef.uri==null) cedef.uri = topLevelUri;		// If URI is null change it to top-level expression URI (necessary in order to group sub-expressions correctly. According to the final Policy Editor JSON format, sub-expressions do not have URIs)
															// Side-effect: this hack "pollutes" the JsonDefinition that resulted from JSON message from Policy Editor. As a result XACML generation includes the same AttributeId's multiple times
															// Workaround: re-build JsonDefinition from the initial JSON message
		// process expression parameters
		if (cedef.params!=null) {
			// 
			for (ContextExpressionDefinition paramDef : cedef.params) {
				// store in hash map
				String type = paramDef.type;
				if (type!=null && !(type=type.trim()).isEmpty() && !type.equals("instance") && !type.equals("property-value")) {
					// recursively call prepareContextExpression for sub-expressions
					prepareContextExpression(paramDef, topLevelUri, hmExpressions, hmIndividuals, classCache, prefixes);
				} else
				if (hmIndividuals!=null) {
					if (paramDef.id>0) hmIndividuals.put(paramDef.id, paramDef);
					else hmIndividuals.put(dummyIdCounter--, paramDef);
				}
			}
		}
	}
	
	private static String toXACML(JsonDefinition jdef) {
		try {
			
			return _toXACML(jdef);
			
		} catch (ParserConfigurationException pce) {
			throw new RuntimeException(pce);
		} catch (TransformerException tfe) {
			throw new RuntimeException(tfe);
		}
	}
	
	private static String _toXACML(JsonDefinition jdef) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // Create XML document and add header in comments
        Document doc = docBuilder.newDocument();
        doc.appendChild( doc.createComment(
			"\n\tGenerated by: "+PolicyModelParser.class.getName()+
			"\n\tDate: "+new java.util.Date()+"\n\n"
		) );
        // root element (i.e. Default PolicySet)
        Element rootElement = doc.createElement("PolicySet");
		doc.appendChild(rootElement);
		// root element attributes
		addAttributeToXacml(doc, rootElement, "xmlns", "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17");
		addAttributeToXacml(doc, rootElement, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		addAttributeToXacml(doc, rootElement, "PolicySetId", "urn:www.paasword.eu:policies:policysetid:_:DEFAULT_POLICY_SET");
		addAttributeToXacml(doc, rootElement, "Version", "1.0");
		addAttributeToXacml(doc, rootElement, "PolicyCombiningAlgId", "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:permit-unless-deny");	// Top-level policies combining algorithm
		
		// add an empty target node
		rootElement.appendChild( doc.createElement("Target") );
		
		// add policy sets
		for (PolicySetDefinition psdef : jdef.policySets) {
			Element policySetElem = addPolicySetToXacml(doc, rootElement, psdef);
			rootElement.appendChild(policySetElem);
		}
		
		// hash rule definitions by uri
		HashMap<String,RuleDefinition> ruleDefs = new HashMap<String,RuleDefinition>();
		if (jdef.rules!=null) {
			for (RuleDefinition rdef : jdef.rules) {
				ruleDefs.put( sanitizeUri(rdef.uri), rdef );
			}
		}
		// hash context expression definitions by uri
		HashMap<String,ContextExpressionDefinition> exprDefs = new HashMap<String,ContextExpressionDefinition>();
		if (jdef.contextExpressions!=null) {
			for (ContextExpressionDefinition cedef : jdef.contextExpressions) {
				exprDefs.put( sanitizeUri(cedef.uri), cedef );
			}
		}
		
		// add policies
		HashMap<String,String> prefixes = jdef.prefixes;
		for (PolicyDefinition pdef : jdef.policies) {
			Element policyElem = addPolicyToXacml(doc, rootElement, pdef, ruleDefs, exprDefs, prefixes);
			rootElement.appendChild(policyElem);
		}
		
		// serialize to string
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		
		StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
		
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
		
		return sw.toString();
	}
	
	private static Attr addAttributeToXacml(Document doc, Element element, String attrId, String attrValue) {
        Attr attr = doc.createAttribute(attrId);
        attr.setValue(attrValue);
        element.setAttributeNode(attr);
		return attr;
	}
	private static Element addPolicySetToXacml(Document doc, Element element, PolicySetDefinition psdef) {
		// create the PolicySet element
		Element policySetElem = doc.createElement("PolicySet");
		addAttributeToXacml(doc, policySetElem, "PolicySetId", "urn:www.paasword.eu:policies:policysetid:"+sanitizeUri(psdef.uri));
		addAttributeToXacml(doc, policySetElem, "Version", "1.0");
		addAttributeToXacml(doc, policySetElem, "PolicyCombiningAlgId", psdef.combiningAlgorithm);
		
		// add an empty target node
		policySetElem.appendChild( doc.createElement("Target") );
		
		// create and append child policy sets
		if (psdef.policySets!=null) {
			for (String psUri : psdef.policySets) {
				String refElemType = "PolicySetIdReference";
				Element refElem = doc.createElement(refElemType);
				refElem.appendChild( doc.createTextNode( "urn:www.paasword.eu:policies:policysetid:"+sanitizeUri(psUri) ) );
				policySetElem.appendChild(refElem);
			}
		}
		// create and append child policies
		if (psdef.policies!=null) {
			for (String pUri : psdef.policies) {
				String refElemType = "PolicyIdReference";
				Element refElem = doc.createElement(refElemType);
				refElem.appendChild( doc.createTextNode( "urn:www.paasword.eu:policies:policyid:"+sanitizeUri(pUri) ) );
				policySetElem.appendChild(refElem);
			}
		}
		return policySetElem;
	}
	private static Element addPolicyToXacml(Document doc, Element element, PolicyDefinition pdef, HashMap<String,RuleDefinition> ruleDefs, HashMap<String,ContextExpressionDefinition> exprDefs, HashMap<String,String> prefixes) {
		// create the Policy element
		Element policyElem = doc.createElement("Policy");
		String policyUri = sanitizeUri(pdef.uri);
		
		addAttributeToXacml(doc, policyElem, "PolicyId", "urn:www.paasword.eu:policies:policyid:"+policyUri);
		addAttributeToXacml(doc, policyElem, "Version", "1.0");
		addAttributeToXacml(doc, policyElem, "RuleCombiningAlgId", pdef.combiningAlgorithm);
		
		// add an empty target node
		policyElem.appendChild( doc.createElement("Target") );
		
		// create and append child elements (i.e. rules)
		if (pdef.rules!=null) {
			for (String rUri : pdef.rules) {
				RuleDefinition rdef = ruleDefs.get( sanitizeUri(rUri) );
				Element ruleElem = addRuleToXacml(doc, policyElem, rdef, policyUri, exprDefs, prefixes);
				policyElem.appendChild(ruleElem);
			}
		}
		return policyElem;
	}
	private static Element addRuleToXacml(Document doc, Element element, RuleDefinition rdef, String policyUri, HashMap<String,ContextExpressionDefinition> exprDefs, HashMap<String,String> prefixes) {
		// create the Rule element
		Element ruleElem = doc.createElement("Rule");
		String effect = null;
		if (rdef.authorizationUri.equals("pac:positive")) effect = "Permit";
		else if (rdef.authorizationUri.equals("pac:negative")) effect = "Deny";
		else throw new IllegalArgumentException("Incorrect RuleDefinition: authorizationUri MUST BE pac:positive OR pac:negative: "+rdef);
		addAttributeToXacml(doc, ruleElem, "Effect", effect);
		addAttributeToXacml(doc, ruleElem, "RuleId", "urn:www.paasword.eu:policies:ruleid:"+sanitizeUri(rdef.uri)+":at-policy:"+policyUri);
		
		// create target part
		Element targetElem = doc.createElement("Target"); 
		ruleElem.appendChild(targetElem);
		Element anyOfElem = doc.createElement("AnyOf");
		Element allOfElem = doc.createElement("AllOf");
		targetElem.appendChild( anyOfElem );
		anyOfElem.appendChild( allOfElem );
		
		addMatchToXacml(doc, allOfElem, sanitizeUri(rdef.controlledObjectUri), "urn:oasis:names:tc:xacml:1.0:resource:resource-id", "urn:oasis:names:tc:xacml:3.0:attribute-category:resource");
		addMatchToXacml(doc, allOfElem, sanitizeUri(rdef.actorUri), "urn:oasis:names:tc:xacml:1.0:subject:subject-id", "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");
		addMatchToXacml(doc, allOfElem, sanitizeUri(rdef.actionUri), "urn:oasis:names:tc:xacml:1.0:action:action-id", "urn:oasis:names:tc:xacml:3.0:attribute-category:action");
		
		// create condition part
		Element conditionElem = doc.createElement("Condition"); 
		ruleElem.appendChild(conditionElem);
		
		ContextExpressionDefinition cedef = exprDefs.get( sanitizeUri(rdef.exprUri) );
		if (cedef.nestedExpressions!=null && cedef.nestedExpressions.size()>0) {
			String joinType = cedef.nestedExpressions.get(0).property;
			String funcId = joinType.equalsIgnoreCase("ORnestedExpression") ? "urn:oasis:names:tc:xacml:1.0:function:or" : "urn:oasis:names:tc:xacml:1.0:function:and";
			//
			Element applyElem = doc.createElement("Apply");
			addAttributeToXacml(doc, applyElem, "FunctionId", funcId);
			conditionElem.appendChild( applyElem );
			
			// include strored expressions
			for (ContextExpressionDefinition nedef : cedef.nestedExpressions) {
				String exprToIncludeUri = nedef.hasParameter;
				applyElem.appendChild( doc.createComment(
					"Inclusion of stored expression : "+exprToIncludeUri
				) );
				
				ContextExpressionDefinition exprToInclude = exprDefs.get( sanitizeUri(exprToIncludeUri) );
				addApplyToXacml(doc, applyElem, exprToInclude, prefixes);
			}
			
			// add extra expression parameters
			applyElem.appendChild( doc.createComment(
				"Extra expression parameters"
			) );
			addApplyToXacml(doc, applyElem, cedef, prefixes);
		} else {
			conditionElem.appendChild( doc.createComment(
				"Expression parameters"
			) );
			addApplyToXacml(doc, conditionElem, cedef, prefixes);
		}
		
		return ruleElem;
	}
	private static Element addMatchToXacml(Document doc, Element parentElem, String attrValue, String requestAttrId, String attrCategory) {
		return addMatchToXacml(doc, parentElem, attrValue, requestAttrId, "http://www.w3.org/2001/XMLSchema#string", attrCategory, "false");
	}
	private static Element addMatchToXacml(Document doc, Element parentElem, String attrValue, String requestAttrId) {
		return addMatchToXacml(doc, parentElem, attrValue, requestAttrId, "http://www.w3.org/2001/XMLSchema#string", null, "false");
	}
	private static Element addMatchToXacml(Document doc, Element parentElem, String attrValue, String requestAttrId, String attrDatatype, String attrCategory, String mustBePresent) {
		Element matchElem = doc.createElement("Match");
		addAttributeToXacml(doc, matchElem, "MatchId", "urn:oasis:names:tc:xacml:1.0:function:string-equal");
		parentElem.appendChild( matchElem );
		
		Element attrValueElem = doc.createElement("AttributeValue");
		addAttributeToXacml(doc, attrValueElem, "DataType", attrDatatype);
		attrValueElem.appendChild( doc.createTextNode( attrValue ) );
		matchElem.appendChild( attrValueElem );
		
		Element attrDesigElem = doc.createElement("AttributeDesignator");
		addAttributeToXacml(doc, attrDesigElem, "DataType", attrDatatype);
		addAttributeToXacml(doc, attrDesigElem, "AttributeId", requestAttrId);
		if (attrCategory!=null) addAttributeToXacml(doc, attrDesigElem, "Category", attrCategory);
		if (mustBePresent!=null) addAttributeToXacml(doc, attrDesigElem, "MustBePresent", mustBePresent);
		matchElem.appendChild( attrDesigElem );
		
		return matchElem;
	}
	private static Element addApplyToXacml(Document doc, Element parentElem, ContextExpressionDefinition cedef, HashMap<String,String> prefixes) {
		String funcId;
		if (cedef.type.equals("pac:ANDContextExpression")) funcId = "urn:oasis:names:tc:xacml:1.0:function:and";
		else if (cedef.type.equals("pac:ORContextExpression")) funcId = "urn:oasis:names:tc:xacml:1.0:function:or";
		else throw new IllegalArgumentException("Incorrect ContextExpressionDefinition: expression type MUST BE pac:ANDContextExpression -OR- pac:ORContextExpression: "+cedef);
		
		Element applyElem = null;
		if (cedef.params!=null && cedef.params.size()>0) {
			if (cedef.params.size()>1) {
				applyElem = doc.createElement("Apply");
				addAttributeToXacml(doc, applyElem, "FunctionId", funcId);
				parentElem.appendChild( applyElem );
			} else {
				applyElem = parentElem;		// Add the expression parameter directly under parent element (without an Apply node)
			}
			
			for (ContextExpressionDefinition pdef : cedef.params) {
				if (pdef.type==null) {
					// It is a simple parameter
					if (pdef.refersTo!=null) {
						applyElem.appendChild( doc.createComment(sanitizeUri(pdef.refersTo)+"  "+sanitizeUri(pdef.property)+"  "+sanitizeUri(pdef.hasParameter)) );		// Add a comment with parameter info
						addMatchToXacml(doc, applyElem, sanitizeUri( pdef.hasParameter ), "urn:www.paasword.eu:refersTo:"+sanitizeUri( pdef.refersTo )+":property:"+sanitizeUri( pdef.property ), "http://www.w3.org/2001/XMLSchema#string", "urn:www.paasword.eu:context", "true");
					} else {
						applyElem.appendChild( doc.createComment(sanitizeUri(pdef.associatedWith)+"  "+sanitizeUri(pdef.property)+"  "+sanitizeUri(pdef.hasParameter)) );		// Add a comment with parameter info
						addMatchToXacml(doc, applyElem, sanitizeUri( pdef.hasParameter ), "urn:www.paasword.eu:associatedWith:"+sanitizeUri( pdef.associatedWith )+":property:"+sanitizeUri( pdef.property ), "http://www.w3.org/2001/XMLSchema#string", "urn:www.paasword.eu:context", "true");
					}
				} else {
					// It is a composite (AND/OR) parameter
					addApplyToXacml(doc, applyElem, pdef, prefixes);
				}
			}
		}
		
		return applyElem;
	}
	
	// =========================================================================================================
	//  GENERATE RDF/TTL POLICY DESCRIPTION (used for validating policy)
	// =========================================================================================================
	
	private static String prepUri(String uri, HashMap<String,String> prefixes) {
		for (Map.Entry<String,String> entry : prefixes.entrySet()) {
			String pref = entry.getKey();
			String ns = entry.getValue();
			if (uri.startsWith(ns)) {
				return new StringBuilder(pref).append(":").append( uri.substring(ns.length()) ).toString();
			}
		}
		// not found
		return new StringBuilder("<").append(uri).append(">").toString();
	}
	
	private static String toRdf(JsonDefinition jdef) {
		StringBuilder sb = new StringBuilder();
		// produce header
		sb.append("# Generated by: ").append(PolicyModelParser.class.getName()).append("\n");
		sb.append("# Date: ").append(new java.util.Date()).append("\n\n");
		
		// produce prefixes section
		HashMap<String,String> hmPrefixes = new HashMap<String,String>();
		// add base (if any)
		String baseUri = jdef.prefixes.get("");
		if (baseUri==null ||  baseUri.trim().isEmpty()) baseUri = jdef.prefixes.get(null);
		if (baseUri!=null && !baseUri.trim().isEmpty()) {
			sb.append("@base <").append(baseUri).append("> .").append("\n");
			hmPrefixes.put("", baseUri);
		}
		// add prefixes
		for (String prefix : jdef.prefixes.keySet()) {
			if (prefix==null || (prefix=prefix.trim()).isEmpty()) continue;
			String ns = jdef.prefixes.get(prefix);
			if (ns!=null) ns = ns.trim(); else ns = "";
			if (!ns.isEmpty()) {
				sb.append("@prefix ").append(prefix).append(": <").append(ns).append("> .").append("\n");
				hmPrefixes.put(prefix, ns);
			}
		}
		
		// prepare reserved URIs
		String URI_ID = prepUri("http://purl.org/dc/terms/identifier", hmPrefixes);
		String URI_LABEL = prepUri("http://www.w3.org/2000/01/rdf-schema#label", hmPrefixes);
		String URI_STRING = prepUri("http://www.w3.org/2001/XMLSchema#string", hmPrefixes);
		String URI_OBJECT = prepUri("http://www.paasword-project.eu/ontologies/casm/2016/05/20#Object", hmPrefixes);

		String URI_POLICY_SET = prepUri("http://www.paasword.eu/security-policy/seerc/pac#ABACPolicySet", hmPrefixes);
		String URI_POLICY = prepUri("http://www.paasword.eu/security-policy/seerc/pac#ABACPolicy", hmPrefixes);
		String URI_RULE = prepUri("http://www.paasword.eu/security-policy/seerc/pac#ABACRule", hmPrefixes);
		String URI_COMB_ALG_POLICY_SET = prepUri("http://www.paasword.eu/security-policy/seerc/pac#PolicySetCombiningAlgorithms", hmPrefixes);
		String URI_COMB_ALG_POLICY = prepUri("http://www.paasword.eu/security-policy/seerc/pac#PolicyCombiningAlgorithms", hmPrefixes);
		String URI_HAS_COMB_ALG_POLICY_SET = prepUri("http://www.paasword.eu/security-policy/seerc/pac#hasPolicySetCombiningAlgorithm", hmPrefixes);
		String URI_HAS_COMB_ALG_POLICY = prepUri("http://www.paasword.eu/security-policy/seerc/pac#hasPolicyCombiningAlgorithm", hmPrefixes);
		String URI_BELONGS_TO = prepUri("http://www.paasword.eu/security-policy/seerc/pac#belongsToABACPolicySet", hmPrefixes);
		String URI_HAS_RULE = prepUri("http://www.paasword.eu/security-policy/seerc/pac#hasABACRule", hmPrefixes);
		
		String URI_HAS_AUTH = prepUri("http://www.paasword.eu/security-policy/seerc/pac#hasAuthorisation", hmPrefixes);
		String URI_HAS_ACTION = prepUri("http://www.paasword.eu/security-policy/seerc/pac#hasAction", hmPrefixes);
		String URI_HAS_ACTOR = prepUri("http://www.paasword.eu/security-policy/seerc/pac#hasActor", hmPrefixes);
		String URI_HAS_CONTROLLED_OBJECT = prepUri("http://www.paasword.eu/security-policy/seerc/pac#hasControlledObject", hmPrefixes);
		String URI_HAS_EXPRESSION = prepUri("http://www.paasword.eu/security-policy/seerc/pac#hasContextExpression", hmPrefixes);
		
		String URI_EXPRESSION = prepUri("http://www.paasword.eu/security-policy/seerc/pac#ContextExpression", hmPrefixes);
		String URI_AND = prepUri("http://www.paasword.eu/security-policy/seerc/pac#ANDContextExpression", hmPrefixes);
		String URI_OR = prepUri("http://www.paasword.eu/security-policy/seerc/pac#ORContextExpression", hmPrefixes);
		String URI_HAS_PARAM = prepUri("http://www.paasword.eu/security-policy/seerc/pac#hasParameter", hmPrefixes);
		String URI_HAS_VALUE = prepUri("http://www.paasword.eu/security-policy/seerc/pac#hasEvaluationResult", hmPrefixes);
		String URI_REFERS_TO = prepUri("http://www.paasword.eu/security-policy/seerc/pac#refersTo", hmPrefixes);
		String URI_ASSOC_WITH = prepUri("http://www.paasword.eu/security-policy/seerc/pac#associatedWith", hmPrefixes);
		
		
		// pre-process definitions (store in hash maps)
		HashMap<Long,PolicySetDefinition> hmPolicySets = new LinkedHashMap<Long,PolicySetDefinition>();
		HashMap<Long,PolicyDefinition> hmPolicies = new LinkedHashMap<Long,PolicyDefinition>();
		HashMap<Long,RuleDefinition> hmRules = new LinkedHashMap<Long,RuleDefinition>();
		HashMap<Long,ContextExpressionDefinition> hmExpressions = new LinkedHashMap<Long,ContextExpressionDefinition>();
		HashMap<String,Vector<PolicySetDefinition>> hmHierarchy = new LinkedHashMap<String,Vector<PolicySetDefinition>>();
		HashMap<String,String> classCache = new HashMap<String,String>();
		
		for (PolicySetDefinition psdef : jdef.policySets) {
			// store in hash map
			if (hmPolicySets.containsKey(psdef.id)) throw new RuntimeException(PolicyModelParser.class.getName()+": ID already used for policy-set definition: "+hmPolicySets.get(psdef.id).name);
			hmPolicySets.put(psdef.id, psdef);
			// search policy set hierarchies
			if (psdef.policySets!=null) {
				for (String childPsdefUri : psdef.policySets) {
					Vector<PolicySetDefinition> v = hmHierarchy.get(childPsdefUri);
					if (v==null) { v = new Vector<PolicySetDefinition>(); hmHierarchy.put(childPsdefUri, v); }
					v.add(psdef);
				}
			}
			if (psdef.policies!=null) {
				for (String pdefUri : psdef.policies) {
					Vector<PolicySetDefinition> v = hmHierarchy.get(pdefUri);
					if (v==null) { v = new Vector<PolicySetDefinition>(); hmHierarchy.put(pdefUri, v); }
					v.add(psdef);
				}
			}
		}
		for (PolicyDefinition pdef : jdef.policies) {
			// store in hash map
			if (hmPolicies.containsKey(pdef.id)) throw new RuntimeException(PolicyModelParser.class.getName()+": ID already used for policy definition: "+hmPolicies.get(pdef.id).name);
			hmPolicies.put(pdef.id, pdef);
		}
		for (RuleDefinition rdef : jdef.rules) {
			// store in hash map
			if (hmRules.containsKey(rdef.id)) throw new RuntimeException(PolicyModelParser.class.getName()+": ID already used for rule definition: "+hmRules.get(rdef.id).name);
			hmRules.put(rdef.id, rdef);
		}
		for (ContextExpressionDefinition cedef : jdef.contextExpressions) {
			prepareContextExpression(cedef, null, hmExpressions, null, null, null);
		}
		
		
		// produce policy sets section
		for (Long id : hmPolicySets.keySet()) {
			PolicySetDefinition psdef = hmPolicySets.get(id);
			// policy set definition header
			sb.append("# =============================================================================").append("\n");
			sb.append("# Definition of Policy Set: ").append(psdef.name);
			Vector<PolicySetDefinition> v = hmHierarchy.get(psdef.uri);
			if (v!=null && v.size()>0) {
				sb.append("  in Policy Set: ");
				boolean first = true;
				for (PolicySetDefinition parentPsdef : v) {
					if (first) first=false; else sb.append(", ");
					sb.append( parentPsdef.uri );
				}
			}
			sb.append("\n");
			
			// policy set uri and type
			sb.append( sanitizeUri(psdef.uri) ).append("\ta\t").append(URI_POLICY_SET).append(" ;").append("\n");
			// parent policy sets (if any)
			if (v!=null && v.size()>0) {
				for (PolicySetDefinition parentPsdef : v) {
					sb.append("\t").append(URI_BELONGS_TO).append("\t").append( sanitizeUri(parentPsdef.uri) ).append(" ;").append("\n");
				}
			}
			
			// combining algorithm
			sb.append("\t").append(URI_HAS_COMB_ALG_POLICY_SET).append("\t").append( sanitizeUri(psdef.combiningAlgorithm) ).append(" ; ").append("\n");
			// policy set id
			sb.append("\t").append(URI_ID).append("\t\"").append(psdef.id).append("\"^^").append(URI_STRING).append(" ; ").append("\n");
			// policy set name
			sb.append("\t").append(URI_LABEL).append("\t\"").append( sanitizeText(psdef.name) ).append("\"^^").append(URI_STRING).append(" . ").append("\n\n");
		}
		
		// produce policies section
		for (Long id : hmPolicies.keySet()) {
			PolicyDefinition pdef = hmPolicies.get(id);
			// policy definition header
			sb.append("# =============================================================================").append("\n");
			sb.append("# Definition of Policy: ").append(pdef.name);
			Vector<PolicySetDefinition> v = hmHierarchy.get(pdef.uri);
			if (v!=null && v.size()>0) {
				sb.append("  in Policy Set: ");
				boolean first = true;
				for (PolicySetDefinition parentPsdef : v) {
					if (first) first=false; else sb.append(", ");
					sb.append( parentPsdef.uri );
				}
			}
			sb.append("\n");
			
			// policy uri and type
			sb.append( sanitizeUri(pdef.uri) ).append("\ta\t").append(URI_POLICY).append(" ;").append("\n");
			// parent policy sets (if any)
			if (v!=null && v.size()>0) {
				for (PolicySetDefinition parentPsdef : v) {
					sb.append("\t").append(URI_BELONGS_TO).append("\t").append( sanitizeUri(parentPsdef.uri) ).append(" ;").append("\n");
				}
			}
			// policy rules
			for (String rUri : pdef.rules) {
				sb.append("\t").append(URI_HAS_RULE).append("\t").append( sanitizeUri(rUri) ).append(" ;").append("\n");
			}
			
			// combining algorithm
			sb.append("\t").append(URI_HAS_COMB_ALG_POLICY).append("\t").append( sanitizeUri(pdef.combiningAlgorithm) ).append(" ; ").append("\n");
			// policy id
			sb.append("\t").append(URI_ID).append("\t\"").append(pdef.id).append("\"^^").append(URI_STRING).append(" ; ").append("\n");
			// policy name
			sb.append("\t").append(URI_LABEL).append("\t\"").append( sanitizeText(pdef.name) ).append("\"^^").append(URI_STRING).append(" . ").append("\n\n");
		}
		
		// produce rules section
		for (Long id : hmRules.keySet()) {
			RuleDefinition rdef = hmRules.get(id);
			// rule definition header
			sb.append("# =============================================================================").append("\n");
			sb.append("# Definition of Policy Rule: ").append(rdef.name).append("\n");
			// rule uri and type
			sb.append( sanitizeUri(rdef.uri) ).append("\ta\t").append(URI_RULE).append(" ;").append("\n");
			// rule id
			sb.append("\t").append(URI_ID).append("\t\"").append(rdef.id).append("\"^^").append(URI_STRING).append(" ; ").append("\n");
			// rule name
			sb.append("\t").append(URI_LABEL).append("\t\"").append( sanitizeText(rdef.name) ).append("\"^^").append(URI_STRING).append(" ; ").append("\n");
			// rule controlled object, authorization, action, actor, expression
			sb.append("\t").append("pac:hasControlledObject").append("\t").append( sanitizeUri(rdef.controlledObjectUri) ).append(" ;").append("\n");
			sb.append("\t").append(URI_HAS_AUTH).append("\t").append( sanitizeUri(rdef.authorizationUri) ).append(" ;").append("\n");
			sb.append("\t").append(URI_HAS_ACTION).append("\t").append( sanitizeUri(rdef.actionUri) ).append(" ;").append("\n");
			sb.append("\t").append(URI_HAS_ACTOR).append("\t").append( sanitizeUri(rdef.actorUri) ).append(" ;").append("\n");
			sb.append("\t").append(URI_HAS_EXPRESSION).append("\t").append( sanitizeUri(rdef.exprUri) ).append(" .").append("\n\n");
		}
		
		// produce expressions section
		for (Long id : hmExpressions.keySet()) {
			ContextExpressionDefinition cedef = hmExpressions.get(id);
			if (id<0 || cedef.uri==null) continue;	// ignore sub-expressions
			// Ctx-Expr definition header
			sb.append("# =============================================================================").append("\n");
			sb.append("# Definition of Context Expression: ").append(cedef.name).append("\n");
			// Ctx-Expr uri and type
			sb.append( sanitizeUri(cedef.uri) ).append("\n");
			// Ctx-Expr include stored expressions (in cedef.nestedExpressions field)
			if (cedef.nestedExpressions!=null && cedef.nestedExpressions.size()>0) {
				String joinType = cedef.nestedExpressions.get(0).property;
				String nestedExprType = joinType.equalsIgnoreCase("ORnestedExpression") ? URI_OR : URI_AND;
				sb.append("\ta\t").append(nestedExprType).append(" ;").append("\n");
				for (ContextExpressionDefinition nedef : cedef.nestedExpressions) {
					String exprToIncludeUri = nedef.hasParameter;
					sb.append("\t").append("# Include stored expression : ").append( sanitizeUri(exprToIncludeUri) ).append("\n");
					sb.append("\t").append(URI_HAS_PARAM).append("\t").append( sanitizeUri(exprToIncludeUri) ).append(" ; ").append("\n");	
				}
				sb.append("\t").append("# Extra expression parameters").append("\n");
				sb.append("\t").append(URI_HAS_PARAM).append("\n");
				sb.append("\t\t\t").append("[");
				// Ctx-Expr expression parameters
				prepareNestedExpression(sb, "\t\t\t", cedef.type, cedef.params, URI_EXPRESSION, URI_HAS_PARAM, URI_REFERS_TO, URI_ASSOC_WITH);
				sb.append("\t\t\t").append("] ; ").append("\n");
			} else {
				// Ctx-Expr expression parameters
				sb.append("\t").append("# Expression parameters").append("\n");
				prepareNestedExpression(sb, "", cedef.type, cedef.params, URI_EXPRESSION, URI_HAS_PARAM, URI_REFERS_TO, URI_ASSOC_WITH);
			}
			// Ctx-Expr evaluation result (if any)
			if (cedef.evalValue!=null) {
				sb.append("\t").append(URI_HAS_VALUE).append("\t").append( cedef.evalValue ).append(" ;").append("\n");
			}
			// Ctx-Expr id
			sb.append("\t").append(URI_ID).append("\t\"").append(cedef.id).append("\"^^").append(URI_STRING).append(" ; ").append("\n");
			// Ctx-Expr name
			sb.append("\t").append(URI_LABEL).append("\t\"").append( sanitizeUri(cedef.name) ).append("\"^^").append(URI_STRING).append(" . ").append("\n\n");
		}
		
		// produce footer
		sb.append("\n#EOF");
		return sb.toString();
	}
	
	protected static void prepareNestedExpression(StringBuilder sb, String ident, String nestedExprType, Vector<ContextExpressionDefinition> nestedExprParams, String URI_EXPRESSION, String URI_HAS_PARAM, String URI_REFERS_TO, String URI_ASSOC_WITH) {
		// Nested Ctx-Expr type
		sb.append("\ta\t").append( sanitizeUri(nestedExprType) ).append(" ;").append("\n");
		// If expression contains more than two parameters then chain them
		if (nestedExprParams.size()>2) {
			// add first parameter
			ContextExpressionDefinition childCedef = nestedExprParams.get(0);
			if (childCedef.type==null) {
				// It is a simple parameter
				sb.append(ident).append("\t").append(URI_HAS_PARAM).append("\n");
				sb.append(ident).append("\t\t\t").append("[");
				sb.append("\t").append("a").append("\t").append( URI_EXPRESSION ).append(" ; ").append("\n");
				if (childCedef.refersTo!=null)
					sb.append(ident).append("\t\t\t\t").append(URI_REFERS_TO).append("\t").append( sanitizeUri(childCedef.refersTo) ).append(" ; ").append("\n");
				else
					sb.append(ident).append("\t\t\t\t").append(URI_ASSOC_WITH).append("\t").append( sanitizeUri(childCedef.associatedWith) ).append(" ; ").append("\n");
				
				sb.append(ident).append("\t\t\t\t").append(URI_HAS_PARAM).append("\n");
				sb.append(ident).append("\t\t\t\t\t").append("[");
				if (childCedef.datatype!=null) sb.append("\t").append("a").append("\t").append( sanitizeUri(childCedef.datatype) ).append(" ; ");
				else sb.append("\t# Unknown datatype");
				sb.append("\n");
				sb.append(ident).append("\t\t\t\t\t\t").append( sanitizeUri(childCedef.property) ).append("\t").append( sanitizeUri(childCedef.hasParameter) ).append(" ; ").append("\n");
				sb.append(ident).append("\t\t\t\t\t").append("] ; ").append("\n");
				sb.append(ident).append("\t\t\t").append("] ; ").append("\n");
			} else {
				// It is a composite (AND/OR) parameter
				sb.append(ident).append("\t").append(URI_HAS_PARAM).append("\n").append(ident).append("\t\t\t").append("[");
				prepareNestedExpression(sb, ident+"\t\t\t", childCedef.type, childCedef.params, URI_EXPRESSION, URI_HAS_PARAM, URI_REFERS_TO, URI_ASSOC_WITH);
				sb.append(ident).append("\t\t\t").append("] ; ").append("\n");
			}
			// add a new AND/OR expression of the same type as second parameter
			sb.append(ident).append("\t").append(URI_HAS_PARAM).append("\n").append(ident).append("\t\t\t").append("[");
			nestedExprParams.remove(0);		// remove the first parameter
			prepareNestedExpression(sb, ident+"\t\t\t", nestedExprType, nestedExprParams, URI_EXPRESSION, URI_HAS_PARAM, URI_REFERS_TO, URI_ASSOC_WITH);
			sb.append(ident).append("\t\t\t").append("] ; ").append("\n");
			return;
		}
		// Nested Ctx-Expr parameters (contains at most 2 parameters)
		for (ContextExpressionDefinition childCedef : nestedExprParams) {
			if (childCedef.type==null) {
				// It is a simple parameter
				sb.append(ident).append("\t").append(URI_HAS_PARAM).append("\n");
				sb.append(ident).append("\t\t\t").append("[");
				sb.append("\t").append("a").append("\t").append( URI_EXPRESSION ).append(" ; ").append("\n");
				if (childCedef.refersTo!=null)
					sb.append(ident).append("\t\t\t\t").append(URI_REFERS_TO).append("\t").append( sanitizeUri(childCedef.refersTo) ).append(" ; ").append("\n");
				else
					sb.append(ident).append("\t\t\t\t").append(URI_ASSOC_WITH).append("\t").append( sanitizeUri(childCedef.associatedWith) ).append(" ; ").append("\n");
				sb.append(ident).append("\t\t\t\t").append(URI_HAS_PARAM).append("\n");
				sb.append(ident).append("\t\t\t\t\t").append("[");
				if (childCedef.datatype!=null) sb.append("\t").append("a").append("\t").append( sanitizeUri(childCedef.datatype) ).append(" ; ");
				else sb.append("\t# Unknown datatype");
				sb.append("\n");
				sb.append(ident).append("\t\t\t\t\t\t").append( sanitizeUri(childCedef.property) ).append("\t").append( sanitizeUri(childCedef.hasParameter) ).append(" ; ").append("\n");
				sb.append(ident).append("\t\t\t\t\t").append("] ; ").append("\n");
				sb.append(ident).append("\t\t\t").append("] ; ").append("\n");
			} else {
				// It is a composite (AND/OR) parameter
				sb.append(ident).append("\t").append(URI_HAS_PARAM).append("\n");
				sb.append(ident).append("\t\t\t").append("[");
				prepareNestedExpression(sb, ident+"\t\t\t", childCedef.type, childCedef.params, URI_EXPRESSION, URI_HAS_PARAM, URI_REFERS_TO, URI_ASSOC_WITH);
				sb.append(ident).append("\t\t\t").append("] ; ").append("\n");
			}
		}
	}
	
	//NOT IMPLEMENTED: It is not needed for the time being
	/*protected static JsonDefinition toJsonDefinition(String rdfStr) {
		throw new RuntimeException("Method not implemented : toJsonDefinition");
	}*/

	// =========================================================================================================
	//  DEFINITION OF CLASSES representing the various JSON constructs encompassed in a JSON message
	// =========================================================================================================

	protected static class JsonDefinition {
		java.util.HashMap<String,String> prefixes;
		java.util.Vector<PolicySetDefinition> policySets;
		java.util.Vector<PolicyDefinition> policies;
		java.util.Vector<RuleDefinition> rules;
		java.util.Vector<ContextExpressionDefinition> contextExpressions;
		
		public JsonDefinition() {
			prefixes = new java.util.HashMap<String,String>();
			policySets = new java.util.Vector<PolicySetDefinition>();
			policies = new java.util.Vector<PolicyDefinition>();
			rules = new java.util.Vector<RuleDefinition>();
			contextExpressions = new java.util.Vector<ContextExpressionDefinition>();
		}
		
		public String toString() {
			return toString(null).toString();
		}
		
		public StringBuilder toString(StringBuilder sb) {
			if (sb==null) sb = new StringBuilder();
			sb.append("@PREFIXES: ").append(prefixes).append("\n");
			sb.append("@POLICY-SETS: \n");
			for (PolicySetDefinition psdef : policySets) psdef.toString(sb).append("\n");
			sb.append("@POLICIES: \n");
			for (PolicyDefinition pdef : policies) pdef.toString(sb).append("\n");
			sb.append("@POLICY-RULES: \n");
			for (RuleDefinition rdef : rules) rdef.toString(sb).append("\n");
			sb.append("@CONTEXT-EXPRESSIONS: \n");
			for (ContextExpressionDefinition cedef : contextExpressions) cedef.toString(sb).append("\n");
			return sb;
		}
	}

	protected static class PolicySetDefinition {
		long id;
		String name;
		String uri;
		String combiningAlgorithm;
		java.util.Vector<String> policies;
		java.util.Vector<String> policySets;
		
		public PolicySetDefinition(long p1, String p2, String p3, String p4) {
			id = p1; name = p2; uri = p3; combiningAlgorithm = p4;
			policies = new java.util.Vector<String>();
			policySets = new java.util.Vector<String>();
		}
		public void addPolicy(String p) { policies.add(p); }
		public void addPolicy(PolicyDefinition p) { policies.add(p.uri); }
		public void addPolicySet(String ps) { policySets.add(ps); }
		public void addPolicySet(PolicySetDefinition ps) { policySets.add(ps.uri); }
		
		public StringBuilder toString(StringBuilder sb) {
			sb.append( String.format("\t@POLICY-SET: id=%d, name=%s, comb.alg.=%s \n\turi=%s \n", id, name, combiningAlgorithm, uri) );
			sb.append("\tpolicies = \n");
			for (String p : policies) sb.append("\t\t").append(p).append("\n");
			sb.append("\tpolicy sets = \n");
			if (policySets!=null) for (String ps : policySets) sb.append("\t\t").append(ps).append("\n");
			return sb;
		}
	}
	protected static class PolicyDefinition {
		long id;
		String name;
		String uri;
		String combiningAlgorithm;
		java.util.Vector<String> rules;
		
		public PolicyDefinition(long p1, String p2, String p3, String p4) {
			id = p1; name = p2; uri = p3; combiningAlgorithm = p4;
			rules = new java.util.Vector<String>();
		}
		public void addRule(String r) { rules.add(r); }
		public void addRule(RuleDefinition r) { rules.add(r.uri); }
		
		public StringBuilder toString(StringBuilder sb) {
			sb.append( String.format("\t\t@POLICY: id=%d, name=%s, comb.alg.=%s\n\t\t\turi=%s\n", id, name, combiningAlgorithm, uri) );
			sb.append("\t\trules = \n");
			for (String r : rules) sb.append("\t\t\t").append(r).append("\n");
			return sb;
		}
	}
	protected static class RuleDefinition {
		long id;
		String name;
		String uri;
		String controlledObjectUri;
		String authorizationUri;
		String actionUri;
		String actorUri;
		String exprUri;
		
		public RuleDefinition(long p1, String p2, String p3,  String r1, String r2, String r3, String r4, String r5) {
			id = p1; name = p2; uri = p3;
			controlledObjectUri = r1; authorizationUri = r2; actionUri = r3;
			actorUri = r4; exprUri = r5;
		}
		
		public StringBuilder toString(StringBuilder sb) {
			sb.append( String.format("\t\t\t@RULE: id=%d, name=%s, uri=%s\n\t\t\tcontrolled-obj: %s\n\t\t\tauthorization: %s\n\t\t\taction: %s\n\t\t\tactor: %s\n\t\t\texpression: %s\n", 
						id, name, uri, controlledObjectUri, authorizationUri, actionUri, actorUri, exprUri) );
			return sb;
		}
	}
	protected static class ContextExpressionDefinition {
		long id;
		String name;
		String uri;
		String type;
		String evalValue;
		java.util.Vector<ContextExpressionDefinition> nestedExpressions;
		java.util.Vector<ContextExpressionDefinition> params;
		String refersTo;
		String associatedWith;
		String property;
		String hasParameter;
		String datatype;
		
		public ContextExpressionDefinition(long p1, String p2, String p3, String p4, String p5) {
			id = p1; name = p2; uri = p3; type = p4; evalValue = p5;
		}
		public ContextExpressionDefinition(String p1, String p2, String p3, String p4) {
			refersTo = p1; property = p2; hasParameter = p3; datatype = p4;
		}
		public void addNestedExpression(ContextExpressionDefinition ne) {
			if (nestedExpressions==null) nestedExpressions = new java.util.Vector<ContextExpressionDefinition>();
			nestedExpressions.add(ne);
		}
		public void addExpressionParameter(ContextExpressionDefinition ep) {
			if (params==null) params = new java.util.Vector<ContextExpressionDefinition>();
			params.add(ep);
		}
		
		public StringBuilder toString(StringBuilder sb) {
			sb.append( String.format("\t@CONTEXT-EXPRESSION: id=%d, name=%s, type=%s, eval-value=%s, uri=%s\n", id, name, type, evalValue, uri) );
			sb.append( String.format("\t                     refers-to=%s, assoc-with=%s, property=%s, has-param=%s, datatype=%s\n", refersTo, associatedWith, property, hasParameter, datatype) );
			sb.append( String.format("\t                     nested-expressions = \n") );
			if (nestedExpressions!=null) sb.append("\t\t\t").append(nestedExpressions.toString()).append("\n");
			sb.append( String.format("\t                     expression-params = \n") );
			if (params!=null) sb.append("\t\t\t").append(params.toString()).append("\n");
			return sb;
		}
		public String toString() {
			return toString(new StringBuilder()).toString();
		}
	}
}