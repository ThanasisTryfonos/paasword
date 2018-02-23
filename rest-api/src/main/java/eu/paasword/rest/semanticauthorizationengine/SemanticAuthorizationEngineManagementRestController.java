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
package eu.paasword.rest.semanticauthorizationengine;

import eu.paasword.api.repository.IAPIKeyService;
import eu.paasword.api.repository.IApplicationInstanceService;
import eu.paasword.api.repository.IApplicationService;
import eu.paasword.api.repository.ISystemPropertyService;
import eu.paasword.api.repository.exception.apikey.APIKeyUniqueIDDoesNotExist;
import eu.paasword.api.repository.exception.application.ApplicationDoesNotExist;
import eu.paasword.repository.relational.dao.*;
import eu.paasword.repository.relational.domain.*;
import eu.paasword.repository.relational.domain.Policy;
import eu.paasword.repository.relational.domain.PolicySet;
import eu.paasword.repository.relational.domain.Rule;
import eu.paasword.repository.relational.util.DroolsExpression;
import eu.paasword.repository.relational.util.RepositoryUtil;
import eu.paasword.repository.relational.util.Triple;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import eu.paasword.rest.semanticauthorizationengine.transferobject.*;
import eu.paasword.util.Util;
import eu.paasword.util.entities.AnnotatedAnnotation;
import eu.paasword.util.entities.AnnotatedCode;
import eu.paasword.util.entities.AnnotatedMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.web.client.RestTemplate;

