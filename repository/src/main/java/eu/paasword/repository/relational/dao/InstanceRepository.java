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

import java.util.List;

import eu.paasword.repository.relational.domain.Clazz;
import eu.paasword.repository.relational.domain.Instance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author smantzouratos
 */
@Repository
@Transactional
public interface InstanceRepository extends JpaRepository<Instance, Long> {
    
    /**
     *
     * @param instanceName
     * @return
     */
    public Instance findByInstanceName(String instanceName);

    /**
     *
     * @param classID
     * @return
     */
    public Page<Instance> findByClassID(Clazz classID, Pageable page);

    @Query("select i from Instance i where i.classID.id = ?2 and i.instanceName like %?1%")
    public List<Instance> findFirst10ByInstanceNameOrderByInstanceNameAsc(String instanceName, long clazzID);

    @Query("select i from Instance i where i.instanceName like %?1%")
    public List<Instance> findFirst10ByInstanceNameOrderByInstanceNameAsc(String instanceName);
    
    @Query("select c from Instance c where c.instanceName = ?1 and c.classID.id = ?2")
    public Instance findByInstanceNameAndClassID(String instanceName, long classID);

    public List<Instance> findByOrderByInstanceName();
    
}
