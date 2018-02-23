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

import eu.paasword.api.repository.exception.propertyType.PropertyTypeAlreadyExistsException;
import eu.paasword.api.repository.exception.propertyType.PropertyTypeNameDoesNotExist;
import eu.paasword.api.repository.exception.propertyType.PropertyTypeDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface IPropertyTypeService<T> {

    /**
     * Fetch a property type from database given a name.
     * 
     * @param name Find a property type based on a name
     * @return An instance of PropertyType object wrapped in an Optional object
     * @throws PropertyTypeNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws PropertyTypeNameDoesNotExist;

    /**
     * Fetch a property type from database given an id.
     *
     * @param id The id of the property type to fetch
     * @return An instance of Property Type object wrapped in an Optional object
     * @throws PropertyTypeDoesNotExist
     */
    public Optional<T> findOne(long id) throws PropertyTypeDoesNotExist;

    /**
     * Delete a property type from database.
     *
     * @param id The id of the property type to be deleted
     * @throws PropertyTypeDoesNotExist
     */
    public void delete(long id) throws PropertyTypeDoesNotExist;

    /**
     * Fetch all property types from database.
     *
     * @return A list of Property Type objects
     */
    public List<T> findAll();

    /**
     * Creates a new property type to the database.
     *
     * @param t A Property Type object
     * @throws PropertyTypeAlreadyExistsException
     */
    public void create(T t) throws PropertyTypeAlreadyExistsException;
    
    public void edit(T t) throws PropertyTypeDoesNotExist;

}
