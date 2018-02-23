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

import eu.paasword.api.repository.exception.application.ApplicationAlreadyExistsException;
import eu.paasword.api.repository.exception.application.ApplicationDoesNotExist;
import eu.paasword.api.repository.exception.application.ApplicationNameDoesNotExist;
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
public interface IApplicationService<T> {

    /**
     * Fetch an application from database given a name.
     * 
     * @param name Find an Application based on a name
     * @return An instance of Application object wrapped in an Optional object
     * @throws ApplicationNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws ApplicationNameDoesNotExist;

    /**
     * Fetch an Application from database given an id.
     *
     * @param id The id of the Application to fetch
     * @return An instance of Application object wrapped in an Optional object
     * @throws ApplicationDoesNotExist
     */
    public Optional<T> findOne(long id) throws ApplicationDoesNotExist;

    /**
     * Delete an Application from database.
     *
     * @param id The id of the Application to be deleted
     * @throws ApplicationDoesNotExist
     */
    public void delete(long id) throws ApplicationDoesNotExist;

    /**
     * Fetch all Applications from database.
     *
     * @return A list of Application objects
     */
    public List<T> findAll();

    /**
     * Creates a new Application to the database.
     *
     * @param t An Application object
     * @throws ApplicationAlreadyExistsException
     */
    public void create(T t) throws ApplicationAlreadyExistsException;
    
    public void edit(T t) throws ApplicationDoesNotExist;

    public Page<T> findAllWithoutBlob(Pageable pageable);

    public Optional<T> findOneWithoutBlob(long id) throws ApplicationDoesNotExist;

}
