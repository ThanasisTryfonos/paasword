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

import eu.paasword.api.repository.exception.policy.PolicyAlreadyExistsException;
import eu.paasword.api.repository.exception.policy.PolicyDoesNotExist;
import eu.paasword.api.repository.exception.policy.PolicyNameDoesNotExist;
import eu.paasword.api.repository.exception.policy.PolicyValidityException;
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
public interface IPolicyService<T> {

    /**
     * Fetch a policy from database given a policyName.
     * 
     * @param policyName Find a instance based on a policyName
     * @return An instance of instance object wrapped in an Optional object
     * @throws PolicyNameDoesNotExist
     */
    public Optional<T> findByPolicyName(String policyName) throws PolicyNameDoesNotExist;

    /**
     * Fetch a policy from database given an id.
     *
     * @param id The id of the policy to fetch
     * @return An instance of Policy object wrapped in an Optional object
     * @throws PolicyDoesNotExist
     */
    public Optional<T> findOne(long id) throws PolicyDoesNotExist;

    /**
     * Delete a pyolic from database.
     *
     * @param id The id of the policy to be deleted
     * @throws PolicyDoesNotExist
     */
    public void delete(long id) throws PolicyDoesNotExist, PolicyValidityException;

    /**
     * Fetch all policies from database.
     *
     * @return A list of Policy objects
     */
    public List<T> findAll();

    public Page<T> findAll(Pageable pageable);

    /**
     * Creates a new policy to the database.
     *
     * @param t A Policy object
     * @throws PolicyAlreadyExistsException
     */
    public void create(T t) throws PolicyAlreadyExistsException, PolicyValidityException;
    
    public void edit(T t) throws PolicyDoesNotExist, PolicyValidityException;
    

}
