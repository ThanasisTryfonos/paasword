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

import eu.paasword.api.repository.IApplicationAffinityConstraintService;
import eu.paasword.api.repository.IApplicationService;
import eu.paasword.api.repository.exception.application.ApplicationDoesNotExist;
import eu.paasword.api.repository.exception.applicationAffinityConstraint.ApplicationAffinityConstraintAlreadyExistsException;
import eu.paasword.api.repository.exception.applicationAffinityConstraint.ApplicationAffinityConstraintDoesNotExist;
import eu.paasword.repository.relational.domain.Application;
import eu.paasword.repository.relational.domain.ApplicationAffinityConstraint;
import eu.paasword.rest.repository.transferobject.TAffinityConstraint;
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
@RequestMapping("/api/v1/affinityconstraint")
public class ApplicationAffinityConstraintManagementRestController {

    private static final Logger logger = Logger.getLogger(ApplicationAffinityConstraintManagementRestController.class.getName());

    @Autowired
    private IApplicationAffinityConstraintService<ApplicationAffinityConstraint> applicationAffinityConstraintService;

    @Autowired
    private IApplicationService<Application> applicationService;

    /**
     * Fetch all available affinity constraints from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getAffinityConstraints() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, applicationAffinityConstraintService.findAll());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to fetch an Affinity Constraint with a specific ID from database.
     *
     * @param id The id of the Affinity Constraint to fetch
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getAffinityConstraintByID(@PathVariable("id") long id) {
        try {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, (ApplicationAffinityConstraint) applicationAffinityConstraintService.findOne(id).get());
        } catch (ApplicationAffinityConstraintDoesNotExist ex) {
            Logger.getLogger(ApplicationAffinityConstraintManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new Affinity Constraint to the database.
     *
     * @param tAffinityConstraint A JSON object which will be casted to an Affinity Constraint (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody TAffinityConstraint tAffinityConstraint) {
        try {

            Application app = (Application) applicationService.findOneWithoutBlob(tAffinityConstraint.getApplicationID()).get();

            ApplicationAffinityConstraint affinityConstraint = new ApplicationAffinityConstraint();
            affinityConstraint.setName(tAffinityConstraint.getName());
            affinityConstraint.setApplicationID(app);
            affinityConstraint.setDateCreated(new Date());

            affinityConstraint.setAffinityConstraint(new JSONArray(tAffinityConstraint.getAffinityConstraint()).toString());

            applicationAffinityConstraintService.create(affinityConstraint);

        } catch (ApplicationAffinityConstraintAlreadyExistsException | ApplicationDoesNotExist ex) {
            Logger.getLogger(ApplicationAffinityConstraintManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_AFFINITY_CONSTRAINT_CREATED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to update an existing Affinity Constraint to the database.
     *
     * @param tAffinityConstraint A JSON object which will be casted to a Affinity Constraint (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse edit(@RequestBody TAffinityConstraint tAffinityConstraint) {
        try {

            Application app = (Application) applicationService.findOne(tAffinityConstraint.getApplicationID()).get();

            ApplicationAffinityConstraint affinityConstraint = applicationAffinityConstraintService.findOne(tAffinityConstraint.getId()).get();

            affinityConstraint.setName(tAffinityConstraint.getName());
            affinityConstraint.setAffinityConstraint(new JSONArray(tAffinityConstraint.getAffinityConstraint()).toString());

            applicationAffinityConstraintService.edit(affinityConstraint);


        } catch (ApplicationAffinityConstraintDoesNotExist | ApplicationDoesNotExist ex) {
            Logger.getLogger(ApplicationAffinityConstraintManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_AFFINITY_CONSTRAINT_UPDATED, Optional.empty());
    }

    /**
     * Deletes a Application Affinity Constraint from database.
     *
     * @param id The id of the Application Affinity Constraint to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {
            applicationAffinityConstraintService.delete(id);
        } catch (ApplicationAffinityConstraintDoesNotExist ex) {
            Logger.getLogger(ApplicationAffinityConstraintManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_AFFINITY_CONSTRAINT_KEY_DELETED, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an PaaSwordRestResponse.
     *
     */
    private final static class Message {

        final static String APPLICATION_AFFINITY_CONSTRAINT_KEY_DELETED = "Affinity constraint has been deleted";
        final static String APPLICATION_AFFINITY_CONSTRAINT_UPDATED = "Affinity constraint has been updated";
        final static String APPLICATION_AFFINITY_CONSTRAINT_CREATED = "Affinity constraint has been created";
    }

}
