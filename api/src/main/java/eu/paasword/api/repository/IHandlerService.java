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

import eu.paasword.api.repository.exception.handler.HandlerAlreadyExistsException;
import eu.paasword.api.repository.exception.handler.HandlerDoesNotExist;
import eu.paasword.api.repository.exception.handler.HandlerNameDoesNotExist;
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
public interface IHandlerService<T> {

    /**
     * Fetch a handler from database given a handlerName.
     * 
     * @param handlerName Find a handler based on a name
     * @return An instance of Handler object wrapped in an Optional object
     * @throws HandlerNameDoesNotExist
     */
    public Optional<T> findByHandlerName(String handlerName) throws HandlerNameDoesNotExist;

    /**
     * Fetch a handler from database given an id.
     *
     * @param id The id of the handler to fetch
     * @return An instance of Handler object wrapped in an Optional object
     * @throws HandlerDoesNotExist
     */
    public Optional<T> findOne(long id) throws HandlerDoesNotExist;

    /**
     * Delete a handler from database.
     *
     * @param id The id of the handler to be deleted
     * @throws HandlerDoesNotExist
     */
    public void delete(long id) throws HandlerDoesNotExist;

    /**
     * Fetch all handlers from database.
     *
     * @return A list of Handler objects
     */
    public List<T> findAll();

    public Page<T> findAll(Pageable pageable);

    /**
     * Creates a new handler to the database.
     *
     * @param t A Handler object
     * @throws HandlerAlreadyExistsException
     */
    public void create(T t) throws HandlerAlreadyExistsException;
    
    public void edit(T t) throws HandlerDoesNotExist;

}
