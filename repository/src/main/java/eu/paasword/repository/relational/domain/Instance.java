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

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 *
 * @author smantzouratos
 */
@Entity
@Table(name = "instance", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
public class Instance implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "instance_name")
    private String instanceName;

    @JoinColumn(name = "class_id", referencedColumnName = "id")
    @OneToOne(optional = true)
    @Cascade(CascadeType.SAVE_UPDATE)
    @JsonBackReference
    private Clazz classID;

    @JoinColumn(name = "namespace_id", referencedColumnName = "id")
    @OneToOne(optional = true)
    @Cascade(CascadeType.SAVE_UPDATE)
    private Namespace namespaceID;

    @JsonManagedReference
    @Cascade(CascadeType.SAVE_UPDATE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "instanceID")
    private List<PropertyInstance> propertyInstances;

    @Basic(optional = false)
    @Column(name = "last_modified", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private String lastModified;

    public Namespace getNamespaceID() {
        return namespaceID;
    }

    public void setNamespaceID(Namespace namespaceID) {
        this.namespaceID = namespaceID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public Clazz getClassID() {
        return classID;
    }

    public void setClassID(Clazz classID) {
        this.classID = classID;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public List<PropertyInstance> getPropertyInstances() {
        return propertyInstances;
    }

    public void setPropertyInstances(List<PropertyInstance> propertyInstances) {
        this.propertyInstances = propertyInstances;
    }

    public Instance() {

    }

    public boolean hasPropertyInstanceByPropertyID(long propertyID) {

        boolean propertyInstanceExists = false;

        if (!propertyInstances.isEmpty()) {

            for (PropertyInstance propInst : propertyInstances) {

                if (propInst.getPropertyID().getId() == propertyID) {
                    propertyInstanceExists = true;
                }

            }

        }

        return propertyInstanceExists;
    }

    public PropertyInstance getPropertyInstanceByPropertyID(long propertyID) {

        if (!propertyInstances.isEmpty()) {

            for (PropertyInstance propInst : propertyInstances) {

                if (propInst.getPropertyID().getId() == propertyID) {
                    return propInst;
                }

            }

        }

        return null;
    }
}
