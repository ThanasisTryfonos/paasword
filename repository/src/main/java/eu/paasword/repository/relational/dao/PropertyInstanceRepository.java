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
package eu.paasword.repository.relational.dao;

import eu.paasword.repository.relational.domain.PropertyInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * @author smantzouratos
 */
@Repository
@Transactional
public interface PropertyInstanceRepository extends JpaRepository<PropertyInstance, Long> {

    /**
     *
     * @param name
     * @return
     */
    public PropertyInstance findByName(String name);

    /**
     *
     * @param propertyID
     * @return
     */
    @Query("select p from PropertyInstance p where p.propertyID.id = ?1")
    public List<PropertyInstance> findByPropertyID(long propertyID);

    @Query("select p from PropertyInstance p where p.instanceID.id = ?1")
    public List<PropertyInstance> findByInstanceID(long instanceID);
    
}
