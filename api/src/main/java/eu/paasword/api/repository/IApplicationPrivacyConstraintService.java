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


import eu.paasword.api.repository.exception.applicationPrivacyConstraint.ApplicationPrivacyConstraintAlreadyExistsException;
import eu.paasword.api.repository.exception.applicationPrivacyConstraint.ApplicationPrivacyConstraintDoesNotExist;
import eu.paasword.api.repository.exception.applicationPrivacyConstraint.ApplicationPrivacyConstraintNameDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface IApplicationPrivacyConstraintService<T> {

    /**
     * Fetch an application privacy constraint from database given a name.
     * 
     * @param name Find an Application Privacy Constraint based on a name
     * @return An instance of Application object wrapped in an Optional object
     * @throws ApplicationPrivacyConstraintNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws ApplicationPrivacyConstraintNameDoesNotExist;

    /**
     * Fetch an Application Privacy Constraint from database given an id.
     *
     * @param id The id of the Application privacy constraint to fetch
     * @return An instance of Application privacy constraint object wrapped in an Optional object
     * @throws ApplicationPrivacyConstraintDoesNotExist
     */
    public Optional<T> findOne(long id) throws ApplicationPrivacyConstraintDoesNotExist;

    /**
     * Delete an Application privacy constraint from database.
     *
     * @param id The id of the Application privacy constraint to be deleted
     * @throws ApplicationPrivacyConstraintDoesNotExist
     */
    public void delete(long id) throws ApplicationPrivacyConstraintDoesNotExist;

    /**
     * Fetch all Application privacy constraints from database.
     *
     * @return A list of Application privacy constraint objects
     */
    public List<T> findAll();

    /**
     * Creates a new Application privacy constraint to the database.
     *
     * @param t An Application privacy constraint object
     * @throws ApplicationPrivacyConstraintAlreadyExistsException
     */
    public void create(T t) throws ApplicationPrivacyConstraintAlreadyExistsException;
    
    public void edit(T t) throws ApplicationPrivacyConstraintDoesNotExist;

    public List<T> findByApplicationID(long applicationID);

    public T findOneWithoutApplication(long applicationPrivacyConstraintID);

}
