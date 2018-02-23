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
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author smantzouratos
 */
@Entity
@Table(name = "application_instance", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
public class ApplicationInstance implements Serializable {

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
    @Size(min = 0, max = 250)
    @Column(unique = true, name = "unique_id")
    private String uniqueID;

    @Basic(optional = true)
    @Size(min = 0, max = 250)
    @Column(unique = true, name = "validator")
    private String validator;

    @Basic(optional = true)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Basic(optional = true)
    @Size(min = 1, max = 250)
    @Column(name = "encryption_algorithm")
    private String encryptionAlgorithm;

    @Basic(optional = true)
    @Size(min = 1, max = 250)
    @Column(name = "running_endpoint_url")
    private String runningEndpointURL;

    @Basic(optional = true)
    @Size(min = 1, max = 250)
    @Column(name = "git_url")
    private String gitURL;

    @Column(nullable = false, name = "deployment_type")
    @Basic(optional = true)
    private int deploymentType;

    @Column(nullable = false, name = "db_proxy_deployment_type")
    @Basic(optional = true)
    private int dbProxyDeploymentType;

    @Basic(optional = true)
    @Column(name = "privacy_constraints_set", columnDefinition = "TEXT")
    private String privacyConstraintsSet;

    @Basic(optional = true)
    @Column(name = "affinity_constraints_set", columnDefinition = "TEXT")
    private String affinityConstraintsSet;

    @Basic(optional = true)
    @Column(name = "iaas_providers", columnDefinition = "TEXT")
    private String iaasProviders;

    @Basic(optional = true)
    @Column(name = "iaas_provider_instances", columnDefinition = "TEXT")
    private String iaasProviderInstances;

    @Basic(optional = true)
    @Column(name = "location_constraints", columnDefinition = "TEXT")
    private String locationConstraints;

    @Basic(optional = true)
    @Column(name = "fragmentation_schema", columnDefinition = "TEXT")
    private String fragmentationSchema;

    @Basic(optional = true)
    @Column(name = "configuration_file", columnDefinition = "TEXT")
    private String configurationFile;

    @JoinColumn(name = "application_id", referencedColumnName = "id")
    @OneToOne(optional = true)
    @Cascade(CascadeType.SAVE_UPDATE)
    @JsonIgnore
    private Application applicationID;

    @JoinColumn(name = "paas_provider_id", referencedColumnName = "id")
    @OneToOne(optional = true)
    @Cascade(CascadeType.SAVE_UPDATE)
    @Basic(optional = true)
    private PaaSProvider paaSProviderID;

    @Column(nullable = false, name = "overall_status")
    @NotNull
    @Basic(optional = false)
    private int overallStatus;

    @Column(nullable = false, name = "date_created")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Basic(optional = false)
    @Column(name = "last_modified", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private String lastModified;

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    @JsonManagedReference
//    @JsonIgnore
    @Cascade(CascadeType.SAVE_UPDATE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "applicationInstanceID")
    private List<ApplicationInstanceHandler> applicationInstanceHandlers;

    @JsonManagedReference
//    @JsonIgnore
    @Cascade(CascadeType.SAVE_UPDATE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "applicationInstanceID")
    private List<ApplicationInstanceUser> applicationInstanceUsers;

    public ApplicationInstance() {
    }

    public ApplicationInstance(Long id, String name, String uniqueID, String validator, String description, String encryptionAlgorithm, String privacyConstraintsSet, String affinityConstraintsSet, String locationConstraints, String fragmentationSchema, String configurationFile, String lastModified, Date dateCreated, String iaasProviders, String iaasProviderInstances, String runningEndpointURL, String gitURL, PaaSProvider paaSProviderID, int overallStatus, int deploymentType, int dbProxyDeploymentType) {
        this.id = id;
        this.name = name;
        this.uniqueID = uniqueID;
        this.validator = validator;
        this.description = description;
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.privacyConstraintsSet = privacyConstraintsSet;
        this.affinityConstraintsSet = affinityConstraintsSet;
        this.locationConstraints = locationConstraints;
        this.fragmentationSchema = fragmentationSchema;
        this.configurationFile = configurationFile;
        this.lastModified = lastModified;
        this.dateCreated = dateCreated;
        this.iaasProviders = iaasProviders;
        this.iaasProviderInstances = iaasProviderInstances;
        this.paaSProviderID = paaSProviderID;
        this.runningEndpointURL = runningEndpointURL;
        this.gitURL = gitURL;
        this.overallStatus = overallStatus;
        this.deploymentType = deploymentType;
        this.dbProxyDeploymentType = dbProxyDeploymentType;
    }

    public ApplicationInstance(Long id, String name, String uniqueID, String validator, String description, String encryptionAlgorithm, String privacyConstraintsSet, String affinityConstraintsSet, String locationConstraints, String fragmentationSchema, String configurationFile, String lastModified, Date dateCreated, String iaasProviders, String iaasProviderInstances, String runningEndpointURL, String gitURL, int overallStatus, int deploymentType, int dbProxyDeploymentType) {
        this.id = id;
        this.name = name;
        this.uniqueID = uniqueID;
        this.validator = validator;
        this.description = description;
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.privacyConstraintsSet = privacyConstraintsSet;
        this.affinityConstraintsSet = affinityConstraintsSet;
        this.locationConstraints = locationConstraints;
        this.fragmentationSchema = fragmentationSchema;
        this.configurationFile = configurationFile;
        this.lastModified = lastModified;
        this.dateCreated = dateCreated;
        this.iaasProviders = iaasProviders;
        this.iaasProviderInstances = iaasProviderInstances;
        this.runningEndpointURL = runningEndpointURL;
        this.gitURL = gitURL;
        this.overallStatus = overallStatus;
        this.deploymentType = deploymentType;
        this.dbProxyDeploymentType = dbProxyDeploymentType;
    }

    public String getTenantKey() {
        return tenantKey;
    }

    public void setTenantKey(String tenantKey) {
        this.tenantKey = tenantKey;
    }

    public int getDbProxyDeploymentType() {
        return dbProxyDeploymentType;
    }

    public void setDbProxyDeploymentType(int dbProxyDeploymentType) {
        this.dbProxyDeploymentType = dbProxyDeploymentType;
    }

    public int getDeploymentType() {
        return deploymentType;
    }

    public void setDeploymentType(int deploymentType) {
        this.deploymentType = deploymentType;
    }

    public int getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(int overallStatus) {
        this.overallStatus = overallStatus;
    }

    public String getRunningEndpointURL() {
        return runningEndpointURL;
    }

    public void setRunningEndpointURL(String runningEndpointURL) {
        this.runningEndpointURL = runningEndpointURL;
    }

    public String getGitURL() {
        return gitURL;
    }

    public void setGitURL(String gitURL) {
        this.gitURL = gitURL;
    }

    public String getIaasProviders() {
        return iaasProviders;
    }

    public void setIaasProviders(String iaasProviders) {
        this.iaasProviders = iaasProviders;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getLocationConstraints() {
        return locationConstraints;
    }

    public void setLocationConstraints(String locationConstraints) {
        this.locationConstraints = locationConstraints;
    }

    public String getFragmentationSchema() {
        return fragmentationSchema;
    }

    public void setFragmentationSchema(String fragmentationSchema) {
        this.fragmentationSchema = fragmentationSchema;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public String getConfigurationFile() {
        return configurationFile;
    }

    public void setConfigurationFile(String configurationFile) {
        this.configurationFile = configurationFile;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrivacyConstraintsSet() {
        return privacyConstraintsSet;
    }

    public void setPrivacyConstraintsSet(String privacyConstraintsSet) {
        this.privacyConstraintsSet = privacyConstraintsSet;
    }

    public String getAffinityConstraintsSet() {
        return affinityConstraintsSet;
    }

    public void setAffinityConstraintsSet(String affinityConstraintsSet) {
        this.affinityConstraintsSet = affinityConstraintsSet;
    }

    public Application getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(Application applicationID) {
        this.applicationID = applicationID;
    }

    public PaaSProvider getPaaSProviderID() {
        return paaSProviderID;
    }

    public void setPaaSProviderID(PaaSProvider paaSProviderID) {
        this.paaSProviderID = paaSProviderID;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public int getNeededServers() {
        return new JSONArray(this.fragmentationSchema).length() + 2;
    }

    public List<String> getConstraints() {

        List<String> constraints = new ArrayList<>();

        if (null != this.privacyConstraintsSet && !this.privacyConstraintsSet.isEmpty()) {

            JSONArray constraintsArray = new JSONArray(this.privacyConstraintsSet);

            for (int i = 0; i < constraintsArray.length(); i++) {

                constraints.add(constraintsArray.getString(i));

            }
        }

        return constraints;
    }

    public List<String> getAffinityConstraints() {

        List<String> constraints = new ArrayList<>();

        JSONArray constraintsArray = new JSONArray(this.affinityConstraintsSet);

        for (int i=0; i < constraintsArray.length(); i++) {

            constraints.add(constraintsArray.getString(i));

        }

        return constraints;
    }

    public String getIaasProviderInstances() {
        return iaasProviderInstances;
    }

    public void setIaasProviderInstances(String iaasProviderInstances) {
        this.iaasProviderInstances = iaasProviderInstances;
    }

    public List<String> getUsedIaaSProviders() {

        List<String> iaasProviders = new ArrayList<>();

        JSONArray iaasProvidersArray = new JSONArray(this.iaasProviders);

        for (int i=0; i < iaasProvidersArray.length(); i++) {

            iaasProviders.add(iaasProvidersArray.getString(i));

        }

        return iaasProviders;
    }

    public List<String> getFragmentsForUI() {

        List<String> listOfFragments = new ArrayList<>();

        JSONArray fragmentsArray = new JSONArray(this.fragmentationSchema);

        for (Object innerFragment : fragmentsArray) {

            String innerFragmentStr = "";

            JSONArray innerFragmentArray = (JSONArray) innerFragment;

            for (Object field : innerFragmentArray) {

                String fieldStr = (String) field;

                innerFragmentStr += fieldStr + ", ";

            }

            listOfFragments.add(innerFragmentStr.substring(0, innerFragmentStr.length() - 2));

        }

        return listOfFragments;

    }

    public String getTimestamp() {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return formatter.format(this.dateCreated);
    }

    public String getOverallStatusMSG() {
        String msg = "-";
        switch (this.overallStatus) {
            case 0:
                msg = "Configuration needed";
                break;
            case 2:
                msg = "DB Proxy not initialized yet";
                break;
            case 3:
                msg = "DB Proxy initialization pending";
                break;
            case 4:
                msg = "DB Proxy deployment failed";
                break;
            case 5:
                msg = "DB Proxy deployed successfully";
                break;
            case 6:
                msg = "DB Proxy is initializing";
                break;
            case 7:
                msg = "Application Instance deployment not configured yet";
                break;
            case 8:
                msg = "Application Instance deployment pending";
                break;
            case 9:
                msg = "Application Instance deployment failed";
                break;
            case 10:
                msg = "Application Instance deployed successfully";
                break;
            case 11:
                msg = "Application Instance is running";
                break;
            case 12:
                msg = "Application Instance is deploying";
                break;
            default:
                msg = "-";
                break;
        }

        return msg;
    }

    public List<String> getFragmentationInstancesForUI() {

        List<String> listOfFragmentationInstances = new ArrayList<>();

        if (null != this.iaasProviderInstances && !this.iaasProviderInstances.isEmpty()) {

            JSONArray fragInstancesArray = new JSONArray(this.iaasProviderInstances);

            int counter = 0;

            for (Object fragInstance : fragInstancesArray) {

                counter++;

                String innerFragmentInstStr = "";

                JSONObject innerFragmentInst = (JSONObject) fragInstance;

                innerFragmentInstStr = counter + ". DB Host: " + innerFragmentInst.getString("dbHost") + ", DB Name: " + innerFragmentInst.getString("dbName") + ", IaaS Provider: " + innerFragmentInst.getString("iaasFriendlyName");

                listOfFragmentationInstances.add(innerFragmentInstStr);

            }

        }

        return listOfFragmentationInstances;

    }

    public List<ApplicationInstanceHandler> getApplicationInstanceHandlers() {
        return applicationInstanceHandlers;
    }

    public void setApplicationInstanceHandlers(List<ApplicationInstanceHandler> applicationInstanceHandlers) {
        this.applicationInstanceHandlers = applicationInstanceHandlers;
    }

    public List<ApplicationInstanceUser> getApplicationInstanceUsers() {
        return applicationInstanceUsers;
    }

    public void setApplicationInstanceUsers(List<ApplicationInstanceUser> applicationInstanceUsers) {
        this.applicationInstanceUsers = applicationInstanceUsers;
    }
}

