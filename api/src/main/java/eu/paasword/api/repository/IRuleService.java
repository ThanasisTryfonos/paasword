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

import eu.paasword.api.repository.exception.rule.RuleAlreadyExistsException;
import eu.paasword.api.repository.exception.rule.RuleDoesNotExist;
import eu.paasword.api.repository.exception.rule.RuleNameDoesNotExist;
import eu.paasword.api.repository.exception.rule.RuleValidityException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T> as Rule
 * @param <E> as Expression
 */
@Service
public interface IRuleService<T,E> {

    /**
     * Fetch a rule from database given a ruleName.
     * 
     * @param ruleName Find a instance based on a ruleName
     * @return An instance of instance object wrapped in an Optional object
     * @throws RuleNameDoesNotExist
     */
    public Optional<T> findByRuleName(String ruleName) throws RuleNameDoesNotExist;

    /**
     * Fetch a rule from database given an id.
     *
     * @param id The id of the rule to fetch
     * @return An instance of Rule object wrapped in an Optional object
     * @throws RuleDoesNotExist
     */
    public Optional<T> findOne(long id) throws RuleDoesNotExist;

    /**
     * Delete a rule from database.
     *
     * @param id The id of the rule to be deleted
     * @throws RuleDoesNotExist
     */
    public void delete(long id) throws RuleDoesNotExist, RuleValidityException;

    /**
     * Fetch all rules from database.
     *
     * @return A list of Rule objects
     */
    public List<T> findAll();

    public Page<T> findAll(Pageable pageable);

    /**
     * Creates a new rule to the database.
     *
     * @param t A Rule object
     * @throws RuleAlreadyExistsException
     */
    public void create(T t) throws RuleAlreadyExistsException, RuleValidityException;
    
    public void edit(T t) throws RuleDoesNotExist, RuleValidityException;
    
    
    /**
     * Fetch all rules from database that make use of the given expression_id.
     *
     * @param expression
     * @return A list of Rule objects
     */
    public List<T>  findByExpression_id(E expression) throws RuleDoesNotExist, RuleValidityException;

}
