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
import eu.paasword.repository.relational.dao.ApplicationInstanceUserRepository;
import eu.paasword.repository.relational.domain.*;
import eu.paasword.rest.bounce.BounceService;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all the rest endpoints regarding Application Instances Users
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/applicationinstanceuser")
public class ApplicationInstanceUserManagementRestController {

    private static final Logger logger = Logger.getLogger(ApplicationInstanceUserManagementRestController.class.getName());

    @Autowired
    ApplicationInstanceUserRepository applicationInstanceUserRepository;

    @Autowired
    IAPIKeyService<APIKey> apiKeyService;

    @Autowired
    BounceService bounceService;

    /**
     * The exposed endpoint is used via the UI, which attempts to authorize a new application instance user to the database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public PaaSwordRestResponse authorizeUser(@RequestBody ApplicationInstanceUser applicationInstanceUser) {

        try {

            if (null != applicationInstanceUser && null != applicationInstanceUser.getPrincipal() && !applicationInstanceUser.getPrincipal().isEmpty()
                    && null != applicationInstanceUser.getApplicationInstanceKey() && !applicationInstanceUser.getApplicationInstanceKey().isEmpty()) {

                // Check if principal already exists for this application
                Application application = ((APIKey) apiKeyService.findByUniqueID(applicationInstanceUser.getApplicationInstanceKey()).get()).getApplicationID();

                if (null != application && application.isDataModel()) {

                    if (applicationInstanceUserRepository.findByPrincipalAndApplicationInstanceKey(applicationInstanceUser.getPrincipal(), applicationInstanceUser.getApplicationInstanceKey()).isPresent()) {
                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_USER_ALREADY_EXISTS, Optional.empty());
                    }

                    applicationInstanceUser.setApplicationInstanceID(application.getApplicationInstance());
                    applicationInstanceUser.setDateCreated(new Date());

                    // TODO XOR in order to create user key
                    applicationInstanceUser.setUserKey("testkey");

                    applicationInstanceUserRepository.save(applicationInstanceUser);

                    // Send Email with User key
                    bounceService.sendEmailForUserKey(applicationInstanceUser);

                    return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_USER_AUTHORIZED, Optional.empty());

                }

            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            ex.printStackTrace();
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_USER_AUTHORIZED_ERROR, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to revoke an existing user from the database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse revokeUser(@PathVariable(value = "id") long id) {

        try {

            if (0 != id) {

                ApplicationInstanceUser applicationInstanceUser = applicationInstanceUserRepository.findOne(id);

                if (null != applicationInstanceUser) {

                    applicationInstanceUserRepository.delete(id);

                    return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_USER_REVOKED, Optional.empty());

                }

            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            ex.printStackTrace();
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_USER_ERROR, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an PaaSwordRestResponse.
     */
    private final static class Message {

        final static String APPLICATION_INSTANCE_USER_AUTHORIZED = "Application user has been authorized successfully";
        final static String APPLICATION_INSTANCE_USER_REVOKED = "Application user has been de-authorized successfully";
        final static String APPLICATION_INSTANCE_USER_ALREADY_EXISTS = "This principal has already been authorized";
        final static String APPLICATION_INSTANCE_USER_AUTHORIZED_ERROR = "Application user has not been authorized successfully";
        final static String APPLICATION_INSTANCE_USER_ERROR = "Generic Error. Please try again.";
    }

}
