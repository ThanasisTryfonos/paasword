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

import eu.paasword.api.repository.exception.applicationInstanceHandler.ApplicationInstanceHandlerDoesNotExist;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface IApplicationInstanceHandlerService<T, AI> {

    /**
     * Fetches an application instance handler from database given an id.
     *
     * @param id The id of the application instance handler to fetch
     * @return An instance of application instance handler object wrapped in an Optional object
     * @throws ApplicationInstanceHandlerDoesNotExist
     */
    public Optional<T> findOne(long id) throws ApplicationInstanceHandlerDoesNotExist;

    /**
     * Deletes an application instance handler from database.
     *
     * @param id The id of the application handler activity to be deleted
     * @throws ApplicationInstanceHandlerDoesNotExist
     */
    public void delete(long id) throws ApplicationInstanceHandlerDoesNotExist;

    /**
     * Fetches all application instance handlers from database.
     *
     * @return A list of application instance handlers objects
     */
    public List<T> findAll();

    public List<T> findByApplicationInstanceID(long id);

    /**
     * Creates a new application instance handlers to the database.
     *
     * @param t A application instance handlers object
     *
     */
    public void create(T t);

    public void edit(T t) throws ApplicationInstanceHandlerDoesNotExist;

}
