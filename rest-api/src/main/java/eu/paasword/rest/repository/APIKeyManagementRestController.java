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

import eu.paasword.api.repository.IAPIKeyService;
import eu.paasword.api.repository.IApplicationService;
import eu.paasword.api.repository.exception.apikey.APIKeyAlreadyExistsException;
import eu.paasword.api.repository.exception.apikey.APIKeyDoesNotExist;
import eu.paasword.api.repository.exception.application.ApplicationDoesNotExist;
import eu.paasword.repository.relational.domain.APIKey;
import eu.paasword.repository.relational.domain.Application;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all the rest endpoints regarding API keys
 *
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/apikey")
public class APIKeyManagementRestController {

    private static final Logger logger = Logger.getLogger(APIKeyManagementRestController.class.getName());

    @Autowired
    private IAPIKeyService<APIKey> apiKeyService;

    @Autowired
    private IApplicationService<Application> applicationService;

    /**
     * Fetch all available API keys from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getAPIKeys() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, apiKeyService.findAll());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to fetch an API key with a specific ID from database.
     *
     * @param id The id of the API Key to fetch
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getAPIKeyByID(@PathVariable("id") long id) {
        try {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, (APIKey) apiKeyService.findOne(id).get());
        } catch (APIKeyDoesNotExist ex) {
            Logger.getLogger(APIKeyManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new API Key to the database.
     *
     * @param apiKey A JSON object which will be casted to a APIKey (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody APIKey apiKey) {
        try {

            Application app = (Application) applicationService.findOne(apiKey.getApplicationID().getId()).get();

            apiKey.setDescription(app.getName() + "_" + app.getVersion());

            apiKeyService.create(apiKey);
        } catch (APIKeyAlreadyExistsException | ApplicationDoesNotExist ex) {
            Logger.getLogger(APIKeyManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.API_KEY_CREATED, apiKey.getUniqueID());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to update an existing API Key to the database.
     *
     * @param apiKey A JSON object which will be casted to a Application (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse edit(@RequestBody APIKey apiKey) {
        try {
            apiKeyService.edit(apiKey);
        } catch (APIKeyDoesNotExist ex) {
            Logger.getLogger(APIKeyManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.API_KEY_UPDATED, Optional.empty());
    }

    /**
     * Deletes a API Key from database.
     *
     * @param id The id of the API Key to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {
            apiKeyService.delete(id);
        } catch (APIKeyDoesNotExist ex) {
            Logger.getLogger(APIKeyManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.API_KEY_DELETED, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an PaaSwordRestResponse.
     *
     */
    private final static class Message {

        final static String API_KEY_DELETED = "API Key has been deleted";
        final static String API_KEY_UPDATED = "API Key has been updated";
        final static String API_KEY_CREATED = "API Key has been created";
    }

}
