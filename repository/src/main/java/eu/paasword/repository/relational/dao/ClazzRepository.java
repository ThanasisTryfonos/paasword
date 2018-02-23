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
import java.util.Optional;

import eu.paasword.repository.relational.domain.Clazz;
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
public interface ClazzRepository extends JpaRepository<Clazz, Long> {

    /**
     *
     * @param className
     * @return
     */
    public Clazz findByClassName(String className);

    /**
     *
     * @param classID
     * @return
     */
    public List<Clazz> findByParentID(Clazz classID);

    @Query("select c from Clazz c where c.parentID is not null and c.rootID.id = ?2 and c.className like %?1%")
    public List<Clazz> findFirst10ByClassNameOrderByClassNameAsc(String className, long rootID);

    @Query("select c from Clazz c where c.parentID is not null and c.className like %?1%")
    public List<Clazz> findFirst10ByClassNameOrderByClassNameAsc(String className);

    @Query("select c from Clazz c where c.parentID is not null and c.rootID.id = ?1 order by c.parentID, c.className asc")
    public List<Clazz> findAllCustom(long rootID, Pageable page);

    @Query("select c from Clazz c where c.parentID is not null order by c.className, c.parentID asc")
    public List<Clazz> findAllCustom(Pageable page);

    @Query("select c from Clazz c where c.id in ?2 and c.className like %?1%")
    public List<Clazz> findByClassNameAndIdIn(String className, List<Long> ids);

    public List<Clazz> findByIdIn(List<Long> ids, Pageable pageable);

}
