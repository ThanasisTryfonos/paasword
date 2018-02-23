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

import eu.paasword.api.repository.exception.iaasProviderImage.IaaSProviderImageAlreadyExistsException;
import eu.paasword.api.repository.exception.iaasProviderImage.IaaSProviderImageDoesNotExist;
import eu.paasword.api.repository.exception.iaasProviderImage.IaaSProviderImageNameDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface IIaaSProviderImageService<T> {

    /**
     * Fetch a IaaS Provider Image from database given a name.
     * 
     * @param name Find a IaaS Provider Image based on a name
     * @return An instance of IaaSProviderImage object wrapped in an Optional object
     * @throws IaaSProviderImageNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws IaaSProviderImageNameDoesNotExist;

    /**
     * Fetch a IaaS Provider Image from database given an id.
     *
     * @param id The id of the IaaS Provider Image to fetch
     * @return An instance of IaaS Provider Image object wrapped in an Optional object
     * @throws IaaSProviderImageDoesNotExist
     */
    public Optional<T> findOne(long id) throws IaaSProviderImageDoesNotExist;

    /**
     * Delete an IaaS Provider Image from database.
     *
     * @param id The id of the IaaS Provider Image to be deleted
     * @throws IaaSProviderImageDoesNotExist
     */
    public void delete(long id) throws IaaSProviderImageDoesNotExist;

    /**
     * Fetch all IaaS Provider Image from database.
     *
     * @return A list of IaaS Provider Image objects
     */
    public List<T> findAll();

    /**
     * Creates a new IaaS Provider Image to the database.
     *
     * @param t A IaaS Provider Image object
     * @throws IaaSProviderImageAlreadyExistsException
     */
    public void create(T t) throws IaaSProviderImageAlreadyExistsException;
    
    public void edit(T t) throws IaaSProviderImageDoesNotExist;

    public List<T> findIaaSProviderImagesByIaaSProvider(long iaasProviderID);

}
