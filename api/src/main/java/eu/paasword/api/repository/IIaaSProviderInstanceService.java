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

import eu.paasword.api.repository.exception.iaasProviderInstance.IaaSProviderInstanceAlreadyExistsException;
import eu.paasword.api.repository.exception.iaasProviderInstance.IaaSProviderInstanceDoesNotExist;
import eu.paasword.api.repository.exception.iaasProviderInstance.IaaSProviderInstanceNameDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface IIaaSProviderInstanceService<T> {

    /**
     * Fetch a IaaS Provider Instance from database given a name.
     * 
     * @param name Find a IaaS Provider Instance based on a name
     * @return An instance of IaaSProviderInstance object wrapped in an Optional object
     * @throws IaaSProviderInstanceNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws IaaSProviderInstanceNameDoesNotExist;

    /**
     * Fetch a IaaS Provider Instance from database given an id.
     *
     * @param id The id of the IaaS Provider Instance to fetch
     * @return An instance of IaaS Provider Instance object wrapped in an Optional object
     * @throws IaaSProviderInstanceDoesNotExist
     */
    public Optional<T> findOne(long id) throws IaaSProviderInstanceDoesNotExist;

    /**
     * Delete an IaaS Provider Instance from database.
     *
     * @param id The id of the IaaS Provider Instance to be deleted
     * @throws IaaSProviderInstanceDoesNotExist
     */
    public void delete(long id) throws IaaSProviderInstanceDoesNotExist;

    /**
     * Fetch all IaaS Provider Instance from database.
     *
     * @return A list of IaaS Provider Instance objects
     */
    public List<T> findAll();

    /**
     * Creates a new IaaS Provider Instance to the database.
     *
     * @param t A IaaS Provider Instance object
     * @throws IaaSProviderInstanceAlreadyExistsException
     */
    public void create(T t) throws IaaSProviderInstanceAlreadyExistsException;
    
    public void edit(T t) throws IaaSProviderInstanceDoesNotExist;

    public List<T> findIaaSProviderInstancesByIaaSProvider(long iaasProviderID);

}
