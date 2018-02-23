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

import eu.paasword.api.repository.exception.combiningAlgorithm.CombiningAlgorithmAlreadyExistsException;
import eu.paasword.api.repository.exception.combiningAlgorithm.CombiningAlgorithmDoesNotExist;
import eu.paasword.api.repository.exception.combiningAlgorithm.CombiningAlgorithmNameDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface ICombiningAlgorithmService<T> {

    /**
     * Fetch a combining algorithm from database given a name.
     * 
     * @param name Find a combining algorithm based on a name
     * @return An instance of CombiningAlgorithm object wrapped in an Optional object
     * @throws CombiningAlgorithmNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws CombiningAlgorithmNameDoesNotExist;

    /**
     * Fetch a combining algorithm from database given an id.
     *
     * @param id The id of the combining algorithm to fetch
     * @return An instance of CombiningAlgorithm object wrapped in an Optional object
     * @throws CombiningAlgorithmDoesNotExist
     */
    public Optional<T> findOne(long id) throws CombiningAlgorithmDoesNotExist;

    /**
     * Delete a combining algorithm from database.
     *
     * @param id The id of the combining algorithm to be deleted
     * @throws CombiningAlgorithmDoesNotExist
     */
    public void delete(long id) throws CombiningAlgorithmDoesNotExist;

    /**
     * Fetch all combining algorithms from database.
     *
     * @return A list of CombiningAlgorithm objects
     */
    public List<T> findAll();

    public List<T> findAllByOrderByNameAsc();

    /**
     * Creates a new combining algorithm to the database.
     *
     * @param t A CombiningAlgorithm object
     * @throws CombiningAlgorithmAlreadyExistsException
     */
    public void create(T t) throws CombiningAlgorithmAlreadyExistsException;
    
    public void edit(T t) throws CombiningAlgorithmDoesNotExist;

}
