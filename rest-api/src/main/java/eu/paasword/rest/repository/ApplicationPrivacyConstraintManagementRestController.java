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

import eu.paasword.api.repository.IApplicationPrivacyConstraintService;
import eu.paasword.api.repository.IApplicationService;
import eu.paasword.api.repository.exception.application.ApplicationDoesNotExist;
import eu.paasword.api.repository.exception.applicationPrivacyConstraint.ApplicationPrivacyConstraintAlreadyExistsException;
import eu.paasword.api.repository.exception.applicationPrivacyConstraint.ApplicationPrivacyConstraintDoesNotExist;
import eu.paasword.repository.relational.domain.Application;
import eu.paasword.repository.relational.domain.ApplicationPrivacyConstraint;
import eu.paasword.rest.repository.transferobject.TPrivacyConstraint;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all the rest endpoints regarding Privacy Constraints
 *
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/privacyconstraint")
public class ApplicationPrivacyConstraintManagementRestController {

    private static final Logger logger = Logger.getLogger(ApplicationPrivacyConstraintManagementRestController.class.getName());

    @Autowired
    private IApplicationPrivacyConstraintService<ApplicationPrivacyConstraint> applicationPrivacyConstraintService;

    @Autowired
    private IApplicationService<Application> applicationService;

    /**
     * Fetch all available privacy constraints from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getPrivacyConstraints() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, applicationPrivacyConstraintService.findAll());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to fetch a Privacy Constraint with a specific ID from database.
     *
     * @param id The id of the Privacy Constraint to fetch
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getPrivacyConstraintByID(@PathVariable("id") long id) {
        try {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, (ApplicationPrivacyConstraint) applicationPrivacyConstraintService.findOne(id).get());
        } catch (ApplicationPrivacyConstraintDoesNotExist ex) {
            Logger.getLogger(ApplicationPrivacyConstraintManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new Privacy Constraint to the database.
     *
     * @param tPrivacyConstraint A JSON object which will be casted to a PrivacyConstraint (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody TPrivacyConstraint tPrivacyConstraint) {
        try {

            Application app = (Application) applicationService.findOneWithoutBlob(tPrivacyConstraint.getApplicationID()).get();

            ApplicationPrivacyConstraint privacyConstraint = new ApplicationPrivacyConstraint();
            privacyConstraint.setName(tPrivacyConstraint.getName());
            privacyConstraint.setApplicationID(app);
            privacyConstraint.setDateCreated(new Date());

            privacyConstraint.setPrivacyConstraint(new JSONArray(tPrivacyConstraint.getPrivacyConstraint()).toString());

            applicationPrivacyConstraintService.create(privacyConstraint);
        } catch (ApplicationPrivacyConstraintAlreadyExistsException | ApplicationDoesNotExist ex) {
            Logger.getLogger(ApplicationPrivacyConstraintManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_PRIVACY_CONSTRAINT_CREATED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to update an existing Privacy Constraint to the database.
     *
     * @param tPrivacyConstraint A JSON object which will be casted to a Privacy Constraint (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse edit(@RequestBody TPrivacyConstraint tPrivacyConstraint) {
        try {

            Application app = (Application) applicationService.findOneWithoutBlob(tPrivacyConstraint.getApplicationID()).get();

            ApplicationPrivacyConstraint privacyConstraint = applicationPrivacyConstraintService.findOne(tPrivacyConstraint.getId()).get();

            privacyConstraint.setName(tPrivacyConstraint.getName());
            privacyConstraint.setPrivacyConstraint(new JSONArray(tPrivacyConstraint.getPrivacyConstraint()).toString());

            applicationPrivacyConstraintService.edit(privacyConstraint);


        } catch (ApplicationPrivacyConstraintDoesNotExist | ApplicationDoesNotExist ex) {
            Logger.getLogger(ApplicationPrivacyConstraintManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_PRIVACY_CONSTRAINT_UPDATED, Optional.empty());
    }

    /**
     * Deletes a Application Privacy Constraint from database.
     *
     * @param id The id of the Application Privacy Constraint to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {
            applicationPrivacyConstraintService.delete(id);
        } catch (ApplicationPrivacyConstraintDoesNotExist ex) {
            Logger.getLogger(ApplicationPrivacyConstraintManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_PRIVACY_CONSTRAINT_KEY_DELETED, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an PaaSwordRestResponse.
     *
     */
    private final static class Message {

        final static String APPLICATION_PRIVACY_CONSTRAINT_KEY_DELETED = "Privacy constraint has been deleted";
        final static String APPLICATION_PRIVACY_CONSTRAINT_UPDATED = "Privacy constraint has been updated";
        final static String APPLICATION_PRIVACY_CONSTRAINT_CREATED = "Privacy constraint has been created";
    }

}
