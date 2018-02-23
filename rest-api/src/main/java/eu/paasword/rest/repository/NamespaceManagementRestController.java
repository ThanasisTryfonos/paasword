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

import eu.paasword.api.repository.INamespaceService;
import eu.paasword.api.repository.exception.namespace.NamespaceAlreadyExistsException;
import eu.paasword.api.repository.exception.namespace.NamespaceDoesNotExist;
import eu.paasword.repository.relational.domain.Namespace;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all the rest endpoints regarding Namespaces
 *
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/namespace")
public class NamespaceManagementRestController {

    private static final Logger logger = Logger.getLogger(NamespaceManagementRestController.class.getName());

    @Autowired
    private INamespaceService<Namespace> namespaceService;

    /**
     * Fetch all available Namespaces from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getNamespaces() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, namespaceService.findAll());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to fetch a Namespace with a specific ID from database.
     *
     * @param id The id of the Namespace to fetch
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getNamespaceByID(@PathVariable("id") long id) {
        try {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, (Namespace) namespaceService.findOne(id).get());
        } catch (NamespaceDoesNotExist ex) {
            Logger.getLogger(NamespaceManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new Namespace to the database.
     *
     * @param namespace A JSON object which will be casted to a Namespace (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody Namespace namespace) {
        try {

            namespaceService.create(namespace);
        } catch (Exception ex) {
            Logger.getLogger(NamespaceManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.NAMESPACE_CREATED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to update an existing Namespace to the database.
     *
     * @param namespace A JSON object which will be casted to a Namespace (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse edit(@RequestBody Namespace namespace) {
        try {
            namespaceService.edit(namespace);
        } catch (Exception ex) {
            Logger.getLogger(NamespaceManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.NAMESPACE_UPDATED, Optional.empty());
    }

    /**
     * Deletes a Namespace from database.
     *
     * @param id The id of the Namespace to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {
            namespaceService.delete(id);
        } catch (NamespaceDoesNotExist ex) {
            Logger.getLogger(NamespaceManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.NAMESPACE_DELETED, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an PaaSwordRestResponse.
     *
     */
    private final static class Message {

        final static String NAMESPACE_DELETED = "Namespace has been deleted";
        final static String NAMESPACE_UPDATED = "Namespace has been updated";
        final static String NAMESPACE_CREATED = "Namespace has been created";
    }

}
