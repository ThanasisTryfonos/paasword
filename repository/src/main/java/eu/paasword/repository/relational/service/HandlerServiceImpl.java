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

import eu.paasword.api.repository.IHandlerService;
import eu.paasword.api.repository.exception.handler.HandlerAlreadyExistsException;
import eu.paasword.api.repository.exception.handler.HandlerDoesNotExist;
import eu.paasword.api.repository.exception.handler.HandlerNameDoesNotExist;
import eu.paasword.repository.relational.dao.HandlerRepository;
import eu.paasword.repository.relational.domain.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 *
 * @author smantzouratos
 */
@Component
public class HandlerServiceImpl implements IHandlerService<Handler> {

    @Autowired
    HandlerRepository handlerRepository;

    private static final Logger logger = Logger.getLogger(HandlerServiceImpl.class.getName());

    @Override
    public void create(Handler handler) throws HandlerAlreadyExistsException {

        //Check if handler name already exists
        if (null != handlerRepository.findByHandlerName(handler.getHandlerName())) {
            throw new HandlerAlreadyExistsException(handler.getHandlerName());
        }

        //Store handler to database
        handlerRepository.save(handler);

        // Not needed, scheduler does this
//        triplestoreService.synchronizeCMToTripleStore();
    }

    @Override
    public void delete(long id) throws HandlerDoesNotExist {
        try {
            handlerRepository.delete(id);

            // Not needed, scheduler does this
//            triplestoreService.synchronizeCMToTripleStore();

        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(HandlerServiceImpl.class.getName()).severe(ex.getMessage());
            throw new HandlerDoesNotExist(id);
        }
    }

    @Override
    public Optional<Handler> findByHandlerName(String handlerName) throws HandlerNameDoesNotExist {
        Optional<Handler> handler = Optional.ofNullable(handlerRepository.findByHandlerName(handlerName));

        if (handler.isPresent()) {
            return handler;
        }

        throw new HandlerNameDoesNotExist(handlerName);
    }

    @Override
    public Optional<Handler> findOne(long id) throws HandlerDoesNotExist {
        Optional<Handler> handler = Optional.ofNullable(handlerRepository.findOne(id));

        if (handler.isPresent()) {
            return handler;
        }

        throw new HandlerDoesNotExist(id);
    }

    @Override
    public List<Handler> findAll() {
        return handlerRepository.findAll();
    }

    @Override
    public Page<Handler> findAll(Pageable pageable) {
        return handlerRepository.findAll(pageable);
    }

    @Override
    public void edit(Handler handler) throws HandlerDoesNotExist {

        Handler currentHandler = handlerRepository.findOne(handler.getId());

        //Check if current handler exists
        if (null == currentHandler) {
            throw new HandlerDoesNotExist(handler.getId());
        }

        //Store handler to database
        handlerRepository.save(handler);

        // Not needed, scheduler does this
//        triplestoreService.synchronizeCMToTripleStore();

    }

}
