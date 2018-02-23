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
public class KnowledgeTriple {
    
    InstanceOfClazz subject;
    ObjectProperty predicate;
    InstanceOfClazz object;    

    public KnowledgeTriple(InstanceOfClazz subject, ObjectProperty predicate, InstanceOfClazz object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }    
    
    public InstanceOfClazz getSubject() {
        return subject;
    }

    public void setSubject(InstanceOfClazz subject) {
        this.subject = subject;
    }

    public ObjectProperty getPredicate() {
        return predicate;
    }

    public void setPredicate(ObjectProperty predicate) {
        this.predicate = predicate;
    }

    public InstanceOfClazz getObject() {
        return object;
    }

    public void setObject(InstanceOfClazz object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return "KnowledgeTriple{" + "subject=" + subject + ", predicate=" + predicate + ", object=" + object + '}';
    }    
    
}
