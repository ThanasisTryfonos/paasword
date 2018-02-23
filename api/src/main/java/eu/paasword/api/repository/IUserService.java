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
package eu.paasword.api.repository;

import eu.paasword.api.repository.exception.user.InvalidEmailAddressException;
import eu.paasword.api.repository.exception.user.UserDoesNotExist;
import eu.paasword.api.repository.exception.user.UsernameAlreadyExistsException;
import eu.paasword.api.repository.exception.user.UsernameIsTooShortException;
import eu.paasword.api.repository.exception.user.WeakPasswordException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 * @param <T>
 */
@Service
public interface IUserService<T> {

    public int MIN_CHARS_FOR_SPECIAL_FIELDS = 6;

    /**
     * Fetch a user from database given a username.
     * 
     * @param username Find a user based on a username
     * @return An instance of User object wrapped in an Optional object
     */
    public Optional<T> findByUsername(String username);

    /**
     * Fetch a user from database given an id.
     *
     * @param id The id of the user to fetch
     * @return An instance of User object wrapped in an Optional object
     * @throws UserDoesNotExist
     */
    public Optional<T> findOne(long id) throws UserDoesNotExist;

    /**
     * Delete a user from database.
     *
     * @param id The id of the user to be deleted
     * @throws UserDoesNotExist
     */
    public void delete(long id) throws UserDoesNotExist;

    /**
     * Fetch all users from database.
     *
     * @return A list of User objects
     */
    public List<T> findAll();

    public Page<T> findAll(Pageable pageable);

    /**
     * Creates a new user to the database.
     *
     * @param t A User object
     * @throws UsernameAlreadyExistsException
     * @throws UsernameIsTooShortException
     * @throws WeakPasswordException
     * @throws InvalidEmailAddressException
     */
    public void create(T t) throws UsernameAlreadyExistsException, UsernameIsTooShortException, WeakPasswordException, InvalidEmailAddressException;
    
    public void edit(T t) throws UserDoesNotExist, WeakPasswordException, InvalidEmailAddressException;

}
