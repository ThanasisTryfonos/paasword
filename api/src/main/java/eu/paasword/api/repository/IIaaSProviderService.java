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

import eu.paasword.api.repository.exception.iaasProvider.IaaSProviderAlreadyExistsException;
import eu.paasword.api.repository.exception.iaasProvider.IaaSProviderDoesNotExist;
import eu.paasword.api.repository.exception.iaasProvider.IaaSProviderNameDoesNotExist;
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
public interface IIaaSProviderService<T, U> {

    /**
     * Fetch a IaaS Provider from database given a name.
     * 
     * @param name Find a IaaS Provider based on a name
     * @return An instance of IaaSProvider object wrapped in an Optional object
     * @throws IaaSProviderNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws IaaSProviderNameDoesNotExist;

    /**
     * Fetch a IaaS Provider from database given an id.
     *
     * @param id The id of the IaaS Provider to fetch
     * @return An instance of IaaS Provider object wrapped in an Optional object
     * @throws IaaSProviderDoesNotExist
     */
    public Optional<T> findOne(long id) throws IaaSProviderDoesNotExist;

    /**
     * Delete an IaaS Provider from database.
     *
     * @param id The id of the IaaS Provider to be deleted
     * @throws IaaSProviderDoesNotExist
     */
    public void delete(long id) throws IaaSProviderDoesNotExist;

    /**
     * Fetch all IaaS Provider from database.
     *
     * @return A list of IaaS Provider objects
     */
    public List<T> findAll();

    /**
     * Creates a new IaaS Provider to the database.
     *
     * @param t A IaaS Provider object
     * @throws IaaSProviderAlreadyExistsException
     */
    public void create(T t) throws IaaSProviderAlreadyExistsException;
    
    public void edit(T t) throws IaaSProviderDoesNotExist;

    public List<T> findIaaSProvidersByUsername(String username);

    public Page<T> findByUserID(U u, Pageable pageable);

}
