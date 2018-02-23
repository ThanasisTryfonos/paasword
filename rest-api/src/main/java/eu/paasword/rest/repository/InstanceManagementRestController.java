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
import eu.paasword.api.repository.exception.clazz.ClassNameDoesNotExist;
import eu.paasword.api.repository.exception.clazz.ClazzDoesNotExist;
import eu.paasword.api.repository.exception.instance.InstanceAlreadyExistsException;
import eu.paasword.api.repository.exception.instance.InstanceDoesNotExist;
import eu.paasword.api.repository.exception.namespace.NamespaceDoesNotExist;
import eu.paasword.api.repository.exception.property.PropertyDoesNotExist;
import eu.paasword.api.repository.exception.propertyInstance.PropertyInstanceAlreadyExistsException;
import eu.paasword.api.repository.exception.propertyInstance.PropertyInstanceDoesNotExist;
import eu.paasword.repository.relational.domain.*;
import eu.paasword.rest.repository.transferobject.TInstance;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all the rest endpoints regarding instances
 *
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/instance")
public class InstanceManagementRestController {

    private static final Logger logger = Logger.getLogger(InstanceManagementRestController.class.getName());

    @Autowired
    private IInstanceService<Instance, Clazz> instanceService;

    @Autowired
    private IClazzService<Clazz> clazzService;

    @Autowired
    private IPropertyService<Property, Clazz> propertyService;

    @Autowired
    private IPropertyInstanceService<PropertyInstance> propertyInstanceService;

    @Autowired
    private INamespaceService<Namespace> namespaceService;

