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
package eu.paasword.triplestoreapi.parser.drools;

/**
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class ObjectProperty {

    private String name;
    private Clazz domain;
    private Clazz range;
    private boolean transitive;
    private ObjectProperty parent;
    
    public ObjectProperty(String name, Clazz domain, Clazz range) {
        this.name = name;
        this.domain = domain;
        this.range = range;
        this.parent = null;
        this.transitive = false;
    }    

    public ObjectProperty(String name, Clazz domain, Clazz range, ObjectProperty parent) {
        this.name = name;
        this.domain = domain;
        this.range = range;
        this.parent = parent;
        this.transitive = false;
    }   

    public ObjectProperty(String name, Clazz domain, Clazz range, boolean transitive) {
        this.name = name;
        this.domain = domain;
        this.range = range;
        this.transitive = transitive;
    }
    
    public ObjectProperty(String name, Clazz domain, Clazz range, boolean transitive, ObjectProperty parent) {
        this.name = name;
        this.domain = domain;
        this.range = range;
        this.transitive = transitive;
        this.parent = parent;
    }    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Clazz getDomain() {
        return domain;
    }

    public void setDomain(Clazz domain) {
        this.domain = domain;
    }

    public Clazz getRange() {
        return range;
    }

    public void setRange(Clazz range) {
        this.range = range;
    }

    public ObjectProperty getParent() {
        return parent;
    }

    public void setParent(ObjectProperty parent) {
        this.parent = parent;
    }        

    public boolean isTransitive() {
        return transitive;
    }

    public void setTransitive(boolean transitive) {
        this.transitive = transitive;
    }    
    
    @Override
    public String toString() {
        return "ObjectProperty{" + "name=" + name + ", domain=" + domain + ", range=" + range + ", parent=" + parent + '}';
    }
    
}
