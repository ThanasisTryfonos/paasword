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

import eu.paasword.api.repository.exception.applicationAffinityConstraint.ApplicationAffinityConstraintAlreadyExistsException;
import eu.paasword.api.repository.exception.applicationAffinityConstraint.ApplicationAffinityConstraintDoesNotExist;
import eu.paasword.api.repository.exception.applicationAffinityConstraint.ApplicationAffinityConstraintNameDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface IApplicationAffinityConstraintService<T> {

    /**
     * Fetch an application affinity constraint from database given a name.
     * 
     * @param name Find an Application Affinity Constraint based on a name
     * @return An instance of ApplicationAffinityConstraint object wrapped in an Optional object
     * @throws ApplicationAffinityConstraintNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws ApplicationAffinityConstraintNameDoesNotExist;

    /**
     * Fetch an Application Affinity Constraint from database given an id.
     *
     * @param id The id of the Application Affinity constraint to fetch
     * @return An instance of Application Affinity constraint object wrapped in an Optional object
     * @throws ApplicationAffinityConstraintDoesNotExist
     */
    public Optional<T> findOne(long id) throws ApplicationAffinityConstraintDoesNotExist;

    /**
     * Delete an Application Affinity constraint from database.
     *
     * @param id The id of the Application Affinity constraint to be deleted
     * @throws ApplicationAffinityConstraintDoesNotExist
     */
    public void delete(long id) throws ApplicationAffinityConstraintDoesNotExist;

    /**
     * Fetch all Application Affinity constraints from database.
     *
     * @return A list of Application privacy constraint objects
     */
    public List<T> findAll();

    /**
     * Creates a new Application Affinity constraint to the database.
     *
     * @param t An Application Affinity constraint object
     * @throws ApplicationAffinityConstraintAlreadyExistsException
     */
    public void create(T t) throws ApplicationAffinityConstraintAlreadyExistsException;
    
    public void edit(T t) throws ApplicationAffinityConstraintDoesNotExist;

    public List<T> findByApplicationID(long applicationID);

    public T findOneWithoutApplication(long applicationID);

}
