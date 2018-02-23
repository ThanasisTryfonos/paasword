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

import eu.paasword.api.repository.exception.apikey.APIKeyAlreadyExistsException;
import eu.paasword.api.repository.exception.apikey.APIKeyDescriptionDoesNotExist;
import eu.paasword.api.repository.exception.apikey.APIKeyDoesNotExist;
import eu.paasword.api.repository.exception.apikey.APIKeyUniqueIDDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface IAPIKeyService<T> {

    /**
     * Fetch an API Key from database given a description.
     * 
     * @param description Find a handler based on a description
     * @return An instance of API Key object wrapped in an Optional object
     * @throws APIKeyDescriptionDoesNotExist
     */
    public Optional<T> findByDescription(String description) throws APIKeyDescriptionDoesNotExist;

    /**
     * Fetch a API Key from database given a uniqueID.
     *
     * @param uniqueID Find an API Key based on a uniqueID
     * @return An instance of API Key object wrapped in an Optional object
     * @throws APIKeyUniqueIDDoesNotExist
     */
    public Optional<T> findByUniqueID(String uniqueID) throws APIKeyUniqueIDDoesNotExist;

    /**
     * Fetch an API Key from database given an id.
     *
     * @param id The id of the API Key to fetch
     * @return An instance of API Key object wrapped in an Optional object
     * @throws APIKeyDoesNotExist
     */
    public Optional<T> findOne(long id) throws APIKeyDoesNotExist;

    /**
     * Delete an API Key from database.
     *
     * @param id The id of the API Key to be deleted
     * @throws APIKeyDoesNotExist
     */
    public void delete(long id) throws APIKeyDoesNotExist;

    /**
     * Fetch all API Keys from database.
     *
     * @return A list of API Key objects
     */
    public List<T> findAll();

    /**
     * Creates a new API Key to the database.
     *
     * @param t A API Key object
     * @throws APIKeyAlreadyExistsException
     */
    public void create(T t) throws APIKeyAlreadyExistsException;
    
    public void edit(T t) throws APIKeyDoesNotExist;

    public List<T> findByApplicationID(long applicationID);

}
