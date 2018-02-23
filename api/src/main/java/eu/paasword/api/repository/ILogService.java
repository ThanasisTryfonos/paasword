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


import eu.paasword.api.repository.exception.log.LogDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface ILogService<T> {

    /**
     * Fetch a log from database given an id.
     *
     * @param id The id of the log to fetch
     * @return An instance of Log object wrapped in an Optional object
     * @throws LogDoesNotExist
     */
    public Optional<T> findOne(long id) throws LogDoesNotExist;

    /**
     * Delete a log from database.
     *
     * @param id The id of the log to be deleted
     * @throws LogDoesNotExist
     */
    public void delete(long id) throws LogDoesNotExist;

    /**
     * Fetch all logs from database.
     *
     * @return A list of Log objects
     */
    public List<T> findAll();

    public List<T> findByApplicationID(long id);

    public List<T> findFirst100();

    /**
     * Creates a new log to the database.
     *
     * @param t A Log object
     *
     */
    public void create(T t);
    
    public void edit(T t) throws LogDoesNotExist;

    public List<T> findAllWithoutApplication();

}
