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

import eu.paasword.api.repository.exception.instance.InstanceAlreadyExistsException;
import eu.paasword.api.repository.exception.instance.InstanceDoesNotExist;
import eu.paasword.api.repository.exception.instance.InstanceNameDoesNotExist;
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
public interface IInstanceService<T, C> {

    /**
     * Fetch an instance from database given a instanceName.
     * 
     * @param instanceName Find a instance based on a instanceName
     * @return An instance of instance object wrapped in an Optional object
     * @throws InstanceNameDoesNotExist
     */
    public Optional<T> findByInstanceName(String instanceName) throws InstanceNameDoesNotExist;

    /**
     * Fetch an instance from database given an id.
     *
     * @param id The id of the instance to fetch
     * @return An instance of instance object wrapped in an Optional object
     * @throws InstanceDoesNotExist
     */
    public Optional<T> findOne(long id) throws InstanceDoesNotExist;

    /**
     * Delete an instance from database.
     *
     * @param id The id of the instance to be deleted
     * @throws InstanceDoesNotExist
     */
    public void delete(long id) throws InstanceDoesNotExist;

    /**
     * Fetch all instances from database.
     *
     * @return A list of Instance objects
     */
    public List<T> findAll();

    public List<T> findByOrderByInstanceName();

    public Page<T> findByClassID(C c, Pageable pageable);

    /**
     * Fetch all instances from database.
     *
     * @return An Instance object
     */
    public T findByInstanceNameAndClassID(String instanceName, long id);

    /**
     * Creates a new instance to the database.
     *
     * @param t An Instance object
     * @throws InstanceAlreadyExistsException
     */
    public void create(T t) throws InstanceAlreadyExistsException;
    
    public void edit(T t) throws InstanceDoesNotExist;

    /**
     * Fetch all instances from database.
     *
     * @return A list of Instance objects
     */
    public List<T> getInstancesByKeyword(String keyword, long clazzID);

    /**
     * Fetch all instances from database.
     *
     * @return A list of Instance objects
     */
    public List<T> getInstancesByKeyword(String keyword);

}
