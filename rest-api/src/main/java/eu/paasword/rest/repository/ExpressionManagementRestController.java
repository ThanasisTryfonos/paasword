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
package eu.paasword.rest.repository;

import eu.paasword.api.repository.*;
import eu.paasword.api.repository.exception.clazz.ClazzDoesNotExist;
import eu.paasword.api.repository.exception.expression.ExpressionAlreadyExistsException;
import eu.paasword.api.repository.exception.expression.ExpressionDoesNotExist;
import eu.paasword.api.repository.exception.expression.ExpressionValidityException;
import eu.paasword.api.repository.exception.instance.InstanceDoesNotExist;
import eu.paasword.api.repository.exception.namespace.NamespaceDoesNotExist;
import eu.paasword.repository.relational.domain.*;
import eu.paasword.rest.repository.transferobject.TExpression;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import eu.paasword.spi.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all the rest endpoints regarding expressions
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/expression")
public class ExpressionManagementRestController {

    private static final Logger logger = Logger.getLogger(ExpressionManagementRestController.class.getName());

    @Value("${paasword.semauthengine.url}")
    private String semauthengineURL;

    @Autowired
    IExpressionService<Expression> expressionService;

    @Autowired
    INamespaceService<Namespace> namespaceService;

    @Autowired
    IInstanceService instanceService;

    @Autowired
    IPropertyInstanceService propertyInstanceService;

    @Autowired
    IClazzService<Clazz> clazzService;

