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
import eu.paasword.api.repository.exception.clazz.ClazzAlreadyExistsException;
import eu.paasword.api.repository.exception.clazz.ClazzDoesNotExist;
import eu.paasword.api.repository.exception.handler.HandlerDoesNotExist;
import eu.paasword.api.repository.exception.namespace.NamespaceDoesNotExist;
import eu.paasword.api.repository.exception.property.PropertyDoesNotExist;
import eu.paasword.repository.relational.domain.*;
import eu.paasword.rest.repository.transferobject.TClazz;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all the rest endpoints regarding classes
 *
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@RestController
@RequestMapping("/api/v1/class")
public class ClassManagementRestController {

    private static final Logger logger = Logger.getLogger(ClassManagementRestController.class.getName());

    @Autowired
    private IClazzService<Clazz> clazzService;

    @Autowired
    private INamespaceService<Namespace> namespaceService;

    @Autowired
    private IHandlerService<Handler> handlerService;

    @Autowired
    private IPropertyService<Property, Clazz> propertyService;

    @Autowired
    private IPropertyInstanceService<PropertyInstance> propertyInstanceService;

    /**
     * Fetch all available classes from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getClasses() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, clazzService.findAllCustom(1));
    }

    /**
     * Fetch all available classes from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/tree/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getClassesForTreeGrid(@PathVariable("id") long id) {

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.MODEL_LOADED, constructTreeGrid(clazzService.findAllCustom(id), id));
    }

    /**
     * Fetch all available classes from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/queryBuilder", method = RequestMethod.GET)
    public PaaSwordRestResponse getFilterDataForQueryBuilder() {

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.EXPRESSION_EDITOR_LOADED, constructQueryBuilder(clazzService.findAllCustom()));
    }

    /**
     * Fetch all available classes from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/autocompleteObj", method = RequestMethod.GET)
    public PaaSwordRestResponse getClassesAutocompleteObj(@RequestParam String keyword, @RequestParam long subPropertyOfID) {

        List<Clazz> classes = new ArrayList<>();

        List<Long> ids = new ArrayList<>();

        try {

            Property property = (Property) propertyService.findOne(subPropertyOfID).get();

            Clazz clazz = (Clazz) clazzService.findOne(property.getClassID().getId()).get();

            classes.add(clazz);

            classes.addAll(clazzService.findByParentID(clazz));

            classes.stream().forEach(clas -> {

                if (!ids.contains(clas.getId())) {
                    ids.add(clas.getId());
                }
            });

            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.AUTOCOMPLETE_LOADED, constructAutocomplete(clazzService.getClassesByKeywordAndId(keyword, ids)));

        } catch (ClazzDoesNotExist | PropertyDoesNotExist e) {
            Logger.getLogger(ClassManagementRestController.class.getName()).log(Level.SEVERE, null, e);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, e.getMessage(), Optional.empty());
        }

    }

    /**
     * Fetch all available classes from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/autocomplete", method = RequestMethod.GET)
    public PaaSwordRestResponse getClassesAutocomplete(@RequestParam String keyword, @RequestParam long rootID) {

        if (0 != rootID) {

            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.AUTOCOMPLETE_LOADED, constructAutocomplete(clazzService.getClassesByKeyword(keyword)));

        } else {

            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.AUTOCOMPLETE_LOADED, constructAutocomplete(clazzService.getClassesByKeyword(keyword)));
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to fetch a clazz with a specific ID from database.
     *
     * @param id The id of the clazz to fetch
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getClassByID(@PathVariable("id") long id) {
        try {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, (Clazz) clazzService.findOne(id).get());
        } catch (ClazzDoesNotExist ex) {
            Logger.getLogger(ClassManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new clazz to the database.
     *
     * @param tClazz A JSON object which will be casted to a Clazz (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody TClazz tClazz) {
        try {

            Clazz clazz = new Clazz();
            clazz.setClassName(tClazz.getClassName());

            clazz.setParentID(clazzService.findOne(tClazz.getParentID()).get());

            clazz.setRootID(clazzService.findOne(tClazz.getRootID()).get());

            if (0 == tClazz.getNamespaceID()) {
                clazz.setNamespaceID(null);
            } else {
                clazz.setNamespaceID(namespaceService.findOne(tClazz.getNamespaceID()).get());
            }

            clazzService.create(clazz);

        } catch (ClazzAlreadyExistsException | ClazzDoesNotExist | NamespaceDoesNotExist ex) {
            Logger.getLogger(ClassManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.CLASS_CREATED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to update an existing clazz to the database.
     *
     * @param tClazz A JSON object which will be casted to a TClazz (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse edit(@RequestBody TClazz tClazz) {
        try {

            Clazz clazz = clazzService.findOne(tClazz.getId()).get();
            clazz.setClassName(tClazz.getClassName());

            if (0 != tClazz.getParentID()) {
                clazz.setParentID(clazzService.findOne(tClazz.getParentID()).get());
            } else {
                clazz.setParentID(clazzService.findOne(tClazz.getRootID()).get());
            }

            if (0 == tClazz.getNamespaceID()) {
                clazz.setNamespaceID(null);
            } else {
                clazz.setNamespaceID(namespaceService.findOne(tClazz.getNamespaceID()).get());
            }

            clazzService.edit(clazz);

        } catch (ClazzDoesNotExist | NamespaceDoesNotExist ex) {
            Logger.getLogger(ClassManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.CLASS_UPDATED, Optional.empty());
    }

    /**
     * Deletes a clazz from database.
     *
     * @param id The id of the clazz to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {
            Clazz clazz = (Clazz) clazzService.findOne(id).get();

            if (null != clazz && clazz.isDeletable()) {
                clazzService.delete(id);
            } else {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.CLASS_CANNOT_DELETED, Optional.empty());
            }
        } catch (ClazzDoesNotExist ex) {
            Logger.getLogger(ClassManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.CLASS_DELETED, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an PaaSwordRestResponse.
     *
     */
    private final static class Message {

