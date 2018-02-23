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

import eu.paasword.api.repository.exception.expression.ExpressionAlreadyExistsException;
import eu.paasword.api.repository.exception.expression.ExpressionDoesNotExist;
import eu.paasword.api.repository.exception.expression.ExpressionNameDoesNotExist;
import eu.paasword.api.repository.exception.expression.ExpressionValidityException;
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
public interface IExpressionService<T> {

    /**
     * Fetch an expression from database given a name.
     * 
     * @param expressionName Find an expression based on a name
     * @return An instance of Expression object wrapped in an Optional object
     * @throws ExpressionNameDoesNotExist
     */
    public Optional<T> findByExpressionName(String expressionName) throws ExpressionNameDoesNotExist;

    /**
     * Fetch an expression from database given an id.
     *
     * @param id The id of the expression to fetch
     * @return An instance of Expression object wrapped in an Optional object
     * @throws ExpressionDoesNotExist
     */
    public Optional<T> findOne(long id) throws ExpressionDoesNotExist;

    /**
     * Delete an expression from database.
     *
     * @param id The id of the expression to be deleted
     * @throws ExpressionDoesNotExist
     */
    public void delete(long id) throws ExpressionDoesNotExist, ExpressionValidityException;

    /**
     * Fetch all expressions from database.
     *
     * @return A list of Expression objects
     */
    public List<T> findAll();

    public Page<T> findAll(Pageable pageable);

    public List<T> findByOrderByExpressionName();

    /**
     * Creates a new expression to the database.
     *
     * @param t A Expression object
     * @throws ExpressionAlreadyExistsException
     */
    public void create(T t) throws ExpressionAlreadyExistsException, ExpressionValidityException;
    
    public void edit(T t) throws ExpressionDoesNotExist, ExpressionValidityException;

}
