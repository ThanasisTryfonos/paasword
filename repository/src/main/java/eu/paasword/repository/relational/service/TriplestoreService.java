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
package eu.paasword.repository.relational.service;

import eu.paasword.repository.relational.dao.*;
import eu.paasword.repository.relational.domain.*;
import eu.paasword.triplestoreapi.client.TriplestoreClient;
import eu.paasword.triplestoreapi.parser.ContextModel2RdfParser;
import eu.paasword.triplestoreapi.parser.PolicyModelParser;
import eu.paasword.triplestoreapi.response.PolicyModelParserResponse;
import eu.paasword.util.entities.ContextModel;
import eu.paasword.util.entities.PolicyModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by smantzouratos on 12/04/16.
 */
@Component
public class TriplestoreService {

    private static final Logger logger = Logger.getLogger(TriplestoreService.class.getName());

    @Autowired
    TriplestoreClient triplestoreClient;

    @Autowired
    ClazzRepository clazzRepository;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    PolicySetRepository policySetRepository;

    @Autowired
    PolicyRepository policyRepository;

    @Autowired
    PolicySetPolicyRepository policySetPolicyRepository;

    @Autowired
    RuleRepository ruleRepository;

    @Autowired
    ExpressionRepository expressionRepository;

    @Autowired
    NamespaceRepository namespaceRepository;

    @Autowired
    HandlerRepository handlerRepository;

    @Autowired
    InstanceRepository instanceRepository;

    @Autowired
    PropertyInstanceRepository propertyInstanceRepository;

