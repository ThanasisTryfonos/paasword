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
import eu.paasword.api.repository.exception.handler.HandlerDoesNotExist;
import eu.paasword.api.repository.exception.namespace.NamespaceDoesNotExist;
import eu.paasword.api.repository.exception.property.PropertyAlreadyExistsException;

import eu.paasword.api.repository.exception.property.PropertyDoesNotExist;
import eu.paasword.api.repository.exception.propertyType.PropertyTypeDoesNotExist;
import eu.paasword.repository.relational.domain.*;
import eu.paasword.repository.relational.util.RepositoryUtil;
import eu.paasword.rest.repository.transferobject.TProperty;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all the rest endpoints regarding properties
 *
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/property")
public class PropertyManagementRestController {

    private static final Logger logger = Logger.getLogger(PropertyManagementRestController.class.getName());

    @Autowired
    IPropertyService<Property, Clazz> propertyService;

    @Autowired
    INamespaceService<Namespace> namespaceService;

    @Autowired
    IClazzService<Clazz> clazzService;

    @Autowired
    IPropertyTypeService<PropertyType> propertyTypeService;

    @Autowired
    IHandlerService<Handler> handlerService;

    /**
     * Fetch all available properties from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getProperties() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, propertyService.findAll());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to fetch a property with a specific ID from database.
     *
     * @param id The id of the property to fetch
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getPropertyByID(@PathVariable("id") long id) {
        try {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, propertyService.findOne(id));
        } catch (PropertyDoesNotExist ex) {
            Logger.getLogger(PropertyManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * Fetch all available classes from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/autocomplete", method = RequestMethod.GET)
    public PaaSwordRestResponse getPropertiesAutocomplete(@RequestParam String keyword, @RequestParam long classID) {

        try {
            Clazz clazz = (Clazz) clazzService.findOne(classID).get();

            List<Property> listOfProperties = clazz.getAllProperties();

            List<Long> ids = new ArrayList<>();

            listOfProperties.stream().forEach(property -> {

                if (!ids.contains(property.getId())) {
                    ids.add(property.getId());
                }
            });

            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.AUTOCOMPLETE_LOADED, constructAutocomplete(propertyService.getPropertiesByKeyword(keyword, ids)));


        } catch (ClazzDoesNotExist e) {
            Logger.getLogger(PropertyManagementRestController.class.getName()).severe(e.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, e.getMessage(), Optional.empty());
        }

    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new property to the database.
     *
     * @param tProperty A JSON object which will be casted to a Property (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody TProperty tProperty) {

        try {

            Property newProperty = new Property();
            newProperty.setName(tProperty.getName());
            newProperty.setNamespaceID((Namespace) namespaceService.findOne(tProperty.getNamespaceID()).get());
            newProperty.setClassID((Clazz) clazzService.findOne(tProperty.getClassID()).get());
            newProperty.setObjectProperty(tProperty.isObjectProperty());
            if (newProperty.isObjectProperty()) {
                newProperty.setTransitivity(tProperty.getTransitivity());
                newProperty.setObjectPropertyClassID((Clazz) clazzService.findOne(tProperty.getObjectPropertyClassID()).get());
            } else {
                newProperty.setTransitivity(0);
                newProperty.setPropertyTypeID((PropertyType) propertyTypeService.findOne(tProperty.getPropertyTypeID()).get());
            }

            if (0L != tProperty.getSubPropertyOfID()) {

                newProperty.setSubPropertyOfID((Property) propertyService.findOne(tProperty.getSubPropertyOfID()).get());

            }

            propertyService.create(newProperty);
        } catch (PropertyAlreadyExistsException | NamespaceDoesNotExist | ClazzDoesNotExist | PropertyTypeDoesNotExist | PropertyDoesNotExist ex) {
            Logger.getLogger(PropertyManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.PROPERTY_CREATED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to update an existing property to the database.
     *
     * @param tProperty A JSON object which will be casted to an Property (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse edit(@RequestBody TProperty tProperty) {
        try {

            Property property = (Property) propertyService.findOne(tProperty.getId()).get();
            property.setName(tProperty.getName());
            property.setNamespaceID((Namespace) namespaceService.findOne(tProperty.getNamespaceID()).get());
            property.setClassID((Clazz) clazzService.findOne(tProperty.getClassID()).get());
            property.setObjectProperty(tProperty.isObjectProperty());
            if (property.isObjectProperty()) {
                property.setTransitivity(tProperty.getTransitivity());
                property.setObjectPropertyClassID((Clazz) clazzService.findOne(tProperty.getObjectPropertyClassID()).get());
            } else {
                property.setTransitivity(0);
                property.setPropertyTypeID((PropertyType) propertyTypeService.findOne(tProperty.getPropertyTypeID()).get());
            }

            if (0L != tProperty.getSubPropertyOfID()) {

                property.setSubPropertyOfID((Property) propertyService.findOne(tProperty.getSubPropertyOfID()).get());

            } else {
                property.setSubPropertyOfID(null);
            }

            propertyService.edit(property);

        } catch (PropertyDoesNotExist | NamespaceDoesNotExist | ClazzDoesNotExist |  PropertyTypeDoesNotExist ex) {
            Logger.getLogger(PropertyManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }


        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.PROPERTY_UPDATED, Optional.empty());
    }

    /**
     * Deletes a property from database.
     *
     * @param id The id of the property to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {
            propertyService.delete(id);
        } catch (PropertyDoesNotExist ex) {
            Logger.getLogger(PropertyManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.PROPERTY_DELETED, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an PaaSwordRestResponse.
     *
     */
    private final static class Message {

        final static String PROPERTY_DELETED = "Property has been deleted";
        final static String PROPERTY_UPDATED = "Property has been updated";
        final static String PROPERTY_CREATED = "Property has been created";
        final static String AUTOCOMPLETE_LOADED = "Properties loaded successfully!";
    }

    /**
     * Constructs the autocomplete json of returned Properties
     *
     * @param listProperty
     * @return A String object
     *
     */
    public static String constructAutocomplete(List<Property> listProperty) {

        JSONObject autocompleteObj = new JSONObject();
        JSONArray jsonNodes = new JSONArray();

        listProperty.forEach(node -> {

            JSONObject jsonNode = new JSONObject();

            jsonNode.put("id", node.getId());
            jsonNode.put("value", node.getName());
            jsonNode.put("isObjectProperty", node.isObjectProperty());
            if (node.isObjectProperty()) {
                jsonNode.put("objectPropertyClassID", node.getObjectPropertyClassID().getId());
            } else {
                jsonNode.put("objectPropertyClassID", -1);
            }
            jsonNodes.put(jsonNode);

        });

        autocompleteObj.put("total", listProperty.size());
        autocompleteObj.put("values", jsonNodes);

        return autocompleteObj.toString();

    }

}
