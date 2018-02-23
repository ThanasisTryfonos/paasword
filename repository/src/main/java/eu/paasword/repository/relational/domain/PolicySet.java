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

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author smantzouratos
 */
@Entity
@Table(name = "policy_set", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
public class PolicySet implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "name")
    private String policySetName;

    @Basic(optional = true)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JoinColumn(name = "combining_algorithm_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @OneToOne(optional = true)
    private CombiningAlgorithm combiningAlgorithmID;

    @Cascade(CascadeType.SAVE_UPDATE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "policySet")
    @JsonManagedReference
    private List<PolicySetPolicy> policySetPolicies;

    @JoinColumn(name = "namespace_id", referencedColumnName = "id")
    @OneToOne(optional = true)
    @Cascade(CascadeType.SAVE_UPDATE)
    private Namespace namespaceID;

    @Basic(optional = false)
    @Column(name = "last_modified", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private String lastModified;

    @Column(nullable = false, name = "date_created")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dateCreated;

    public PolicySet() {
    }

    public Namespace getNamespaceID() {
        return namespaceID;
    }

    public void setNamespaceID(Namespace namespaceID) {
        this.namespaceID = namespaceID;
    }

    public CombiningAlgorithm getCombiningAlgorithmID() {
        return combiningAlgorithmID;
    }

    public void setCombiningAlgorithmID(CombiningAlgorithm combiningAlgorithmID) {
        this.combiningAlgorithmID = combiningAlgorithmID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPolicySetName() {
        return policySetName;
    }

    public void setPolicySetName(String policySetName) {
        this.policySetName = policySetName;
    }

    public List<PolicySetPolicy> getPolicySetPolicies() {
        return policySetPolicies;
    }

    public void setPolicySetPolicies(List<PolicySetPolicy> policySetPolicies) {
        this.policySetPolicies = policySetPolicies;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
