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

import eu.paasword.api.repository.exception.paasProviderType.PaaSProviderTypeAlreadyExistsException;
import eu.paasword.api.repository.exception.paasProviderType.PaaSProviderTypeDoesNotExist;
import eu.paasword.api.repository.exception.paasProviderType.PaaSProviderTypeNameDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface IPaaSProviderTypeService<T> {

    /**
     * Fetch a PaaS Provider from database given a name.
     * 
     * @param name Find a PaaS Provider based on a name
     * @return An instance of PaaSProvider object wrapped in an Optional object
     * @throws PaaSProviderTypeNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws PaaSProviderTypeNameDoesNotExist;

    /**
     * Fetch a PaaS Provider from database given an id.
     *
     * @param id The id of the PaaS Provider to fetch
     * @return An instance of PaaS Provider object wrapped in an Optional object
     * @throws PaaSProviderTypeDoesNotExist
     */
    public Optional<T> findOne(long id) throws PaaSProviderTypeDoesNotExist;

    /**
     * Delete an PaaS Provider from database.
     *
     * @param id The id of the PaaS Provider to be deleted
     * @throws PaaSProviderTypeDoesNotExist
     */
    public void delete(long id) throws PaaSProviderTypeDoesNotExist;

    /**
     * Fetch all PaaS Provider from database.
     *
     * @return A list of PaaS Provider objects
     */
    public List<T> findAll();

    public List<T> findByOrderByNameAsc();

    /**
     * Creates a new PaaS Provider to the database.
     *
     * @param t A PaaS Provider object
     * @throws PaaSProviderTypeAlreadyExistsException
     */
    public void create(T t) throws PaaSProviderTypeAlreadyExistsException;
    
    public void edit(T t) throws PaaSProviderTypeDoesNotExist;

}
