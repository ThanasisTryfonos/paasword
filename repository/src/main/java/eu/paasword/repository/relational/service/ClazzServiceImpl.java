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
package eu.paasword.repository.relational.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import eu.paasword.api.repository.IClazzService;
import eu.paasword.api.repository.exception.clazz.ClassNameDoesNotExist;
import eu.paasword.api.repository.exception.clazz.ClazzAlreadyExistsException;
import eu.paasword.api.repository.exception.clazz.ClazzDoesNotExist;
import eu.paasword.repository.relational.dao.ClazzRepository;
import eu.paasword.repository.relational.domain.Clazz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 *
 * @author smantzouratos
 */
@Component
public class ClazzServiceImpl implements IClazzService<Clazz> {

    @Autowired
    ClazzRepository clazzRepository;

    private static final Logger logger = Logger.getLogger(ClazzServiceImpl.class.getName());

    @Override
    public void create(Clazz clazz) throws ClazzAlreadyExistsException {

        //Check if class name already exists
        if (null != clazzRepository.findByClassName(clazz.getClassName())) {
            throw new ClazzAlreadyExistsException(clazz.getClassName());
        }

        //Store clazz to database
        clazzRepository.save(clazz);

        // Not needed, scheduler does this
//        triplestoreService.synchronizeCMToTripleStore();

    }

    @Override
    public void delete(long id) throws ClazzDoesNotExist {
        try {
            clazzRepository.delete(id);

            // Not needed, scheduler does this
//            triplestoreService.synchronizeCMToTripleStore();

        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(ClazzServiceImpl.class.getName()).severe(ex.getMessage());
            throw new ClazzDoesNotExist(id);
        }
    }

    @Override
    public Optional<Clazz> findByClassName(String className) throws ClassNameDoesNotExist {
        Optional<Clazz> clazz = Optional.ofNullable(clazzRepository.findByClassName(className));

        if (clazz.isPresent()) {
            return clazz;
        }

        throw new ClassNameDoesNotExist(className);
    }

    @Override
    public Optional<Clazz> findOne(long id) throws ClazzDoesNotExist {
        Optional<Clazz> clazz = Optional.ofNullable(clazzRepository.findOne(id));

        if (clazz.isPresent()) {
            return clazz;
        }

        throw new ClazzDoesNotExist(id);
    }

    @Override
    public List<Clazz> findAll() {
        return clazzRepository.findAll();
    }

    @Override
    public List<Clazz> findByParentID(Clazz clazzID) {
        return clazzRepository.findByParentID(clazzID);
    }

    @Override
    public List<Clazz> findAllCustom(long rootID) {
        return clazzRepository.findAllCustom(rootID, null);
    }

    @Override
    public List<Clazz> findAllCustom() {
        return clazzRepository.findAllCustom(null);
    }

    @Override
    public void edit(Clazz clazz) throws ClazzDoesNotExist {

        Clazz currentClazz = clazzRepository.findOne(clazz.getId());

        //Check if current clazz exists
        if (null == currentClazz) {
            throw new ClazzDoesNotExist(clazz.getId());
        }

        currentClazz.setClassName(clazz.getClassName());

        if (null != clazz.getParentID()) {
            currentClazz.setParentID(clazz.getParentID());
        } else {
            currentClazz.setParentID(clazzRepository.findOne(1L));
        }
        //Store clazz to database
        clazzRepository.save(currentClazz);

        // Not needed, scheduler does this
//        triplestoreService.synchronizeCMToTripleStore();

    }

    /**
     * This method is used by the autocomplete functionality to retrieve Classes
     *
     * @param keyword
     *
     * @return A List of Clazz object
     */
    public List<Clazz> getClassesByKeyword(String keyword) {
        List<Clazz> listOfClasses;
        if (keyword.matches("\\*+")) {
            int length = keyword.length();
            int page = 10 * (length - 1);
            Pageable pageable = new PageRequest(page, 10, new Sort(Sort.Direction.ASC, "className"));

            listOfClasses = clazzRepository.findAllCustom(pageable);

            //listOfClasses = clazzRepository.findAll(pageable).getContent();
        } else {
            listOfClasses = clazzRepository.findFirst10ByClassNameOrderByClassNameAsc(keyword);

        }
        return listOfClasses;
    }

    @Override
    public List<Clazz> findByIdIn(List<Long> ids) {
        return clazzRepository.findByIdIn(ids, null);
    }

    @Override
    public List<Clazz> findByClassNameAndIdIn(String keyword, List<Long> ids) {
        return clazzRepository.findByClassNameAndIdIn(keyword, ids);
    }

    public List<Clazz> getClassesByKeywordAndId(String keyword, List<Long> ids) {

        List<Clazz> listOfClasses = new ArrayList<>();
        if (keyword.matches("\\*+")) {
            int length = keyword.length();
            int page = 10 * (length - 1);
            Pageable pageable = new PageRequest(page, 10, new Sort(Sort.Direction.ASC, "className"));

            listOfClasses = clazzRepository.findByIdIn(ids, pageable);

        } else {
            listOfClasses = clazzRepository.findByClassNameAndIdIn(keyword, ids);

        }

        return listOfClasses;
    }

    /**
     * This method is used by the autocomplete functionality to retrieve Classes
     *
     * @param keyword
     * @param rootID
     *
     * @return A List of Clazz object
     */
    public List<Clazz> getClassesByKeyword(String keyword, long rootID) {
        List<Clazz> listOfClasses;
        if (keyword.matches("\\*+")) {
            int length = keyword.length();
            int page = 10 * (length - 1);
            Pageable pageable = new PageRequest(page, 10, new Sort(Sort.Direction.ASC, "className"));

            listOfClasses = clazzRepository.findAllCustom(rootID, pageable);

            //listOfClasses = clazzRepository.findAll(pageable).getContent();
        } else {
            listOfClasses = clazzRepository.findFirst10ByClassNameOrderByClassNameAsc(keyword, rootID);

        }
        return listOfClasses;
    }

}
