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

import eu.paasword.repository.relational.domain.Expression;
import eu.paasword.repository.relational.domain.Rule;
import java.util.List;
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
public interface RuleRepository extends JpaRepository<Rule, Long> {

    /**
     *
     * @param ruleName
     * @return
     */
    @Query("select r from Rule r where r.ruleName = ?1")
    public Rule findByRuleName(String ruleName);

    /**
     * Fetch all rules from database that make use of the given expression_id.
     *
     * @param expression
     * @return
     */
    public List<Rule> findByExpressionID(Expression expression);
    
    

}
