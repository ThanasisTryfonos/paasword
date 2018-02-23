package eu.paasword.repository.relational.util;

import eu.paasword.repository.relational.dao.*;
import eu.paasword.repository.relational.domain.*;
import eu.paasword.util.Util;
import eu.paasword.util.entities.AnnotatedAnnotation;
import eu.paasword.util.entities.AnnotatedCode;
import eu.paasword.util.entities.AnnotatedMethod;
import eu.paasword.util.entities.AnnotatedParameter;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by smantzouratos on 24/05/16.
 */
public class RepositoryUtil {

    private static final Logger logger = Logger.getLogger(RepositoryUtil.class.getName());

    public static String constructOntologyForSemAuthEngine(ClazzRepository clazzRepository, PropertyRepository propertyRepository, InstanceRepository instanceRepository) {

        logger.info("Constructing Ontology for Semantic Authorization Engine...");

        String ontology = "";

        List<String> classesSTR = new ArrayList<>();
        List<String> instancesSTR = new ArrayList<>();
        List<String> propertiesSTR = new ArrayList<>();

        try {

            // Construct JSON from Database Data
            JSONObject jsonObject = new JSONObject();

            // Fetch classes
            List<Clazz> classes = clazzRepository.findAllCustom(null);

            if (null != classes && !classes.isEmpty()) {

                classes.stream().forEach(clazz -> {

                    if (!clazz.hasFather()) {
                        classesSTR.add("C," + clazz.getClassName() + ",null");
                    } else {
                        classesSTR.add("C," + clazz.getClassName() + "," + clazz.getParentID().getClassName());
                    }

                });

            }

            List<Instance> instances = instanceRepository.findAll();

            if (null != instances && !instances.isEmpty()) {

                instances.stream().forEach(instance -> {

                    instancesSTR.add("IoC," + instance.getInstanceName() + "," + instance.getClassID().getClassName());

                });

            }

            List<Property> properties = propertyRepository.findAll();

            if (null != properties && !properties.isEmpty()) {

                properties.stream().forEach(property -> {

                    if (property.isObjectProperty()) {

                        boolean transitivity = property.getTransitivity() == 1 ? true : false;

                        if (null == property.getSubPropertyOfID()) {
                            propertiesSTR.add("OP," + property.getName() + "," + property.getClassID().getClassName() + "," + property.getObjectPropertyClassID().getClassName() + "," + transitivity + ",null");
                        } else {
                            propertiesSTR.add("OP," + property.getName() + "," + property.getClassID().getClassName() + "," + property.getObjectPropertyClassID().getClassName() + "," + transitivity + "," + property.getSubPropertyOfID().getName());
                        }

                    }

                });

            }

            if (!classesSTR.isEmpty()) {

                for (String clazzSTR : classesSTR) {
                    ontology += clazzSTR + "\n";
                }

            }

            if (!instancesSTR.isEmpty()) {

                for (String instanceSTR : instancesSTR) {
                    ontology += instanceSTR + "\n";
                }

            }

            if (!propertiesSTR.isEmpty()) {

                for (String propertySTR : propertiesSTR) {
                    ontology += propertySTR + "\n";
                }

            }

            logger.info("Ontology: " + ontology);

            logger.info("Ontology fetched successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            logger.severe(e.getMessage());
            return null;
        }

        return ontology;
    }

    public static String constructPolicyModelForSemAuthEngine(RuleRepository ruleRepository, ExpressionRepository expressionRepository, InstanceRepository instanceRepository, PropertyRepository propertyRepository) {

        String policyModel = "";

        logger.info("Constructing Policy Model for Semantic Authorization Engine...");

        try {

            // Fetch Rules
            List<Rule> rules = ruleRepository.findAll();
            List<String> droolRules = new ArrayList<>();

            if (null != rules && !rules.isEmpty()) {

                rules.stream().forEach(rule -> {

                    constructDroolsRule(rule, droolRules, expressionRepository, instanceRepository, propertyRepository);

                });

            }

            if (null != droolRules && !droolRules.isEmpty()) {

                for (String droolRule : droolRules) {
                    policyModel += droolRule;
                }

            }

            logger.info("Policy Model: " + policyModel);

        } catch (Exception e) {
            e.printStackTrace();
            logger.severe(e.getMessage());
            return null;
        }

        return policyModel;
    }

    public static void constructDroolsRule(Rule rule, List<String> droolsRule, ExpressionRepository expressionRepository, InstanceRepository instanceRepository, PropertyRepository propertyRepository) {

        String droolsRuleSTR = "";

        String negativeDroolsRuleSTR = "";

        Expression expression = rule.getExpressionID();

        DroolsExpression droolsExpression = new DroolsExpression();

        JSONObject expressionObj = new JSONObject(expression.getExpression());

        JSONArray rulesArray = expressionObj.getJSONArray("rules");

        JSONObject newExpressionObj = new JSONObject();

        if (null != expression.getReferredExpressions() && !expression.getReferredExpressions().isEmpty()) {

            String condition = expression.getCondition();

            newExpressionObj.put("condition", condition);

            JSONArray newRulesArray = new JSONArray();

            for (int i = 0; i < rulesArray.length(); i++) {
                newRulesArray.put(rulesArray.getJSONObject(i));
            }

            expression.getReferredExpressionsFormatted().stream().forEach(expressionID -> {

                Expression tempExpression = expressionRepository.findOne(Long.valueOf(expressionID));

                JSONArray newTempRulesArray = new JSONObject(tempExpression.getExpression()).getJSONArray("rules");

                for (int j = 0; j < newTempRulesArray.length(); j++) {

                    newRulesArray.put(newTempRulesArray.getJSONObject(j));

                }

            });

            newExpressionObj.put("rules", newRulesArray);

        } else {
            newExpressionObj = expressionObj;
        }

        parseExpressionForInstancesToDroolsExpression(droolsExpression, newExpressionObj, instanceRepository, propertyRepository);

        parseExpressionForPredicatesToDroolsExpression(droolsExpression, newExpressionObj, instanceRepository, propertyRepository);

        parseExpressionForTriplesToDroolsExpression(droolsExpression, newExpressionObj, instanceRepository, propertyRepository);

        parseExpressionForEvalStringToDroolsExpression(droolsExpression, newExpressionObj, instanceRepository, propertyRepository);

        droolsRuleSTR += "rule \"" + rule.getRuleName() + "\" \n";
        negativeDroolsRuleSTR += "rule \"" + rule.getRuleName() + " Negation\" \n";

        droolsRuleSTR += "when \n";
        negativeDroolsRuleSTR += "when \n";

        droolsRuleSTR += "$req: InstanceOfClazz( clazz.name == 'Request' )\n";
        negativeDroolsRuleSTR += "$req: InstanceOfClazz( clazz.name == 'Request' )\n";

        if (null != droolsExpression.getInstances() && !droolsExpression.getInstances().isEmpty()) {

            for (Instance tempInstance : droolsExpression.getInstances()) {

                if (null != tempInstance) {

                    if (tempInstance.getId() != 1 && tempInstance.getId() != 2) {

                        droolsRuleSTR += "$inst" + tempInstance.getId() + ": InstanceOfClazz( clazz.name == '" + tempInstance.getClassID().getClassName() + "', name =='" + tempInstance.getInstanceName() + "' )\n";
                        negativeDroolsRuleSTR += "$inst" + tempInstance.getId() + ": InstanceOfClazz( clazz.name == '" + tempInstance.getClassID().getClassName() + "', name =='" + tempInstance.getInstanceName() + "' )\n";

                    } else if (tempInstance.getId() == 2) {

                        droolsRuleSTR += "$anySub: InstanceOfClazz( clazz.name == 'Subject' )\n";
                        negativeDroolsRuleSTR += "$anySub: InstanceOfClazz( clazz.name == 'Subject' )\n";

                    }

                }

            }

        }

        // Basic Predicates from Object Properties
        if (!rule.getActor().equals("Any Subject")) {
            droolsRuleSTR += "$pred1: ObjectProperty( name == 'requestHasSubject' )\n";
        }
        droolsRuleSTR += "$pred2: ObjectProperty( name == 'requestHasObject' )\n";
        droolsRuleSTR += "$pred3: ObjectProperty( name == 'requestHasAction' )\n";

        if (!rule.getActor().equals("Any Subject")) {
            negativeDroolsRuleSTR += "$pred1: ObjectProperty( name == 'requestHasSubject' )\n";
        }
        negativeDroolsRuleSTR += "$pred2: ObjectProperty( name == 'requestHasObject' )\n";
        negativeDroolsRuleSTR += "$pred3: ObjectProperty( name == 'requestHasAction' )\n";

        // Predicates from Expressions

        if (null != droolsExpression.getPredicates() && !droolsExpression.getPredicates().isEmpty()) {

            for (Property objectProperty : droolsExpression.getPredicates()) {

                droolsRuleSTR += "$predOP" + objectProperty.getId() + ": ObjectProperty( name == '" + objectProperty.getName() + "') \n";

                negativeDroolsRuleSTR += "$predOP" + objectProperty.getId() + ": ObjectProperty( name == '" + objectProperty.getName() + "') \n";

            }

        }


        if (!rule.getActor().equals("Any Subject")) {
            droolsRuleSTR += "$tr1: KnowledgeTriple( subject == $req , predicate == $pred1, $object1: object )\n";
        }
        droolsRuleSTR += "$tr2: KnowledgeTriple( subject == $req , predicate == $pred2, $object2: object )\n";
        droolsRuleSTR += "$tr3: KnowledgeTriple( subject == $req , predicate == $pred3, $object3: object )\n";

        if (!rule.getActor().equals("Any Subject")) {
            negativeDroolsRuleSTR += "$tr1: KnowledgeTriple( subject == $req , predicate == $pred1, $object1: object )\n";
        }
        negativeDroolsRuleSTR += "$tr2: KnowledgeTriple( subject == $req , predicate == $pred2, $object2: object )\n";
        negativeDroolsRuleSTR += "$tr3: KnowledgeTriple( subject == $req , predicate == $pred3, $object3: object )\n";

        // Knowledge Triples by parsing Expressions
        if (null != droolsExpression.getTriples() && !droolsExpression.getTriples().isEmpty()) {

            int counter = 4;

            for (Triple triple : droolsExpression.getTriples()) {

                if (triple.getDomainInstance().getId() == 1) {

                    droolsRuleSTR += "$tr" + counter + ": KnowledgeTriple( subject == $req , predicate == $predOP" + triple.getProperty().getId() + ", $reqPredOP" + triple.getProperty().getId() + "objectInst" + triple.getRangeInstance().getId() + ": object )\n";

                    negativeDroolsRuleSTR += "$tr" + counter + ": KnowledgeTriple( subject == $req , predicate == $predOP" + triple.getProperty().getId() + ", $reqPredOP" + triple.getProperty().getId() + "objectInst" + triple.getRangeInstance().getId() + ": object )\n";

                } else if (triple.getDomainInstance().getId() == 2) {

                    droolsRuleSTR += "$tr" + counter + ": KnowledgeTriple( subject == $anySub , predicate == $predOP" + triple.getProperty().getId() + ", $anySubPredOP" + triple.getProperty().getId() + "objectInst" + triple.getRangeInstance().getId() + ": object )\n";

                    negativeDroolsRuleSTR += "$tr" + counter + ": KnowledgeTriple( subject == $anySub , predicate == $predOP" + triple.getProperty().getId() + ", $anySubPredOP" + triple.getProperty().getId() + "objectInst" + triple.getRangeInstance().getId() + ": object )\n";

                } else {

                    droolsRuleSTR += "$tr" + counter + ": KnowledgeTriple( subject == $inst" + triple.getDomainInstance().getId() + " , predicate == $predOP" + triple.getProperty().getId() + ", $inst" + triple.getDomainInstance().getId() + "PredOP" + triple.getProperty().getId() + "objectInst" + triple.getRangeInstance().getId() + ": object )\n";

                    negativeDroolsRuleSTR += "$tr" + counter + ": KnowledgeTriple( subject == $inst" + triple.getDomainInstance().getId() + " , predicate == $predOP" + triple.getProperty().getId() + ", $inst" + triple.getDomainInstance().getId() + "PredOP" + triple.getProperty().getId() + "objectInst" + triple.getRangeInstance().getId() + ": object )\n";

                }

                counter++;

            }

        }

        // not ( exists(  Advice(  requestid == $req.getName() , rulename == 'Test' ) ) ) and (
        droolsRuleSTR += "not ( exists( Advice ( requestid == $req.getName(), rulename == '" + rule.getRuleName() + "' ) ) ) and ( \n";

        negativeDroolsRuleSTR += "not ( exists( Advice ( requestid == $req.getName(), rulename == '" + rule.getRuleName() + " Negation' ) ) ) and ( \n";


        negativeDroolsRuleSTR += "not ( \n";

        if (!rule.getActor().equals("Any Subject")) {
            droolsRuleSTR += "eval ( $object1.getName().equalsIgnoreCase(\"" + rule.getActor() + "\") ) and \n";
        }
        droolsRuleSTR += "eval ( $object2.getName().equalsIgnoreCase(\"" + rule.getControlledObject() + "\") ) and \n";
        droolsRuleSTR += "eval ( $object3.getName().equalsIgnoreCase(\"" + rule.getAction() + "\") ) and \n";

        if (!rule.getActor().equals("Any Subject")) {
            negativeDroolsRuleSTR += "eval ( $object1.getName().equalsIgnoreCase(\"" + rule.getActor() + "\") ) and \n";
        }
        negativeDroolsRuleSTR += "eval ( $object2.getName().equalsIgnoreCase(\"" + rule.getControlledObject() + "\") ) and \n";
        negativeDroolsRuleSTR += "eval ( $object3.getName().equalsIgnoreCase(\"" + rule.getAction() + "\") ) and \n";

        // From Expressions
        if (null != droolsExpression.getEval() && !droolsExpression.getEval().isEmpty()) {

            String eval = droolsExpression.getEval();

            droolsRuleSTR += eval;
            negativeDroolsRuleSTR += eval;

        }

        droolsRuleSTR += " )\n";

        negativeDroolsRuleSTR += "  \n";

        negativeDroolsRuleSTR += ") )\n";

        droolsRuleSTR += "then \n";

        String authorization = rule.getAuthorization().substring(rule.getAuthorization().indexOf(":") + 1);

        droolsRuleSTR += "Advice advice = new Advice('" + authorization + "', $req.getName() , '" + rule.getRuleName() + "' ); \n";

        droolsRuleSTR += "insert(advice); \n";

        droolsRuleSTR += "System.out.println(\"rule-" + rule.getId() + " " + authorization + " Advice for request: \" + $req.getName() + \", " + rule.getRuleName()  + "\"); \n";

        droolsRuleSTR += "end \n";

        negativeDroolsRuleSTR += "then \n";

        String negationAuthorization = authorization.equals("positive") ? "negative" : "positive";

        negativeDroolsRuleSTR += "Advice advice = new Advice('" + negationAuthorization + "', $req.getName() , '" + rule.getRuleName() + " Negation'  ); \n";

        negativeDroolsRuleSTR += "insert(advice); \n";

        negativeDroolsRuleSTR += "System.out.println(\"rule-" + rule.getId() + " " + negationAuthorization + " Advice for request: \" + $req.getName() + \", " + rule.getRuleName()  + " Negation \"); \n";

        negativeDroolsRuleSTR += "end \n";

        droolsRule.add(droolsRuleSTR);

        droolsRule.add(negativeDroolsRuleSTR);

    }

    public static void parseExpressionForInstancesToDroolsExpression(DroolsExpression droolsExpression, JSONObject expressionObj, InstanceRepository instanceRepository, PropertyRepository propertyRepository) {

        JSONArray rulesArray = expressionObj.getJSONArray("rules");

        for (Object rule : rulesArray) {

            JSONObject ruleObj = (JSONObject) rule;

            if (ruleObj.has("rules")) {

                // Iterate
                parseExpressionForInstancesToDroolsExpression(droolsExpression, ruleObj, instanceRepository, propertyRepository);

            } else {

                String id = ruleObj.getString("id");
                // Instance
                long instanceID = Long.valueOf(id.substring(1, id.lastIndexOf("p")));
                Instance instance = instanceRepository.findOne(instanceID);

                // Property
                long propertyID = Long.valueOf(id.substring(id.lastIndexOf("p") + 1));
                Property property = propertyRepository.findOne(propertyID);

                // Instances
                List<Instance> instances = null;
                if (null != droolsExpression.getInstances() && !droolsExpression.getInstances().isEmpty()) {
                    instances = droolsExpression.getInstances();
                } else {
                    instances = new ArrayList<>();
                }

                if (!instances.contains(instance)) {
                    instances.add(instance);
                }

                droolsExpression.setInstances(instances);

            }

        }

    }

    public static void parseExpressionForPredicatesToDroolsExpression(DroolsExpression droolsExpression, JSONObject expressionObj, InstanceRepository instanceRepository, PropertyRepository propertyRepository) {

        JSONArray rulesArray = expressionObj.getJSONArray("rules");

        for (Object rule : rulesArray) {

            JSONObject ruleObj = (JSONObject) rule;

            if (ruleObj.has("rules")) {

                // Iterate
                parseExpressionForPredicatesToDroolsExpression(droolsExpression, ruleObj, instanceRepository, propertyRepository);

            } else {

                String id = ruleObj.getString("id");
                // Instance
                long instanceID = Long.valueOf(id.substring(1, id.lastIndexOf("p")));
                Instance instance = instanceRepository.findOne(instanceID);

                // Property
                long propertyID = Long.valueOf(id.substring(id.lastIndexOf("p") + 1));
                Property property = propertyRepository.findOne(propertyID);

                // Predicates
                List<Property> properties = null;
                if (null != droolsExpression.getPredicates() && !droolsExpression.getPredicates().isEmpty()) {

                    properties = droolsExpression.getPredicates();

                } else {
                    properties = new ArrayList<>();
                }

                if (!properties.contains(property)) {
                    properties.add(property);
                }

                droolsExpression.setPredicates(properties);

            }

        }

    }

    public static void parseExpressionForTriplesToDroolsExpression(DroolsExpression droolsExpression, JSONObject expressionObj, InstanceRepository instanceRepository, PropertyRepository propertyRepository) {

        JSONArray rulesArray = expressionObj.getJSONArray("rules");

        String condition = expressionObj.getString("condition");

        for (Object rule : rulesArray) {

            JSONObject ruleObj = (JSONObject) rule;

            if (ruleObj.has("rules")) {

                // Iterate
                parseExpressionForTriplesToDroolsExpression(droolsExpression, ruleObj, instanceRepository, propertyRepository);

            } else {

                String id = ruleObj.getString("id");
                // Instance
                long instanceID = Long.valueOf(id.substring(1, id.lastIndexOf("p")));
                Instance instance = instanceRepository.findOne(instanceID);

                // Property
                long propertyID = Long.valueOf(id.substring(id.lastIndexOf("p") + 1));
                Property property = propertyRepository.findOne(propertyID);

                // Triples
                List<Triple> triples = null;
                if (null != droolsExpression.getTriples() && !droolsExpression.getTriples().isEmpty()) {
                    triples = droolsExpression.getTriples();
                } else {
                    triples = new ArrayList<>();
                }

                Triple triple = new Triple();
                triple.setCondition(ruleObj.getString("operator"));
                triple.setDomainClazz(instance.getClassID());
                triple.setDomainInstance(instance);

                triple.setProperty(property);

                triple.setRangeClazz(property.getObjectPropertyClassID());

                Instance rangeInstance = instanceRepository.findOne(Long.valueOf(ruleObj.getString("value")));

                triple.setRangeInstance(rangeInstance);

                if (!triples.contains(triple)) {
                    triples.add(triple);
                }

                droolsExpression.setTriples(triples);

            }

        }

    }

    public static void parseExpressionForEvalStringToDroolsExpression(DroolsExpression droolsExpression, JSONObject expressionObj, InstanceRepository instanceRepository, PropertyRepository propertyRepository) {

        JSONArray rulesArray = expressionObj.getJSONArray("rules");

        String condition = expressionObj.getString("condition").toLowerCase();

        if (condition.equals("or")) {
            condition = "||";
        } else {
            condition = "&&";
        }

        if (null != droolsExpression.getEval() && !droolsExpression.getEval().isEmpty()) {

            // Coming from inner loop
            int length = rulesArray.length();
            String newEval = "";

            for (int i = 0; i < length; i++) {

                newEval += "^^ " + condition;

            }

            newEval = newEval.substring(0, newEval.lastIndexOf(condition));

            String eval = StringUtils.replaceOnce(droolsExpression.getEval(), "^^", newEval);

            droolsExpression.setEval(eval);

        } else {

            // Empty Eval
            int length = rulesArray.length();
            String eval = "( ";

            for (int i = 0; i < length; i++) {

                eval += "^^ " + condition;

            }

            eval = eval.substring(0, eval.lastIndexOf(condition));

            eval += " )";

            droolsExpression.setEval(eval);


        }

        for (Object rule : rulesArray) {

            JSONObject ruleObj = (JSONObject) rule;

            if (ruleObj.has("rules")) {

                String eval = StringUtils.replaceOnce(droolsExpression.getEval(), "^^", " ( ^^ ) ");

                droolsExpression.setEval(eval);

                // Iterate
                parseExpressionForEvalStringToDroolsExpression(droolsExpression, ruleObj, instanceRepository, propertyRepository);

            } else {

                String id = ruleObj.getString("id");
                // Instance
                long instanceID = Long.valueOf(id.substring(1, id.lastIndexOf("p")));
                Instance instance = instanceRepository.findOne(instanceID);

                // Property
                long propertyID = Long.valueOf(id.substring(id.lastIndexOf("p") + 1));
                Property property = propertyRepository.findOne(propertyID);

                // Range Instance
                Instance rangeInstance = instanceRepository.findOne(Long.valueOf(ruleObj.getString("value")));

                // Eval String
                String tempEval = "";

                String evaluator = ".equalsIgnoreCase(";

                String notEqual = "";

                if (!ruleObj.getString("operator").equals("equal")) {
                    notEqual = "!";
                }

                if (instance.getId() == 1) {

                    tempEval += " eval (" + notEqual + "$reqPredOP" + property.getId() + "objectInst" + rangeInstance.getId() + ".getName()" + evaluator + " \"" + rangeInstance.getInstanceName().replace(" ", "_") + "\") ) ";

                } else if (instance.getId() == 2) {

                    tempEval += " eval (" + notEqual + "$anySubPredOP" + property.getId() + "objectInst" + rangeInstance.getId() + ".getName()" + evaluator + " \"" + rangeInstance.getInstanceName().replace(" ", "_") + "\") ) ";

                } else {

                    tempEval += " eval (" + notEqual + "$inst" + instance.getId() + "PredOP" + property.getId() + "objectInst" + rangeInstance.getId() + ".getName()" + evaluator + " \"" + rangeInstance.getInstanceName().replace(" ", "_") + "\") ) ";

                }

                String eval = StringUtils.replaceOnce(droolsExpression.getEval(), "^^", tempEval);

                droolsExpression.setEval(eval);

            }

        }

    }

    public static List<AnnotatedCode> identifyAllPEPsPerApplication(Application application, RuleRepository ruleRepository, PolicyRepository policyRepository, PolicySetRepository policySetRepository) {

        List<AnnotatedCode> listOfAnnotatedCode = new ArrayList<>();
        AnnotatedCode annotatedCode = null;

        boolean findPEP = false;

        if (null != application.getAnnotatedCodePEP() && !application.getAnnotatedCodePEP().isEmpty()) {

            JSONObject jsonObject = new JSONObject(application.getAnnotatedCodePEP());

            try {

                JSONArray annotatedCodeJSONArray = jsonObject.getJSONArray("annotatedCode");

                for (int i = 0; i < annotatedCodeJSONArray.length(); i++) {

                    findPEP = false;

                    JSONObject annot = annotatedCodeJSONArray.getJSONObject(i);

                    annotatedCode = new AnnotatedCode();

                    annotatedCode.setId(i + 1);
                    annotatedCode.setName(annot.getString("name"));
                    annotatedCode.setType(annot.getString("type"));

                    if (!annot.getString("category").equalsIgnoreCase("PEP")) {
                        continue;
                    }

                    // Check for Methods
                    if (null != annot.getJSONArray("methods") && annot.getJSONArray("methods").length() > 0) {

                        List<AnnotatedMethod> listOfAnnotatedMethods = new ArrayList<>();

                        for (int j = 0; j < annot.getJSONArray("methods").length(); j++) {

                            JSONObject method = annot.getJSONArray("methods").getJSONObject(j);

                            AnnotatedMethod annotMethod = new AnnotatedMethod();
                            annotMethod.setId(j + 1);
                            annotMethod.setName(method.getString("name"));

                            if (null != method.getJSONArray("annotations") && method.getJSONArray("annotations").length() > 0 && method.getJSONArray("annotations").toString().contains("PaaSwordPEP")) {

                                findPEP = true;

                                List<AnnotatedAnnotation> listOfPaaSwordAnnotations = new ArrayList<>();

                                for (int k = 0; k < method.getJSONArray("annotations").length(); k++) {

                                    JSONObject annotJSON = method.getJSONArray("annotations").getJSONObject(k);

                                    AnnotatedAnnotation paaSwordAnnotation = new AnnotatedAnnotation();
                                    paaSwordAnnotation.setId(k + 1);
                                    paaSwordAnnotation.setName(annotJSON.getString("name").substring(annotJSON.getString("name").lastIndexOf(".") + 1));

                                    if (annotJSON.get("value") instanceof JSONArray) {

                                        String annotArray = annotJSON.getJSONArray("value").toString();

                                        paaSwordAnnotation.setValue(annotArray.substring(1, annotArray.length() - 1));

                                    } else {
                                        paaSwordAnnotation.setValue(annotJSON.getString("value"));
                                    }

                                    paaSwordAnnotation.setType(annotJSON.getString("type"));

                                    listOfPaaSwordAnnotations.add(paaSwordAnnotation);

                                }

                                annotMethod.setMethodAnnotations(listOfPaaSwordAnnotations);

                            }

                            if (null != method.getJSONArray("parameters") && method.getJSONArray("parameters").length() > 0) {

                                List<AnnotatedParameter> listOfParameters = new ArrayList<>();

                                for (int m = 0; m < method.getJSONArray("parameters").length(); m++) {

                                    JSONObject paramJSON = method.getJSONArray("parameters").getJSONObject(m);

                                    AnnotatedParameter annotatedParameter = new AnnotatedParameter();
                                    annotatedParameter.setId(m + 1);
                                    annotatedParameter.setName(paramJSON.getString("name"));
                                    annotatedParameter.setType(paramJSON.getString("type"));

                                    listOfParameters.add(annotatedParameter);

                                }

                                annotMethod.setParameters(listOfParameters);

                            }

                            listOfAnnotatedMethods.add(annotMethod);

                        }

                        annotatedCode.setMethods(listOfAnnotatedMethods);

                    }

                    // Check for Annotations
                    if (null != annot.getJSONArray("annotations") && annot.getJSONArray("annotations").length() > 0 && annot.getJSONArray("annotations").toString().contains("PaaSwordPEP")) {

                        findPEP = true;

                        List<AnnotatedAnnotation> listOfAnnotations = new ArrayList<>();

                        for (int n = 0; n < annot.getJSONArray("annotations").length(); n++) {

                            JSONObject annotJSON = annot.getJSONArray("annotations").getJSONObject(n);

                            AnnotatedAnnotation paaSwordAnnotation = new AnnotatedAnnotation();
                            paaSwordAnnotation.setId(n + 1);
                            paaSwordAnnotation.setName(annotJSON.getString("name").substring(annotJSON.getString("name").lastIndexOf(".") + 1));

                            if (annotJSON.get("value") instanceof JSONArray) {
                                String annotArray = annotJSON.getJSONArray("value").toString();

                                paaSwordAnnotation.setValue(annotArray.substring(1, annotArray.length() - 1));
                            } else {
                                paaSwordAnnotation.setValue(annotJSON.getString("value"));
                            }

                            paaSwordAnnotation.setType(annotJSON.getString("type"));

                            listOfAnnotations.add(paaSwordAnnotation);


                        }

                        annotatedCode.setAnnotations(listOfAnnotations);

                    }

                    if (findPEP) {
                        listOfAnnotatedCode.add(annotatedCode);
                    }

                }

            } catch (JSONException e) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        if (null != listOfAnnotatedCode & !listOfAnnotatedCode.isEmpty()) {

            listOfAnnotatedCode.stream().forEach(pep -> {

                List<AnnotatedAnnotation> classAnnotation = pep.getAnnotations();

                if (null != classAnnotation && !classAnnotation.isEmpty()) {

                    classAnnotation.stream().forEach(annot -> {

                        String value = annot.getValue().substring(1, annot.getValue().length() - 1);

                        switch (annot.getType()) {
                            case "RULE":

                                Rule existingRule = ruleRepository.findByRuleName(value);
                                if (null != existingRule) {
                                    annot.setExists(true);
                                    annot.setEntityID(existingRule.getId());
                                } else {
                                    annot.setExists(false);
                                }

                                break;
                            case "POLICY":

                                Policy existingPolicy = policyRepository.getPolicyByName(value);
                                if (null != existingPolicy) {
                                    annot.setExists(true);
                                    annot.setEntityID(existingPolicy.getId());
                                } else {
                                    annot.setExists(false);
                                }

                                break;
                            case "POLICY_SET":

                                PolicySet existingPolicySet = policySetRepository.findByPolicySetName(value);
                                if (null != existingPolicySet) {
                                    annot.setExists(true);
                                    annot.setEntityID(existingPolicySet.getId());
                                } else {
                                    annot.setExists(false);
                                }
                                break;
                        }

                    });

                }

                List<AnnotatedMethod> methods = pep.getMethods();

                if (null != methods && !methods.isEmpty()) {

                    methods.stream().forEach(method -> {

                        method.getMethodAnnotations().stream().forEach(annot -> {

                            String value = annot.getValue().substring(1, annot.getValue().length() - 1);

                            switch (annot.getType()) {
                                case "RULE":

                                    Rule existingRule = ruleRepository.findByRuleName(value);
                                    if (null != existingRule) {
                                        annot.setExists(true);
                                        annot.setEntityID(existingRule.getId());
                                    } else {
                                        annot.setExists(false);
                                    }

                                    break;
                                case "POLICY":

                                    Policy existingPolicy = policyRepository.getPolicyByName(value);
                                    if (null != existingPolicy) {
                                        annot.setExists(true);
                                        annot.setEntityID(existingPolicy.getId());
                                    } else {
                                        annot.setExists(false);
                                    }

                                    break;
                                case "POLICY_SET":

                                    PolicySet existingPolicySet = policySetRepository.findByPolicySetName(value);
                                    if (null != existingPolicySet) {
                                        annot.setExists(true);
                                        annot.setEntityID(existingPolicySet.getId());
                                    } else {
                                        annot.setExists(false);
                                    }
                                    break;
                            }

                        });

                    });
                }

            });

        }

        return listOfAnnotatedCode;
    }

    @Deprecated
    public static void parseExpressionToDroolsExpression(DroolsExpression droolsExpression, JSONObject expressionObj, InstanceRepository instanceRepository, PropertyRepository propertyRepository) {

        JSONArray rulesArray = expressionObj.getJSONArray("rules");

        String condition = expressionObj.getString("condition");

        for (Object rule : rulesArray) {

            JSONObject ruleObj = (JSONObject) rule;

            if (ruleObj.has("rules")) {

                String tempCondition = ruleObj.getString("condition");

                // Eval String
                String eval = "";
                if (null != droolsExpression.getEval() && !droolsExpression.getEval().isEmpty()) {
                    eval = droolsExpression.getEval() + " (";
                }

                droolsExpression.setEval(eval);

                droolsExpression.setNested(tempCondition);

                // Iterate
                parseExpressionToDroolsExpression(droolsExpression, ruleObj, instanceRepository, propertyRepository);

            } else {

                String id = ruleObj.getString("id");
                // Instance
                long instanceID = Long.valueOf(id.substring(1, id.lastIndexOf("p")));
                Instance instance = instanceRepository.findOne(instanceID);

                // Property
                long propertyID = Long.valueOf(id.substring(id.lastIndexOf("p") + 1));
                Property property = propertyRepository.findOne(propertyID);

                // Instances
                List<Instance> instances = null;
                if (null != droolsExpression.getInstances() && !droolsExpression.getInstances().isEmpty()) {
                    instances = droolsExpression.getInstances();
                } else {
                    instances = new ArrayList<>();
                }

                if (!instances.contains(instance)) {
                    instances.add(instance);
                }

                droolsExpression.setInstances(instances);

                // Predicates
                List<Property> properties = null;
                if (null != droolsExpression.getPredicates() && !droolsExpression.getPredicates().isEmpty()) {

                    properties = droolsExpression.getPredicates();

                } else {
                    properties = new ArrayList<>();
                }

                if (!properties.contains(property)) {
                    properties.add(property);
                }

                droolsExpression.setPredicates(properties);

                // Triples
                List<Triple> triples = null;
                if (null != droolsExpression.getTriples() && !droolsExpression.getTriples().isEmpty()) {
                    triples = droolsExpression.getTriples();
                } else {
                    triples = new ArrayList<>();
                }

                Triple triple = new Triple();
                triple.setCondition(ruleObj.getString("operator"));
                triple.setDomainClazz(instance.getClassID());
                triple.setDomainInstance(instance);

                triple.setProperty(property);

                triple.setRangeClazz(property.getObjectPropertyClassID());

                Instance rangeInstance = instanceRepository.findOne(Long.valueOf(ruleObj.getString("value")));

                triple.setRangeInstance(rangeInstance);

                if (!triples.contains(triple)) {
                    triples.add(triple);
                }

                droolsExpression.setTriples(triples);

                // Eval String
                String eval = "";
                if (null != droolsExpression.getEval() && !droolsExpression.getEval().isEmpty()) {
                    eval = droolsExpression.getEval();
                }

                String evaluator = "==";

                if (!triple.getCondition().equals("equal")) {
                    evaluator = "!=";
                }

                if (null != droolsExpression.getNested() && !droolsExpression.getNested().isEmpty()) {
                    eval += " eval ($objectInst" + rangeInstance.getId() + ".getName() " + evaluator + " '" + rangeInstance.getInstanceName() + "' ) " + droolsExpression.getNested().toLowerCase() + " \n";

                } else {

                    eval += " eval ($objectInst" + rangeInstance.getId() + ".getName() " + evaluator + " '" + rangeInstance.getInstanceName() + "' ) " + condition.toLowerCase() + "\n";

                }

                droolsExpression.setNested(null);

                droolsExpression.setEval(eval);

            }

        }

    }

    @Deprecated
    public static void parseExpressionForNeededTriples(JSONObject expressionObj, List<Triple> triples, InstanceRepository instanceRepository, PropertyRepository propertyRepository) {

        JSONArray rulesArray = expressionObj.getJSONArray("rules");

        for (Object rule : rulesArray) {

            JSONObject ruleObj = (JSONObject) rule;

            if (ruleObj.has("rules")) {

                // Iterate
                parseExpressionForNeededTriples(ruleObj, triples, instanceRepository, propertyRepository);

            } else {

                String id = ruleObj.getString("id");

                // Instance
                long instanceID = Long.valueOf(id.substring(1, id.lastIndexOf("p")));

                Instance instance = instanceRepository.findOne(instanceID);

                // Property

                long propertyID = Long.valueOf(id.substring(id.lastIndexOf("p") + 1));

                Property property = propertyRepository.findOne(propertyID);

                Triple triple = new Triple();
                triple.setCondition(ruleObj.getString("operator"));
                triple.setDomainClazz(instance.getClassID());
                triple.setDomainInstance(instance);

                triple.setProperty(property);

                triple.setRangeClazz(property.getObjectPropertyClassID());

                Instance rangeInstance = instanceRepository.findOne(Long.valueOf(ruleObj.getString("value")));

                triple.setRangeInstance(rangeInstance);

                if (!triples.contains(triple)) {
                    triples.add(triple);
                }

            }


        }

    }

    @Deprecated
    public static void parseExpressionForNeededObjectProperties(JSONObject expressionObj, List<Property> objectProperties, InstanceRepository instanceRepository, PropertyRepository propertyRepository) {

        JSONArray rulesArray = expressionObj.getJSONArray("rules");

        for (Object rule : rulesArray) {

            JSONObject ruleObj = (JSONObject) rule;

            if (ruleObj.has("rules")) {

                // Iterate
                parseExpressionForNeededObjectProperties(ruleObj, objectProperties, instanceRepository, propertyRepository);

            } else {

                String id = ruleObj.getString("id");
                // Property

                long propertyID = Long.valueOf(id.substring(id.lastIndexOf("p") + 1));

                Property property = propertyRepository.findOne(propertyID);

                if (!objectProperties.contains(property)) {
                    objectProperties.add(property);
                }

            }


        }

    }

    public static JSONArray enabledPEPs(Application app) {

        if (null != app) {
            return Util.parseAnnotatedSourceCodeJSONForMultiplePEPs(app.getAnnotatedCodePEP());
        } else {
            return null;
        }

    }

    public static List<Handler> identifyHandlersPerApplication(Application application, ExpressionRepository expressionRepository, RuleRepository ruleRepository, PolicyRepository policyRepository, PolicySetRepository policySetRepository, InstanceRepository instanceRepository, PropertyRepository propertyRepository) {

        List<Handler> handlers = new ArrayList<>();

        if (application.isPep()) {

            List<AnnotatedCode> annotatedCodes = Util.parseAnnotatedSourceCodeJSONOnlyForPEPs(application.getAnnotatedCodePEP());

            List<PolicySet> policySets = new ArrayList<>();
            List<Policy> policies = new ArrayList<>();
            List<Rule> rules = new ArrayList<>();
            List<Expression> expressions = new ArrayList<>();

            if (null != annotatedCodes & !annotatedCodes.isEmpty()) {

                for (AnnotatedCode pep : annotatedCodes) {

                    List<AnnotatedAnnotation> classAnnotation = pep.getAnnotations();

                    if (null != classAnnotation && !classAnnotation.isEmpty()) {

                        for (AnnotatedAnnotation annot : classAnnotation) {

                            String value = annot.getValue().substring(1, annot.getValue().length() - 1);

                            switch (annot.getType()) {
                                case "RULE":

                                    Rule existingRule = ruleRepository.findByRuleName(value);
                                    if (null != existingRule) {

                                        rules.add(existingRule);

                                    } else {
                                        // TODO
                                    }

                                    break;
                                case "POLICY":

                                    Policy existingPolicy = policyRepository.getPolicyByName(value);
                                    if (null != existingPolicy) {
                                        policies.add(existingPolicy);
                                    } else {
                                        // TODO
                                    }

                                    break;
                                case "POLICY_SET":

                                    PolicySet existingPolicySet = policySetRepository.findByPolicySetName(value);
                                    if (null != existingPolicySet) {
                                        policySets.add(existingPolicySet);
                                    } else {
                                        // TODO
                                    }

                                    break;
                            }

                        }

                    }

                    List<AnnotatedMethod> methods = pep.getMethods();

                    if (null != methods && !methods.isEmpty()) {

                        for (AnnotatedMethod method : methods) {

                            for (AnnotatedAnnotation annot : method.getMethodAnnotations()) {

                                String value = annot.getValue().substring(1, annot.getValue().length() - 1);

                                switch (annot.getType()) {
                                    case "RULE":

                                        Rule existingRule = ruleRepository.findByRuleName(value);
                                        if (null != existingRule) {

                                            rules.add(existingRule);

                                        } else {
                                            // TODO
                                        }

                                        break;
                                    case "POLICY":

                                        Policy existingPolicy = policyRepository.getPolicyByName(value);
                                        if (null != existingPolicy) {
                                            policies.add(existingPolicy);
                                        } else {
                                            // TODO
                                        }

                                        break;
                                    case "POLICY_SET":

                                        PolicySet existingPolicySet = policySetRepository.findByPolicySetName(value);
                                        if (null != existingPolicySet) {
                                            policySets.add(existingPolicySet);
                                        } else {
                                            // TODO
                                        }

                                        break;
                                }

                            }

                        }
                    }

                }

                // Check lists

                if (!policySets.isEmpty()) {

//                    logger.info("Found Policy Sets: " + policySets.size());

                    policySets.stream().forEach(policySet -> {

                        if (null != policySet.getPolicySetPolicies() && !policySet.getPolicySetPolicies().isEmpty()) {

                            policySet.getPolicySetPolicies().stream().forEach(policySetPolicy -> {

                                if (!policies.contains(policySetPolicy.getPolicy())) {
                                    policies.add(policySetPolicy.getPolicy());
                                }

                            });

                        }

                    });

                }

                if (!policies.isEmpty()) {

//                    logger.info("Found Policies: " + policies.size());

                    policies.stream().forEach(policy -> {

                        if (null != policy.getPolicyRules() && !policy.getPolicyRules().isEmpty()) {

                            policy.getPolicyRules().stream().forEach(policyRule -> {

                                if (!rules.contains(policyRule.getRule())) {
                                    rules.add(policyRule.getRule());
                                }

                            });

                        }

                    });

                }

                if (!rules.isEmpty()) {

//                    logger.info("Found Rules: " + rules.size());

                    rules.stream().forEach(rule -> {

                        if (!expressions.contains(rule.getExpressionID())) {
                            expressions.add(rule.getExpressionID());
                        }


                    });

                }


                // All expressions found

                if (!expressions.isEmpty()) {

//                    logger.info("Found Expressions: " + expressions.size());

                    expressions.stream().forEach(expression -> {

                        JSONObject expressionObj = new JSONObject(expression.getExpression());

                        if (expressionObj.has("rules")) {

                            // handlers
                            parseExpression(expressionObj, handlers, instanceRepository, propertyRepository);

                        }

                        if (null != expression.getReferredExpressions() && !expression.getReferredExpressions().isEmpty()) {


                            expression.getReferredExpressionsFormatted().stream().forEach(exprID -> {

                                JSONObject exprObj = new JSONObject(expressionRepository.findOne(Long.valueOf(exprID)).getExpression());

                                if (exprObj.has("rules")) {

                                    // handlers
                                    parseExpression(exprObj, handlers, instanceRepository, propertyRepository);

                                }

                            });

                        }

                    });

                }

            }

        } else {
            logger.info("Application doesn't use PEP annotations!");
        }

        return handlers;

    }

    public static boolean checkHandlersPerApplicationInstance(Application application, List<ApplicationInstanceHandler> applicationInstanceHandlers, ExpressionRepository expressionRepository, RuleRepository ruleRepository, PolicyRepository policyRepository, PolicySetRepository policySetRepository, InstanceRepository instanceRepository, PropertyRepository propertyRepository) {

        boolean isSuccess = false;
        int greenFlags = 0;

        List<Handler> handlers = new ArrayList<>();

        if (application.isPep()) {

            List<AnnotatedCode> annotatedCodes = Util.parseAnnotatedSourceCodeJSONOnlyForPEPs(application.getAnnotatedCodePEP());

            List<PolicySet> policySets = new ArrayList<>();
            List<Policy> policies = new ArrayList<>();
            List<Rule> rules = new ArrayList<>();
            List<Expression> expressions = new ArrayList<>();

            if (null != annotatedCodes & !annotatedCodes.isEmpty()) {

                for (AnnotatedCode pep : annotatedCodes) {

                    List<AnnotatedAnnotation> classAnnotation = pep.getAnnotations();

                    if (null != classAnnotation && !classAnnotation.isEmpty()) {

                        for (AnnotatedAnnotation annot : classAnnotation) {

                            String value = annot.getValue().substring(1, annot.getValue().length() - 1);

                            switch (annot.getType()) {
                                case "RULE":

                                    Rule existingRule = ruleRepository.findByRuleName(value);
                                    if (null != existingRule) {

                                        rules.add(existingRule);

                                    } else {
                                        // TODO
                                    }

                                    break;
                                case "POLICY":

                                    Policy existingPolicy = policyRepository.getPolicyByName(value);
                                    if (null != existingPolicy) {
                                        policies.add(existingPolicy);
                                    } else {
                                        // TODO
                                    }

                                    break;
                                case "POLICY_SET":

                                    PolicySet existingPolicySet = policySetRepository.findByPolicySetName(value);
                                    if (null != existingPolicySet) {
                                        policySets.add(existingPolicySet);
                                    } else {
                                        // TODO
                                    }

                                    break;
                            }

                        }

                    }

                    List<AnnotatedMethod> methods = pep.getMethods();

                    if (null != methods && !methods.isEmpty()) {

                        for (AnnotatedMethod method : methods) {

                            for (AnnotatedAnnotation annot : method.getMethodAnnotations()) {

                                String value = annot.getValue().substring(1, annot.getValue().length() - 1);

                                switch (annot.getType()) {
                                    case "RULE":

                                        Rule existingRule = ruleRepository.findByRuleName(value);
                                        if (null != existingRule) {

                                            rules.add(existingRule);

                                        } else {
                                            // TODO
                                        }

                                        break;
                                    case "POLICY":

                                        Policy existingPolicy = policyRepository.getPolicyByName(value);
                                        if (null != existingPolicy) {
                                            policies.add(existingPolicy);
                                        } else {
                                            // TODO
                                        }

                                        break;
                                    case "POLICY_SET":

                                        PolicySet existingPolicySet = policySetRepository.findByPolicySetName(value);
                                        if (null != existingPolicySet) {
                                            policySets.add(existingPolicySet);
                                        } else {
                                            // TODO
                                        }

                                        break;
                                }

                            }

                        }
                    }

                }

                // Check lists

                if (!policySets.isEmpty()) {

//                    logger.info("Found Policy Sets: " + policySets.size());

                    policySets.stream().forEach(policySet -> {

                        if (null != policySet.getPolicySetPolicies() && !policySet.getPolicySetPolicies().isEmpty()) {

                            policySet.getPolicySetPolicies().stream().forEach(policySetPolicy -> {

                                if (!policies.contains(policySetPolicy.getPolicy())) {
                                    policies.add(policySetPolicy.getPolicy());
                                }

                            });

                        }

                    });

                }

                if (!policies.isEmpty()) {

//                    logger.info("Found Policies: " + policies.size());

                    policies.stream().forEach(policy -> {

                        if (null != policy.getPolicyRules() && !policy.getPolicyRules().isEmpty()) {

                            policy.getPolicyRules().stream().forEach(policyRule -> {

                                if (!rules.contains(policyRule.getRule())) {
                                    rules.add(policyRule.getRule());
                                }

                            });

                        }

                    });

                }

                if (!rules.isEmpty()) {

//                    logger.info("Found Rules: " + rules.size());

                    rules.stream().forEach(rule -> {

                        if (!expressions.contains(rule.getExpressionID())) {
                            expressions.add(rule.getExpressionID());
                        }


                    });

                }


                // All expressions found

                if (!expressions.isEmpty()) {

//                    logger.info("Found Expressions: " + expressions.size());

                    expressions.stream().forEach(expression -> {

                        JSONObject expressionObj = new JSONObject(expression.getExpression());

                        if (expressionObj.has("rules")) {

                            // handlers
                            parseExpression(expressionObj, handlers, instanceRepository, propertyRepository);

                        }

                        if (null != expression.getReferredExpressions() && !expression.getReferredExpressions().isEmpty()) {


                            expression.getReferredExpressionsFormatted().stream().forEach(exprID -> {

                                JSONObject exprObj = new JSONObject(expressionRepository.findOne(Long.valueOf(exprID)).getExpression());

                                if (exprObj.has("rules")) {

                                    // handlers
                                    parseExpression(exprObj, handlers, instanceRepository, propertyRepository);

                                }

                            });

                        }

                    });

                }

            }


        } else {
            logger.info("Application doesn't use PEP annotations!");
        }


        if (!handlers.isEmpty() && null != applicationInstanceHandlers && !applicationInstanceHandlers.isEmpty()) {

            // Perform Check
            for (ApplicationInstanceHandler applicationInstanceHandler : applicationInstanceHandlers) {

                for (Handler handler : handlers) {

                    if (applicationInstanceHandler.getHandlerID().getHasInput().getId() == handler.getHasInput().getId()
                            && applicationInstanceHandler.getHandlerID().getHasOutput().getId() == handler.getHasOutput().getId()) {

                        greenFlags++;

                    }

                }

            }

        }

        if (greenFlags == applicationInstanceHandlers.size() && greenFlags > 0) {
            isSuccess = true;
        }

        return isSuccess;

    }

    public static void parseExpression(JSONObject expressionObj, List<Handler> handlers, InstanceRepository instanceRepository, PropertyRepository propertyRepository) {

        JSONArray rulesArray = expressionObj.getJSONArray("rules");

        for (Object rule : rulesArray) {

            JSONObject ruleObj = (JSONObject) rule;

            if (ruleObj.has("rules")) {

                // Iterate
                parseExpression(ruleObj, handlers, instanceRepository, propertyRepository);

            } else {

                String id = ruleObj.getString("id");

                // Instance
                long instanceID = Long.valueOf(id.substring(1, id.lastIndexOf("p")));

                Instance instance = instanceRepository.findOne(instanceID);

                Clazz hasInput = instance.getClassID();

                // Property

                long propertyID = Long.valueOf(id.substring(id.lastIndexOf("p") + 1));

                Property property = propertyRepository.findOne(propertyID);

                Clazz hasOutput = property.getObjectPropertyClassID();

                Handler handler = new Handler();
                handler.setHasInput(hasInput);
                handler.setHasOutput(hasOutput);

                if (handlers.stream().filter(tempHandler ->
                        tempHandler.getHasInput().equals(handler.getHasInput()) && (tempHandler.getHasOutput().equals(handler.getHasOutput()))
                ).collect(Collectors.toList()).isEmpty()) {

                    handlers.add(handler);
                }

            }


        }

    }

    public static List<Handler> identifyHandlers(List<Application> applications, ExpressionRepository expressionRepository, RuleRepository ruleRepository, PolicyRepository policyRepository, PolicySetRepository policySetRepository, InstanceRepository instanceRepository, PropertyRepository propertyRepository) {

        List<Handler> handlers = new ArrayList<>();

        applications.stream().forEach(application -> {

            List<Handler> subHandlers = identifyHandlersPerApplication(application, expressionRepository, ruleRepository, policyRepository, policySetRepository, instanceRepository, propertyRepository);

            if (!subHandlers.isEmpty()) {

                subHandlers.stream().forEach(subHandler -> {

                    if (!handlers.contains(subHandler)) {
                        handlers.add(subHandler);
                    }

                });

            }

        });

        return handlers;

    }

    public static JSONArray PEPsForDeletion(Application app, List<Application> deployedApps) {

        if (null != app) {

            JSONArray pepsToBeDeleted = new JSONArray();

            JSONArray enabledPEPs = Util.parseAnnotatedSourceCodeJSONForMultiplePEPs(app.getAnnotatedCodePEP());
            logger.info("Enabled PEPs: " + enabledPEPs);

            List<String> enabledPEPsToList = new ArrayList<>();

            for (int z = 0; z < enabledPEPs.length(); z++) {
                //enabledPEPsToList.add(enabledPEPs.get(z).toString());
                JSONArray enPEP = (JSONArray) enabledPEPs.get(z);
                enabledPEPsToList.add(enPEP.toString());
            }

            if (null != enabledPEPsToList && !enabledPEPsToList.isEmpty()) {

                deployedApps.stream().forEach(deployedApp -> {

                    JSONArray enabledPEPsOfDeployedApp = Util.parseAnnotatedSourceCodeJSONForMultiplePEPs(deployedApp.getAnnotatedCodePEP());

                    for (int i = 0; i < enabledPEPsOfDeployedApp.length(); i++) {

                        String pep = enabledPEPsOfDeployedApp.get(i).toString();

//                        logger.info("Checking PEP: " + pep);

                        if (!enabledPEPsToList.contains(pep)) {

//                            logger.info("PEP: " + pep + " is not used. Delete it!");

                        } else {
//                            logger.info("PEP: " + pep + " is used!");

                            enabledPEPsToList.remove(pep);
                        }
                    }

                });

                for (String pep : enabledPEPsToList) {
                    pepsToBeDeleted.put(new JSONArray(pep));
                }

                return pepsToBeDeleted;

            } else {
                return null;
            }

        } else {
            return null;
        }

    }

    public static List<Clazz> constructClassesList(List<Clazz> listClazz, long rootClassID) {

        JSONArray jsonNodes = new JSONArray();
        List<Clazz> classes = new ArrayList<>();

        listClazz.forEach(node -> {

            //Check if is father class
            if (node.isFather(rootClassID)) {
                JSONObject fatherNode = new JSONObject();
                fatherNode.put("label", node.getClassName());
                fatherNode.put("id", node.getId());
                jsonNodes.put(fatherNode);
                classes.add(node);
                //continue;
            } else {

                Object father = findMyFather(jsonNodes, node.getParentID().getId());

                JSONObject childNode = new JSONObject();
                childNode.put("label", node.getClassName());
                childNode.put("id", node.getId());
                classes.add(node);

                if (null != father) {

                    if (father instanceof JSONObject) {

                        JSONObject jsonFather = (JSONObject) father;

                        if (jsonFather.has("children")) {

                            ((JSONArray) jsonFather.get("children")).put(childNode);
                        } else {
                            JSONArray jsonArray = new JSONArray();
                            jsonArray.put(childNode);
                            jsonFather.put("children", jsonArray);

                        }

                    }

                }
            }

        });

        return classes;

    }

    public static Object findMyFather(Object jsonNode, long father_id) {

        // Terminate condition for reursive function
        if (jsonNode instanceof JSONObject && ((JSONObject) jsonNode).has("id") && (long) ((JSONObject) jsonNode).get("id") == father_id) {
            return jsonNode;
        }

        // Array condition
        if (jsonNode instanceof JSONArray) {
            //Create an iterator to traverse all nodes
            Iterator<?> jsonIterator = ((JSONArray) jsonNode).iterator();
            while (jsonIterator.hasNext()) {
                Object foundObject = findMyFather((JSONObject) jsonIterator.next(), father_id);
                if (null != foundObject) {
                    return foundObject;
                }
            }
        }

        // Object
        if (jsonNode instanceof JSONObject && ((JSONObject) jsonNode).has("children")) {
            return findMyFather(((JSONObject) jsonNode).get("children"), father_id);
        }

        // Father is not in this node
        return null;
    }

    public static void parseExpressionRules(JSONArray params, JSONArray expParamsArray, NamespaceRepository namespaceRepository, InstanceRepository instanceRepository, PropertyInstanceRepository propertyInstanceRepository) {

        for (int i = 0; i < params.length(); i++) {

            Object expParam = params.get(i);

            if (expParam instanceof JSONObject) {

                JSONObject expParamJSON = (JSONObject) expParam;

                JSONObject expParamTemp = new JSONObject();

                if (expParamJSON.has("id")) {

                    Object id = expParamJSON.get("id");

                    if (id instanceof String) {

                        PropertyInstance tempPropertyInstance = propertyInstanceRepository.findOne(Long.valueOf(expParamJSON.getString("value")));

                        expParamTemp.put("type", "property-value");

                        String propertyInstanceNamespace = "";

                        if (null != tempPropertyInstance.getPropertyID().getNamespaceID()) {
                            propertyInstanceNamespace = (null != namespaceRepository.findOne(tempPropertyInstance.getPropertyID().getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(tempPropertyInstance.getPropertyID().getNamespaceID().getId()).getPrefix() : "pcm");
                        } else {
                            propertyInstanceNamespace = "pcm";
                        }

                        String value = "";

                        if (tempPropertyInstance.getName().contains(":")) {
                            value = tempPropertyInstance.getName();
                        } else {
                            value = propertyInstanceNamespace + ":" + tempPropertyInstance.getName();
                        }

                        if (tempPropertyInstance.getPropertyID().isObjectProperty()) {

                            expParamTemp.put("value", value);


                            String propertyClassNameSpace = "";

                            if (null != tempPropertyInstance.getPropertyID().getObjectPropertyClassID().getNamespaceID()) {
                                propertyClassNameSpace = (null != namespaceRepository.findOne(tempPropertyInstance.getPropertyID().getObjectPropertyClassID().getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(tempPropertyInstance.getPropertyID().getObjectPropertyClassID().getNamespaceID().getId()).getPrefix() : "pcm");
                            } else {
                                propertyClassNameSpace = "pcm";
                            }

                            String classNamespace = "";

                            if (null != tempPropertyInstance.getPropertyID().getClassID().getNamespaceID()) {
                                classNamespace = (null != namespaceRepository.findOne(tempPropertyInstance.getPropertyID().getClassID().getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(tempPropertyInstance.getPropertyID().getClassID().getNamespaceID().getId()).getPrefix() : "pcm");
                            } else {
                                classNamespace = "pcm";
                            }


                            expParamTemp.put("datatype", propertyClassNameSpace + ":" + tempPropertyInstance.getPropertyID().getObjectPropertyClassID().getClassName());

                            expParamTemp.put("classUri", classNamespace + ":" + tempPropertyInstance.getPropertyID().getClassID().getClassName());

                            expParamTemp.put("propertyUri", propertyInstanceNamespace + ":" + tempPropertyInstance.getPropertyID().getName());

                        } else {
                            expParamTemp.put("datatype", tempPropertyInstance.getPropertyID().getPropertyTypeID().getSchemaXSD());

                            String classNamespace = "";

                            if (null != tempPropertyInstance.getPropertyID().getClassID().getNamespaceID()) {
                                classNamespace = (null != namespaceRepository.findOne(tempPropertyInstance.getPropertyID().getClassID().getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(tempPropertyInstance.getPropertyID().getClassID().getNamespaceID().getId()).getPrefix() : "pcm");
                            } else {
                                classNamespace = "pcm";
                            }

                            expParamTemp.put("classUri", classNamespace + ":" + tempPropertyInstance.getPropertyID().getClassID().getClassName());

                            expParamTemp.put("propertyUri", propertyInstanceNamespace + ":" + tempPropertyInstance.getPropertyID().getName());

                            expParamTemp.put("value", tempPropertyInstance.getName());
                        }

                        expParamsArray.put(expParamTemp);


                    } else {

                        String instanceNamespace = "";

                        Instance tempInstance = instanceRepository.findOne(Long.valueOf(expParamJSON.getString("value")));

                        if (null != tempInstance) {

                            if (null != tempInstance && null != tempInstance.getNamespaceID()) {
                                instanceNamespace = (null != namespaceRepository.findOne(tempInstance.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(tempInstance.getNamespaceID().getId()).getPrefix() : "pcm");
                            } else {
                                instanceNamespace = "pcm";
                            }

                            String classNamespace = "";

                            if (null != tempInstance && null != tempInstance.getClassID().getNamespaceID()) {
                                classNamespace = (null != namespaceRepository.findOne(tempInstance.getClassID().getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(tempInstance.getClassID().getNamespaceID().getId()).getPrefix() : "pcm");
                            } else {
                                classNamespace = "pcm";
                            }

                            expParamTemp.put("type", "instance");
                            expParamTemp.put("value", instanceNamespace + ":" + tempInstance.getInstanceName());
                            expParamTemp.put("classUri", classNamespace + ":" + tempInstance.getClassID().getClassName());

                            expParamsArray.put(expParamTemp);
                        }

                    }


                } else {

                    if (expParamJSON.getString("condition").equalsIgnoreCase("AND")) {
                        expParamTemp.put("type", "pac:ANDContextExpression");
                    } else {
                        expParamTemp.put("type", "pac:ORContextExpression");
                    }

                    JSONArray newParamsJSONArray = new JSONArray();

                    findMyParams(expParamJSON.getJSONArray("rules"), expParamTemp, newParamsJSONArray, namespaceRepository, instanceRepository, propertyInstanceRepository);

                    expParamsArray.put(expParamTemp);

                }

            }

        }

    }

    public static void findMyParams(JSONArray rules, JSONObject expParamTempOld, JSONArray newParamsJSONArray, NamespaceRepository namespaceRepository, InstanceRepository instanceRepository, PropertyInstanceRepository propertyInstanceRepository) {

        for (int i = 0; i < rules.length(); i++) {

            Object expParam = rules.get(i);

            if (expParam instanceof JSONObject) {

                JSONObject expParamJSON = (JSONObject) expParam;

                JSONObject expParamTemp = new JSONObject();

                if (expParamJSON.has("id")) {

                    Object id = expParamJSON.get("id");

                    if (id instanceof String) {

                        PropertyInstance tempPropertyInstance = propertyInstanceRepository.findOne(Long.valueOf(expParamJSON.getString("value")));

                        expParamTemp.put("type", "property-value");

                        String propertyInstanceNamespace = "";

                        if (null != tempPropertyInstance.getPropertyID().getNamespaceID()) {
                            propertyInstanceNamespace = (null != namespaceRepository.findOne(tempPropertyInstance.getPropertyID().getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(tempPropertyInstance.getPropertyID().getNamespaceID().getId()).getPrefix() : "pcm");
                        } else {
                            propertyInstanceNamespace = "pcm";
                        }

                        if (tempPropertyInstance.getPropertyID().isObjectProperty()) {

                            expParamTemp.put("value", propertyInstanceNamespace + ":" + tempPropertyInstance.getName());

                        } else {
                            expParamTemp.put("datatype", tempPropertyInstance.getPropertyID().getPropertyTypeID().getSchemaXSD());

                            String classNamespace = "";

                            if (null != tempPropertyInstance.getPropertyID().getClassID().getNamespaceID()) {
                                classNamespace = (null != namespaceRepository.findOne(tempPropertyInstance.getPropertyID().getClassID().getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(tempPropertyInstance.getPropertyID().getClassID().getNamespaceID().getId()).getPrefix() : "pcm");
                            } else {
                                classNamespace = "pcm";
                            }

                            expParamTemp.put("classUri", classNamespace + ":" + tempPropertyInstance.getPropertyID().getClassID().getClassName());

                            expParamTemp.put("propertyUri", propertyInstanceNamespace + ":" + tempPropertyInstance.getPropertyID().getName());

                            expParamTemp.put("value", tempPropertyInstance.getName());
                        }

                    } else {

                        String instanceNamespace = "";

                        logger.info("Value: " + Long.valueOf(expParamJSON.getString("value")));

                        Instance tempInstance = instanceRepository.findOne(Long.valueOf(expParamJSON.getString("value")));

                        if (null != tempInstance.getNamespaceID()) {
                            instanceNamespace = (null != namespaceRepository.findOne(tempInstance.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(tempInstance.getNamespaceID().getId()).getPrefix() : "pcm");
                        } else {
                            instanceNamespace = "pcm";
                        }

                        String classNamespace = "";

                        if (null != tempInstance.getClassID().getNamespaceID()) {
                            classNamespace = (null != namespaceRepository.findOne(tempInstance.getClassID().getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(tempInstance.getClassID().getNamespaceID().getId()).getPrefix() : "pcm");
                        } else {
                            classNamespace = "pcm";
                        }

                        expParamTemp.put("type", "instance");
                        expParamTemp.put("value", instanceNamespace + ":" + tempInstance.getInstanceName());
                        expParamTemp.put("classUri", classNamespace + ":" + tempInstance.getClassID().getClassName());

                    }

                    newParamsJSONArray.put(expParamTemp);

                } else {

                    if (expParamJSON.getString("condition").equalsIgnoreCase("AND")) {
                        expParamTemp.put("type", "pac:ANDContextExpression");
                    } else {
                        expParamTemp.put("type", "pac:ORContextExpression");
                    }

                    JSONArray newNestedParamsJSONArray = new JSONArray();

                    findMyParams(expParamJSON.getJSONArray("rules"), expParamTemp, newNestedParamsJSONArray, namespaceRepository, instanceRepository, propertyInstanceRepository);

                    newParamsJSONArray.put(expParamTemp);

                }

            }

        }


        expParamTempOld.put("params", newParamsJSONArray);

    }


}
