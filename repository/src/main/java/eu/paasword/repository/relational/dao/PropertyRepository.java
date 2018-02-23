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

import eu.paasword.repository.relational.domain.Clazz;
import eu.paasword.repository.relational.domain.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface PropertyRepository extends JpaRepository<Property, Long> {
    
    /**
     *
     * @param name
     * @return
     */
    public Property findByName(String name);

    /**
     *
     * @param classID
     * @return
     */
    public List<Property> findByClassID(Clazz classID);

    public Page<Property> findByClassID(Clazz classID, Pageable pageable);
    
    @Query("select p from Property p where p.name = ?1 and p.classID.id = ?2")
    public Property findByNameAndClassID(String name, long classID);

    @Query("select p from Property p where p.id in ?2 and p.name like %?1%")
    public List<Property> findByNameAndIdIn(String name, List<Long> ids);

    public List<Property> findByIdIn(List<Long> ids, Pageable pageable);

}
