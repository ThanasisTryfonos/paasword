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

import eu.paasword.api.repository.exception.propertyInstance.PropertyInstanceAlreadyExistsException;
import eu.paasword.api.repository.exception.propertyInstance.PropertyInstanceDoesNotExist;
import eu.paasword.api.repository.exception.propertyInstance.PropertyInstanceNameDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface IPropertyInstanceService<T> {

    /**
     * Fetch a property instance from database given a name.
     *
     * @param name Find a property based on a name
     * @return A property of instance object wrapped in an Optional object
     * @throws PropertyInstanceNameDoesNotExist
     */
    public Optional<T> findByName(String name) throws PropertyInstanceNameDoesNotExist;

    /**
     * Fetch a property instance from database given a property ID.
     *
     * @param propertyID Find a property based on a property
     * @return A property of instance object wrapped in an Optional object
     * @throws PropertyInstanceNameDoesNotExist
     */
    public List<T> findByPropertyID(long propertyID);

    public List<T> findByInstanceID(long instanceID);

    /**
     * Fetch a property instance from database given an id.
     *
     * @param id The id of the property instance to fetch
     * @return An instance of PropertyInstance object wrapped in an Optional object
     * @throws PropertyInstanceDoesNotExist
     */
    public Optional<T> findOne(long id) throws PropertyInstanceDoesNotExist;

    /**
     * Delete a property instance from database.
     *
     * @param id The id of the property instance to be deleted
     * @throws PropertyInstanceDoesNotExist
     */
    public void delete(long id) throws PropertyInstanceDoesNotExist;

    /**
     * Fetch all property instances from database.
     *
     * @return A list of Property Instances objects
     */
    public List<T> findAll();

    /**
     * Creates a new property instance to the database.
     *
     * @param t A Property Instance object
     * @throws PropertyInstanceAlreadyExistsException
     */
    public void create(T t) throws PropertyInstanceAlreadyExistsException;
    
    public void edit(T t) throws PropertyInstanceDoesNotExist;

}
