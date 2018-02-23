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

import eu.paasword.api.repository.exception.clazz.ClassNameDoesNotExist;
import eu.paasword.api.repository.exception.clazz.ClazzAlreadyExistsException;
import eu.paasword.api.repository.exception.clazz.ClazzDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface IClazzService<T> {

    /**
     * Fetch a clazz from database given a name.
     * 
     * @param className Find a clazz based on a name
     * @return An instance of Clazz object wrapped in an Optional object
     * @throws ClassNameDoesNotExist
     */
    public Optional<T> findByClassName(String className) throws ClassNameDoesNotExist;

    /**
     * Fetch a clazz from database given a parent class.
     *
     * @param clazzID Find a clazz based on a parent id
     * @return An list of Clazz objects
     * @throws ClazzDoesNotExist
     */
    public List<T> findByParentID(T clazzID) throws ClazzDoesNotExist;

    /**
     * Fetch a clazz from database given an id.
     *
     * @param id The id of the clazz to fetch
     * @return An instance of Clazz object wrapped in an Optional object
     * @throws ClazzDoesNotExist
     */
    public Optional<T> findOne(long id) throws ClazzDoesNotExist;

    /**
     * Delete a clazz from database.
     *
     * @param id The id of the clazz to be deleted
     * @throws ClazzDoesNotExist
     */
    public void delete(long id) throws ClazzDoesNotExist;

    /**
     * Fetch all clazz from database.
     *
     * @return A list of Clazz objects
     */
    public List<T> findAll();

    /**
     * Fetch all clazz except root from database.
     *
     * @return A list of Clazz objects
     */
    public List<T> findAllCustom(long rootID);

    /**
     * Fetch all clazz except root from database.
     *
     * @return A list of Clazz objects
     */
    public List<T> findAllCustom();

    /**
     * Fetch all clazzes from database.
     *
     * @return A list of Clazz objects
     */
    public List<T> getClassesByKeyword(String keyword, long rootID);

    public List<T> findByClassNameAndIdIn(String keyword, List<Long> ids);

    public List<T> getClassesByKeywordAndId(String keyword, List<Long> ids);

    public List<T> findByIdIn(List<Long> ids);

    /**
     * Fetch all clazzes from database.
     *
     * @return A list of Clazz objects
     */
    public List<T> getClassesByKeyword(String keyword);



    /**
     * Creates a new clazz to the database.
     *
     * @param t A Clazz object
     * @throws ClazzAlreadyExistsException
     */
    public void create(T t) throws ClazzAlreadyExistsException;
    
    public void edit(T t) throws ClazzDoesNotExist;

}
