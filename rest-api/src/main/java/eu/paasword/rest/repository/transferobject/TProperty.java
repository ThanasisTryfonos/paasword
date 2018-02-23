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
package eu.paasword.rest.repository.transferobject;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author smantzouratos
 */
public class TProperty implements Serializable {

    private long id;
    private long classID;
    private String name;
    private long namespaceID;
    private long propertyTypeID;
    private long objectPropertyClassID;
    private long subPropertyOfID;
    private int transitivity;
    private boolean objectProperty;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getClassID() {
        return classID;
    }

    public void setClassID(long classID) {
        this.classID = classID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getNamespaceID() {
        return namespaceID;
    }

    public void setNamespaceID(long namespaceID) {
        this.namespaceID = namespaceID;
    }

    public long getPropertyTypeID() {
        return propertyTypeID;
    }

    public void setPropertyTypeID(long propertyTypeID) {
        this.propertyTypeID = propertyTypeID;
    }

    public long getObjectPropertyClassID() {
        return objectPropertyClassID;
    }

    public void setObjectPropertyClassID(long objectPropertyClassID) {
        this.objectPropertyClassID = objectPropertyClassID;
    }

    public boolean isObjectProperty() {
        return objectProperty;
    }

    public void setObjectProperty(boolean objectProperty) {
        this.objectProperty = objectProperty;
    }

    public long getSubPropertyOfID() {
        return subPropertyOfID;
    }

    public void setSubPropertyOfID(long subPropertyOfID) {
        this.subPropertyOfID = subPropertyOfID;
    }

    public int getTransitivity() {
        return transitivity;
    }

    public void setTransitivity(int transitivity) {
        this.transitivity = transitivity;
    }

    public TProperty() {
    }
}
