package eu.paasword.spi.adapter;

import eu.paasword.spi.model.*;
import eu.paasword.spi.response.SPIResponse;

/**
 * Created by smantzouratos on 30/11/2016.
 */
public interface ProxyAdapter {

    public SPIResponse validateCredentials(CredentialsModel credentials);

    public SPIResponse getCloudProviders(CredentialsModel credentials);

    public SPIResponse getVirtualMachines(CredentialsModel credentials);

    public SPIResponse getRunningInstances(CredentialsModel credentials);

    public SPIResponse getUsages(CredentialsModel credentials);

}
