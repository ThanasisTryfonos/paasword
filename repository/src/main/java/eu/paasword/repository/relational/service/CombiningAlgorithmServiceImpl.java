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


import eu.paasword.api.repository.ICombiningAlgorithmService;
import eu.paasword.api.repository.exception.combiningAlgorithm.CombiningAlgorithmAlreadyExistsException;
import eu.paasword.api.repository.exception.combiningAlgorithm.CombiningAlgorithmDoesNotExist;
import eu.paasword.api.repository.exception.combiningAlgorithm.CombiningAlgorithmNameDoesNotExist;
import eu.paasword.repository.relational.dao.CombiningAlgorithmRepository;
import eu.paasword.repository.relational.domain.CombiningAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 *
 * @author smantzouratos
 */
@Component
public class CombiningAlgorithmServiceImpl implements ICombiningAlgorithmService<CombiningAlgorithm> {

    @Autowired
    CombiningAlgorithmRepository combiningAlgorithmRepository;

    private static final Logger logger = Logger.getLogger(CombiningAlgorithmServiceImpl.class.getName());

    @Override
    public void create(CombiningAlgorithm combiningAlgorithm) throws CombiningAlgorithmAlreadyExistsException {

        //Check if combining algorithm already exists
        if (null != combiningAlgorithmRepository.findByName(combiningAlgorithm.getName())) {
            throw new CombiningAlgorithmAlreadyExistsException(combiningAlgorithm.getName());
        }

        //Store property type to database
        combiningAlgorithmRepository.save(combiningAlgorithm);
    }

    @Override
    public void delete(long id) throws CombiningAlgorithmDoesNotExist {
        try {
            combiningAlgorithmRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(CombiningAlgorithmServiceImpl.class.getName()).severe(ex.getMessage());
            throw new CombiningAlgorithmDoesNotExist(id);
        }
    }

    @Override
    public Optional<CombiningAlgorithm> findByName(String name) throws CombiningAlgorithmNameDoesNotExist {
        Optional<CombiningAlgorithm> combiningAlgorithm = Optional.ofNullable(combiningAlgorithmRepository.findByName(name));

        if (combiningAlgorithm.isPresent()) {
            return combiningAlgorithm;
        }

        throw new CombiningAlgorithmNameDoesNotExist(name);
    }

    @Override
    public Optional<CombiningAlgorithm> findOne(long id) throws CombiningAlgorithmDoesNotExist {
        Optional<CombiningAlgorithm> combiningAlgorithm = Optional.ofNullable(combiningAlgorithmRepository.findOne(id));

        if (combiningAlgorithm.isPresent()) {
            return combiningAlgorithm;
        }

        throw new CombiningAlgorithmDoesNotExist(id);
    }

    @Override
    public List<CombiningAlgorithm> findAll() {
        return combiningAlgorithmRepository.findAll();
    }

    @Override
    public List<CombiningAlgorithm> findAllByOrderByNameAsc() {
        return combiningAlgorithmRepository.findAllByOrderByNameAsc();
    }

    @Override
    public void edit(CombiningAlgorithm combiningAlgorithm) throws CombiningAlgorithmDoesNotExist {

        CombiningAlgorithm currentCombiningAlgorithm = combiningAlgorithmRepository.findOne(combiningAlgorithm.getId());

        //Check if current CombiningAlgorithm exists
        if (null == currentCombiningAlgorithm) {
            throw new CombiningAlgorithmDoesNotExist(combiningAlgorithm.getId());
        }

        currentCombiningAlgorithm.setName(combiningAlgorithm.getName());

        //Store CombiningAlgorithm to database
        combiningAlgorithmRepository.save(currentCombiningAlgorithm);

    }

}
