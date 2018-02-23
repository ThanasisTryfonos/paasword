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

import eu.paasword.api.repository.exception.applicationInstanceActivity.ApplicationInstanceActivityDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface IApplicationInstanceActivityService<T> {

    /**
     * Fetches an application instance activity from database given an id.
     *
     * @param id The id of the application instance activity to fetch
     * @return An instance of application instance activity object wrapped in an Optional object
     * @throws ApplicationInstanceActivityDoesNotExist
     */
    public Optional<T> findOne(long id) throws ApplicationInstanceActivityDoesNotExist;

    /**
     * Deletes an application instance activity from database.
     *
     * @param id The id of the application instance activity to be deleted
     * @throws ApplicationInstanceActivityDoesNotExist
     */
    public void delete(long id) throws ApplicationInstanceActivityDoesNotExist;

    /**
     * Fetches all application instance activities from database.
     *
     * @return A list of application instance activities objects
     */
    public List<T> findAll();

    public List<T> findByApplicationInstanceID(long id);

    public List<T> findByApplicationInstanceIDFirst100(long id);

    /**
     * Creates a new application instance activities to the database.
     *
     * @param t A application instance activities object
     *
     */
    public void create(T t);

    public void edit(T t) throws ApplicationInstanceActivityDoesNotExist;

}
