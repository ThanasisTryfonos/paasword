package eu.paasword.spi.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by smantzouratos on 23/12/2016.
 */
public class RunningInstanceModel implements Serializable {

    private String resourceURI;
    private String UUID;
    private String moduleResourceURI;
    private UserModel user;
    private String status;
    private String abort;
    private String type;
    private String tags;
    private String serviceURL;
    private int activeVM;
    private Date startTime;
    private CloudProviderModel cloudProvider;

    public RunningInstanceModel() {
    }

    public String getAbort() {
        return abort;
    }

    public void setAbort(String abort) {
        this.abort = abort;
    }

    public String getResourceURI() {
        return resourceURI;
    }

    public void setResourceURI(String resourceURI) {
        this.resourceURI = resourceURI;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getModuleResourceURI() {
        return moduleResourceURI;
    }

    public void setModuleResourceURI(String moduleResourceURI) {
        this.moduleResourceURI = moduleResourceURI;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public int getActiveVM() {
        return activeVM;
    }

    public void setActiveVM(int activeVM) {
        this.activeVM = activeVM;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public CloudProviderModel getCloudProvider() {
        return cloudProvider;
    }

    public void setCloudProvider(CloudProviderModel cloudProvider) {
        this.cloudProvider = cloudProvider;
    }
}
