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

import eu.paasword.api.repository.exception.applicationInstance.ApplicationInstanceAlreadyExistsException;
import eu.paasword.api.repository.exception.applicationInstance.ApplicationInstanceDoesNotExist;
import eu.paasword.api.repository.exception.applicationInstance.ApplicationInstanceNameDoesNotExist;
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
public interface IApplicationInstanceService<T> {

    /**
     * Fetch an application instance from database given a name.
     *
     * @param name Find an Application Instance based on a name
     * @return An instance of Application Instance object wrapped in an Optional
     * object
     * @throws ApplicationInstanceNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws ApplicationInstanceNameDoesNotExist;

    /**
     * Fetch an Application Instance from database given an id.
     *
     * @param id The id of the Application Instance to fetch
     * @return An instance of Application Instance object wrapped in an Optional
     * object
     * @throws ApplicationInstanceDoesNotExist
     */
    public Optional<T> findOne(long id) throws ApplicationInstanceDoesNotExist;

    /**
     * Delete an Application Instance from database.
     *
     * @param id The id of the Application Instance to be deleted
     * @throws ApplicationInstanceDoesNotExist
     */
    public void delete(long id) throws ApplicationInstanceDoesNotExist;

    /**
     * Fetch all Application Instance from database.
     *
     * @return A list of Application Instances objects
     */
    public List<T> findAll();

    /**
     * Creates a new Application Instance to the database.
     *
     * @param t An Application Instance object
     * @throws ApplicationInstanceAlreadyExistsException
     */
    public void create(T t) throws ApplicationInstanceAlreadyExistsException;

    public void edit(T t) throws ApplicationInstanceDoesNotExist;

    public T findByValidator(String validator);

    public T findByApplicationID(long applicationID);

    public T findByApplicationIDWithPaaS(long applicationID);

    public T findOneWithoutApplication(long applicationInstanceID);

    public T findOneWithPaaSProviderWithoutApplication(long applicationInstanceID);

//    public T findOneOnlyWithApplicationID(long applicationInstanceID);

//    public T findOneWithPaaSProviderAndOnlyWithApplicationID(long applicationInstanceID);

    public T findByUniqueID(String uniqueID);

}
