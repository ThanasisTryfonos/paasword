package eu.paasword.spi.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by smantzouratos on 30/11/2016.
 */
public class PaaSOfferingModel implements Serializable {

    private Long id;
    private String name;
    private String adapterImplementation;
    private String endpointURI;
    private DeploymentTypeModel deploymentTypeModel;
    private AuthenticationTypeModel authenticationTypeModel;
    private PaaSProviderModel paasProvider;
    private List<ServiceModel> services;
    private List<StackModel> stacks;

    public PaaSOfferingModel() {
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

    public String getAdapterImplementation() {
        return adapterImplementation;
    }

    public void setAdapterImplementation(String adapterImplementation) {
        this.adapterImplementation = adapterImplementation;
    }

    public String getEndpointURI() {
        return endpointURI;
    }

    public void setEndpointURI(String endpointURI) {
        this.endpointURI = endpointURI;
    }

    public PaaSProviderModel getPaasProvider() {
        return paasProvider;
    }

    public void setPaasProvider(PaaSProviderModel paasProvider) {
        this.paasProvider = paasProvider;
    }

    public List<ServiceModel> getServices() {
        return services;
    }

    public void setServices(List<ServiceModel> services) {
        this.services = services;
    }

    public List<StackModel> getStacks() {
        return stacks;
    }

    public void setStacks(List<StackModel> stacks) {
        this.stacks = stacks;
    }

    public DeploymentTypeModel getDeploymentTypeModel() {
        return deploymentTypeModel;
    }

    public void setDeploymentTypeModel(DeploymentTypeModel deploymentTypeModel) {
        this.deploymentTypeModel = deploymentTypeModel;
    }

    public AuthenticationTypeModel getAuthenticationTypeModel() {
        return authenticationTypeModel;
    }

    public void setAuthenticationTypeModel(AuthenticationTypeModel authenticationTypeModel) {
        this.authenticationTypeModel = authenticationTypeModel;
    }
}
