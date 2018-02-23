package eu.paasword.adapter.openshift;

import com.openshift.client.*;
import com.openshift.client.cartridge.IEmbeddableCartridge;
import com.openshift.client.cartridge.IEmbeddedCartridge;
import com.openshift.client.cartridge.IStandaloneCartridge;
import com.openshift.client.cartridge.query.LatestEmbeddableCartridge;
import com.openshift.client.cartridge.query.LatestStandaloneCartridge;
import eu.paasword.spi.adapter.PaaSAdapter;
import eu.paasword.spi.model.*;
import eu.paasword.spi.response.BasicResponseCode;
import eu.paasword.spi.response.SPIResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by smantzouratos on 21/09/16.
 */
public class OpenShiftAdapter implements PaaSAdapter {

    private static final Logger logger = Logger.getLogger(OpenShiftAdapter.class.getName());

    @Override
    public SPIResponse validateCredentials(CredentialsModel credentialsModel, PaaSOfferingModel paasOfferingModel) {

        logger.info("OpenShift Adapter invoked for deployment validation of user: " + credentialsModel.getUsername());

        try {

            IOpenShiftConnection connection = new ConnectionBuilder(paasOfferingModel.getEndpointURI().substring(7)).credentials(credentialsModel.getUsername(), credentialsModel.getPassword()).disableSSLCertificateChecks().create();

            if (null != connection && null != connection.getUser()) {

                logger.info("Credentials validated successfully!");
                return new SPIResponse(BasicResponseCode.SUCCESS, "Credentials validated successfully!", paasOfferingModel);
            } else {
                logger.info("Credentials are invalid!");
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Credentials are invalid!");
            }

        } catch (OpenShiftException | IOException e) {
            logger.severe(e.getMessage());
//            logger.info("Credentials are invalid!");
            return new SPIResponse(BasicResponseCode.EXCEPTION, e.getMessage());
        }


    }

    @Override
    public SPIResponse createApplication(CredentialsModel credentialsModel, ApplicationRequestModel applicationRequestModel) {

        logger.info("OpenShift Adapter invoked for creating application: " + applicationRequestModel.getName() + ", for user: " + credentialsModel.getUsername());

        ApplicationModel createdApp = new ApplicationModel();
        createdApp.setApplicationStateTypeModel(ApplicationStateTypeModel.CREATING);
        createdApp.setName(applicationRequestModel.getName());
        createdApp.setApplicationRequest(applicationRequestModel);

        try {

            IOpenShiftConnection connection = new ConnectionBuilder(credentialsModel.getPaaSOffering().getEndpointURI().substring(7)).credentials(credentialsModel.getUsername(), credentialsModel.getPassword()).disableSSLCertificateChecks().create();

            if (null != connection) {

                IUser user = connection.getUser();
                IDomain domain = connection.getUser().getDefaultDomain();

                //TODO
                IApplication application = domain.createApplication(applicationRequestModel.getName(), new LatestStandaloneCartridge(applicationRequestModel.getStackName()).get(user));

//                application.stop();

                String endPoint = application.getApplicationUrl();
                String gitURL = application.getGitUrl();

                if (endPoint.indexOf("://") == -1) {
                    endPoint = "http://" + endPoint;
                }

                createdApp.setRunningEndpoint(endPoint);
                createdApp.setRemoteGitURL(gitURL);
                createdApp.setApplicationStateTypeModel(ApplicationStateTypeModel.CREATED);

                logger.info("Application created successfully!");

                return new SPIResponse(BasicResponseCode.SUCCESS, "Application created successfully!", createdApp);
            } else {
                createdApp.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Application isn't created successfully!", createdApp);
            }

        } catch (OpenShiftException | IOException e) {
            logger.severe(e.getMessage());
            e.printStackTrace();
            return new SPIResponse(BasicResponseCode.EXCEPTION, e.getMessage());
        }

    }