/**
 * Contains all the rest endpoints regarding semantic authorization engine
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/semanticauthorizationengine")
public class SemanticAuthorizationEngineManagementRestController {

    private static final Logger logger = Logger.getLogger(SemanticAuthorizationEngineManagementRestController.class.getName());

    @Autowired
    Environment environment;

    @Value("${paasword.semauthengine.url}")
    String semauthengineURL;

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static RestTemplate restTemplate;

    @Autowired
    IApplicationService<Application> applicationService;

    @Autowired
    IApplicationInstanceService<ApplicationInstance> applicationInstanceService;

    @Autowired
    IAPIKeyService<APIKey> apiKeyService;

    @Autowired
    ISystemPropertyService<SystemProperty> systemPropertyService;

    @Autowired
    ClazzRepository clazzRepository;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    InstanceRepository instanceRepository;

    @Autowired
    PolicyRepository policyRepository;

    @Autowired
    PolicySetRepository policySetRepository;

    @Autowired
    RuleRepository ruleRepository;

    @Autowired
    ExpressionRepository expressionRepository;

    @Autowired
    ApplicationInstanceActivityRepository applicationInstanceActivityRepository;

    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse hello() {

        String enabled = environment.getProperty("semantic.authorization.engine.enabled");
        if (enabled.equals("true")) {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, "Semantic Authorization Engine is enabled!", Optional.empty());
        } else {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, "Semantic Authorization Engine is disabled!", Optional.empty());
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/ontology")
    public PaaSwordRestResponse fetchOntology() {


        String ontology = RepositoryUtil.constructOntologyForSemAuthEngine(clazzRepository, propertyRepository, instanceRepository);

        if (null != ontology && !ontology.isEmpty()) {

            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, "Ontology fetched successfully!", ontology);

        } else {
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, "Ontology cannot be fetched!", Optional.empty());
        }

    }

    @RequestMapping(method = RequestMethod.GET, value = "/policymodel")
    public PaaSwordRestResponse fetchPolicyModel() {


        String policyModel = RepositoryUtil.constructPolicyModelForSemAuthEngine(ruleRepository, expressionRepository, instanceRepository, propertyRepository);

        if (null != policyModel && !policyModel.isEmpty()) {

            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, "Policy Model fetched successfully!", policyModel);

        } else {
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, "Policy Model cannot be fetched!", Optional.empty());
        }

    }

    /**
     * Synchronize Semantic Authorization Engine
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/synchronize", method = RequestMethod.POST)
    public PaaSwordRestResponse synchronizeSemanticAuthorizationEngine() {

        String enabled = environment.getProperty("semantic.authorization.engine.enabled");

        if (enabled.equals("true")) {

            if (null == restTemplate) {
                restTemplate = new RestTemplate();
            }

            String SEMANTIC_AUTHORIZATION_ENGINE_URL = semauthengineURL + "/api/semanticpolicyengine/";

            ResponseEntity<String> responseEntity = restTemplate.exchange(SEMANTIC_AUTHORIZATION_ENGINE_URL + "/loadontology", HttpMethod.GET, null, String.class);

            if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.SEMANTIC_AUTHORIZATION_ENGINE_SYNCHRONIZED, responseEntity.getBody());

            } else {

                logger.log(Level.SEVERE, "Response Status: {0}", new Object[]{responseEntity.getStatusCode()});
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.SEMANTIC_AUTHORIZATION_ENGINE_SYNCHRONIZED_ERROR, Optional.empty());

            }

        } else {
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.SEMANTIC_AUTHORIZATION_ENGINE_SYNCHRONIZED_ERROR, Optional.empty());
        }

    }

    /**
     * Validate an application through Semantic Authorization Engine
     *
     * @param jsonForValidation
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/validation", method = RequestMethod.POST)
    public PaaSwordRestResponse validateApplication(@RequestBody String jsonForValidation) {

        String enabled = environment.getProperty("semantic.authorization.engine.enabled");

//        logger.info("JSON for validation: " + jsonForValidation);

        boolean usesJPA = false;

        if (enabled.equals("true")) {

            if (null == restTemplate) {
                restTemplate = new RestTemplate();
            }

            try {

                JSONObject jsonValidation = new JSONObject(jsonForValidation);

                Application application = ((APIKey) apiKeyService.findByUniqueID(jsonValidation.getString("apiKey")).get()).getApplicationID();

                if (null != application) {

//                jsonValidation.put("annotatedCode", new JSONObject(application.getAnnotatedCodePEP()).getJSONArray("annotatedCode"));

                    // TODO
                    // Change status of App Instance
                    //ApplicationInstance appInstance = (ApplicationInstance) applicationInstanceService.findByApplicationIDWithPaaS(application.getId()).get(0);

//                HttpEntity entity = new HttpEntity(jsonForValidation.toString(), null);
//
//                ResponseEntity<String> responseEntity = restTemplate.exchange(SEMANTIC_AUTHORIZATION_ENGINE_URL + "/validation", HttpMethod.POST, entity, String.class);
//
//                if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                    // TODO Status of the Application

                    if (application.isDataModel()) {
                        usesJPA = true;
                    }

                    return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_VALIDATED, usesJPA);

                } else {
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION,Message.APPLICATION_INVALID, usesJPA);
                }


//                } else {
//                    logger.log(Level.SEVERE, "SemanticAuthorizationEngineManagementRestController: Response Status: {0}", new Object[]{responseEntity.getStatusCode()});
//                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, responseEntity.getStatusCode().toString(), responseEntity.getStatusCode());
//                }

            } catch (JSONException | APIKeyUniqueIDDoesNotExist ex) {
                Logger.getLogger(SemanticAuthorizationEngineManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), ex);
            }

        } else {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_VALIDATED, true);
        }
    }

    // Get Authentication Permission from Semantic Authorization Service
    @RequestMapping(value = "/getAuthenticationPermission", method = RequestMethod.POST, produces = "application/json")
    public PaaSwordRestResponse getAuthenticationPermission(@RequestBody String jsonForProcessing) {

//        String enabled = environment.getProperty("semantic.authorization.engine.enabled");
//
//        logger.info("Authorization Request invoked() with JSON: " + jsonForProcessing);
//
//        if (enabled.equals("true")) {
//
//            if (null == restTemplate) {
//                restTemplate = new RestTemplate();
//            }
//
//            JSONObject newJSONObj = null;
//
//            // TODO
//
//            if (null != jsonForProcessing && !jsonForProcessing.isEmpty()) {
//
//                newJSONObj = new JSONObject(jsonForProcessing);
//                String object = newJSONObj.getString("object");
//
//                try {
//
//                    SystemProperty policyCache = (SystemProperty) systemPropertyService.findByName("policy_cache_properties").get();
//
//                    List<String> attributes = findResources(policyCache, object);
//
//                    if (null != attributes && !attributes.isEmpty()) {
//
//                        JSONArray resources = new JSONArray();
//                        for (String attribute : attributes) {
//                            resources.put(attribute);
//                        }
//                        newJSONObj.put("resources", resources);
//                    }
//
//                } catch (SystemPropertyNameDoesNotExist e) {
//                    e.printStackTrace();
//                }
//
//
//            } else {
//                newJSONObj = new JSONObject();
//            }
//
//            HttpEntity entity = new HttpEntity(newJSONObj.toString(), null);
//
//            ResponseEntity<String> responseEntity = restTemplate.exchange(SEMANTIC_AUTHORIZATION_ENGINE_URL + "/getAuthenticationPermission", HttpMethod.POST, entity, String.class);
//
//            if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
//
//                return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, responseEntity.getBody(), responseEntity.getBody());
//
//            } else {
//                logger.log(Level.SEVERE, "SemanticAuthorizationEngineManagementRestController: Response Status: {0}", new Object[]{responseEntity.getStatusCode()});
//                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, responseEntity.getStatusCode().toString(), responseEntity.getStatusCode());
//            }
//
//        } else {
//            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.ALLOW_REQUEST, "ALLOW");
//        }


        String enabled = environment.getProperty("semantic.authorization.engine.enabled");

//        logger.info("Authorization Request invoked() with JSON: " + jsonForProcessing);

        if (enabled.equals("true")) {

            if (null == restTemplate) {
                restTemplate = new RestTemplate();
            }

            if (null != jsonForProcessing && !jsonForProcessing.isEmpty()) {

                JSONObject jsonObject = new JSONObject(jsonForProcessing);

                // Check if APP_KEY is valid
                String APP_KEY = jsonObject.getString("appKey");

                Application application = null;

                try {

                    application = ((APIKey) apiKeyService.findByUniqueID(APP_KEY).get()).getApplicationID();

//                    application = (Application) applicationService.findOneWithoutBlob(application.getId()).get();

                } catch (APIKeyUniqueIDDoesNotExist e) {
                    logger.severe(Message.APPLICATION_INVALID);
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INVALID, Optional.empty());
                }

                if (null != application) {

                    try {

                        ApplicationInstanceActivity activity = new ApplicationInstanceActivity();

                        AuthorizationRequest authorizationRequest = new AuthorizationRequest();

                        String action = null;
                        if (null != jsonObject.getString("action") && !jsonObject.getString("action").isEmpty()) {
                            action = jsonObject.getString("action");
                        } else {
                            logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                        }

                        String object = null;
                        if (null != jsonObject.getString("object") && !jsonObject.getString("object").isEmpty()) {
                            object = jsonObject.getString("object");
                        } else {
                            logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                        }

                        String remoteAddress = null;
                        if (null != jsonObject.getString("remoteAddress") && !jsonObject.getString("remoteAddress").isEmpty()) {
                            remoteAddress = jsonObject.getString("remoteAddress");
                        } else {
                            remoteAddress = "Undefined";
                        }

                        String actor = null;
                        if (null != jsonObject.getString("actor") && !jsonObject.getString("actor").isEmpty()) {
                            actor = jsonObject.getString("actor");
                        } else {
                            actor = "Unauthorized";
                        }

                        String pepType = null;
                        if (null != jsonObject.getString("PEPType") && !jsonObject.getString("PEPType").isEmpty()) {
                            pepType = jsonObject.getString("PEPType");
                        } else {
                            logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                        }

                        String pepIdentifier = null;
                        if (null != jsonObject.getString("PEPIdentifier") && !jsonObject.getString("PEPIdentifier").isEmpty()) {
                            pepIdentifier = jsonObject.getString("PEPIdentifier");
                        } else {
                            logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                        }

                        authorizationRequest.setRequestid(Util.sha256hash(String.format("%s-%s",UUID.randomUUID().toString(), new Date().getTime())));
                        authorizationRequest.setActioninstance(action);
                        authorizationRequest.setObjectinstance(object);
                        authorizationRequest.setRemoteAddress(remoteAddress);
                        authorizationRequest.setRequestContext(jsonObject.getJSONArray("requestContext").toString());
                        authorizationRequest.setSubjectinstance(actor);

                        // ACTIVITY
                        activity.setActor(actor);
                        activity.setApplicationInstanceID(application.getApplicationInstance());
                        activity.setHeader(jsonObject.getJSONArray("requestContext").toString());
                        activity.setInvocationTimestamp(new Date());
                        activity.setDateRegistered(formatter.format(new Date()));
                        activity.setAnnotatedCode(pepType + " (" + pepIdentifier + ")");
                        activity.setObject(object);
                        activity.setRemoteAddress(remoteAddress);
                        activity.setLocalAddress(remoteAddress);
                        activity.setName(application.getApplicationInstance().getName());

                        List<Triple> triples = new ArrayList<>();

                        List<AnnotatedCode> pepsPerApplication = null;

                        if (application.isPep()) {

                            pepsPerApplication = RepositoryUtil.identifyAllPEPsPerApplication(application, ruleRepository, policyRepository, policySetRepository);

                            if (null == pepsPerApplication || pepsPerApplication.isEmpty()) {
                                logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                            }

                        } else {
                            // TODO Is it ????
                            logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                        }

                        if (pepType.equals("POLICY")) {

                            boolean policyOK = false;

                            Policy policy = policyRepository.getPolicyByName(pepIdentifier);

                            if (null != policy) {

                                if (null != policy.getPolicyRules() && !policy.getPolicyRules().isEmpty()) {

                                } else {

                                    logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());

                                }


                                // Check if Specific Policy is associated with the specific application
                                for (AnnotatedCode pep : pepsPerApplication) {

                                    if (null != pep.getMethods() && !pep.getMethods().isEmpty()) {

                                        for (AnnotatedMethod annotatedMethod : pep.getMethods()) {

                                            if (null != annotatedMethod && !annotatedMethod.getMethodAnnotations().isEmpty()) {

                                                for (AnnotatedAnnotation annotation : annotatedMethod.getMethodAnnotations()) {

                                                    if (annotation.getType().equals("POLICY") && annotation.getEntityID() == policy.getId()) {
                                                        policyOK = true;
                                                    }

                                                }

                                            }

                                        }
                                    }

                                    if (null != pep.getAnnotations() && !pep.getAnnotations().isEmpty()) {

                                        for (AnnotatedAnnotation annotation : pep.getAnnotations()) {

                                            if (annotation.getType().equals("POLICY") && annotation.getEntityID() == policy.getId()) {
                                                policyOK = true;
                                            }

                                        }

                                    }

                                }

                                if (policyOK) {

                                    List<eu.paasword.rest.semanticauthorizationengine.transferobject.Policy> authPolicies = new ArrayList<>();

                                    eu.paasword.rest.semanticauthorizationengine.transferobject.Policy authPolicy = new eu.paasword.rest.semanticauthorizationengine.transferobject.Policy();
                                    authPolicy.setPolicyidentifier(policy.getPolicyName());
                                    authPolicy.setPolicycombiningalgorithm(policy.getCombiningAlgorithmID().getUri());

                                    List<eu.paasword.rest.semanticauthorizationengine.transferobject.Rule> authRules = new ArrayList<>();


                                    for (PolicyRule policyRule : policy.getPolicyRules()) {

                                        eu.paasword.rest.semanticauthorizationengine.transferobject.Rule rule = new eu.paasword.rest.semanticauthorizationengine.transferobject.Rule();

                                        // Find Triples
                                        DroolsExpression droolsExpression = new DroolsExpression();
                                        JSONArray rulesArray = new JSONObject(policyRule.getRule().getExpressionID().getExpression()).getJSONArray("rules");

                                        JSONObject newExpressionObj = new JSONObject();

                                        if (null != policyRule.getRule().getExpressionID().getReferredExpressions() && !policyRule.getRule().getExpressionID().getReferredExpressions().isEmpty()) {

                                            String condition = policyRule.getRule().getExpressionID().getCondition();

                                            newExpressionObj.put("condition", condition);

                                            JSONArray newRulesArray = new JSONArray();

                                            for (int i = 0; i < rulesArray.length(); i++) {
                                                newRulesArray.put(rulesArray.getJSONObject(i));
                                            }

                                            policyRule.getRule().getExpressionID().getReferredExpressionsFormatted().stream().forEach(expressionID -> {

                                                Expression tempExpression = expressionRepository.findOne(Long.valueOf(expressionID));

                                                JSONArray newTempRulesArray = new JSONObject(tempExpression.getExpression()).getJSONArray("rules");

                                                for (int j = 0; j < newTempRulesArray.length(); j++) {

                                                    newRulesArray.put(newTempRulesArray.getJSONObject(j));

                                                }

                                            });

                                            newExpressionObj.put("rules", newRulesArray);

                                        } else {
                                            newExpressionObj = new JSONObject(policyRule.getRule().getExpressionID().getExpression());
                                        }


                                        RepositoryUtil.parseExpressionForTriplesToDroolsExpression(
                                                droolsExpression, newExpressionObj, instanceRepository, propertyRepository);

                                        if (null != droolsExpression.getTriples() && !droolsExpression.getTriples().isEmpty()) {

                                            droolsExpression.getTriples().stream().forEach(triple -> {

                                                if (!triples.contains(triple)) {
                                                    triples.add(triple);
                                                }

                                            });

                                        }


                                        rule.setRuleidentifier(policyRule.getRule().getRuleName());

                                        authRules.add(rule);
                                    }

                                    authPolicy.setRules(authRules);

                                    authPolicies.add(authPolicy);

                                    authorizationRequest.setPolicies(authPolicies);


                                } else {

                                    logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());

                                }

                            } else {

                                logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());

                            }

                        } else if (pepType.equals("POLICY_SET")) {

                            boolean policySetOK = false;

                            PolicySet policySet = policySetRepository.findByPolicySetName(pepIdentifier);

                            if (null != policySet) {

                                if (null != policySet.getPolicySetPolicies() && !policySet.getPolicySetPolicies().isEmpty()) {

                                    if (policySet.getPolicySetPolicies().stream().filter(
                                            policySetPolicy -> null != policySetPolicy.getPolicy().getPolicyRules() && !policySetPolicy.getPolicy().getPolicyRules().isEmpty())
                                            .collect(Collectors.toList()).isEmpty()) {

                                        logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());

                                    }

                                } else {

                                    logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());

                                }

                                // Check if Specific Policy is associated with the specific application
                                for (AnnotatedCode pep : pepsPerApplication) {

                                    if (null != pep.getMethods() && !pep.getMethods().isEmpty()) {

                                        for (AnnotatedMethod annotatedMethod : pep.getMethods()) {

                                            if (null != annotatedMethod && !annotatedMethod.getMethodAnnotations().isEmpty()) {

                                                for (AnnotatedAnnotation annotation : annotatedMethod.getMethodAnnotations()) {

                                                    if (annotation.getType().equals("POLICY_SET") && annotation.getEntityID() == policySet.getId()) {
                                                        policySetOK = true;
                                                    }

                                                }

                                            }

                                        }
                                    }

                                    if (null != pep.getAnnotations() && !pep.getAnnotations().isEmpty()) {

                                        for (AnnotatedAnnotation annotation : pep.getAnnotations()) {

                                            if (annotation.getType().equals("POLICY_SET") && annotation.getEntityID() == policySet.getId()) {
                                                policySetOK = true;
                                            }

                                        }

                                    }

                                }

                                if (policySetOK) {

                                    List<eu.paasword.rest.semanticauthorizationengine.transferobject.PolicySet> authPolicySets = new ArrayList<>();

                                    eu.paasword.rest.semanticauthorizationengine.transferobject.PolicySet authPolicySet = new eu.paasword.rest.semanticauthorizationengine.transferobject.PolicySet();
                                    authPolicySet.setPolicysetcombiningalgorithm(policySet.getCombiningAlgorithmID().getUri());
                                    authPolicySet.setPolicysetidentifier(policySet.getPolicySetName());

                                    List<eu.paasword.rest.semanticauthorizationengine.transferobject.Policy> authPolicies = new ArrayList<>();

                                    for (PolicySetPolicy policySetPolicy : policySet.getPolicySetPolicies()) {

                                        eu.paasword.rest.semanticauthorizationengine.transferobject.Policy authPolicy = new eu.paasword.rest.semanticauthorizationengine.transferobject.Policy();

                                        authPolicy.setPolicyidentifier(policySetPolicy.getPolicy().getPolicyName());
                                        authPolicy.setPolicycombiningalgorithm(policySetPolicy.getPolicy().getCombiningAlgorithmID().getUri());

                                        List<eu.paasword.rest.semanticauthorizationengine.transferobject.Rule> authRules = new ArrayList<>();

                                        policySetPolicy.getPolicy().getPolicyRules().stream().forEach( policyRule -> {

                                            eu.paasword.rest.semanticauthorizationengine.transferobject.Rule authRule = new eu.paasword.rest.semanticauthorizationengine.transferobject.Rule();

                                            // Find Triples
                                            DroolsExpression droolsExpression = new DroolsExpression();
                                            JSONArray rulesArray = new JSONObject(policyRule.getRule().getExpressionID().getExpression()).getJSONArray("rules");

                                            JSONObject newExpressionObj = new JSONObject();

                                            if (null != policyRule.getRule().getExpressionID().getReferredExpressions() && !policyRule.getRule().getExpressionID().getReferredExpressions().isEmpty()) {

                                                String condition = policyRule.getRule().getExpressionID().getCondition();

                                                newExpressionObj.put("condition", condition);

                                                JSONArray newRulesArray = new JSONArray();

                                                for (int i = 0; i < rulesArray.length(); i++) {
                                                    newRulesArray.put(rulesArray.getJSONObject(i));
                                                }

                                                policyRule.getRule().getExpressionID().getReferredExpressionsFormatted().stream().forEach(expressionID -> {

                                                    Expression tempExpression = expressionRepository.findOne(Long.valueOf(expressionID));

                                                    JSONArray newTempRulesArray = new JSONObject(tempExpression.getExpression()).getJSONArray("rules");

                                                    for (int j = 0; j < newTempRulesArray.length(); j++) {

                                                        newRulesArray.put(newTempRulesArray.getJSONObject(j));

                                                    }

                                                });

                                                newExpressionObj.put("rules", newRulesArray);

                                            } else {
                                                newExpressionObj = new JSONObject(policyRule.getRule().getExpressionID().getExpression());
                                            }


                                            RepositoryUtil.parseExpressionForTriplesToDroolsExpression(
                                                    droolsExpression, newExpressionObj, instanceRepository, propertyRepository);

                                            if (null != droolsExpression.getTriples() && !droolsExpression.getTriples().isEmpty()) {

                                                droolsExpression.getTriples().stream().forEach(triple -> {

                                                    if (!triples.contains(triple)) {
                                                        triples.add(triple);
                                                    }

                                                });

                                            }

                                            authRule.setRuleidentifier(policyRule.getRule().getRuleName());

                                            authRules.add(authRule);

                                        });

                                        authPolicy.setRules(authRules);

                                        authPolicies.add(authPolicy);

                                    }

                                    authPolicySets.add(authPolicySet);

                                    authorizationRequest.setPolicysets(authPolicySets);

                                } else {
                                    logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                                }

                            } else {
                                logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                            }


                        } else if (pepType.equals("RULE")) {

                            boolean ruleOK = false;

                            Rule rule = ruleRepository.findByRuleName(pepIdentifier);

                            if (null != rule) {

                                // Check if Specific Policy is associated with the specific application
                                for (AnnotatedCode pep : pepsPerApplication) {

                                    if (null != pep.getMethods() && !pep.getMethods().isEmpty()) {

                                        for (AnnotatedMethod annotatedMethod : pep.getMethods()) {

                                            if (null != annotatedMethod && !annotatedMethod.getMethodAnnotations().isEmpty()) {

                                                for (AnnotatedAnnotation annotation : annotatedMethod.getMethodAnnotations()) {

                                                    if (annotation.getType().equals("RULE") && annotation.getEntityID() == rule.getId()) {
                                                        ruleOK = true;
                                                    }

                                                }

                                            }

                                        }
                                    }

                                    if (null != pep.getAnnotations() && !pep.getAnnotations().isEmpty()) {

                                        for (AnnotatedAnnotation annotation : pep.getAnnotations()) {

                                            if (annotation.getType().equals("RULE") && annotation.getEntityID() == rule.getId()) {
                                                ruleOK = true;
                                            }

                                        }

                                    }

                                }

                                if (ruleOK) {

                                    List<eu.paasword.rest.semanticauthorizationengine.transferobject.Rule> authRules = new ArrayList<>();

                                    eu.paasword.rest.semanticauthorizationengine.transferobject.Rule authRule = new eu.paasword.rest.semanticauthorizationengine.transferobject.Rule();
                                    authRule.setRuleidentifier(rule.getRuleName());

                                    // Find Triples
                                    DroolsExpression droolsExpression = new DroolsExpression();
                                    JSONArray rulesArray = new JSONObject(rule.getExpressionID().getExpression()).getJSONArray("rules");

                                    JSONObject newExpressionObj = new JSONObject();

                                    if (null != rule.getExpressionID().getReferredExpressions() && !rule.getExpressionID().getReferredExpressions().isEmpty()) {

                                        String condition = rule.getExpressionID().getCondition();

                                        newExpressionObj.put("condition", condition);

                                        JSONArray newRulesArray = new JSONArray();

                                        for (int i = 0; i < rulesArray.length(); i++) {
                                            newRulesArray.put(rulesArray.getJSONObject(i));
                                        }

                                        rule.getExpressionID().getReferredExpressionsFormatted().stream().forEach(expressionID -> {

                                            Expression tempExpression = expressionRepository.findOne(Long.valueOf(expressionID));

                                            JSONArray newTempRulesArray = new JSONObject(tempExpression.getExpression()).getJSONArray("rules");

                                            for (int j = 0; j < newTempRulesArray.length(); j++) {

                                                newRulesArray.put(newTempRulesArray.getJSONObject(j));

                                            }

                                        });

                                        newExpressionObj.put("rules", newRulesArray);

                                    } else {
                                        newExpressionObj = new JSONObject(rule.getExpressionID().getExpression());
                                    }


                                    RepositoryUtil.parseExpressionForTriplesToDroolsExpression(
                                            droolsExpression, newExpressionObj, instanceRepository, propertyRepository);

                                    if (null != droolsExpression.getTriples() && !droolsExpression.getTriples().isEmpty()) {

                                        droolsExpression.getTriples().stream().forEach(triple -> {

                                            if (!triples.contains(triple)) {
                                                triples.add(triple);
                                            }

                                        });

                                    }

                                    authRules.add(authRule);

                                    authorizationRequest.setRules(authRules);

                                } else {

                                    logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());

                                }

                            } else {

                                logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());

                            }


                        } else {
                            logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                        }

                        // Identify all Handlers and add new information

                        List<ApplicationInstanceHandler> applicationInstanceHandlers = application.getApplicationInstance().getApplicationInstanceHandlers();

                        if (null != triples && !triples.isEmpty() && null != applicationInstanceHandlers && !applicationInstanceHandlers.isEmpty()) {

                            List<eu.paasword.rest.semanticauthorizationengine.transferobject.Handler> authHandlers = new ArrayList<>();

                            triples.stream().forEach(triple -> {

                                long domainClazzID = triple.getDomainClazz().getId();
                                long rangeClazzID = triple.getRangeClazz().getId();

                                applicationInstanceHandlers.stream().forEach(applicationInstanceHandler -> {

                                    if (applicationInstanceHandler.getHandlerID().getHasInput().getId() == domainClazzID && applicationInstanceHandler.getHandlerID().getHasOutput().getId() == rangeClazzID) {

                                        eu.paasword.rest.semanticauthorizationengine.transferobject.Handler authHandler = new eu.paasword.rest.semanticauthorizationengine.transferobject.Handler();

                                        authHandler.setRestendpoint(applicationInstanceHandler.getHandlerID().getRestEndpointURI());
                                        authHandler.setDomainclazzname(applicationInstanceHandler.getHandlerID().getHasInput().getClassName());
                                        authHandler.setHandleridentifier(applicationInstanceHandler.getHandlerID().getHandlerName());
                                        authHandler.setRangeclazzname(applicationInstanceHandler.getHandlerID().getHasOutput().getClassName());

                                        // TODO Convention for any Request
                                        if (triple.getDomainInstance().getInstanceName().equals("Any Request")) {
                                            authHandler.setDomainargumentinstance(jsonObject.getJSONArray("requestContext").toString());
//                                        } else if (triple.getDomainInstance().getInstanceName().equals("Any Subject")) {
//                                            authHandler.setDomainargumentinstance(authorizationRequest.getSubjectinstance());
                                        } else {
                                            authHandler.setDomainargumentinstance(triple.getDomainInstance().getInstanceName());
                                        }

                                        authHandler.setPropertyname(triple.getProperty().getName());

                                        // TODO token, username, password

                                        authHandlers.add(authHandler);

                                    }


                                });

                            });

                            authorizationRequest.setHandlers(authHandlers);

                        } else {
                            authorizationRequest.setHandlers(null);
                        }

                        logger.info("AuthorizationRequest: " + new JSONObject(authorizationRequest).toString());

                        HttpEntity entity = new HttpEntity(authorizationRequest, null);

                        String SEMANTIC_AUTHORIZATION_ENGINE_URL = semauthengineURL + "/api/semanticpolicyengine/";

                        logger.info("URL:" + SEMANTIC_AUTHORIZATION_ENGINE_URL);

                        ResponseEntity<String> responseEntity = restTemplate.exchange(SEMANTIC_AUTHORIZATION_ENGINE_URL + "/handlerequest", HttpMethod.POST, entity, String.class);

                        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                            activity.setPermission(responseEntity.getBody().contains("ALLOW") ? "ALLOW" : "DENY" );
                            applicationInstanceActivityRepository.save(activity);
                            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, responseEntity.getBody(), responseEntity.getBody());

                        } else {

                            activity.setPermission("DENY");
                            applicationInstanceActivityRepository.save(activity);

                            logger.severe("Response Status: " + responseEntity.getStatusCode());
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, responseEntity.getStatusCode().toString(), responseEntity.getStatusCode());
                        }


                    } catch (Exception e) {

                        logger.severe(e.getMessage());
                        e.printStackTrace();
                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                    }


                } else {
                    logger.severe(Message.APPLICATION_INVALID);
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INVALID, Optional.empty());
                }

            } else {
                logger.severe(Message.EMPTY_AUTHORIZATION_REQUEST);
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.EMPTY_AUTHORIZATION_REQUEST, Optional.empty());
            }



        } else {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.ALLOW_REQUEST, Message.ALLOW_REQUEST);
        }

    }

    @RequestMapping(value = "/authorizationrequest", method = RequestMethod.POST, produces = "application/json")
    public PaaSwordRestResponse authorizationRequest(@RequestBody String jsonForProcessing) {

        String enabled = environment.getProperty("semantic.authorization.engine.enabled");

//        logger.info("Authorization Request invoked() with JSON: " + jsonForProcessing);

        if (enabled.equals("true")) {

            if (null == restTemplate) {
                restTemplate = new RestTemplate();
            }

            if (null != jsonForProcessing && !jsonForProcessing.isEmpty()) {

                JSONObject jsonObject = new JSONObject(jsonForProcessing);

                // Check if APP_KEY is valid
                String APP_KEY = jsonObject.getString("appKey");

                Application application = null;

                try {

                    application = ((APIKey) apiKeyService.findByUniqueID(APP_KEY).get()).getApplicationID();

                } catch (APIKeyUniqueIDDoesNotExist e) {
                    logger.severe(Message.APPLICATION_INVALID);
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INVALID, Optional.empty());
                }

                if (null != application) {

                    try {

                        AuthorizationRequest authorizationRequest = new AuthorizationRequest();

                        ApplicationInstanceActivity activity = new ApplicationInstanceActivity();

                        String action = null;
                        if (null != jsonObject.getString("action") && !jsonObject.getString("action").isEmpty()) {
                            action = jsonObject.getString("action");
                        } else {
                            logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                        }

                        String object = null;
                        if (null != jsonObject.getString("object") && !jsonObject.getString("object").isEmpty()) {
                            object = jsonObject.getString("object");
                        } else {
                            logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                        }

                        String remoteAddress = null;
                        if (null != jsonObject.getString("remoteAddress") && !jsonObject.getString("remoteAddress").isEmpty()) {
                            remoteAddress = jsonObject.getString("remoteAddress");
                        } else {
                            remoteAddress = "Undefined";
                        }

                        String actor = null;
                        if (null != jsonObject.getString("actor") && !jsonObject.getString("actor").isEmpty()) {
                            actor = jsonObject.getString("actor");
                        } else {
                            actor = "Unauthorized";
                        }

                        String pepType = null;
                        if (null != jsonObject.getString("PEPType") && !jsonObject.getString("PEPType").isEmpty()) {
                            pepType = jsonObject.getString("PEPType");
                        } else {
                            logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                        }

                        String pepIdentifier = null;
                        if (null != jsonObject.getString("PEPIdentifier") && !jsonObject.getString("PEPIdentifier").isEmpty()) {
                            pepIdentifier = jsonObject.getString("PEPIdentifier");
                        } else {
                            logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                        }

                        authorizationRequest.setRequestid(Util.sha256hash(String.format("%s-%s",UUID.randomUUID().toString(), new Date().getTime())));
                        authorizationRequest.setActioninstance(action);
                        authorizationRequest.setObjectinstance(object);
                        authorizationRequest.setRemoteAddress(remoteAddress);
                        // TODO Pass request context from PaaSword library
                        authorizationRequest.setRequestContext("[{\"remoteAddress\":\"" + remoteAddress + "\"}]");
                        authorizationRequest.setSubjectinstance(actor);

                        // ACTIVITY
                        activity.setActor(actor);
                        activity.setApplicationInstanceID(application.getApplicationInstance());
                        activity.setHeader("[{\"remoteAddress\":\"" + remoteAddress + "\"}]");
                        activity.setInvocationTimestamp(new Date());
                        activity.setDateRegistered(formatter.format(new Date()));
                        activity.setAnnotatedCode(pepType + " (" + pepIdentifier + ")");
                        activity.setObject(object);
                        activity.setRemoteAddress(remoteAddress);
                        activity.setLocalAddress(remoteAddress);
                        activity.setName(application.getApplicationInstance().getName());

                        List<Triple> triples = new ArrayList<>();

                        List<AnnotatedCode> pepsPerApplication = null;

                        if (application.isPep()) {

                            pepsPerApplication = RepositoryUtil.identifyAllPEPsPerApplication(application, ruleRepository, policyRepository, policySetRepository);

                            if (null == pepsPerApplication || pepsPerApplication.isEmpty()) {
                                logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                            }

                        } else {
                            // TODO Is it ????
                            logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                        }

                        if (pepType.equals("POLICY")) {

                            boolean policyOK = false;

                            Policy policy = policyRepository.getPolicyByName(pepIdentifier);

                            if (null != policy) {

                                if (null != policy.getPolicyRules() && !policy.getPolicyRules().isEmpty()) {

                                } else {

                                    logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());

                                }

                                // Check if Specific Policy is associated with the specific application
                                for (AnnotatedCode pep : pepsPerApplication) {

                                    if (null != pep.getMethods() && !pep.getMethods().isEmpty()) {

                                        for (AnnotatedMethod annotatedMethod : pep.getMethods()) {

                                            if (null != annotatedMethod && !annotatedMethod.getMethodAnnotations().isEmpty()) {

                                                for (AnnotatedAnnotation annotation : annotatedMethod.getMethodAnnotations()) {

                                                    if (annotation.getType().equals("POLICY") && annotation.getEntityID() == policy.getId()) {
                                                        policyOK = true;
                                                    }

                                                }

                                            }

                                        }
                                    }

                                    if (null != pep.getAnnotations() && !pep.getAnnotations().isEmpty()) {

                                        for (AnnotatedAnnotation annotation : pep.getAnnotations()) {

                                            if (annotation.getType().equals("POLICY") && annotation.getEntityID() == policy.getId()) {
                                                policyOK = true;
                                            }

                                        }

                                    }

                                }

                                if (policyOK) {

                                    List<eu.paasword.rest.semanticauthorizationengine.transferobject.Policy> authPolicies = new ArrayList<>();

                                    eu.paasword.rest.semanticauthorizationengine.transferobject.Policy authPolicy = new eu.paasword.rest.semanticauthorizationengine.transferobject.Policy();
                                    authPolicy.setPolicyidentifier(policy.getPolicyName());
                                    authPolicy.setPolicycombiningalgorithm(policy.getCombiningAlgorithmID().getUri());

                                    List<eu.paasword.rest.semanticauthorizationengine.transferobject.Rule> authRules = new ArrayList<>();


                                    for (PolicyRule policyRule : policy.getPolicyRules()) {

                                        eu.paasword.rest.semanticauthorizationengine.transferobject.Rule rule = new eu.paasword.rest.semanticauthorizationengine.transferobject.Rule();

                                        // Find Triples
                                        DroolsExpression droolsExpression = new DroolsExpression();
                                        JSONArray rulesArray = new JSONObject(policyRule.getRule().getExpressionID().getExpression()).getJSONArray("rules");

                                        JSONObject newExpressionObj = new JSONObject();

                                        if (null != policyRule.getRule().getExpressionID().getReferredExpressions() && !policyRule.getRule().getExpressionID().getReferredExpressions().isEmpty()) {

                                            String condition = policyRule.getRule().getExpressionID().getCondition();

                                            newExpressionObj.put("condition", condition);

                                            JSONArray newRulesArray = new JSONArray();

                                            for (int i = 0; i < rulesArray.length(); i++) {
                                                newRulesArray.put(rulesArray.getJSONObject(i));
                                            }

                                            policyRule.getRule().getExpressionID().getReferredExpressionsFormatted().stream().forEach(expressionID -> {

                                                Expression tempExpression = expressionRepository.findOne(Long.valueOf(expressionID));

                                                JSONArray newTempRulesArray = new JSONObject(tempExpression.getExpression()).getJSONArray("rules");

                                                for (int j = 0; j < newTempRulesArray.length(); j++) {

                                                    newRulesArray.put(newTempRulesArray.getJSONObject(j));

                                                }

                                            });

                                            newExpressionObj.put("rules", newRulesArray);

                                        } else {
                                            newExpressionObj = new JSONObject(policyRule.getRule().getExpressionID().getExpression());
                                        }


                                        RepositoryUtil.parseExpressionForTriplesToDroolsExpression(
                                                droolsExpression, newExpressionObj, instanceRepository, propertyRepository);

                                        if (null != droolsExpression.getTriples() && !droolsExpression.getTriples().isEmpty()) {

                                            droolsExpression.getTriples().stream().forEach(triple -> {

                                                if (!triples.contains(triple)) {
                                                    triples.add(triple);
                                                }

                                            });

                                        }


                                        rule.setRuleidentifier(policyRule.getRule().getRuleName());

                                        authRules.add(rule);
                                    }

                                    authPolicy.setRules(authRules);

                                    authPolicies.add(authPolicy);

                                    authorizationRequest.setPolicies(authPolicies);


                                } else {

                                    logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());

                                }

                            } else {

                                logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());

                            }

                        } else if (pepType.equals("POLICY_SET")) {

                            boolean policySetOK = false;

                            PolicySet policySet = policySetRepository.findByPolicySetName(pepIdentifier);

                            if (null != policySet) {

                                if (null != policySet.getPolicySetPolicies() && !policySet.getPolicySetPolicies().isEmpty()) {

                                    if (policySet.getPolicySetPolicies().stream().filter(
                                            policySetPolicy -> null != policySetPolicy.getPolicy().getPolicyRules() && !policySetPolicy.getPolicy().getPolicyRules().isEmpty())
                                            .collect(Collectors.toList()).isEmpty()) {

                                        logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());

                                    }

                                } else {

                                    logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());

                                }

                                // Check if Specific Policy Set is associated with the specific application
                                for (AnnotatedCode pep : pepsPerApplication) {

                                    if (null != pep.getMethods() && !pep.getMethods().isEmpty()) {

                                        for (AnnotatedMethod annotatedMethod : pep.getMethods()) {

                                            if (null != annotatedMethod && !annotatedMethod.getMethodAnnotations().isEmpty()) {

                                                for (AnnotatedAnnotation annotation : annotatedMethod.getMethodAnnotations()) {

                                                    if (annotation.getType().equals("POLICY_SET") && annotation.getEntityID() == policySet.getId()) {
                                                        policySetOK = true;
                                                    }

                                                }

                                            }

                                        }
                                    }

                                    if (null != pep.getAnnotations() && !pep.getAnnotations().isEmpty()) {

                                        for (AnnotatedAnnotation annotation : pep.getAnnotations()) {

                                            if (annotation.getType().equals("POLICY_SET") && annotation.getEntityID() == policySet.getId()) {
                                                policySetOK = true;
                                            }

                                        }

                                    }

                                }

                                if (policySetOK) {

                                    List<eu.paasword.rest.semanticauthorizationengine.transferobject.PolicySet> authPolicySets = new ArrayList<>();

                                    eu.paasword.rest.semanticauthorizationengine.transferobject.PolicySet authPolicySet = new eu.paasword.rest.semanticauthorizationengine.transferobject.PolicySet();
                                    authPolicySet.setPolicysetcombiningalgorithm(policySet.getCombiningAlgorithmID().getUri());
                                    authPolicySet.setPolicysetidentifier(policySet.getPolicySetName());

                                    List<eu.paasword.rest.semanticauthorizationengine.transferobject.Policy> authPolicies = new ArrayList<>();

                                    for (PolicySetPolicy policySetPolicy : policySet.getPolicySetPolicies()) {

                                        eu.paasword.rest.semanticauthorizationengine.transferobject.Policy authPolicy = new eu.paasword.rest.semanticauthorizationengine.transferobject.Policy();

                                        authPolicy.setPolicyidentifier(policySetPolicy.getPolicy().getPolicyName());
                                        authPolicy.setPolicycombiningalgorithm(policySetPolicy.getPolicy().getCombiningAlgorithmID().getUri());

                                        List<eu.paasword.rest.semanticauthorizationengine.transferobject.Rule> authRules = new ArrayList<>();

                                        policySetPolicy.getPolicy().getPolicyRules().stream().forEach( policyRule -> {

                                            eu.paasword.rest.semanticauthorizationengine.transferobject.Rule authRule = new eu.paasword.rest.semanticauthorizationengine.transferobject.Rule();

                                            // Find Triples
                                            DroolsExpression droolsExpression = new DroolsExpression();
                                            JSONArray rulesArray = new JSONObject(policyRule.getRule().getExpressionID().getExpression()).getJSONArray("rules");

                                            JSONObject newExpressionObj = new JSONObject();

                                            if (null != policyRule.getRule().getExpressionID().getReferredExpressions() && !policyRule.getRule().getExpressionID().getReferredExpressions().isEmpty()) {

                                                String condition = policyRule.getRule().getExpressionID().getCondition();

                                                newExpressionObj.put("condition", condition);

                                                JSONArray newRulesArray = new JSONArray();

                                                for (int i = 0; i < rulesArray.length(); i++) {
                                                    newRulesArray.put(rulesArray.getJSONObject(i));
                                                }

                                                policyRule.getRule().getExpressionID().getReferredExpressionsFormatted().stream().forEach(expressionID -> {

                                                    Expression tempExpression = expressionRepository.findOne(Long.valueOf(expressionID));

                                                    JSONArray newTempRulesArray = new JSONObject(tempExpression.getExpression()).getJSONArray("rules");

                                                    for (int j = 0; j < newTempRulesArray.length(); j++) {

                                                        newRulesArray.put(newTempRulesArray.getJSONObject(j));

                                                    }

                                                });

                                                newExpressionObj.put("rules", newRulesArray);

                                            } else {
                                                newExpressionObj = new JSONObject(policyRule.getRule().getExpressionID().getExpression());
                                            }


                                            RepositoryUtil.parseExpressionForTriplesToDroolsExpression(
                                                    droolsExpression, newExpressionObj, instanceRepository, propertyRepository);

                                            if (null != droolsExpression.getTriples() && !droolsExpression.getTriples().isEmpty()) {

                                                droolsExpression.getTriples().stream().forEach(triple -> {

                                                    if (!triples.contains(triple)) {
                                                        triples.add(triple);
                                                    }

                                                });

                                            }

                                            authRule.setRuleidentifier(policyRule.getRule().getRuleName());

                                            authRules.add(authRule);

                                        });

                                        authPolicy.setRules(authRules);

                                        authPolicies.add(authPolicy);

                                    }

                                    authPolicySets.add(authPolicySet);

                                    authorizationRequest.setPolicysets(authPolicySets);

                                } else {
                                    logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                                }

                            } else {
                                logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                            }


                        } else if (pepType.equals("RULE")) {

                            boolean ruleOK = false;

                            Rule rule = ruleRepository.findByRuleName(pepIdentifier);

                            if (null != rule) {

                                // Check if Specific Rule is associated with the specific application
                                for (AnnotatedCode pep : pepsPerApplication) {

                                    if (null != pep.getMethods() && !pep.getMethods().isEmpty()) {

                                        for (AnnotatedMethod annotatedMethod : pep.getMethods()) {

                                            if (null != annotatedMethod && !annotatedMethod.getMethodAnnotations().isEmpty()) {

                                                for (AnnotatedAnnotation annotation : annotatedMethod.getMethodAnnotations()) {

                                                    if (annotation.getType().equals("RULE") && annotation.getEntityID() == rule.getId()) {
                                                        ruleOK = true;
                                                    }

                                                }

                                            }

                                        }
                                    }

                                    if (null != pep.getAnnotations() && !pep.getAnnotations().isEmpty()) {

                                        for (AnnotatedAnnotation annotation : pep.getAnnotations()) {

                                            if (annotation.getType().equals("RULE") && annotation.getEntityID() == rule.getId()) {
                                                ruleOK = true;
                                            }

                                        }

                                    }

                                }

                                if (ruleOK) {

                                    List<eu.paasword.rest.semanticauthorizationengine.transferobject.Rule> authRules = new ArrayList<>();

                                    eu.paasword.rest.semanticauthorizationengine.transferobject.Rule authRule = new eu.paasword.rest.semanticauthorizationengine.transferobject.Rule();
                                    authRule.setRuleidentifier(rule.getRuleName());

                                    // Find Triples
                                    DroolsExpression droolsExpression = new DroolsExpression();
                                    JSONArray rulesArray = new JSONObject(rule.getExpressionID().getExpression()).getJSONArray("rules");

                                    JSONObject newExpressionObj = new JSONObject();

                                    if (null != rule.getExpressionID().getReferredExpressions() && !rule.getExpressionID().getReferredExpressions().isEmpty()) {

                                        String condition = rule.getExpressionID().getCondition();

                                        newExpressionObj.put("condition", condition);

                                        JSONArray newRulesArray = new JSONArray();

                                        for (int i = 0; i < rulesArray.length(); i++) {
                                            newRulesArray.put(rulesArray.getJSONObject(i));
                                        }

                                        rule.getExpressionID().getReferredExpressionsFormatted().stream().forEach(expressionID -> {

                                            Expression tempExpression = expressionRepository.findOne(Long.valueOf(expressionID));

                                            JSONArray newTempRulesArray = new JSONObject(tempExpression.getExpression()).getJSONArray("rules");

                                            for (int j = 0; j < newTempRulesArray.length(); j++) {

                                                newRulesArray.put(newTempRulesArray.getJSONObject(j));

                                            }

                                        });

                                        newExpressionObj.put("rules", newRulesArray);

                                    } else {
                                        newExpressionObj = new JSONObject(rule.getExpressionID().getExpression());
                                    }


                                    RepositoryUtil.parseExpressionForTriplesToDroolsExpression(
                                            droolsExpression, newExpressionObj, instanceRepository, propertyRepository);

                                    if (null != droolsExpression.getTriples() && !droolsExpression.getTriples().isEmpty()) {

                                        droolsExpression.getTriples().stream().forEach(triple -> {

                                            if (!triples.contains(triple)) {
                                                triples.add(triple);
                                            }

                                        });

                                    }

                                    authRules.add(authRule);

                                    authorizationRequest.setRules(authRules);

                                } else {

                                    logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());

                                }

                            } else {

                                logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());

                            }


                        } else {
                            logger.severe(Message.INVALID_AUTHORIZATION_REQUEST);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                        }

                        // Identify all Handlers and add new information

                        List<ApplicationInstanceHandler> applicationInstanceHandlers = application.getApplicationInstance().getApplicationInstanceHandlers();

                        if (null != triples && !triples.isEmpty() && null != applicationInstanceHandlers && !applicationInstanceHandlers.isEmpty()) {

                            List<eu.paasword.rest.semanticauthorizationengine.transferobject.Handler> authHandlers = new ArrayList<>();

                            triples.stream().forEach(triple -> {

                                long domainClazzID = triple.getDomainClazz().getId();
                                long rangeClazzID = triple.getRangeClazz().getId();

                                applicationInstanceHandlers.stream().forEach(applicationInstanceHandler -> {

                                    if (applicationInstanceHandler.getHandlerID().getHasInput().getId() == domainClazzID && applicationInstanceHandler.getHandlerID().getHasOutput().getId() == rangeClazzID) {

                                        eu.paasword.rest.semanticauthorizationengine.transferobject.Handler authHandler = new eu.paasword.rest.semanticauthorizationengine.transferobject.Handler();

                                        authHandler.setRestendpoint(applicationInstanceHandler.getHandlerID().getRestEndpointURI());
                                        authHandler.setDomainclazzname(applicationInstanceHandler.getHandlerID().getHasInput().getClassName());
                                        authHandler.setHandleridentifier(applicationInstanceHandler.getHandlerID().getHandlerName());
                                        authHandler.setRangeclazzname(applicationInstanceHandler.getHandlerID().getHasOutput().getClassName());

                                        // TODO Convention for any Request
                                        if (triple.getDomainInstance().getInstanceName().equals("Any Request")) {
                                            authHandler.setDomainargumentinstance(authorizationRequest.getRequestContext());
//                                        } else if (triple.getDomainInstance().getInstanceName().equals("Any Subject")) {
//                                            authHandler.setDomainargumentinstance(authorizationRequest.getSubjectinstance());
                                        } else {
                                            authHandler.setDomainargumentinstance(triple.getDomainInstance().getInstanceName());
                                        }

                                        authHandler.setPropertyname(triple.getProperty().getName());

                                        // TODO token, username, password

                                        authHandlers.add(authHandler);

                                    }


                                });

                            });



                            authorizationRequest.setHandlers(authHandlers);

                        } else {
                            authorizationRequest.setHandlers(null);
                        }

                        logger.info("AuthorizationRequest: " + new JSONObject(authorizationRequest).toString());

                        HttpEntity entity = new HttpEntity(authorizationRequest, null);

                        String SEMANTIC_AUTHORIZATION_ENGINE_URL = semauthengineURL + "/api/semanticpolicyengine/";

                        logger.info("URL:" + SEMANTIC_AUTHORIZATION_ENGINE_URL);

                        ResponseEntity<String> responseEntity = restTemplate.exchange(SEMANTIC_AUTHORIZATION_ENGINE_URL + "/handlerequest", HttpMethod.POST, entity, String.class);

                        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                            activity.setPermission(responseEntity.getBody().contains("ALLOW") ? "ALLOW" : "DENY" );
                            applicationInstanceActivityRepository.save(activity);
                            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, responseEntity.getBody(), responseEntity.getBody());

                        } else {
                            activity.setPermission("DENY");
                            applicationInstanceActivityRepository.save(activity);
                            logger.severe("Response Status: " + responseEntity.getStatusCode());
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, responseEntity.getStatusCode().toString(), responseEntity.getStatusCode());
                        }


                    } catch (Exception e) {
                        logger.severe(e.getMessage());
                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_AUTHORIZATION_REQUEST, Optional.empty());
                    }


                } else {
                    logger.severe(Message.APPLICATION_INVALID);
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INVALID, Optional.empty());
                }

            } else {
                logger.severe(Message.EMPTY_AUTHORIZATION_REQUEST);
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.EMPTY_AUTHORIZATION_REQUEST, Optional.empty());
            }



        } else {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.ALLOW_REQUEST, Message.ALLOW_REQUEST);
        }

    }

//    /**
//     * Test remove knowledge base
//     *
//     * @return PaaSwordRestResponse object
//     */
//    @RequestMapping(value = "/removeknowledgebase", method = RequestMethod.GET)
//    public PaaSwordRestResponse removeKnowledgebase() {
//
//        if (null == restTemplate) {
//            restTemplate = new RestTemplate();
//        }
//
//        try {
//
//            List<Application> deployedApps = new ArrayList<>();
//
//            deployedApps.add((Application) applicationService.findOneWithoutBlob(2L).get());
//
//            JSONObject appToUndeploy = new JSONObject();
//
//            appToUndeploy.put("application_instance_id", "1");
//
//            //AppLifecycleMessageTO appToUndeploy = new AppLifecycleMessageTO();
//            JSONArray policiesToDel = RepositoryUtil.PEPsForDeletion((Application) applicationService.findOneWithoutBlob(1L).get(), deployedApps);
//
//            appToUndeploy.put("policiesToDelete", policiesToDel);
//
//            //System.out.println("appToUndeploy to string" + appToUndeploy.toString());
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<String> entity = new HttpEntity<>(appToUndeploy.toString(), headers);
//            String answer = restTemplate.postForObject(SEMANTIC_AUTHORIZATION_ENGINE_URL + "/removeKnowledgebase", entity, String.class);
//
//
//            HttpEntity entity = new HttpEntity(newJSONObj.toString(), null);
//
//            ResponseEntity<String> responseEntity = restTemplate.exchange(SEMANTIC_AUTHORIZATION_ENGINE_URL + "/getAuthenticationPermission", HttpMethod.POST, entity, String.class);
//
//            if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
//
//                return responseEntity.getBody();
//
//            } else {
//                logger.log(Level.SEVERE, "SemanticAuthorizationEngineManagementRestController: Response Status: {0}", new Object[]{responseEntity.getStatusCode()});
//                return responseEntity.getStatusCode().toString();
//            }
//
//            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, answer, Optional.empty());
//        } catch (ApplicationDoesNotExist ex) {
//            Logger.getLogger(SemanticAuthorizationEngineManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
//            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, "ApplicationDoesNotExist exception", Optional.empty());
//        }
//    }

    private static List<String> findResources(SystemProperty systemProperty, String resource) {

        try {

            Properties p = new Properties();
            p.load(new java.io.StringReader(systemProperty.getValue()));

            for (String objUri : p.stringPropertyNames()) {

                if (objUri.equalsIgnoreCase(resource)) {

                    if (p.getProperty(objUri).contains(" ")) {
                        String[] properties = p.getProperty(objUri).split(" ");
                        return Arrays.asList(properties);
                    } else {
                        return Arrays.asList(p.getProperty(objUri));
                    }
                } else {
                    continue;
                }
            }

            return null;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Inner class containing all the static messages which will be used in an PaaSwordRestResponse.
     */
    private final static class Message {
        final static String APPLICATION_VALIDATED = "Application validated successfully";
        final static String APPLICATION_INVALID = "Application is not valid";
        final static String ALLOW_REQUEST = "ALLOW";
        final static String EMPTY_AUTHORIZATION_REQUEST = "The authorization request is empty";
        final static String INVALID_AUTHORIZATION_REQUEST = "The authorization request is invalid";
        final static String SEMANTIC_AUTHORIZATION_ENGINE_SYNCHRONIZED = "Semantic Authorization Engine has been synchronized successfully";
        final static String SEMANTIC_AUTHORIZATION_ENGINE_SYNCHRONIZED_ERROR = "Error during synchronization of Semantic Authorization Engine";

    }

}
