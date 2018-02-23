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

import eu.paasword.repository.relational.domain.Application;
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
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     *
     * @param name
     * @return
     */
    Application findByName(String name);

    Page<Application> findAllByOrderByNameAsc(Pageable pageable);

    @Query("select new eu.paasword.repository.relational.domain.Application(a.id, a.name, a.version, a.rootPackage, a.description, a.pep, a.annotatedCodePEP, a.dataModel, a.annotatedCodeDataModel, a.enabled, a.dateCreated) from Application a where a.id = ?1")
    Application findOneWithoutBlob(long id);

}
