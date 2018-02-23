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

import eu.paasword.api.repository.exception.iaasProviderType.IaaSProviderTypeAlreadyExistsException;
import eu.paasword.api.repository.exception.iaasProviderType.IaaSProviderTypeDoesNotExist;
import eu.paasword.api.repository.exception.iaasProviderType.IaaSProviderTypeNameDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface IIaaSProviderTypeService<T> {

    /**
     * Fetch a IaaS Provider from database given a name.
     * 
     * @param name Find a IaaS Provider based on a name
     * @return An instance of IaaSProvider object wrapped in an Optional object
     * @throws IaaSProviderTypeNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws IaaSProviderTypeNameDoesNotExist;

    /**
     * Fetch a IaaS Provider from database given an id.
     *
     * @param id The id of the IaaS Provider to fetch
     * @return An instance of IaaS Provider object wrapped in an Optional object
     * @throws IaaSProviderTypeDoesNotExist
     */
    public Optional<T> findOne(long id) throws IaaSProviderTypeDoesNotExist;

    /**
     * Delete an IaaS Provider from database.
     *
     * @param id The id of the IaaS Provider to be deleted
     * @throws IaaSProviderTypeDoesNotExist
     */
    public void delete(long id) throws IaaSProviderTypeDoesNotExist;

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
     * @throws IaaSProviderTypeAlreadyExistsException
     */
    public void create(T t) throws IaaSProviderTypeAlreadyExistsException;
    
    public void edit(T t) throws IaaSProviderTypeDoesNotExist;

}
