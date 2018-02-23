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

import eu.paasword.api.repository.exception.userCredential.UserCredentialAlreadyExistsException;
import eu.paasword.api.repository.exception.userCredential.UserCredentialDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <UC, P, U>
 */
@Service
public interface IUserCredentialService<UC, P, U> {

    /**
     * Fetches a User Credentials from database.
     *
     * @param proxyCloudProvider Find an User Credential based on a proxyCloudProvider
     * @return An instance of UserCredentials object wrapped in an Optional object
     */
    public List<UC> findByProxyCloudProvider(P proxyCloudProvider);

    public UC findByProxyCloudProviderAndUser(Long proxyCloudProviderID, Long userID);

    public List<UC> findByUser(U user);

    /**
     * Fetches an User Credentials from database given an id.
     *
     * @param id The id of the UserCredentials  to fetch
     * @return An instance of UserCredentials  object wrapped in an Optional object
     * @throws UserCredentialDoesNotExist
     */
    public Optional<UC> findOne(long id) throws UserCredentialDoesNotExist;

    /**
     * Deletes an User Credentials from database.
     *
     * @param id The id of the User Credentials to be deleted
     * @throws UserCredentialDoesNotExist
     */
    public void delete(long id) throws UserCredentialDoesNotExist;

    /**
     * Fetch all User Credentials from database.
     *
     * @return A list of User Credentials objects
     */
    public List<UC> findAll();

    /**
     * Creates a new User Credential to the database.
     *
     * @param uc A User Credential object
     */
    public void create(UC uc);
    
    public void edit(UC uc) throws UserCredentialDoesNotExist;


}
