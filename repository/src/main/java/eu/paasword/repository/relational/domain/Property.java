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
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author smantzouratos
 */
@Entity
@Table(name = "property", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
public class Property {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @JoinColumn(name = "class_id", referencedColumnName = "id")
    @OneToOne(optional = true)
    @JsonBackReference
    @Cascade(CascadeType.SAVE_UPDATE)
    private Clazz classID;

    @Column(nullable = false, columnDefinition = "bit(1) default 0", name = "object_property")
    private boolean objectProperty = false;

    @Column(nullable = false, columnDefinition = "tinyint(1) default 0", name = "transitivity")
    private int transitivity = 0;

    @JoinColumn(name = "sub_property_of_id", referencedColumnName = "id")
    @OneToOne(optional = true)
    @Cascade(CascadeType.SAVE_UPDATE)
    private Property subPropertyOfID;

    @JoinColumn(name = "property_type_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @OneToOne(optional = true)
    private PropertyType propertyTypeID;

    @JoinColumn(name = "object_property_class_id", referencedColumnName = "id")
    @OneToOne(optional = true)
    @Cascade(CascadeType.SAVE_UPDATE)
    private Clazz objectPropertyClassID;

    @JoinColumn(name = "namespace_id", referencedColumnName = "id")
    @OneToOne(optional = true)
    @Cascade(CascadeType.SAVE_UPDATE)
    private Namespace namespaceID;

    @Basic(optional = false)
    @Column(name = "last_modified", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private String lastModified;

    @Column(nullable = false, columnDefinition = "bit(1) default 1", name = "enabled")
    private boolean enabled = true;

    public Property() {
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Clazz getClassID() {
        return classID;
    }

    public void setClassID(Clazz classID) {
        this.classID = classID;
    }

    public boolean isObjectProperty() {
        return objectProperty;
    }

    public void setObjectProperty(boolean objectProperty) {
        this.objectProperty = objectProperty;
    }

    public PropertyType getPropertyTypeID() {
        return propertyTypeID;
    }

    public void setPropertyTypeID(PropertyType propertyTypeID) {
        this.propertyTypeID = propertyTypeID;
    }

    public Clazz getObjectPropertyClassID() {
        return objectPropertyClassID;
    }

    public void setObjectPropertyClassID(Clazz objectPropertyClassID) {
        this.objectPropertyClassID = objectPropertyClassID;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Property getSubPropertyOfID() {
        return subPropertyOfID;
    }

    public void setSubPropertyOfID(Property subPropertyOfID) {
        this.subPropertyOfID = subPropertyOfID;
    }

    public int getTransitivity() {
        return transitivity;
    }

    public void setTransitivity(int transitivity) {
        this.transitivity = transitivity;
    }
}
