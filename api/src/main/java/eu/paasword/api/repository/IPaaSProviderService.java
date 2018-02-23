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

import eu.paasword.api.repository.exception.paasProvider.PaaSProviderAlreadyExistsException;
import eu.paasword.api.repository.exception.paasProvider.PaaSProviderDoesNotExist;
import eu.paasword.api.repository.exception.paasProvider.PaaSProviderNameDoesNotExist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface IPaaSProviderService<T, U> {

    /**
     * Fetch a PaaS Provider from database given a name.
     * 
     * @param name Find a PaaS Provider based on a name
     * @return An instance of PaaSProvider object wrapped in an Optional object
     * @throws PaaSProviderNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws PaaSProviderNameDoesNotExist;

    /**
     * Fetch a PaaS Provider from database given an id.
     *
     * @param id The id of the PaaS Provider to fetch
     * @return An instance of PaaS Provider object wrapped in an Optional object
     * @throws PaaSProviderDoesNotExist
     */
    public Optional<T> findOne(long id) throws PaaSProviderDoesNotExist;

    /**
     * Delete an PaaS Provider from database.
     *
     * @param id The id of the PaaS Provider to be deleted
     * @throws PaaSProviderDoesNotExist
     */
    public void delete(long id) throws PaaSProviderDoesNotExist;

    /**
     * Fetch all PaaS Provider from database.
     *
     * @return A list of PaaS Provider objects
     */
    public List<T> findAll();

    /**
     * Creates a new PaaS Provider to the database.
     *
     * @param t A PaaS Provider object
     * @throws PaaSProviderAlreadyExistsException
     */
    public void create(T t) throws PaaSProviderAlreadyExistsException;
    
    public void edit(T t) throws PaaSProviderDoesNotExist;

    public List<T> findPaaSProvidersByUsername(String username);

    public Page<T> findByUserID(U u, Pageable pageable);

}