    /**
     * Fetch all available expressions from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getExpressions() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, expressionService.findAll());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to fetch an
     * expression with a specific ID from database.
     *
     * @param id The id of the expression to fetch
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getExpressionByID(@PathVariable("id") long id) {
        try {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, expressionService.findOne(id));
        } catch (ExpressionDoesNotExist ex) {
            Logger.getLogger(ExpressionManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to load the query builder
     *
     * @param tExpression A JSON object which will be casted to a TExpression (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/querybuilder", method = RequestMethod.POST)
    public PaaSwordRestResponse loadQueryBuilder(@RequestBody TExpression tExpression) {

        if (null != tExpression.getInstances() && !tExpression.getInstances().isEmpty()) {

            List<String> instanceIDs = tExpression.getInstances();

            List<Instance> instances = new ArrayList<>();

            instanceIDs.stream().forEach(instanceID -> {

                try {

                    Instance instance = (Instance) instanceService.findOne(Long.valueOf(instanceID)).get();
                    instances.add(instance);

                } catch (InstanceDoesNotExist e) {
                    e.printStackTrace();
                }

            });

            // Instances fetched
            // Construct query builder


            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.AUTOCOMPLETE_LOADED, constructQueryBuilder(instances));

        } else {

            return new PaaSwordRestResponse(BasicResponseCode.INVALID, "Invalid Parameters", Optional.empty());

        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new
     * expression to the database.
     *
     * @param tExpression A JSON object which will be casted to a TExpression
     *                    (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody TExpression tExpression) {
        try {

            Expression expression = new Expression();

            expression.setExpression(tExpression.getExpression());
            expression.setExpressionName(tExpression.getExpressionName());
            expression.setDescription(tExpression.getDescription());
            List<String> instanceIDs = tExpression.getInstances();

            JSONArray instances = new JSONArray();

            instanceIDs.stream().forEach(instance -> {
                instances.put(instance);
            });

            expression.setInstanceSetIDs(instances.toString());

            if (null != tExpression.getExpressions() && !tExpression.getExpressions().isEmpty()) {

                List<String> expressionIDs = tExpression.getExpressions();

                JSONArray referredExpressionsArray = new JSONArray();

                expressionIDs.stream().forEach(tempExp -> {
                    referredExpressionsArray.put(tempExp);
                });

                expression.setReferredExpressions(referredExpressionsArray.toString());

            } else {
                expression.setReferredExpressions(null);
            }

            expression.setCondition(tExpression.getCondition());

            if (0 == tExpression.getNamespaceID()) {
                expression.setNamespaceID(null);
            } else {
                expression.setNamespaceID(namespaceService.findOne(tExpression.getNamespaceID()).get());
            }

            expression.setDateCreated(new Date());

            expressionService.create(expression);

            // TODO
            //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(semauthengineURL + "/api/semanticpolicyengine/loadrules", String.class);
            logger.info(response);

        } catch (ExpressionAlreadyExistsException | ExpressionValidityException | NamespaceDoesNotExist ex) {
            Logger.getLogger(ExpressionManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.EXPRESSION_CREATED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to update an
     * existing expression to the database.
     *
     * @param tExpression A JSON object which will be casted to a TExpression
     *                    (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse edit(@RequestBody TExpression tExpression) {
        try {

            Expression currentExpression = (Expression) expressionService.findOne(tExpression.getId()).get();

            currentExpression.setExpression(tExpression.getExpression());
            currentExpression.setExpressionName(tExpression.getExpressionName());
            currentExpression.setDescription(tExpression.getDescription());
            List<String> instanceIDs = tExpression.getInstances();

            JSONArray instances = new JSONArray();

            instanceIDs.stream().forEach(instance -> {
                instances.put(instance);
            });

            if (null != tExpression.getExpressions() && !tExpression.getExpressions().isEmpty()) {

                List<String> expressionIDs = tExpression.getExpressions();

                JSONArray referredExpressionsArray = new JSONArray();

                expressionIDs.stream().forEach(tempExp -> {
                    referredExpressionsArray.put(tempExp);
                });

                currentExpression.setReferredExpressions(referredExpressionsArray.toString());

            } else {
                currentExpression.setReferredExpressions(null);
            }

            currentExpression.setCondition(tExpression.getCondition());

            if (0 == tExpression.getNamespaceID()) {
                currentExpression.setNamespaceID(null);
            } else {
                currentExpression.setNamespaceID(namespaceService.findOne(tExpression.getNamespaceID()).get());
            }

            expressionService.edit(currentExpression);

            // TODO
            //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(semauthengineURL + "/api/semanticpolicyengine/loadrules", String.class);
            logger.info(response);

        } catch (ExpressionDoesNotExist | ExpressionValidityException | NamespaceDoesNotExist ex) {
            Logger.getLogger(ExpressionManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.EXPRESSION_UPDATED, Optional.empty());
    }

    /**
     * Deletes an expression from database.
     *
     * @param id The id of the expression to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {

            // Have to check if this expression is referred to other expressions

            Expression currentExp = (Expression) expressionService.findOne(id).get();

            boolean canBeDeleted = true;

            if (null != currentExp) {

                List<Long> expressionIDs = new ArrayList<>();

                List<Expression> allExpressions = expressionService.findAll();

                allExpressions.remove(currentExp);

                for (Expression tempExp : allExpressions) {

                    if (null != tempExp.getReferredExpressions() && !tempExp.getReferredExpressions().isEmpty()) {

                        if (tempExp.getReferredExpressionsFormatted().contains(String.valueOf(currentExp.getId()))) {
                            logger.info("ID: " + currentExp.getId() + " is referred to: " + tempExp.getId());
                            canBeDeleted = false;

                        }

                    }
                }

                if (canBeDeleted) {

                    expressionService.delete(id);

                    // TODO
                    //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
                    RestTemplate restTemplate = new RestTemplate();
                    String response = restTemplate.getForObject(semauthengineURL + "/api/semanticpolicyengine/loadrules", String.class);
                    logger.info(response);

                } else {
                    Logger.getLogger(ExpressionManagementRestController.class.getName()).log(Level.SEVERE, Message.EXPRESSION_DELETED_USED_ERROR);
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.EXPRESSION_DELETED_USED_ERROR, Optional.empty());
                }

            } else {
                Logger.getLogger(ExpressionManagementRestController.class.getName()).log(Level.SEVERE, Message.EXPRESSION_DELETED_ERROR);
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.EXPRESSION_DELETED_ERROR, Optional.empty());
            }


        } catch (ExpressionDoesNotExist | ExpressionValidityException ex) {
            Logger.getLogger(ExpressionManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.EXPRESSION_DELETED, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an
     * PaaSwordRestResponse.
     */
    private final static class Message {

        final static String EXPRESSION_REGISTERED = "Expression has been registered";
        final static String EXPRESSION_DELETED = "Expression has been deleted";
        final static String EXPRESSION_UPDATED = "Expression has been updated";
        final static String EXPRESSION_CREATED = "Expression has been created";
        final static String AUTOCOMPLETE_LOADED = "Instances loaded successfully!";
        final static String EXPRESSION_DELETED_USED_ERROR = "Expression cannot be deleted! Please check if it is referred to other expressions!";
        final static String EXPRESSION_DELETED_ERROR = "Expression hasn't been deleted";
    }

