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

import eu.paasword.repository.relational.domain.ApplicationAffinityConstraint;
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
public interface ApplicationAffinityConstraintRepository extends JpaRepository<ApplicationAffinityConstraint, Long> {

    /**
     *
     * @param name
     * @return
     */
    public ApplicationAffinityConstraint findByName(String name);

    @Query("select new eu.paasword.repository.relational.domain.ApplicationAffinityConstraint(a.id, a.name, a.affinityConstraint, a.lastModified, a.dateCreated) from ApplicationAffinityConstraint a where a.id = ?1")
    public ApplicationAffinityConstraint findOneWithoutApplication(long applicationID);

    @Query("select new eu.paasword.repository.relational.domain.ApplicationAffinityConstraint(a.id, a.name, a.affinityConstraint, a.lastModified, a.dateCreated) from ApplicationAffinityConstraint a where a.applicationID.id = ?1")
    public List<ApplicationAffinityConstraint> findByApplicationID(long applicationID);

}