    /**
     * Fetch all available instances from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getInstances() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, instanceService.findAll());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to fetch an instance with a specific ID from database.
     *
     * @param id The id of the instance to fetch
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getInstanceByID(@PathVariable("id") long id) {
        try {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, instanceService.findOne(id));
        } catch (InstanceDoesNotExist ex) {
            Logger.getLogger(InstanceManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * Fetch all available instances from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/autocomplete", method = RequestMethod.GET)
    public PaaSwordRestResponse getInstancesAutocomplete(@RequestParam String keyword, @RequestParam long propertyID) {

        try {
            Clazz clazz = propertyService.findOne(propertyID).get().getObjectPropertyClassID();

            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.AUTOCOMPLETE_LOADED, constructAutocomplete(instanceService.getInstancesByKeyword(keyword, clazz.getId())));
        } catch (PropertyDoesNotExist ex) {
            Logger.getLogger(InstanceManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

    }

    /**
     * Fetch all available instances from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/class/{className}", method = RequestMethod.GET)
    public PaaSwordRestResponse getInstancesAutocomplete(@PathVariable String className) {

        try {
            Clazz clazz = (Clazz) clazzService.findByClassName(className).get();

            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.AUTOCOMPLETE_LOADED, clazz.getInstances());
        } catch (ClassNameDoesNotExist ex) {
            Logger.getLogger(InstanceManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new instance to the database.
     *
     * @param tInstance A JSON object which will be casted to an TInstance (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody TInstance tInstance) {

        try {

            Instance newInstance = new Instance();
            newInstance.setInstanceName(tInstance.getInstanceName());
            newInstance.setClassID((Clazz) clazzService.findOne(tInstance.getClassID()).get());
            newInstance.setNamespaceID((Namespace) namespaceService.findOne(tInstance.getNamespaceID()).get());
            instanceService.create(newInstance);

            List<PropertyInstance> listOfNewPropInstances = new ArrayList<>();

            tInstance.getPropertyInstances().stream().forEach(tPropInst -> {

                if (!tPropInst.getName().isEmpty()) {

                    PropertyInstance propInst = new PropertyInstance();
                    propInst.setName(tPropInst.getName());
                    propInst.setInstanceID(newInstance);

//                    logger.info("Rest adding: " + tPropInst.getName());

                    try {
                        propInst.setPropertyID(propertyService.findOne(tPropInst.getPropertyID()).get());
//                    propertyInstanceService.create(propInst);

                    } catch (PropertyDoesNotExist ex) {
                        Logger.getLogger(InstanceManagementRestController.class.getName()).severe(ex.getMessage());
                        ex.printStackTrace();
                    }

                    listOfNewPropInstances.add(propInst);

                }
            });

            newInstance.setPropertyInstances(listOfNewPropInstances);

            instanceService.edit(newInstance);

        } catch (InstanceAlreadyExistsException | ClazzDoesNotExist | NamespaceDoesNotExist | InstanceDoesNotExist ex) {
            Logger.getLogger(InstanceManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.INSTANCE_CREATED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to update an existing instance to the database.
     *
     * @param tInstance A JSON object which will be casted to an TInstance (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse edit(@RequestBody TInstance tInstance) {

        try {

            Instance currentInstance = (Instance) instanceService.findOne(tInstance.getId()).get();
            currentInstance.setInstanceName(tInstance.getInstanceName());
            currentInstance.setClassID((Clazz) clazzService.findOne(tInstance.getClassID()).get());
            currentInstance.setNamespaceID((Namespace) namespaceService.findOne(tInstance.getNamespaceID()).get());

            List<PropertyInstance> propertyInstances = new ArrayList<>();

            tInstance.getPropertyInstances().stream().forEach(tProp -> {

                if (!tProp.getName().isEmpty()) {

                    PropertyInstance existingPropertyInstance = currentInstance.getPropertyInstanceByPropertyID(tProp.getPropertyID());

                    if (null != existingPropertyInstance) {

                        try {

                            if (null != tProp.getName() && !tProp.getName().isEmpty()) {

                                existingPropertyInstance.setName(tProp.getName());

                                propertyInstanceService.edit(existingPropertyInstance);

                                propertyInstances.add(existingPropertyInstance);

                            } else {
                                propertyInstanceService.delete(existingPropertyInstance.getId());
                            }

                        } catch (PropertyInstanceDoesNotExist ex) {
                            Logger.getLogger(InstanceManagementRestController.class.getName()).severe(ex.getMessage());
                        }


                    } else {

                        try {

                            if (null != tProp.getName() && !tProp.getName().isEmpty()) {

                                PropertyInstance newPropInstance = new PropertyInstance();
                                newPropInstance.setInstanceID(currentInstance);

                                newPropInstance.setPropertyID((Property) propertyService.findOne(tProp.getPropertyID()).get());

                                newPropInstance.setName(tProp.getName());
                                propertyInstanceService.create(newPropInstance);

                                propertyInstances.add(newPropInstance);

                            }

                        } catch (PropertyDoesNotExist | PropertyInstanceAlreadyExistsException ex) {
                            Logger.getLogger(InstanceManagementRestController.class.getName()).severe(ex.getMessage());
                        }

                    }

                }

            });

            currentInstance.setPropertyInstances(propertyInstances);

            instanceService.edit(currentInstance);

        } catch (InstanceDoesNotExist | ClazzDoesNotExist | NamespaceDoesNotExist ex) {
            Logger.getLogger(InstanceManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.INSTANCE_UPDATED, Optional.empty());
    }

    /**
     * Deletes an instance from database.
     *
     * @param id The id of the instance to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {
            instanceService.delete(id);
        } catch (InstanceDoesNotExist ex) {
            Logger.getLogger(InstanceManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.INSTANCE_DELETED, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an PaaSwordRestResponse.
     *
     */
    private final static class Message {

        final static String INSTANCE_DELETED = "Instance has been deleted";
        final static String INSTANCE_UPDATED = "Instance has been updated";
        final static String INSTANCE_CREATED = "Instance has been created";
        final static String AUTOCOMPLETE_LOADED = "Instances loaded successfully!";
    }

    /**
     * Constructs the autocomplete json of returned Instances
     *
     * @param listInstances
     * @return A String object
     *
     */
    public static String constructAutocomplete(List<Instance> listInstances) {

        JSONObject autocompleteObj = new JSONObject();
        JSONArray jsonNodes = new JSONArray();

        listInstances.forEach(node -> {

            JSONObject jsonNode = new JSONObject();

            jsonNode.put("id", node.getId());
            jsonNode.put("value", node.getNamespaceID().getPrefix() + ":" + node.getInstanceName());
            jsonNodes.put(jsonNode);

        });

        autocompleteObj.put("total", listInstances.size());
        autocompleteObj.put("values", jsonNodes);

        return autocompleteObj.toString();

    }
}
