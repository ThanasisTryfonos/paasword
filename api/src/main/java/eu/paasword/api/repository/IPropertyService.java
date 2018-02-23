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

import eu.paasword.api.repository.exception.property.PropertyAlreadyExistsException;
import eu.paasword.api.repository.exception.property.PropertyDoesNotExist;
import eu.paasword.api.repository.exception.property.PropertyNameDoesNotExist;
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
public interface IPropertyService<T, C> {

    /**
     * Fetch a property from database given a name.
     * 
     * @param name Find a property based on a name
     * @return A property of instance object wrapped in an Optional object
     * @throws PropertyNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws PropertyNameDoesNotExist;

    /**
     * Fetch a property from database given an id.
     *
     * @param id The id of the property to fetch
     * @return An instance of Property object wrapped in an Optional object
     * @throws PropertyDoesNotExist
     */
    public Optional<T> findOne(long id) throws PropertyDoesNotExist;

    /**
     * Delete a property from database.
     *
     * @param id The id of the property to be deleted
     * @throws PropertyDoesNotExist
     */
    public void delete(long id) throws PropertyDoesNotExist;

    /**
     * Fetch all properties from database.
     *
     * @return A list of Property objects
     */
    public List<T> findAll();

    public Page<T> findByClassID(C c, Pageable pageable);

    /**
     * Fetch all properties from database.
     *
     * @return An Property object
     */
    public T findByNameAndClassID(String name, long id);

    public List<T> findByNameAndIdIn(String keyword, List<Long> ids);

    public List<T> getPropertiesByKeyword(String keyword, List<Long> ids);

    public List<T> findByIdIn(List<Long> ids);

    /**
     * Creates a new property to the database.
     *
     * @param t A Property object
     * @throws PropertyAlreadyExistsException
     */
    public void create(T t) throws PropertyAlreadyExistsException;
    
    public void edit(T t) throws PropertyDoesNotExist;

}
