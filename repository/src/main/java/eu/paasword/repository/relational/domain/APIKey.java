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
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * @author smantzouratos
 */
@Entity
@Table(name = "api_key")
public class APIKey {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 0, max = 250)
    @Column(unique = true, name = "description")
    private String description;

    @JsonIgnore
    @Basic(optional = false)
    @NotNull
    @Size(min = 0, max = 250)
    @Column(unique = true, name = "unique_id")
    private String uniqueID;

    @Column(nullable = false, columnDefinition = "bit(1) default 1")
    private boolean enabled = true;

    @Basic(optional = true)
    @Size(min = 0, max = 250)
    @Column(nullable = true, name = "invocation_ip")
    private String invocationIP;

    @Basic(optional = false)
    @Column(name = "last_modified", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private String lastModified;

    @JoinColumn(name = "application_id", referencedColumnName = "id")
    @OneToOne(optional = true)
    @Cascade(CascadeType.SAVE_UPDATE)
    private Application applicationID;

    public APIKey() {
    }

    public APIKey(Long id, String description, String uniqueID, String lastModified, String invocationIP) {
        this.id = id;
        this.description = description;
        this.uniqueID = uniqueID;
        this.lastModified = lastModified;
        this.invocationIP = invocationIP;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getInvocationIP() {
        return invocationIP;
    }

    public void setInvocationIP(String invocationIP) {
        this.invocationIP = invocationIP;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public Application getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(Application applicationID) {
        this.applicationID = applicationID;
    }
}