    /**
     * Constructs the query builder
     *
     * @param listOfInstances
     * @return A String object
     */
    public String constructQueryBuilder(List<Instance> listOfInstances) {

        JSONArray jsonNodes = new JSONArray();

        listOfInstances.forEach(node -> {

            // check class
            Clazz tempClazz = node.getClassID();

            // fetch properties
            List<Property> properties = tempClazz.getAllProperties();

            if (null != properties && !properties.isEmpty()) {

                properties.stream().forEach(property -> {

                    if (property.isObjectProperty()) {

                        JSONObject fatherNode = new JSONObject();
                        fatherNode.put("label", node.getInstanceName() + "." + property.getName());
                        fatherNode.put("id", "i" + node.getId() + "p" + property.getId());
                        fatherNode.put("type", "integer");
                        fatherNode.put("input", "select");

                        JSONArray values = new JSONArray();

                        // Find Class Children

                        List<Clazz> children = new ArrayList<>();
                        children.add(property.getObjectPropertyClassID());

                        findMyChildren(children, property.getObjectPropertyClassID());

                        if (null != children && !children.isEmpty()) {

                            children.stream().forEach(child -> {

                                child.getInstances().forEach(instance -> {

                                    if (instance.getId() != node.getId()) {

                                        JSONObject tempValue = new JSONObject();
                                        tempValue.put(String.valueOf(instance.getId()), instance.getInstanceName());
                                        values.put(tempValue);
                                    }

                                });

                            });

                            JSONArray sortedJsonArray = new JSONArray();
                            List<JSONObject> jsonValues = new ArrayList<JSONObject>();

                            for (int i = 0; i < values.length(); i++) {
                                jsonValues.add(values.getJSONObject(i));
                            }

                            Collections.sort( jsonValues, new Comparator<JSONObject>() {
                                //You can change "Name" with "ID" if you want to sort by ID
                                private static final String KEY_NAME = "Name";

                                @Override
                                public int compare(JSONObject a, JSONObject b) {
                                    String valA = new String();
                                    String valB = new String();

                                    try {

                                        Iterator<String> keys = a.keys();
                                        // get some_name_i_wont_know in str_Name
                                        String str_Name = keys.next();
                                        // get the value i care about
                                        String value = a.optString(str_Name);

                                        valA = value;

                                        keys = b.keys();

                                        str_Name = keys.next();

                                        value = b.optString(str_Name);

                                        valB = value;
                                    }
                                    catch (JSONException e) {
                                        //do something
                                    }

                                    return valA.compareTo(valB);
                                    //if you want to change the sort order, simply use the following:
                                    //return -valA.compareTo(valB);
                                }
                            });

                            for (int i = 0; i < values.length(); i++) {
                                sortedJsonArray.put(jsonValues.get(i));
                            }


                            fatherNode.put("values", sortedJsonArray);

                            fatherNode.put("operators", "['equal', 'not_equal']");

                            if (sortedJsonArray.length() > 0) {
                                jsonNodes.put(fatherNode);
                            }

                        }

                    } else {

                        // TODO

                    }

                });

            }

        });

        JSONArray sortedFinalJsonArray = new JSONArray();
        List<JSONObject> jsonFinalValues = new ArrayList<JSONObject>();

        for (int i = 0; i < jsonNodes.length(); i++) {
            jsonFinalValues.add(jsonNodes.getJSONObject(i));
        }

        Collections.sort( jsonFinalValues, new Comparator<JSONObject>() {
            //You can change "Name" with "ID" if you want to sort by ID
            private static final String KEY_NAME = "label";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();

                try {

                    valA = (String) a.get(KEY_NAME);
                    valB = (String) b.get(KEY_NAME);

                }
                catch (JSONException e) {
                    //do something
                }

                return valA.compareTo(valB);
                //if you want to change the sort order, simply use the following:
                //return -valA.compareTo(valB);
            }
        });

        for (int i = 0; i < jsonNodes.length(); i++) {
            sortedFinalJsonArray.put(jsonFinalValues.get(i));
        }



        return sortedFinalJsonArray.toString();

    }

    public void findMyChildren(List<Clazz> children, Clazz currentClazz) {

        try {

            List<Clazz> childrenOfTempClazz = clazzService.findByParentID(currentClazz);

            if (null != childrenOfTempClazz && !childrenOfTempClazz.isEmpty()) {

                childrenOfTempClazz.stream().forEach(clazz -> {

                    children.add(clazz);

                    try {

                        if (null != clazzService.findByParentID(clazz) && !clazzService.findByParentID(clazz).isEmpty()) {

                            findMyChildren(children, clazz);

                        }

                    } catch (ClazzDoesNotExist e) {
                        e.printStackTrace();
                    }

                });

            }

        } catch (ClazzDoesNotExist e) {
            e.printStackTrace();
        }


    }

}
