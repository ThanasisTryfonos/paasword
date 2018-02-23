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

import eu.paasword.api.repository.IUserService;
import eu.paasword.api.repository.exception.user.InvalidEmailAddressException;
import eu.paasword.api.repository.exception.user.UserDoesNotExist;
import eu.paasword.api.repository.exception.user.UsernameAlreadyExistsException;
import eu.paasword.api.repository.exception.user.UsernameIsTooShortException;
import eu.paasword.api.repository.exception.user.WeakPasswordException;
import eu.paasword.repository.relational.dao.UserRoleRepository;
import eu.paasword.repository.relational.domain.User;
import eu.paasword.repository.relational.domain.UserRole;
import eu.paasword.rest.response.PaaSwordRestResponse;
import eu.paasword.rest.response.BasicResponseCode;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contains all the rest endpoints regarding with authentication actions.
 *
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserManagementRestController {

    @Autowired
    IUserService<User> userService;

    @Autowired
    UserRoleRepository userRoleRepository;
    
    /**
     * Fetch all available users from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getUsers() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, userService.findAll());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to fetch a user with a specific ID from database.
     *
     * @param id The id of the user to fetch
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getUserByID(@PathVariable("id") long id) {
        try {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, userService.findOne(id));
        } catch (UserDoesNotExist ex) {
            Logger.getLogger(UserManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new user to the database.
     *
     * @param user A JSON object which will be casted to a User (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody User user) {
        try {

            userService.create(user);

        } catch (UsernameAlreadyExistsException | UsernameIsTooShortException | WeakPasswordException | InvalidEmailAddressException ex) {
            Logger.getLogger(UserManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.USER_CREATED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new user to the database.
     *
     * @param user A JSON object which will be casted to a User (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse edit(@RequestBody User user) {
        try {
            userService.edit(user);
        } catch (UserDoesNotExist | WeakPasswordException | InvalidEmailAddressException ex) {
            Logger.getLogger(UserManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.USER_UPDATED, Optional.empty());
    }

    /**
     * Deletes a user from database.
     *
     * @param id The id of the user to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {
            userService.delete(id);
        } catch (UserDoesNotExist ex) {
            Logger.getLogger(UserManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.USER_DELETED, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an PaaSwordRestResponse.
     *
     */
    private final static class Message {

        final static String USER_REGISTERED = "User has been registered";
        final static String USER_DELETED = "User has been deleted";
        final static String USER_UPDATED = "User has been updated";
        final static String USER_CREATED = "User has been created";
    }
}
