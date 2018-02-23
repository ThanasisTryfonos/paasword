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
import org.json.JSONArray;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author smantzouratos
 */
@Entity
@Table(name = "application_affinity_constraint", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
public class ApplicationAffinityConstraint implements Serializable {

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
    private String name;

    @Basic(optional = true)
    @Column(name = "affinity_constraint", columnDefinition = "TEXT")
    private String affinityConstraint;

    @JoinColumn(name = "application_id", referencedColumnName = "id")
    @OneToOne(optional = true)
    @Cascade(CascadeType.SAVE_UPDATE)
    @JsonBackReference
    private Application applicationID;

    @Column(nullable = false, name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Basic(optional = false)
    @Column(name = "last_modified", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private String lastModified;

    public ApplicationAffinityConstraint() {

    }

    public ApplicationAffinityConstraint(Long id, String name, String affinityConstraint, String lastModified, Date dateCreated) {
        this.id = id;
        this.name = name;
        this.affinityConstraint = affinityConstraint;
        this.lastModified = lastModified;
        this.dateCreated = dateCreated;
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

    public String getAffinityConstraint() {
        return affinityConstraint;
    }

    public void setAffinityConstraint(String affinityConstraint) {
        this.affinityConstraint = affinityConstraint;
    }

    public Application getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(Application applicationID) {
        this.applicationID = applicationID;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public List<String> getConstraints() {

        List<String> constraints = new ArrayList<>();

        JSONArray constraintsArray = new JSONArray(this.getAffinityConstraint());

        for (int i=0; i < constraintsArray.length(); i++) {

            constraints.add(constraintsArray.getString(i));

        }

        return constraints;
    }
}

