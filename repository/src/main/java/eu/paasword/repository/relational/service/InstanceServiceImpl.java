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

import eu.paasword.api.repository.IInstanceService;
import eu.paasword.api.repository.exception.instance.InstanceAlreadyExistsException;
import eu.paasword.api.repository.exception.instance.InstanceDoesNotExist;
import eu.paasword.api.repository.exception.instance.InstanceNameDoesNotExist;
import eu.paasword.repository.relational.dao.ClazzRepository;
import eu.paasword.repository.relational.dao.InstanceRepository;
import eu.paasword.repository.relational.dao.PropertyInstanceRepository;
import eu.paasword.repository.relational.domain.Clazz;
import eu.paasword.repository.relational.domain.Instance;
import eu.paasword.repository.relational.domain.PropertyInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.logging.Logger;

/**
 *
 * @author smantzouratos
 */
@Component
public class InstanceServiceImpl implements IInstanceService<Instance, Clazz> {

    @Autowired
    InstanceRepository instanceRepository;

    @Autowired
    PropertyInstanceRepository propertyInstanceRepository;

    @Autowired
    ClazzRepository clazzRepository;

    private static final Logger logger = Logger.getLogger(InstanceServiceImpl.class.getName());

    @Override
    public void create(Instance instance) throws InstanceAlreadyExistsException {

        //Check if instance name already exists
        if (null != instanceRepository.findByInstanceName(instance.getInstanceName())) {
            throw new InstanceAlreadyExistsException(instance.getInstanceName());
        }

        //Store instance to database
        instanceRepository.save(instance);

        // Not needed, scheduler does this
//        triplestoreService.synchronizeCMToTripleStore();
    }

    @Override
    public void delete(long id) throws InstanceDoesNotExist {
        try {

            instanceRepository.delete(id);

            // Not needed, scheduler does this
//            triplestoreService.synchronizeCMToTripleStore();
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(InstanceServiceImpl.class.getName()).severe(ex.getMessage());
            throw new InstanceDoesNotExist(id);
        }
    }

    @Override
    public Optional<Instance> findByInstanceName(String instanceName) throws InstanceNameDoesNotExist {
        Optional<Instance> instance = Optional.ofNullable(instanceRepository.findByInstanceName(instanceName));

        if (instance.isPresent()) {
            return instance;
        }

        throw new InstanceNameDoesNotExist(instanceName);
    }

    @Override
    public Optional<Instance> findOne(long id) throws InstanceDoesNotExist {
        Optional<Instance> instance = Optional.ofNullable(instanceRepository.findOne(id));

        if (instance.isPresent()) {
            return instance;
        }

        throw new InstanceDoesNotExist(id);
    }

    @Override
    public List<Instance> findAll() {
        return instanceRepository.findAll();
    }

    @Override
    public List<Instance> findByOrderByInstanceName() {
        return instanceRepository.findByOrderByInstanceName();
    }

    @Override
    public Page<Instance> findByClassID(Clazz clazz, Pageable pageable) {
        return instanceRepository.findByClassID(clazz, pageable);
    }

    @Override
    public Instance findByInstanceNameAndClassID(String instanceName, long classID) {
        return instanceRepository.findByInstanceNameAndClassID(instanceName, classID);
    }

    @Override
    public void edit(Instance instance) throws InstanceDoesNotExist {

        Instance currentInstance = instanceRepository.findOne(instance.getId());

        //Check if current instance exists
        if (null == currentInstance) {
            throw new InstanceDoesNotExist(instance.getId());
        }

        Map<String, PropertyInstance> newPropertyInstancesMap = new HashMap<>();
        List<PropertyInstance> newPropertyInstances = new ArrayList<>();


        instance.getPropertyInstances().stream().forEach(propertyInstance -> {

//            logger.info("PropertyInstance: " + propertyInstance.getName());

            newPropertyInstancesMap.put(propertyInstance.getName(), propertyInstance);
        });

        propertyInstanceRepository.findByInstanceID(currentInstance.getId()).stream().forEach(propertyInstance -> {

//        if (null != currentInstance.getPropertyInstances() && !currentInstance.getPropertyInstances().isEmpty()) {

//            currentInstance.getPropertyInstances().stream().forEach(propertyInstance -> {

//                logger.info("PropertyInstance: " + propertyInstance.getName());

                if (newPropertyInstancesMap.containsKey(propertyInstance.getName())) {

                    newPropertyInstancesMap.remove(propertyInstance.getName());
                    newPropertyInstances.add(propertyInstance);

                } else {
                    propertyInstanceRepository.delete(propertyInstance);
                    newPropertyInstancesMap.remove(propertyInstance.getName());
                }

            });

//        }

        if (!newPropertyInstancesMap.isEmpty()) {

            for (Map.Entry<String, PropertyInstance> entry : newPropertyInstancesMap.entrySet()) {
                String key = entry.getKey();
                PropertyInstance value = (PropertyInstance) entry.getValue();

//                logger.info("Adding : " + value.getName());

                propertyInstanceRepository.save(value);

                newPropertyInstances.add(value);

            }
        }

        currentInstance.setInstanceName(instance.getInstanceName());

        currentInstance.setPropertyInstances(newPropertyInstances);

        //Store instance to database
        instanceRepository.save(currentInstance);

        // Not needed, scheduler does this
//        triplestoreService.synchronizeCMToTripleStore();

    }

    /**
     * This method is used by the autocomplete functionality to retrieve Instances
     *
     * @param keyword
     * @param clazzID
     *
     * @return A List of Instance object
     */
    public List<Instance> getInstancesByKeyword(String keyword, long clazzID) {
        List<Instance> listOfInstances;
        if (keyword.matches("\\*+")) {
            int length = keyword.length();
            int page = 10 * (length - 1);
            Pageable pageable = new PageRequest(page, 10, new Sort(Sort.Direction.ASC, "instanceName"));

            listOfInstances = instanceRepository.findByClassID(clazzRepository.findOne(clazzID), pageable).getContent();

        } else {
            listOfInstances = instanceRepository.findFirst10ByInstanceNameOrderByInstanceNameAsc(keyword, clazzID);
        }
        return listOfInstances;
    }

    /**
     * This method is used by the autocomplete functionality to retrieve Instances
     *
     * @param keyword
     *
     * @return A List of Instance object
     */
    public List<Instance> getInstancesByKeyword(String keyword) {
        List<Instance> listOfInstances;
        if (keyword.matches("\\*+")) {
            int length = keyword.length();
            int page = 10 * (length - 1);
            Pageable pageable = new PageRequest(page, 10, new Sort(Sort.Direction.ASC, "instanceName"));

            listOfInstances = instanceRepository.findAll(pageable).getContent();

        } else {
            listOfInstances = instanceRepository.findFirst10ByInstanceNameOrderByInstanceNameAsc(keyword);
        }
        return listOfInstances;
    }

}
