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

import eu.paasword.repository.relational.domain.IaaSProvider;
import java.util.List;
import eu.paasword.repository.relational.domain.User;
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
public interface IaaSProviderRepository extends JpaRepository<IaaSProvider, Long> {

    public IaaSProvider findByFriendlyName(String friendlyName);

    @Query("select i from IaaSProvider i where i.userID.username =?1 ")
    public List<IaaSProvider> getIaaSProvidersByUsername(String username);

    public Page<IaaSProvider> findByUserID(User user, Pageable pageable);
           
}//EoI
