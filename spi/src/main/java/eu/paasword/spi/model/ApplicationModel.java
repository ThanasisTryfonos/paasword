package eu.paasword.spi.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by smantzouratos on 30/11/2016.
 */
public class ApplicationModel implements Serializable {

    private Long id;
    private String name;
    private String runningEndpoint;
    private String remoteGitURL;
    private ApplicationRequestModel applicationRequest;
    private PaaSOfferingModel paaSOffering;
    private StackModel stack;
    private List<ServiceModel> services;

    private ApplicationStateTypeModel applicationStateTypeModel;

    private DeploymentTypeModel deploymentTypeModel;

    public ApplicationModel() {
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

    public String getRunningEndpoint() {
        return runningEndpoint;
    }

    public void setRunningEndpoint(String runningEndpoint) {
        this.runningEndpoint = runningEndpoint;
    }

    public String getRemoteGitURL() {
        return remoteGitURL;
    }

    public void setRemoteGitURL(String remoteGitURL) {
        this.remoteGitURL = remoteGitURL;
    }

    public ApplicationRequestModel getApplicationRequest() {
        return applicationRequest;
    }

    public void setApplicationRequest(ApplicationRequestModel applicationRequest) {
        this.applicationRequest = applicationRequest;
    }

    public PaaSOfferingModel getPaaSOffering() {
        return paaSOffering;
    }

    public void setPaaSOffering(PaaSOfferingModel paaSOffering) {
        this.paaSOffering = paaSOffering;
    }

    public StackModel getStack() {
        return stack;
    }

    public void setStack(StackModel stack) {
        this.stack = stack;
    }

    public List<ServiceModel> getServices() {
        return services;
    }

    public void setServices(List<ServiceModel> services) {
        this.services = services;
    }

    public ApplicationStateTypeModel getApplicationStateTypeModel() {
        return applicationStateTypeModel;
    }

    public void setApplicationStateTypeModel(ApplicationStateTypeModel applicationStateTypeModel) {
        this.applicationStateTypeModel = applicationStateTypeModel;
    }

    public DeploymentTypeModel getDeploymentTypeModel() {
        return deploymentTypeModel;
    }

    public void setDeploymentTypeModel(DeploymentTypeModel deploymentTypeModel) {
        this.deploymentTypeModel = deploymentTypeModel;
    }
}