        final static String CLASS_REGISTERED = "Class has been registered";
        final static String MODEL_LOADED = "Context model loaded successfully!";
        final static String AUTOCOMPLETE_LOADED = "Classes loaded successfully!";
        final static String EXPRESSION_EDITOR_LOADED = "Expression editor loaded successfully!";
        final static String CLASS_DELETED = "Class has been deleted";
        final static String CLASS_CANNOT_DELETED = "Class cannot be deleted";
        final static String CLASS_UPDATED = "Class has been updated";
        final static String CLASS_CREATED = "Class has been created";
    }

    /**
     * Constructs the tree grid of all Classes
     *
     * @param listClazz
     * @param rootClassID
     * @return A String object
     *
     */
    public static String constructTreeGrid(List<Clazz> listClazz, long rootClassID) {

        JSONArray jsonNodes = new JSONArray();

        listClazz.forEach(node -> {

            //Check if is father class
            if (node.isFather(rootClassID)) {
                JSONObject fatherNode = new JSONObject();
                fatherNode.put("label", node.getClassName());
                fatherNode.put("id", node.getId());
                jsonNodes.put(fatherNode);
                //continue;
            } else {

                Object father = findMyFather(jsonNodes, node.getParentID().getId());

                JSONObject childNode = new JSONObject();
                childNode.put("label", node.getClassName());
                childNode.put("id", node.getId());

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

        return jsonNodes.toString();

    }

    /**
     * Constructs the autocomplete json of returned Classes
     *
     * @param listClazz
     * @return A String object
     *
     */
    public static String constructAutocomplete(List<Clazz> listClazz) {

        JSONObject autocompleteObj = new JSONObject();
        JSONArray jsonNodes = new JSONArray();

        listClazz.forEach(node -> {

            JSONObject jsonNode = new JSONObject();

            jsonNode.put("id", node.getId());
            jsonNode.put("value", node.getClassName());
            jsonNodes.put(jsonNode);

        });

        autocompleteObj.put("total", listClazz.size());
        autocompleteObj.put("values", jsonNodes);

        return autocompleteObj.toString();

    }

    /**
     * Constructs the query builder
     *
     * @param listClazz
     * @return A String object
     *
     */
    public String constructQueryBuilder(List<Clazz> listClazz) {

        JSONArray jsonNodes = new JSONArray();

        listClazz.forEach(node -> {

            //Check if is father class
            if (node.isFather(1) && !node.getInstances().isEmpty()) {
                JSONObject fatherNode = new JSONObject();
                fatherNode.put("label", node.getClassName());
                fatherNode.put("id", "c" + node.getId());
                fatherNode.put("type", "integer");
                fatherNode.put("input", "select");

                JSONArray values = new JSONArray();

                node.getInstances().stream().forEach(instance -> {

                    JSONObject tempValue = new JSONObject();
                    tempValue.put(String.valueOf(instance.getId()), instance.getInstanceName());
                    values.put(tempValue);
                });

                fatherNode.put("values", values);

                fatherNode.put("operators", "['equal', 'not_equal']");
                jsonNodes.put(fatherNode);

                // Add also properties

                if (null != node.getProperties() && !node.getProperties().isEmpty()) {

                    node.getProperties().stream().forEach(property -> {

                        List<PropertyInstance> propertyInstanceList = propertyInstanceService.findByPropertyID(property.getId());

                        if (null != propertyInstanceList && !propertyInstanceList.isEmpty()) {

                            JSONObject propertyNode = new JSONObject();
                            propertyNode.put("label", node.getClassName() + " - " + property.getName());
                            propertyNode.put("id", "c"+ node.getId() + "p" + property.getId());
                            propertyNode.put("type", "integer");
                            propertyNode.put("input", "select");

                            JSONArray propertyInstancesValues = new JSONArray();

                            propertyInstanceList.stream().forEach(propertyInstance -> {

                                JSONObject tempValue = new JSONObject();
                                tempValue.put(String.valueOf(propertyInstance.getId()), propertyInstance.getName());
                                propertyInstancesValues.put(tempValue);

                            });

                            propertyNode.put("values", propertyInstancesValues);

                            propertyNode.put("operators", "['equal', 'not_equal']");
                            jsonNodes.put(propertyNode);

                        }

                    });

                }


                //continue;
            } else if (!node.getInstances().isEmpty()) {

                JSONObject fatherNode = new JSONObject();
                fatherNode.put("label", node.getClassName());
                fatherNode.put("id", node.getId());
                fatherNode.put("type", "integer");
                fatherNode.put("input", "select");

                JSONArray values = new JSONArray();

                node.getInstances().stream().forEach(instance -> {

                    JSONObject tempValue = new JSONObject();
                    tempValue.put(String.valueOf(instance.getId()), instance.getInstanceName());
                    values.put(tempValue);
                });

                fatherNode.put("values", values);

                fatherNode.put("operators", "['equal', 'not_equal']");
                jsonNodes.put(fatherNode);

                // Add also properties

                if (null != node.getProperties() && !node.getProperties().isEmpty()) {

                    node.getProperties().stream().forEach(property -> {

                        List<PropertyInstance> propertyInstanceList = propertyInstanceService.findByPropertyID(property.getId());

                        if (null != propertyInstanceList && !propertyInstanceList.isEmpty()) {

                            JSONObject propertyNode = new JSONObject();
                            propertyNode.put("label", node.getClassName() + " - " + property.getName());
                            propertyNode.put("id", "c"+ node.getId() + "p" + property.getId());
                            propertyNode.put("type", "integer");
                            propertyNode.put("input", "select");

                            JSONArray propertyInstancesValues = new JSONArray();

                            List<String> ids = new ArrayList<String>();

                            propertyInstanceList.stream().forEach(propertyInstance -> {

                                JSONObject tempValue = new JSONObject();
                                tempValue.put(String.valueOf(propertyInstance.getId()), propertyInstance.getName());

                                if (!ids.contains(propertyInstance.getName())) {
                                    propertyInstancesValues.put(tempValue);
                                    ids.add(propertyInstance.getName());
                                }

                            });

                            propertyNode.put("values", propertyInstancesValues);

                            propertyNode.put("operators", "['equal', 'not_equal']");
                            jsonNodes.put(propertyNode);

                        }

                    });

                }

            }

        });

        return jsonNodes.toString();

    }

    public static Object findMyFather(Object jsonNode, long father_id) {

        //Terminate condition for recursive function
        if (jsonNode instanceof JSONObject && ((JSONObject) jsonNode).has("id") && (long) ((JSONObject) jsonNode).get("id") == father_id) {
            return jsonNode;
        }

        //Array condition
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

        //Object
        if (jsonNode instanceof JSONObject && ((JSONObject) jsonNode).has("children")) {
            return findMyFather(((JSONObject) jsonNode).get("children"), father_id);
        }

        //Father is not in this node
        return null;
    }
}
