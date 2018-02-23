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

import eu.paasword.api.repository.exception.systemProperty.SystemPropertyAlreadyExistsException;
import eu.paasword.api.repository.exception.systemProperty.SystemPropertyDoesNotExist;
import eu.paasword.api.repository.exception.systemProperty.SystemPropertyNameDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface ISystemPropertyService<T> {

    /**
     * Fetch a system property from database given a name.
     * 
     * @param name Find a system property based on a name
     * @return An instance of SystemProperty object wrapped in an Optional object
     * @throws SystemPropertyNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws SystemPropertyNameDoesNotExist;

    /**
     * Fetch a system property from database given an id.
     *
     * @param id The id of the system property to fetch
     * @return An instance of SystemProperty object wrapped in an Optional object
     * @throws SystemPropertyDoesNotExist
     */
    public Optional<T> findOne(long id) throws SystemPropertyDoesNotExist;

    /**
     * Delete a system property from database.
     *
     * @param id The id of the system property to be deleted
     * @throws SystemPropertyDoesNotExist
     */
    public void delete(long id) throws SystemPropertyDoesNotExist;

    /**
     * Fetch all system properties from database.
     *
     * @return A list of SystemProperty objects
     */
    public List<T> findAll();

    /**
     * Creates a new system property to the database.
     *
     * @param t A SystemProperty object
     * @throws SystemPropertyAlreadyExistsException
     */
    public void create(T t) throws SystemPropertyAlreadyExistsException;
    
    public void edit(T t) throws SystemPropertyDoesNotExist;

}
