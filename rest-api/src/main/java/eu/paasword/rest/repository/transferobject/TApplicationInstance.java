package eu.paasword.rest.repository.transferobject;

import java.io.Serializable;
import java.util.List;

/**
 * Created by smantzouratos on 26/04/16.
 */
public class TApplicationInstance implements Serializable {

    private long id;
    private long applicationID;
    private int dataModel;
    private int dbProxyDeploymentType;
    private int deploymentType;
    private String name;
    private String description;
    private String appKey;
    private String appInstanceKey;
    private List<String> privacyConstraintSetIDs;
    private List<String> affinityConstraintSetIDs;
    private List<String> iaasProviderIDs;
    private String locationConstraint;
    private String encryptionAlgorithm;
    private long paaSproviderID;

    public TApplicationInstance() {
    }

    public int getDeploymentType() {
        return deploymentType;
    }

    public void setDeploymentType(int deploymentType) {
        this.deploymentType = deploymentType;
    }

    public String getAppInstanceKey() {
        return appInstanceKey;
    }

    public void setAppInstanceKey(String appInstanceKey) {
        this.appInstanceKey = appInstanceKey;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public int getDbProxyDeploymentType() {
        return dbProxyDeploymentType;
    }

    public void setDbProxyDeploymentType(int dbProxyDeploymentType) {
        this.dbProxyDeploymentType = dbProxyDeploymentType;
    }

    public int getDataModel() {
        return dataModel;
    }

    public void setDataModel(int dataModel) {
        this.dataModel = dataModel;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(long applicationID) {
        this.applicationID = applicationID;
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

    public List<String> getPrivacyConstraintSetIDs() {
        return privacyConstraintSetIDs;
    }

    public void setPrivacyConstraintSetIDs(List<String> privacyConstraintSetIDs) {
        this.privacyConstraintSetIDs = privacyConstraintSetIDs;
    }

    public String getLocationConstraint() {
        return locationConstraint;
    }

    public void setLocationConstraint(String locationConstraint) {
        this.locationConstraint = locationConstraint;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public long getPaaSproviderID() {
        return paaSproviderID;
    }

    public void setPaaSproviderID(long paaSproviderID) {
        this.paaSproviderID = paaSproviderID;
    }

    public List<String> getIaasProviderIDs() {
        return iaasProviderIDs;
    }

    public void setIaasProviderIDs(List<String> iaasProviderIDs) {
        this.iaasProviderIDs = iaasProviderIDs;
    }

    public List<String> getAffinityConstraintSetIDs() {
        return affinityConstraintSetIDs;
    }

    public void setAffinityConstraintSetIDs(List<String> affinityConstraintSetIDs) {
        this.affinityConstraintSetIDs = affinityConstraintSetIDs;
    }
}
