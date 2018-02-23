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

import eu.paasword.api.repository.exception.policySet.PolicySetAlreadyExistsException;
import eu.paasword.api.repository.exception.policySet.PolicySetDoesNotExist;
import eu.paasword.api.repository.exception.policySet.PolicySetNameDoesNotExist;
import eu.paasword.api.repository.exception.policySet.PolicySetValidityException;
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
public interface IPolicySetService<T> {

    /**
     * Fetch a policy set from database given a policySetName.
     * 
     * @param policySetName Find a instance based on a policySetName
     * @return An instance of instance object wrapped in an Optional object
     * @throws PolicySetNameDoesNotExist
     */
    public Optional<T> findByPolicySetName(String policySetName) throws PolicySetNameDoesNotExist;

    /**
     * Fetch a policy set from database given an id.
     *
     * @param id The id of the policy set to fetch
     * @return An instance of PolicySet object wrapped in an Optional object
     * @throws PolicySetDoesNotExist
     */
    public Optional<T> findOne(long id) throws PolicySetDoesNotExist;

    /**
     * Delete a policy set from database.
     *
     * @param id The id of the policy set to be deleted
     * @throws PolicySetDoesNotExist
     */
    public void delete(long id) throws PolicySetDoesNotExist, PolicySetValidityException;

    /**
     * Fetch all policy sets from database.
     *
     * @return A list of PolicySet objects
     */
    public List<T> findAll();

    public Page<T> findAll(Pageable pageable);

    /**
     * Creates a new policy set to the database.
     *
     * @param t A PolicySet object
     * @throws PolicySetAlreadyExistsException
     */
    public void create(T t) throws PolicySetAlreadyExistsException, PolicySetValidityException;
    
    public void edit(T t) throws PolicySetDoesNotExist, PolicySetValidityException;

}
