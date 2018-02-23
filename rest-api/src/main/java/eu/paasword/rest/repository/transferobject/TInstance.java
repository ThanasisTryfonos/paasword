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

import eu.paasword.repository.relational.domain.PropertyInstance;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author smantzouratos
 */
public class TInstance implements Serializable {

    private long id;
    private long classID;
    private String instanceName;
    private long namespaceID;
    private List<TPropertyInstance> propertyInstances;

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

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public long getNamespaceID() {
        return namespaceID;
    }

    public void setNamespaceID(long namespaceID) {
        this.namespaceID = namespaceID;
    }

    public List<TPropertyInstance> getPropertyInstances() {
        return propertyInstances;
    }

    public void setPropertyInstances(List<TPropertyInstance> propertyInstances) {
        this.propertyInstances = propertyInstances;
    }

    public TInstance() {
    }
}
