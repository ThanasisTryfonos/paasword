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

import eu.paasword.api.repository.ILogService;
import eu.paasword.api.repository.exception.log.LogDoesNotExist;
import eu.paasword.repository.relational.dao.LogRepository;
import eu.paasword.repository.relational.domain.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 *
 * @author smantzouratos
 */
@Component
public class LogServiceImpl implements ILogService<Log> {

    @Autowired
    LogRepository logRepository;

    private static final Logger logger = Logger.getLogger(LogServiceImpl.class.getName());

    @Override
    public void create(Log log) {

        //Store log to database
        logRepository.save(log);

    }

    @Override
    public void delete(long id) throws LogDoesNotExist {
        try {

            logRepository.delete(id);

        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(LogServiceImpl.class.getName()).severe(ex.getMessage());
            throw new LogDoesNotExist(id);
        }
    }

    @Override
    public Optional<Log> findOne(long id) throws LogDoesNotExist {
        Optional<Log> log = Optional.ofNullable(logRepository.findOne(id));

        if (log.isPresent()) {
            return log;
        }

        throw new LogDoesNotExist(id);
    }

    @Override
    public List<Log> findAll() {

        return logRepository.findAll();
    }

    @Override
    public List<Log> findByApplicationID(long id) {

        Pageable pageable = new PageRequest(0, 100, new Sort(Sort.Direction.DESC, "invocationTimestamp"));

        return logRepository.findByApplicationID(id, pageable);
    }


    @Override
    public List<Log> findFirst100() {

        Pageable pageable = new PageRequest(0, 100, new Sort(Sort.Direction.DESC, "invocationTimestamp"));

        return logRepository.findFirst100(pageable);
    }

    @Override
    public void edit(Log log) throws LogDoesNotExist {

        Log currentLog = logRepository.findOne(log.getId());

        //Check if current log exists
        if (null == currentLog) {
            throw new LogDoesNotExist(log.getId());
        }

        currentLog.setActor(log.getActor());
        currentLog.setAnnotatedCode(log.getAnnotatedCode());
        currentLog.setHeader(log.getHeader());
        currentLog.setLocalAddress(log.getLocalAddress());
        currentLog.setRemoteAddress(log.getRemoteAddress());

        //Store log to database
        logRepository.save(currentLog);

    }

    public List<Log> findAllWithoutApplication() {
        return logRepository.findAllWithoutApplication();
    }

}
