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

import eu.paasword.api.repository.exception.namespace.NamespaceAlreadyExistsException;
import eu.paasword.api.repository.exception.namespace.NamespaceDoesNotExist;
import eu.paasword.api.repository.exception.namespace.NamespaceNameDoesNotExist;
import eu.paasword.api.repository.exception.namespace.NamespacePrefixDoesNotExist;
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
public interface INamespaceService<T> {

    /**
     * Fetch an Namespace from database given a description.
     * 
     * @param name Find a handler based on a description
     * @return An instance of Namespace object wrapped in an Optional object
     * @throws NamespaceNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws NamespaceNameDoesNotExist;

    /**
     * Fetch a Namespace from database given a prefix.
     *
     * @param prefix Find an Namespace based on a prefix
     * @return An instance of Namespace object wrapped in an Optional object
     * @throws NamespacePrefixDoesNotExist
     */
    public Optional<T> findByPrefix(String prefix) throws NamespacePrefixDoesNotExist;

    /**
     * Fetch an Namespace from database given an id.
     *
     * @param id The id of the Namespace to fetch
     * @return An instance of Namespace object wrapped in an Optional object
     * @throws NamespaceDoesNotExist
     */
    public Optional<T> findOne(long id) throws NamespaceDoesNotExist;

    /**
     * Delete an Namespace from database.
     *
     * @param id The id of the Namespace to be deleted
     * @throws NamespaceDoesNotExist
     */
    public void delete(long id) throws NamespaceDoesNotExist;

    /**
     * Fetch all Namespaces from database.
     *
     * @return A list of Namespace objects
     */
    public List<T> findAll();

    public Page<T> findAll(Pageable page);

    public List<T> findAllByOrderByPrefixAsc();

    /**
     * Creates a new Namespace to the database.
     *
     * @param t A Namespace object
     * @throws NamespaceAlreadyExistsException
     */
    public void create(T t) throws NamespaceAlreadyExistsException;
    
    public void edit(T t) throws NamespaceDoesNotExist;

}