    @Override
    public SPIResponse deployApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel, PackageLocatorModel packageLocatorModel) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }

    @Override
    public SPIResponse undeployApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }

    @Override
    public SPIResponse startApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {
        logger.info("OpenShift Adapter invoked for starting application: " + applicationModel.getName() + ", for user: " + credentialsModel.getUsername());

        applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.STARTING);

        try {

            IOpenShiftConnection connection = new ConnectionBuilder(credentialsModel.getPaaSOffering().getEndpointURI().substring(7)).credentials(credentialsModel.getUsername(), credentialsModel.getPassword()).disableSSLCertificateChecks().create();

            if (null != connection) {

                IDomain domain = connection.getUser().getDefaultDomain();
                IApplication application = domain.getApplicationByName(applicationModel.getName());
                application.start();

                logger.info("Application created successfully!");

                applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.STARTED);

                return new SPIResponse(BasicResponseCode.SUCCESS, "Application started successfully!", applicationModel);
            } else {
                applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Application isn't started successfully!", applicationModel);
            }

        } catch (OpenShiftException | IOException e) {
//            logger.severe(e.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, e.getMessage());
        }
    }

    @Override
    public SPIResponse stopApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {
        logger.info("OpenShift Adapter invoked for stopping application: " + applicationModel.getName() + ", for user: " + credentialsModel.getUsername());

        applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.STOPPING);

        try {

            IOpenShiftConnection connection = new ConnectionBuilder(credentialsModel.getPaaSOffering().getEndpointURI().substring(7)).credentials(credentialsModel.getUsername(), credentialsModel.getPassword()).disableSSLCertificateChecks().create();

            if (null != connection) {

                IDomain domain = connection.getUser().getDefaultDomain();
                IApplication application = domain.getApplicationByName(applicationModel.getName());
                application.stop();

                logger.info("Application stopped successfully!");

                applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.STOPPED);

                return new SPIResponse(BasicResponseCode.SUCCESS, "Application stopped successfully!", applicationModel);
            } else {
                applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Application isn't stopped successfully!", applicationModel);
            }

        } catch (OpenShiftException | IOException e) {
//            logger.severe(e.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, e.getMessage());
        }
    }

    @Override
    public SPIResponse deleteApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {
        logger.info("OpenShift Adapter invoked for deleting application: " + applicationModel.getName() + ", for user: " + credentialsModel.getUsername());

        applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.DELETING);

        try {

            IOpenShiftConnection connection = new ConnectionBuilder(credentialsModel.getPaaSOffering().getEndpointURI().substring(7)).credentials(credentialsModel.getUsername(), credentialsModel.getPassword()).disableSSLCertificateChecks().create();

            if (null != connection) {

                IDomain domain = connection.getUser().getDefaultDomain();
                IApplication application = domain.getApplicationByName(applicationModel.getName());
                application.destroy();

                logger.info("Application deleted successfully!");

                applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.DELETED);

                return new SPIResponse(BasicResponseCode.SUCCESS, "Application deleted successfully!", applicationModel);
            } else {
                applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Application isn't deleted successfully!", applicationModel);
            }

        } catch (OpenShiftException | IOException e) {
//            logger.severe(e.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, e.getMessage());
        }
    }

    @Override
    public SPIResponse registerSSHKey() {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }

    @Override
    public SPIResponse removeSSHKey() {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }

    @Override
    public SPIResponse getAvailableServices(CredentialsModel credentialsModel, PaaSOfferingModel paaSOfferingModel) {

        logger.info("OpenShift Adapter invoked for getting all available services: " + credentialsModel.getUsername());

        try {

            IOpenShiftConnection connection = new ConnectionBuilder(credentialsModel.getPaaSOffering().getEndpointURI().substring(7)).credentials(credentialsModel.getUsername(), credentialsModel.getPassword()).disableSSLCertificateChecks().create();

            if (null != connection) {

                List<IEmbeddableCartridge> listOfCartridges = connection.getEmbeddableCartridges();

                List<ServiceModel> listOfServiceModels = new ArrayList<>();

                if (null != listOfCartridges && !listOfCartridges.isEmpty()) {

                    listOfCartridges.stream().forEach(cartridge -> {

                        ServiceModel srv = new ServiceModel();
                        srv.setName(cartridge.getName());
                        srv.setLabel(cartridge.getDescription());
//                        srv.setUrl(cartridge.getUrl().toString());
                        srv.setActive(cartridge.isDownloadable());
                        listOfServiceModels.add(srv);

                    });

                }

                paaSOfferingModel.setServices(listOfServiceModels);

                return new SPIResponse(BasicResponseCode.SUCCESS, "Services fetched successfully!", paaSOfferingModel);
            } else {
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Services aren't fetched successfully!", paaSOfferingModel);
            }

        } catch (OpenShiftException | IOException e) {
//            logger.severe(e.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, e.getMessage());
        }

    }

    @Override
    public SPIResponse getAvailableStacks(CredentialsModel credentialsModel, PaaSOfferingModel paaSOfferingModel) {
        logger.info("OpenShift Adapter invoked for getting all available stacks: " + credentialsModel.getUsername());

        try {

            IOpenShiftConnection connection = new ConnectionBuilder(credentialsModel.getPaaSOffering().getEndpointURI().substring(7)).credentials(credentialsModel.getUsername(), credentialsModel.getPassword()).disableSSLCertificateChecks().create();

            if (null != connection) {

                IUser user = connection.getUser();
                IDomain domain = connection.getUser().getDefaultDomain();

                List<IStandaloneCartridge> listOfCartridges = connection.getStandaloneCartridges();

                List<StackModel> listOfStackModels = new ArrayList<>();

                if (null != listOfCartridges && !listOfCartridges.isEmpty()) {

                    listOfCartridges.stream().forEach(cartridge -> {

                        StackModel stackModel = new StackModel();
                        stackModel.setName(cartridge.getName());
                        stackModel.setDescription(cartridge.getDescription());
//                        stackModel.setUrl(cartridge.getUrl().toString());
                        stackModel.setActive(cartridge.isDownloadable());
                        listOfStackModels.add(stackModel);

                    });

                }

                paaSOfferingModel.setStacks(listOfStackModels);

                return new SPIResponse(BasicResponseCode.SUCCESS, "Stacks fetched successfully!", paaSOfferingModel);
            } else {
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Stacks aren't fetched successfully!", paaSOfferingModel);
            }

        } catch (OpenShiftException | IOException e) {
//            logger.severe(e.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, e.getMessage());
        }
    }

    @Override
    public SPIResponse createServiceBinding(CredentialsModel credentialsModel, ApplicationModel applicationModel, ServiceModel serviceModel) {

        logger.info("OpenShift Adapter invoked for creating a service binding to application: " + applicationModel.getName());

        try {

            IOpenShiftConnection connection = new ConnectionBuilder(credentialsModel.getPaaSOffering().getEndpointURI().substring(7)).credentials(credentialsModel.getUsername(), credentialsModel.getPassword()).disableSSLCertificateChecks().create();

            if (null != connection) {

                IUser user = connection.getUser();
                IDomain domain = connection.getUser().getDefaultDomain();

                IApplication application = domain.getApplicationByName(applicationModel.getName());

                IEmbeddedCartridge embeddedCartridge = application.addEmbeddableCartridge(new LatestEmbeddableCartridge(serviceModel.getName()).get(user));

                if (null != embeddedCartridge) {
                    List<ServiceModel> serviceModels = null != applicationModel.getServices() ? applicationModel.getServices() : new ArrayList<>();

                    serviceModels.add(serviceModel);
                    applicationModel.setServices(serviceModels);

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Service is bound successfully!", applicationModel);
                } else {
                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Service isn't bound successfully!", applicationModel);
                }

            } else {
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Service isn't bound successfully!", applicationModel);
            }

        } catch (OpenShiftException | IOException e) {
//            logger.severe(e.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, e.getMessage());
        }

    }

    @Override
    public SPIResponse getServiceBinding() {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }

    @Override
    public SPIResponse updateServiceBinding() {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }

    @Override
    public SPIResponse deleteServiceBinding(CredentialsModel credentialsModel, ApplicationModel applicationModel, ServiceModel serviceModel) {
        logger.info("OpenShift Adapter invoked for removing a service binding to application: " + applicationModel.getName());

        try {

            IOpenShiftConnection connection = new ConnectionBuilder(credentialsModel.getPaaSOffering().getEndpointURI().substring(7)).credentials(credentialsModel.getUsername(), credentialsModel.getPassword()).disableSSLCertificateChecks().create();

            if (null != connection) {

                IUser user = connection.getUser();
                IDomain domain = connection.getUser().getDefaultDomain();

                IApplication application = domain.getApplicationByName(applicationModel.getName());

                IEmbeddedCartridge embeddedCartridge = application.getEmbeddedCartridge(new LatestEmbeddableCartridge(serviceModel.getName()).get(application));

                if (null != embeddedCartridge) {

                    embeddedCartridge.destroy();

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Service is unbound successfully!", applicationModel);
                } else {

                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Service isn't unbound successfully!", applicationModel);
                }

            } else {
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Service isn't unbound successfully!", applicationModel);
            }

        } catch (OpenShiftException | IOException e) {
            logger.severe(e.getMessage());
            e.printStackTrace();
            return new SPIResponse(BasicResponseCode.EXCEPTION, e.getMessage());
        }
    }

    @Override
    public SPIResponse getAllBoundServices(CredentialsModel credentialsModel, ApplicationModel applicationModel) {
        logger.info("OpenShift Adapter invoked for get all bound services of an application: " + applicationModel.getName());

        try {

            IOpenShiftConnection connection = new ConnectionBuilder(credentialsModel.getPaaSOffering().getEndpointURI().substring(7)).credentials(credentialsModel.getUsername(), credentialsModel.getPassword()).disableSSLCertificateChecks().create();

            if (null != connection) {

                IUser user = connection.getUser();
                IDomain domain = connection.getUser().getDefaultDomain();

                IApplication application = domain.getApplicationByName(applicationModel.getName());

                List<IEmbeddedCartridge> embeddedCartridges = application.getEmbeddedCartridges();

                if (null != embeddedCartridges && !embeddedCartridges.isEmpty()) {

                    List<ServiceModel> serviceModels = new ArrayList<>();

                    embeddedCartridges.stream().forEach(embeddedCartridge -> {

                        ServiceModel srv = new ServiceModel();
                        srv.setName(embeddedCartridge.getName());
                        srv.setLabel(embeddedCartridge.getDescription());
//                        srv.setUrl(embeddedCartridge.getUrl().toString());
                        srv.setActive(embeddedCartridge.isDownloadable());
                        serviceModels.add(srv);

                    });


                    applicationModel.setServices(serviceModels);

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Bound services fetched successfully!", applicationModel);
                } else {
                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Bound services isn't fetched successfully!", applicationModel);
                }

            } else {
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Bound services isn't fetched successfully!", applicationModel);
            }

        } catch (OpenShiftException | IOException e) {
//            logger.severe(e.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, e.getMessage());
        }
    }
}
