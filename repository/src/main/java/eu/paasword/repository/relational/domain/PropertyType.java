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

import javax.persistence.*;

/**
 *
 * @author smantzouratos
 */
@Entity
@Table(name = "property_type", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
public class PropertyType {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;    
    
    @Column(nullable = false)
    private String name;

    @Basic(optional = false)
    @Column(name = "last_modified", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private String lastModified;

    @Column(nullable = false, columnDefinition = "bit(1) default 1", name = "enabled")
    private boolean enabled = true;

    @Column(nullable = true, name ="regexp_rule")
    private String regexpRule;

    @Column(nullable = true, name ="schema_xsd")
    private String schemaXSD;

    public PropertyType() {
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

    public String getRegexpRule() {
        return regexpRule;
    }

    public void setRegexpRule(String regexpRule) {
        this.regexpRule = regexpRule;
    }

    public String getSchemaXSD() {
        return schemaXSD;
    }

    public void setSchemaXSD(String schemaXSD) {
        this.schemaXSD = schemaXSD;
    }
}
