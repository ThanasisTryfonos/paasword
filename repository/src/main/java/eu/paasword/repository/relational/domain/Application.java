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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import eu.paasword.util.Util;
import org.hibernate.annotations.*;
import org.hibernate.type.BlobType;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author smantzouratos
 */
@Entity
@Table(name = "application", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
public class Application implements Serializable {

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
    @Size(min = 1, max = 250)
    @Column(name = "tenant_key")
    private String tenantKey;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "version")
    private String version;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "root_package")
    private String rootPackage;

    @Basic(optional = false)
    @NotNull
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "bit(1) default 1", name = "pep")
    private boolean pep = false;

    @Basic(optional = true)
    @Column(name = "annotated_code_pep", columnDefinition = "TEXT")
    private String annotatedCodePEP;

    @Column(nullable = false, columnDefinition = "bit(1) default 1", name = "data_model")
    private boolean dataModel = false;

    @Basic(optional = true)
    @Column(name = "annotated_code_data_model", columnDefinition = "TEXT")
    private String annotatedCodeDataModel;

    @Column(name = "binary_file")
    @Basic(optional = true)
    @Lob
    private byte[] binary;

    @Basic(optional = true)
    @Size(min = 1, max = 250)
    @Column(name = "file_name")
    private String fileName;

    @JsonIgnore
    @Cascade(CascadeType.SAVE_UPDATE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "applicationID")
    private List<APIKey> apiKeys;

    @JsonIgnore
    @Cascade(CascadeType.SAVE_UPDATE)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "applicationID")
    private ApplicationInstance applicationInstance;

    @JsonIgnore
    @Cascade(CascadeType.SAVE_UPDATE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "applicationID")
    private List<ApplicationPrivacyConstraint> applicationPrivacyConstraints;

    @Column(nullable = false, columnDefinition = "bit(1) default 1", name = "enabled")
    private boolean enabled = true;

    @Column(nullable = false, name = "date_created")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Basic(optional = false)
    @Column(name = "last_modified", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private String lastModified;

    public Application() {
    }

    public Application(Long id, String name, String version, String rootPackage, String description, boolean pep, String annotatedCodePEP, boolean dataModel, String annotatedCodeDataModel, boolean enabled, Date dateCreated) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.rootPackage = rootPackage;
        this.description = description;
        this.pep = pep;
        this.annotatedCodePEP = annotatedCodePEP;
        this.dataModel = dataModel;
        this.annotatedCodeDataModel = annotatedCodeDataModel;
        this.enabled = enabled;
        this.dateCreated = dateCreated;
    }

    public String getTenantKey() {
        return tenantKey;
    }

    public void setTenantKey(String tenantKey) {
        this.tenantKey = tenantKey;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRootPackage() {
        return rootPackage;
    }

    public void setRootPackage(String rootPackage) {
        this.rootPackage = rootPackage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPep() {
        return pep;
    }

    public void setPep(boolean pep) {
        this.pep = pep;
    }

    public String getAnnotatedCodePEP() {
        return annotatedCodePEP;
    }

    public void setAnnotatedCodePEP(String annotatedCodePEP) {
        this.annotatedCodePEP = annotatedCodePEP;
    }

    public boolean isDataModel() {
        return dataModel;
    }

    public void setDataModel(boolean dataModel) {
        this.dataModel = dataModel;
    }

    public String getAnnotatedCodeDataModel() {
        return annotatedCodeDataModel;
    }

    public void setAnnotatedCodeDataModel(String annotatedCodeDataModel) {
        this.annotatedCodeDataModel = annotatedCodeDataModel;
    }

    public List<ApplicationPrivacyConstraint> getApplicationPrivacyConstraints() {
        return applicationPrivacyConstraints;
    }

    public void setApplicationPrivacyConstraints(List<ApplicationPrivacyConstraint> applicationPrivacyConstraints) {
        this.applicationPrivacyConstraints = applicationPrivacyConstraints;
    }

    public byte[] getBinary() {
        return binary;
    }

    public void setBinary(byte[] binary) {
        this.binary = binary;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<APIKey> getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(List<APIKey> apiKeys) {
        this.apiKeys = apiKeys;
    }

    public ApplicationInstance getApplicationInstance() {
        return applicationInstance;
    }

    public void setApplicationInstance(ApplicationInstance applicationInstance) {
        this.applicationInstance = applicationInstance;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

}

