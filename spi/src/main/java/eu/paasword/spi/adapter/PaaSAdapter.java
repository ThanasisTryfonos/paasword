package eu.paasword.spi.adapter;

import eu.paasword.spi.model.*;
import eu.paasword.spi.response.SPIResponse;

/**
 * Created by smantzouratos on 30/11/2016.
 */
public interface PaaSAdapter {

    public SPIResponse validateCredentials(CredentialsModel credentials, PaaSOfferingModel paasOffering);

    public SPIResponse createApplication(CredentialsModel credentials, ApplicationRequestModel applicationRequest);

    public SPIResponse deployApplication(CredentialsModel credentials, ApplicationModel application, PackageLocatorModel packageLocator);

    public SPIResponse undeployApplication(CredentialsModel credentials, ApplicationModel application);

    public SPIResponse startApplication(CredentialsModel credentials, ApplicationModel application);

    public SPIResponse stopApplication(CredentialsModel credentials, ApplicationModel application);

    public SPIResponse deleteApplication(CredentialsModel credentials, ApplicationModel application);

    public SPIResponse registerSSHKey();

    public SPIResponse removeSSHKey();

    public SPIResponse getAvailableServices(CredentialsModel credentials, PaaSOfferingModel paaSOffering);

    public SPIResponse getAvailableStacks(CredentialsModel credentials, PaaSOfferingModel paaSOffering);

    public SPIResponse createServiceBinding(CredentialsModel credentials, ApplicationModel application, ServiceModel service);

    public SPIResponse getServiceBinding();

    public SPIResponse updateServiceBinding();

    public SPIResponse deleteServiceBinding(CredentialsModel credentials, ApplicationModel application, ServiceModel service);

    public SPIResponse getAllBoundServices(CredentialsModel credentials, ApplicationModel application);

}