    public String validatePolicyModel(PolicySet newPolicySet, Policy newPolicy, Rule newRule, Expression newExpression) {

        logger.info("Validating Policy Model...");

        try {

            // Construct JSON from Database Data
            JSONObject jsonObject = new JSONObject();

            // Fetch prefixes
            JSONObject jsonPrefixes = new JSONObject();

            List<Namespace> namespaces = namespaceRepository.findAll();

            if (null != namespaces && !namespaces.isEmpty()) {

                namespaces.stream().forEach(namespace -> {
                    jsonPrefixes.put(namespace.getPrefix(), namespace.getUri());
                    jsonObject.put("prefixes", jsonPrefixes);

                });

            }

            JSONArray jsonHandlers = new JSONArray();

            List<Handler> handlers = handlerRepository.findAll();

            if (null != handlers && !handlers.isEmpty()) {

                handlers.stream().forEach(handler -> {

                    JSONObject jsonHandler = new JSONObject();

                    jsonHandler.put("id", handler.getId());
                    jsonHandler.put("name", handler.getHandlerName());

                    String clazzNamespace = "";

                    if (null != handler.getHasInput().getNamespaceID()) {
                        clazzNamespace = (null != namespaceRepository.findOne(handler.getHasInput().getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(handler.getHasInput().getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        clazzNamespace = "pcm";
                    }

                    jsonHandler.put("hasInput", clazzNamespace + ":" + handler.getHasInput().getClassName());

                    if (null != handler.getHasOutput().getNamespaceID()) {
                        clazzNamespace = (null != namespaceRepository.findOne(handler.getHasOutput().getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(handler.getHasOutput().getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        clazzNamespace = "pcm";
                    }

                    jsonHandler.put("hasOutput", clazzNamespace + ":" + handler.getHasOutput().getClassName());
                    jsonHandler.put("restEndpointURI", handler.getRestEndpointURI());

                    jsonHandlers.put(jsonHandler);

                });

                jsonObject.put("handlers", jsonHandlers);

            }

            // Fetch policySets
            List<PolicySet> policySets = policySetRepository.findAll();
            JSONArray jsonPolicySets = new JSONArray();

            if (null != newPolicySet) {

                if (null != newPolicySet.getId()) {
                    // existing policy set
                    PolicySet existingPolicySet = policySets.stream().filter(policySet -> policySet.getId() == newPolicySet.getId()).collect(Collectors.toList()).get(0);
                    policySets.remove(existingPolicySet);
                    policySets.add(newPolicySet);
                } else {
                    // new policy set
                    policySets.add(newPolicySet);
                }
            }

            if (null != policySets && !policySets.isEmpty()) {

                policySets.stream().forEach(policySet -> {

                    JSONObject jsonPolicySet = new JSONObject();

                    String policySetNamespace = "";

                    if (null != policySet.getNamespaceID()) {
                        policySetNamespace = (null != namespaceRepository.findOne(policySet.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(policySet.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        policySetNamespace = "pcm";
                    }

                    // Generic Info
                    jsonPolicySet.put("id", null != policySet.getId() ? policySet.getId() : 0);
                    jsonPolicySet.put("name", policySet.getPolicySetName());
                    jsonPolicySet.put("uri", policySetNamespace + ":" + policySet.getPolicySetName());
                    jsonPolicySet.put("combiningAlgorithm", policySet.getCombiningAlgorithmID().getName());

                    // Policies
                    JSONArray jsonPolicies = new JSONArray();

                    if (null != policySet.getPolicySetPolicies() && !policySet.getPolicySetPolicies().isEmpty()) {
                        policySet.getPolicySetPolicies().stream().forEach(policy -> {

                            Policy tempPolicy = policy.getPolicy();

                            String policyNamespace = "";

                            if (null != tempPolicy.getNamespaceID()) {
                                policyNamespace = (null != namespaceRepository.findOne(tempPolicy.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(tempPolicy.getNamespaceID().getId()).getPrefix() : "pcm");
                            } else {
                                policyNamespace = "pcm";
                            }

                            jsonPolicies.put(policyNamespace + ":" + tempPolicy.getPolicyName());

                        });
                    }

                    jsonPolicySet.put("policies", jsonPolicies);

                    // Add to Array
                    jsonPolicySets.put(jsonPolicySet);

                });

            }

            // Add Policy Sets to JSON
            jsonObject.put("policySets", jsonPolicySets);

            // Fetch Policies
            List<Policy> policies = policyRepository.findAll();
            JSONArray jsonPolicies = new JSONArray();

            if (null != newPolicy) {

                if (null != newPolicy.getId()) {
                    // existing policy
                    Policy existingPolicy = policies.stream().filter(policy -> policy.getId() == newPolicy.getId()).collect(Collectors.toList()).get(0);
                    policies.remove(existingPolicy);
                    policies.add(newPolicy);
                } else {
                    // new policy
                    policies.add(newPolicy);
                }
            }

            if (null != policies && !policies.isEmpty()) {

                policies.stream().forEach(policy -> {

                    JSONObject jsonPolicy = new JSONObject();

                    String policyNamespace = "";

                    if (null != policy.getNamespaceID()) {
                        policyNamespace = (null != namespaceRepository.findOne(policy.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(policy.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        policyNamespace = "pcm";
                    }

                    // Generic Info
                    jsonPolicy.put("id", null != policy.getId() ? policy.getId() : 0);
                    jsonPolicy.put("name", policy.getPolicyName());
                    jsonPolicy.put("uri", policyNamespace + ":" + policy.getPolicyName());
                    jsonPolicy.put("combiningAlgorithm", policy.getCombiningAlgorithmID().getName());

                    // Rules
                    JSONArray jsonRules = new JSONArray();

                    if (null != policy.getPolicyRules() && !policy.getPolicyRules().isEmpty()) {
                        policy.getPolicyRules().stream().forEach(rule -> {

                            Rule tempRule = rule.getRule();

                            String ruleNamespace = "";

                            if (null != tempRule.getNamespaceID()) {
                                ruleNamespace = (null != namespaceRepository.findOne(tempRule.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(tempRule.getNamespaceID().getId()).getPrefix() : "pcm");
                            } else {
                                ruleNamespace = "pcm";
                            }

                            jsonRules.put(ruleNamespace + ":" + tempRule.getRuleName());

                        });
                    }

                    jsonPolicy.put("rules", jsonRules);

                    // Add to Array
                    jsonPolicies.put(jsonPolicy);

                });

            }

            // Add Policy to JSON
            jsonObject.put("policies", jsonPolicies);

            // Fetch Rules
            List<Rule> rules = ruleRepository.findAll();
            JSONArray jsonRules = new JSONArray();

            if (null != newRule) {

                if (null != newRule.getId()) {
                    // existing rule
                    Rule existingRule = rules.stream().filter(rule -> rule.getId() == newRule.getId()).collect(Collectors.toList()).get(0);
                    rules.remove(existingRule);
                    rules.add(newRule);
                } else {
                    // new rule
                    rules.add(newRule);
                }
            }

            if (null != rules && !rules.isEmpty()) {

                rules.stream().forEach(rule -> {

                    JSONObject jsonRule = new JSONObject();

                    String ruleNamespace = "";

                    if (null != rule.getNamespaceID()) {
                        ruleNamespace = (null != namespaceRepository.findOne(rule.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(rule.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        ruleNamespace = "pcm";
                    }

                    // Generic Info
                    jsonRule.put("id", null != rule.getId() ? rule.getId() : 0);
                    jsonRule.put("name", rule.getRuleName());
                    jsonRule.put("uri", ruleNamespace + ":" + rule.getRuleName());
                    jsonRule.put("controlledObjectUri", rule.getControlledObject());
                    jsonRule.put("authorizationUri", rule.getAuthorization());

                    String action = "";

                    Instance actionInstance = instanceRepository.findByInstanceName(rule.getAction());

                    String instanceActionNamespace = "";

                    if (null != actionInstance.getNamespaceID()) {
                        instanceActionNamespace = (null != namespaceRepository.findOne(actionInstance.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(actionInstance.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        instanceActionNamespace = "pcm";
                    }

                    action = instanceActionNamespace + ":" + actionInstance.getInstanceName();

                    jsonRule.put("actionUri", action);

                    String actor = "";

                    Instance actorInstance = instanceRepository.findByInstanceName(rule.getActor());

                    String instanceActorNamespace = "";

                    if (null != actorInstance.getNamespaceID()) {
                        instanceActorNamespace = (null != namespaceRepository.findOne(actorInstance.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(actorInstance.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        instanceActorNamespace = "pcm";
                    }

                    actor = instanceActorNamespace + ":" + actorInstance.getInstanceName();

                    jsonRule.put("actorUri", actor);

                    String expressionNamespace = "";

                    Expression expression = rule.getExpressionID();

                    if (null != expression.getNamespaceID()) {
                        expressionNamespace = (null != namespaceRepository.findOne(expression.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(expression.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        expressionNamespace = "pcm";
                    }

                    jsonRule.put("exprUri", expressionNamespace + ":" + expression.getExpressionName());

                    jsonRules.put(jsonRule);

                });

            }

            // Add Rules to JSON
            jsonObject.put("rules", jsonRules);

            // Fetch Expressions
            List<Expression> expressions = expressionRepository.findAll();
            JSONArray jsonExpressions = new JSONArray();

            if (null != newExpression) {

                if (null != newExpression.getId()) {
                    // existing expression
                    Expression existingExp = expressions.stream().filter(expression -> expression.getId() == newExpression.getId()).collect(Collectors.toList()).get(0);
                    expressions.remove(existingExp);
                    expressions.add(newExpression);
                } else {
                    // new expression
                    jsonExpressions.put(addContextExpression(newExpression));
                }
            }

            if (null != expressions && !expressions.isEmpty()) {

                expressions.stream().forEach(expression -> {

                    jsonExpressions.put(addContextExpression(expression));

                });

            }

            // Add Expressions to JSON
            jsonObject.put("contextExpressions", jsonExpressions);

            // Parse JSON with TripleStore JSON Parser
            PolicyModelParserResponse response = PolicyModelParser.toTriplestore(jsonObject.toString());

            if (response.getResult().equals(PolicyModelParserResponse.VALIDATION_RESULT.SUCCESS)) {

                logger.info("Synchronization finished successfully!");
                return null;

            } else {
                logger.info("Validation failed: " + response.getMessage());
                return response.getMessage();
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.severe(e.getMessage());
            return e.getMessage();
        }

    }

    public ContextModel exportContextModel() {

        logger.info("Exporting Context Model as JSON and RDF...");

        try {

            // Construct JSON from Database Data
            JSONObject jsonObject = new JSONObject();

            // Fetch prefixes
            JSONObject jsonPrefixes = new JSONObject();

            List<Namespace> namespaces = namespaceRepository.findAll();

            if (null != namespaces && !namespaces.isEmpty()) {

                namespaces.stream().forEach(namespace -> {
                    jsonPrefixes.put(namespace.getPrefix(), namespace.getUri());
                    jsonObject.put("prefixes", jsonPrefixes);

                });

            }

            JSONArray jsonHandlers = new JSONArray();

            List<Handler> handlers = handlerRepository.findAll();

            if (null != handlers && !handlers.isEmpty()) {

                handlers.stream().forEach(handler -> {

                    JSONObject jsonHandler = new JSONObject();

                    jsonHandler.put("id", handler.getId());

                    String handlerNamespace = "";

                    if (null != handler.getNamespaceID()) {
                        handlerNamespace = (null != namespaceRepository.findOne(handler.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(handler.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        handlerNamespace = "pcm";
                    }

                    jsonHandler.put("name", handlerNamespace + ":" + handler.getHandlerName());

                    String clazzNamespace = "";

                    if (null != handler.getHasInput().getNamespaceID()) {
                        clazzNamespace = (null != namespaceRepository.findOne(handler.getHasInput().getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(handler.getHasInput().getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        clazzNamespace = "pcm";
                    }

                    jsonHandler.put("hasInput", clazzNamespace + ":" + handler.getHasInput().getClassName());

                    if (null != handler.getHasOutput().getNamespaceID()) {
                        clazzNamespace = (null != namespaceRepository.findOne(handler.getHasOutput().getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(handler.getHasOutput().getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        clazzNamespace = "pcm";
                    }

                    jsonHandler.put("hasOutput", clazzNamespace + ":" + handler.getHasOutput().getClassName());

                    jsonHandler.put("restEndpointURI", handler.getRestEndpointURI());

                    jsonHandlers.put(jsonHandler);

                });

                jsonObject.put("handlers", jsonHandlers);

            }

            // Fetch classes
            List<Clazz> classes = clazzRepository.findAllCustom(null);
            JSONArray jsonDefinitions = new JSONArray();

            if (null != classes && !classes.isEmpty()) {

                classes.stream().forEach(clazz -> {

                    JSONObject jsonClazz = new JSONObject();

                    String clazzNamespace = "";

                    if (null != clazz.getNamespaceID()) {
                        clazzNamespace = (null != namespaceRepository.findOne(clazz.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(clazz.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        clazzNamespace = "pcm";
                    }

                    // Generic Info
                    jsonClazz.put("id", clazz.getId());
                    jsonClazz.put("className", clazz.getClassName());
                    jsonClazz.put("uri", clazzNamespace + ":" + clazz.getClassName());
                    jsonClazz.put("hasFather", clazz.hasFather());
                    jsonClazz.put("fatherID", clazz.hasFather() ? clazz.getParentID().getId() : -1);

                    // Properties
                    JSONArray jsonProperties = new JSONArray();

                    if (null != propertyRepository.findByClassID(clazz) && !propertyRepository.findByClassID(clazz).isEmpty()) {
                        propertyRepository.findByClassID(clazz).stream().forEach(property -> {

                            String propertyNamespace = "";

                            if (null != property.getNamespaceID()) {
                                propertyNamespace = (null != namespaceRepository.findOne(property.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(property.getNamespaceID().getId()).getPrefix() : "pcm");
                            } else {
                                propertyNamespace = "pcm";
                            }

                            JSONObject jsonProperty = new JSONObject();
                            jsonProperty.put("id", property.getId());
                            jsonProperty.put("name", property.getName());
                            jsonProperty.put("uri", propertyNamespace + ":" + property.getName());

                            jsonProperty.put("isObjectProperty", property.isObjectProperty());
                            jsonProperty.put("transitivity", property.getTransitivity());
                            if (!property.isObjectProperty()) {
                                jsonProperty.put("propertyType", property.getPropertyTypeID().getSchemaXSD());
                            } else {

                                String clazzNamespace1 = "";

                                if (null != property.getObjectPropertyClassID().getNamespaceID()) {
                                    clazzNamespace1 = (null != namespaceRepository.findOne(property.getObjectPropertyClassID().getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(property.getObjectPropertyClassID().getNamespaceID().getId()).getPrefix() : "pcm");
                                } else {
                                    clazzNamespace1 = "pcm";
                                }

                                jsonProperty.put("propertyType", clazzNamespace1 + ":" + property.getObjectPropertyClassID().getClassName());
                            }

                            if (null != property.getSubPropertyOfID()) {
                                JSONObject jsonSubProperty = new JSONObject();
                                jsonSubProperty.put("id", property.getSubPropertyOfID().getId());
                                jsonSubProperty.put("name", property.getSubPropertyOfID().getName());

                                String subPropertyNamespace = "";

                                if (null != property.getSubPropertyOfID().getNamespaceID()) {
                                    subPropertyNamespace = (null != namespaceRepository.findOne(property.getSubPropertyOfID().getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(property.getSubPropertyOfID().getNamespaceID().getId()).getPrefix() : "pcm");
                                } else {
                                    subPropertyNamespace = "pcm";
                                }

                                jsonSubProperty.put("uri", subPropertyNamespace + ":" + property.getSubPropertyOfID().getName());

                                jsonProperty.put("subPropertyOf", jsonSubProperty);
                            }

                            jsonProperties.put(jsonProperty);

                        });
                    }

                    jsonClazz.put("properties", jsonProperties);

                    // Instances
                    JSONArray jsonInstances = new JSONArray();

                    if (null != instanceRepository.findByClassID(clazz, null).getContent() && !instanceRepository.findByClassID(clazz, null).getContent().isEmpty()) {
                        instanceRepository.findByClassID(clazz, null).getContent().stream().forEach(instance -> {

                            String instanceNamespace = "";

                            if (null != instance.getNamespaceID()) {
                                instanceNamespace = (null != namespaceRepository.findOne(instance.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(instance.getNamespaceID().getId()).getPrefix() : "pcm");
                            } else {
                                instanceNamespace = "pcm";
                            }

                            JSONObject jsonInstance = new JSONObject();
                            jsonInstance.put("id", instance.getId());
                            jsonInstance.put("name", instance.getInstanceName());
                            jsonInstance.put("uri", instanceNamespace + ":" + instance.getInstanceName());

                            JSONArray jsonPropertyInstances = new JSONArray();

                            if (null != propertyInstanceRepository.findByInstanceID(instance.getId()) && !propertyInstanceRepository.findByInstanceID(instance.getId()).isEmpty()) {
                                propertyInstanceRepository.findByInstanceID(instance.getId()).stream().forEach(propertyInstance -> {

                                    JSONObject jsonPropertyInstance = new JSONObject();

                                    jsonPropertyInstance.put("id", propertyInstance.getId());
                                    jsonPropertyInstance.put("propertyID", propertyInstance.getPropertyID().getId());
                                    jsonPropertyInstance.put("value", propertyInstance.getName());

                                    jsonPropertyInstances.put(jsonPropertyInstance);

                                });
                            }
                            jsonInstance.put("propertyInstances", jsonPropertyInstances);
                            jsonInstances.put(jsonInstance);

                        });
                    }

                    jsonClazz.put("instances", jsonInstances);

                    // Add to Array
                    jsonDefinitions.put(jsonClazz);

                });

            }

            // Add Classes to JSON
            jsonObject.put("definitions", jsonDefinitions);

//            logger.info("CM JSON: " + jsonObject.toString());

            // Parse JSON with TripleStore JSON Parser
            String parsedRDF = ContextModel2RdfParser.toTriplestore(jsonObject.toString());

            logger.info("Export finished successfully!");

            return new ContextModel(jsonObject.toString(), parsedRDF);

        } catch (Exception e) {
            e.printStackTrace();
            logger.severe(e.getMessage());
            return null;
        }

    }

    public boolean uploadContextModelToTriplestore(String contextModelRDF) {

        if (triplestoreClient.uploadToTriplestore(contextModelRDF)) {
            return true;
        } else {
            return false;
        }

    }

    public PolicyModel exportPolicyModel() {

        logger.info("Exporting Policy Model to JSON, XACML and RDF...");

        try {

            // Construct JSON from Database Data
            JSONObject jsonObject = new JSONObject();

            // Fetch prefixes
            JSONObject jsonPrefixes = new JSONObject();

            List<Namespace> namespaces = namespaceRepository.findAll();

            if (null != namespaces && !namespaces.isEmpty()) {

                namespaces.stream().forEach(namespace -> {
                    jsonPrefixes.put(namespace.getPrefix(), namespace.getUri());
                    jsonObject.put("prefixes", jsonPrefixes);

                });

            }

            // Fetch policySets
            List<PolicySet> policySets = policySetRepository.findAll();
            JSONArray jsonPolicySets = new JSONArray();

            if (null != policySets && !policySets.isEmpty()) {

                policySets.stream().forEach(policySet -> {

                    JSONObject jsonPolicySet = new JSONObject();

                    String policySetNamespace = "";

                    if (null != policySet.getNamespaceID()) {
                        policySetNamespace = (null != namespaceRepository.findOne(policySet.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(policySet.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        policySetNamespace = "pcm";
                    }

                    // Generic Info
                    jsonPolicySet.put("id", null != policySet.getId() ? policySet.getId() : 0);
                    jsonPolicySet.put("name", policySet.getPolicySetName());
                    jsonPolicySet.put("uri", policySetNamespace + ":" + policySet.getPolicySetName());
                    jsonPolicySet.put("combiningAlgorithm", policySet.getCombiningAlgorithmID().getUri());

                    // Policies
                    JSONArray jsonPolicies = new JSONArray();

                    if (null != policySetPolicyRepository.findByPolicySet(policySet) && !policySetPolicyRepository.findByPolicySet(policySet).isEmpty()) {
                        policySetPolicyRepository.findByPolicySet(policySet).stream().forEach(policySetPolicy -> {

                            Policy tempPolicy = policySetPolicy.getPolicy();

                            String policyNamespace = "";

                            if (null != tempPolicy.getNamespaceID()) {
                                policyNamespace = (null != namespaceRepository.findOne(tempPolicy.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(tempPolicy.getNamespaceID().getId()).getPrefix() : "pcm");
                            } else {
                                policyNamespace = "pcm";
                            }

                            jsonPolicies.put(policyNamespace + ":" + tempPolicy.getPolicyName());

                        });
                    }

                    jsonPolicySet.put("policies", jsonPolicies);

                    // Add to Array
                    jsonPolicySets.put(jsonPolicySet);

                });

            }

            // Add Policy Sets to JSON
            jsonObject.put("policySets", jsonPolicySets);

            // Fetch Policies
            List<Policy> policies = policyRepository.findAll();
            JSONArray jsonPolicies = new JSONArray();

            if (null != policies && !policies.isEmpty()) {

                policies.stream().forEach(policy -> {

                    JSONObject jsonPolicy = new JSONObject();

                    String policyNamespace = "";

                    if (null != policy.getNamespaceID()) {
                        policyNamespace = (null != namespaceRepository.findOne(policy.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(policy.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        policyNamespace = "pcm";
                    }

                    // Generic Info
                    jsonPolicy.put("id", null != policy.getId() ? policy.getId() : 0);
                    jsonPolicy.put("name", policy.getPolicyName());
                    jsonPolicy.put("uri", policyNamespace + ":" + policy.getPolicyName());
                    jsonPolicy.put("combiningAlgorithm", policy.getCombiningAlgorithmID().getUri());

                    // Rules
                    JSONArray jsonRules = new JSONArray();

                    if (null != policy.getPolicyRules() && !policy.getPolicyRules().isEmpty()) {
                        policy.getPolicyRules().stream().forEach(rule -> {

                            Rule tempRule = rule.getRule();

                            String ruleNamespace = "";

                            if (null != tempRule.getNamespaceID()) {
                                ruleNamespace = (null != namespaceRepository.findOne(tempRule.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(tempRule.getNamespaceID().getId()).getPrefix() : "pcm");
                            } else {
                                ruleNamespace = "pcm";
                            }

                            jsonRules.put(ruleNamespace + ":" + tempRule.getRuleName());

                        });
                    }

                    jsonPolicy.put("rules", jsonRules);

                    // Add to Array
                    jsonPolicies.put(jsonPolicy);

                });

            }

            // Add Policy to JSON
            jsonObject.put("policies", jsonPolicies);

            // Fetch Rules
            List<Rule> rules = ruleRepository.findAll();
            JSONArray jsonRules = new JSONArray();

            if (null != rules && !rules.isEmpty()) {

                rules.stream().forEach(rule -> {

                    JSONObject jsonRule = new JSONObject();

                    String ruleNamespace = "";

                    if (null != rule.getNamespaceID()) {
                        ruleNamespace = (null != namespaceRepository.findOne(rule.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(rule.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        ruleNamespace = "pcm";
                    }

                    // Generic Info
                    jsonRule.put("id", null != rule.getId() ? rule.getId() : 0);
                    jsonRule.put("name", rule.getRuleName());
                    jsonRule.put("uri", ruleNamespace + ":" + rule.getRuleName());

                    String controlledObject = "";

                    Instance instanceControlledObject = instanceRepository.findByInstanceName(rule.getControlledObject());

                    String instanceControlObjectNamespace = "";

                    if (null != instanceControlledObject.getNamespaceID()) {
                        instanceControlObjectNamespace = (null != namespaceRepository.findOne(instanceControlledObject.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(instanceControlledObject.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        instanceControlObjectNamespace = "pcm";
                    }

                    controlledObject = instanceControlObjectNamespace + ":" + instanceControlledObject.getInstanceName();

                    jsonRule.put("controlledObjectUri", controlledObject);
                    jsonRule.put("authorizationUri", rule.getAuthorization());

                    String action = "";

                    Instance actionInstance = instanceRepository.findByInstanceName(rule.getAction());

                    String instanceActionNamespace = "";

                    if (null != actionInstance.getNamespaceID()) {
                        instanceActionNamespace = (null != namespaceRepository.findOne(actionInstance.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(actionInstance.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        instanceActionNamespace = "pcm";
                    }

                    action = instanceActionNamespace + ":" + actionInstance.getInstanceName();

                    jsonRule.put("actionUri", action);

                    String actor = "";

                    Instance actorInstance = instanceRepository.findByInstanceName(rule.getActor());

                    String instanceActorNamespace = "";

                    if (null != actorInstance.getNamespaceID()) {
                        instanceActorNamespace = (null != namespaceRepository.findOne(actorInstance.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(actorInstance.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        instanceActorNamespace = "pcm";
                    }

                    actor = instanceActorNamespace + ":" + actorInstance.getInstanceName();

                    jsonRule.put("actorUri", actor);

                    String expressionNamespace = "";

                    Expression expression = rule.getExpressionID();

                    if (null != expression.getNamespaceID()) {
                        expressionNamespace = (null != namespaceRepository.findOne(expression.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(expression.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        expressionNamespace = "pcm";
                    }

                    jsonRule.put("exprUri", expressionNamespace + ":" + expression.getExpressionName());

                    jsonRules.put(jsonRule);

                });

            }

            // Add Rules to JSON
            jsonObject.put("rules", jsonRules);

            // Fetch Expressions
            List<Expression> expressions = expressionRepository.findAll();
            JSONArray jsonExpressions = new JSONArray();

            if (null != expressions && !expressions.isEmpty()) {

                expressions.stream().forEach(expression -> {

                    jsonExpressions.put(addContextExpression(expression));

                });

            }

            // Add Expressions to JSON
            jsonObject.put("contextExpressions", jsonExpressions);

//            logger.info("Policy Model JSON: " + jsonObject.toString());

            // Parse JSON with TripleStore JSON Parser
            PolicyModelParserResponse response = PolicyModelParser.toTriplestore(jsonObject.toString());

            if (response.getResult().equals(PolicyModelParserResponse.VALIDATION_RESULT.SUCCESS)) {

                logger.info("Export finished successfully!");

                return new PolicyModel(jsonObject.toString(), null, response.getXacml(), response.getRdf());

            } else {
                logger.info("Validation failed: " + response.getMessage());
                return new PolicyModel(response.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.severe(e.getMessage());
            return null;
        }

    }

    public boolean uploadPolicyModelToTriplestore(String policyModelRDF) {

        if (triplestoreClient.uploadToTriplestore(policyModelRDF)) {
            return true;
        } else {
            return false;
        }

    }

    ///////////////////////
    // Utility Methods
    ///////////////////////

    public JSONObject addContextExpression(Expression expression) {

        JSONObject jsonExpression = new JSONObject();

        String expressionNamespace = "";

        if (null != expression.getNamespaceID()) {
            expressionNamespace = (null != namespaceRepository.findOne(expression.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(expression.getNamespaceID().getId()).getPrefix() : "pcm");
        } else {
            expressionNamespace = "pcm";
        }

        // Generic Info
        jsonExpression.put("id", null != expression.getId() ? expression.getId() : 0);
        jsonExpression.put("name", expression.getExpressionName());
        jsonExpression.put("uri", expressionNamespace + ":" + expression.getExpressionName());

        JSONObject expressionObject = new JSONObject(expression.getExpression());

        if (expressionObject.getString("condition").equalsIgnoreCase("AND")) {
            jsonExpression.put("type", "pac:ANDContextExpression");
        } else {
            jsonExpression.put("type", "pac:ORContextExpression");
        }

        JSONArray nestedExpressionsArray = new JSONArray();

        if (null != expression.getReferredExpressions() && !expression.getReferredExpressions().isEmpty()) {


            for (String expressionID : expression.getReferredExpressionsFormatted()) {


                JSONObject nestedExpr = new JSONObject();

                nestedExpr.put("property", expression.getCondition() + "nestedExpression");

                Expression tempExp = expressionRepository.findOne(Long.valueOf(expressionID));

                String namespace = "";

                if (null != tempExp.getNamespaceID()) {
                    namespace = (null != namespaceRepository.findOne(tempExp.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(tempExp.getNamespaceID().getId()).getPrefix() : "pcm");
                } else {
                    namespace = "pcm";
                }

                nestedExpr.put("hasParameter", namespace + ":" + tempExp.getExpressionName());

                nestedExpressionsArray.put(nestedExpr);

            }


        }

        jsonExpression.put("nestedExpressions", nestedExpressionsArray);

        JSONArray expressionParamsArray = expressionObject.getJSONArray("rules");

        JSONArray expParamsArray = new JSONArray();

        parseExpressionRules(expressionParamsArray, expParamsArray);

        jsonExpression.put("params", expParamsArray);

        return jsonExpression;
    }

    public void parseExpressionRules(JSONArray params, JSONArray expParamsArray) {

        for (int i = 0; i < params.length(); i++) {

            Object expParam = params.get(i);

            if (expParam instanceof JSONObject) {

                JSONObject expParamJSON = (JSONObject) expParam;

                JSONObject expParamTemp = new JSONObject();

                if (expParamJSON.has("id")) {

                    String id = expParamJSON.getString("id");

                    // Parse Rule ID

                    String instanceID = id.substring(1, id.indexOf("p"));
                    String propertyID = id.substring(id.indexOf("p") + 1);

                    String instanceNamespace = "";

                    Instance refersToInstance = instanceRepository.findOne(Long.valueOf(instanceID));

                    if (null != refersToInstance && null != refersToInstance.getNamespaceID()) {
                        instanceNamespace = (null != namespaceRepository.findOne(refersToInstance.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(refersToInstance.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        instanceNamespace = "pcm";
                    }

                    if (refersToInstance.getClassID().getId() == 12L || refersToInstance.getClassID().getId() == 13L || refersToInstance.getClassID().getId() == 126L) {
                        expParamTemp.put("refersTo", instanceNamespace + ":" + refersToInstance.getInstanceName());
                    } else {
                        expParamTemp.put("associatedWith", instanceNamespace + ":" + refersToInstance.getInstanceName());
                    }

                    String propertyNamespace = "";

                    Property property = propertyRepository.findOne(Long.valueOf(propertyID));

                    if (null != property && null != property.getNamespaceID()) {
                        propertyNamespace = (null != namespaceRepository.findOne(property.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(property.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        propertyNamespace = "pcm";
                    }

                    expParamTemp.put("property", propertyNamespace + ":" + property.getName());

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

                        expParamTemp.put("datatype", classNamespace + ":" + tempInstance.getClassID().getClassName());

                        expParamTemp.put("hasParameter", instanceNamespace + ":" + tempInstance.getInstanceName());


                    }

                    expParamsArray.put(expParamTemp);

                } else {

                    if (expParamJSON.getString("condition").equalsIgnoreCase("AND")) {

                        expParamTemp.put("type", "pac:ANDContextExpression");

                    } else {

                        expParamTemp.put("type", "pac:ORContextExpression");

                    }

                    JSONArray newParamsJSONArray = new JSONArray();

                    findMyParams(expParamJSON.getJSONArray("rules"), expParamTemp, newParamsJSONArray);

                    expParamsArray.put(expParamTemp);

                }

            }

        }

    }

    public void findMyParams(JSONArray rules, JSONObject expParamTempOld, JSONArray newParamsJSONArray) {

        for (int i = 0; i < rules.length(); i++) {

            Object expParam = rules.get(i);

            if (expParam instanceof JSONObject) {

                JSONObject expParamJSON = (JSONObject) expParam;

                JSONObject expParamTemp = new JSONObject();

                if (expParamJSON.has("id")) {

                    String id = expParamJSON.getString("id");

                    String instanceID = id.substring(1, id.indexOf("p"));
                    String propertyID = id.substring(id.indexOf("p") + 1);

                    String instanceNamespace = "";

                    Instance refersToInstance = instanceRepository.findOne(Long.valueOf(instanceID));

                    if (null != refersToInstance && null != refersToInstance.getNamespaceID()) {
                        instanceNamespace = (null != namespaceRepository.findOne(refersToInstance.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(refersToInstance.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        instanceNamespace = "pcm";
                    }

                    String propertyNamespace = "";

                    Property property = propertyRepository.findOne(Long.valueOf(propertyID));

                    if (null != property && null != property.getNamespaceID()) {
                        propertyNamespace = (null != namespaceRepository.findOne(property.getNamespaceID().getId()).getPrefix() ? namespaceRepository.findOne(property.getNamespaceID().getId()).getPrefix() : "pcm");
                    } else {
                        propertyNamespace = "pcm";
                    }

                    expParamTemp.put("refersTo", instanceNamespace + ":" + refersToInstance.getInstanceName());

                    expParamTemp.put("property", propertyNamespace + ":" + property.getName());

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

                        expParamTemp.put("datatype", classNamespace + ":" + tempInstance.getClassID().getClassName());

                        expParamTemp.put("hasParameter", instanceNamespace + ":" + tempInstance.getInstanceName());

                    }


                    newParamsJSONArray.put(expParamTemp);

                    expParamTempOld.put("params", newParamsJSONArray);

                } else {

                    if (expParamJSON.getString("condition").equalsIgnoreCase("AND")) {
                        expParamTemp.put("type", "pac:ANDContextExpression");
                    } else {
                        expParamTemp.put("type", "pac:ORContextExpression");
                    }

                    JSONArray newNestedParamsJSONArray = new JSONArray();

                    findMyParams(expParamJSON.getJSONArray("rules"), expParamTemp, newNestedParamsJSONArray);

                    newParamsJSONArray.put(expParamTemp);

                }

            }

        }

        expParamTempOld.put("params", newParamsJSONArray);

    }

}
