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

import eu.paasword.api.repository.IProxyCloudProviderService;
import eu.paasword.api.repository.IUserCredentialService;
import eu.paasword.api.repository.IUserService;
import eu.paasword.api.repository.exception.proxyCloudProvider.ProxyCloudProviderDoesNotExist;
import eu.paasword.api.repository.exception.user.*;
import eu.paasword.api.repository.exception.userCredential.UserCredentialDoesNotExist;
import eu.paasword.repository.relational.domain.ProxyCloudProvider;
import eu.paasword.repository.relational.domain.User;
import eu.paasword.repository.relational.domain.UserCredential;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import eu.paasword.spi.adapter.ProxyAdapter;
import eu.paasword.spi.model.CloudProviderModel;
import eu.paasword.spi.model.CredentialsModel;
import eu.paasword.spi.model.PaaSOfferingModel;
import eu.paasword.spi.response.SPIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Contains all the rest endpoints regarding with user credentials.
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/proxycloudprovider")
public class UserCredentialManagementRestController {

    private static final Logger logger = Logger.getLogger(UserCredentialManagementRestController.class.getName());

    @Autowired
    IUserCredentialService<UserCredential, ProxyCloudProvider, User> userCredentialService;

    @Autowired
    IProxyCloudProviderService<ProxyCloudProvider, User> proxyCloudProviderService;

    @Resource(name = "proxyAdaptersList")
    List proxyAdapters;

    /**
     * Fetch all available user credentials from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getUserCredentials() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, userCredentialService.findAll());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to fetch user credential with a specific ID from database.
     *
     * @param id The id of the user to fetch
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getUserCredentialByID(@PathVariable("id") long id) {
        try {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, userCredentialService.findOne(id));
        } catch (UserCredentialDoesNotExist ex) {
            Logger.getLogger(UserCredentialManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create new credentials for a user to the database.
     *
     * @param userCredential A JSON object which will be casted to a UserCredential (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody UserCredential userCredential) {

        try {
            ProxyCloudProvider proxyCloudProvider = (ProxyCloudProvider) proxyCloudProviderService.findOne(userCredential.getProxyCloudProvider().getId()).get();

            ProxyAdapter proxyAdapter = (ProxyAdapter) ((List) proxyAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(proxyCloudProvider.getAdapterImplementation())).collect(Collectors.toList())).get(0);

            CredentialsModel credentialsModel = new CredentialsModel();

            PaaSOfferingModel paaSOfferingModelSPI = new PaaSOfferingModel();
            paaSOfferingModelSPI.setEndpointURI(proxyCloudProvider.getConnectionURL());
            credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

            credentialsModel.setUsername(userCredential.getUsername());
            credentialsModel.setPassword(userCredential.getPassword());
            credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

            SPIResponse spiResponse = proxyAdapter.validateCredentials(credentialsModel);

            if (null != spiResponse && spiResponse.getCode().equals(eu.paasword.spi.response.BasicResponseCode.SUCCESS)) {

                userCredential.setDateCreated(new Date());

                userCredentialService.create(userCredential);

                return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.USER_CREDENTIALS_CREATED, Optional.empty());

            } else {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.USER_CREDENTIALS_INVALID, Optional.empty());
            }

        } catch (Exception e) {
            Logger.getLogger(UserCredentialManagementRestController.class.getName()).severe(e.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.USER_CREDENTIALS_INVALID, Optional.empty());
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to edit user credentials to the database.
     *
     * @param userCredential A JSON object which will be casted to a UserCredential (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse edit(@RequestBody UserCredential userCredential) {
        try {
            userCredentialService.edit(userCredential);
        } catch (UserCredentialDoesNotExist ex) {
            Logger.getLogger(UserCredentialManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.USER_CREDENTIALS_UPDATED, Optional.empty());
    }

    /**
     * Deletes user credentials from database.
     *
     * @param id The id of the user credentials to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {
            userCredentialService.delete(id);
        } catch (UserCredentialDoesNotExist ex) {
            Logger.getLogger(UserCredentialManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.USER_CREDENTIALS_DELETED, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an PaaSwordRestResponse.
     */
    private final static class Message {

        final static String USER_CREDENTIALS_DELETED = "User has been deauthorized successfully";
        final static String USER_CREDENTIALS_UPDATED = "User has been updated";
        final static String USER_CREDENTIALS_INVALID = "User cannot be authorized";
        final static String USER_CREDENTIALS_CREATED = "User has been authorized successfully";
    }
}
