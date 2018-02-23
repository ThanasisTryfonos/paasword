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
package eu.paasword.repository.relational.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author smantzouratos
 */
@Entity
@Table(name = "policy_set_policy", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
public class PolicySetPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue()
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policy_set_id", referencedColumnName = "id")
    @JsonBackReference
    @Cascade(CascadeType.SAVE_UPDATE)
    private PolicySet policySet;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policy_id", referencedColumnName = "id")
    @JsonBackReference
    @Cascade(CascadeType.SAVE_UPDATE)
    private Policy policy;

    public PolicySetPolicy() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PolicySet getPolicySet() {
        return policySet;
    }

    public void setPolicySet(PolicySet policySet) {
        this.policySet = policySet;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }
}
