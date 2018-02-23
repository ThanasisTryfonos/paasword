package eu.paasword.adapter.bluemix;

import eu.paasword.spi.adapter.PaaSAdapter;
import eu.paasword.spi.model.*;
import eu.paasword.spi.response.BasicResponseCode;
import eu.paasword.spi.response.SPIResponse;
import eu.paasword.spi.util.Util;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.StartingInfo;
import org.cloudfoundry.client.lib.domain.*;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by smantzouratos on 21/09/16.
 */
public class BluemixAdapter implements PaaSAdapter {

    private static final Logger logger = Logger.getLogger(BluemixAdapter.class.getName());
    private static final int DEFAULT_MEM = 1024;

    @Override
    public SPIResponse validateCredentials(CredentialsModel credentialsModel, PaaSOfferingModel paasOfferingModel) {
        logger.info("Bluemix Adapter invoked for validation of user: " + credentialsModel.getUsername());

        CloudFoundryClient client = null;
        try {

            URL cloudControllerUrl = new URL(credentialsModel.getPaaSOffering().getEndpointURI());

            CloudFoundryClient connectedClientNoSpaceOrg = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl);

            OAuth2AccessToken token = connectedClientNoSpaceOrg.login();

            if (null != token) {
                logger.info("Credentials validated successfully!");
                connectedClientNoSpaceOrg.logout();
                return new SPIResponse(BasicResponseCode.SUCCESS, "Credentials validated successfully!", paasOfferingModel);

            } else {
                logger.info("Credentials are invalid!");
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Credentials are invalid!");
            }

        } catch (Exception ex) {
//            logger.severe(ex.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public SPIResponse createApplication(CredentialsModel credentialsModel, ApplicationRequestModel applicationRequestModel) {

        logger.info("Bluemix Adapter invoked for deployment for user: " + credentialsModel.getUsername() + ", application: " + applicationRequestModel.getName() + ", endpoint: " + credentialsModel.getPaaSOffering().getEndpointURI());

        ApplicationModel createdApp = new ApplicationModel();
        createdApp.setApplicationStateTypeModel(ApplicationStateTypeModel.CREATING);
        createdApp.setName(applicationRequestModel.getName());
        createdApp.setApplicationRequest(applicationRequestModel);


        CloudFoundryClient client = null;
        try {

            URL cloudControllerUrl = new URL(credentialsModel.getPaaSOffering().getEndpointURI());

            CloudFoundryClient connectedClientNoSpaceOrg = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl);

            String space = null;
            String org = null;

            if (null != applicationRequestModel.getSpace() && !applicationRequestModel.getSpace().isEmpty()) {
                if (null == connectedClientNoSpaceOrg.getSpace(applicationRequestModel.getSpace())) {
                    connectedClientNoSpaceOrg.createSpace(applicationRequestModel.getSpace());
                }
                space = applicationRequestModel.getSpace();
            } else {
                space = connectedClientNoSpaceOrg.getSpaces().get(0).getName();
            }
            applicationRequestModel.setSpace(space);

            if (null != applicationRequestModel.getOrganization() && !applicationRequestModel.getOrganization().isEmpty()) {
                if (null == connectedClientNoSpaceOrg.getOrgByName(applicationRequestModel.getOrganization(), true)) {
                    // TODO throw error that the selected organization doesn't exist

                }
                org = applicationRequestModel.getOrganization();
            } else {
                org = connectedClientNoSpaceOrg.getOrganizations().get(0).getName();
            }
            applicationRequestModel.setOrganization(org);

            connectedClientNoSpaceOrg.logout();

            logger.info("Connecting to Organization: " + org + ", Space: " + space);

            client = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl, org, space);

            client.login();

            // TODO check domain or not ???

            CloudDomain defaultDomain = client.getDefaultDomain();

            logger.info("Default domain: " + defaultDomain.getName());

            List<String> uris = Collections.singletonList(applicationRequestModel.getName()  + "." + defaultDomain.getName());

            Staging staging = new Staging(null, applicationRequestModel.getStackName()); // "java_buildpack");

            client.createApplication(applicationRequestModel.getName(), staging, DEFAULT_MEM, uris, null);

            CloudApplication application = client.getApplication(applicationRequestModel.getName());

            String endPoint = application.getUris().get(0);

            if (endPoint.indexOf("://") == -1) {
                endPoint = "http://" + endPoint;
            }

            logger.info("Running endpoint: " + endPoint);

            logger.info("Application name: " + application.getName());

            createdApp.setRunningEndpoint(endPoint);
            createdApp.setApplicationStateTypeModel(ApplicationStateTypeModel.CREATED);

            client.logout();

            return new SPIResponse(BasicResponseCode.SUCCESS, "Application created successfully!", createdApp);


        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            createdApp.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), createdApp);
        }

    }

    @Override
    public SPIResponse deployApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel, PackageLocatorModel packageLocatorModel) {

        logger.info("Bluemix Adapter invoked for deployment for user: " + credentialsModel.getUsername() + ", application: " + applicationModel.getName());
        applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.DEPLOYING);

        CloudFoundryClient client = null;
        String originalFilename = null;
        String suffix = null;
        File fileApp = null;

        try {

            URL cloudControllerUrl = new URL(credentialsModel.getPaaSOffering().getEndpointURI());
            CloudFoundryClient connectedClientNoSpaceOrg = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl);

            connectedClientNoSpaceOrg.login();

            String space = connectedClientNoSpaceOrg.getSpaces().get(0).getName();
            String org = connectedClientNoSpaceOrg.getOrganizations().get(0).getName();

            connectedClientNoSpaceOrg.logout();

            logger.info("Connecting to Organization: " + org + ", Space: " + space);

            client = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl, org, space);

            client.login();

            if (null != packageLocatorModel.getFilename() && !packageLocatorModel.getFilename().isEmpty() && null != packageLocatorModel.getFile()) {

                originalFilename = packageLocatorModel.getFilename().substring(0, packageLocatorModel.getFilename().lastIndexOf("."));

                suffix = packageLocatorModel.getFilename().substring(packageLocatorModel.getFilename().lastIndexOf("."));

                fileApp = File.createTempFile(originalFilename, suffix);

                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(fileApp));
                FileCopyUtils.copy(packageLocatorModel.getFile(), stream);
                stream.close();

                client.uploadApplication(applicationModel.getName(), fileApp);

            } else if (null != packageLocatorModel.getFileURL()) {

                client.uploadApplication(applicationModel.getName(), new File(packageLocatorModel.getFileURL().getFile()));

            } else {
                // TODO
                // throw error that file doesn't exist
            }

            client.logout();

            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.DEPLOYED);

            return new SPIResponse(BasicResponseCode.SUCCESS, "Application deployed successfully!", applicationModel);

        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), applicationModel);
        }

    }

    @Override
    public SPIResponse undeployApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {

        logger.info("Bluemix Adapter invoked for undeployment for user: " + credentialsModel.getUsername() + ", application: " + applicationModel.getName());
        applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.UNDEPLOYING);

        CloudFoundryClient client = null;

        try {

            URL cloudControllerUrl = new URL(credentialsModel.getPaaSOffering().getEndpointURI());
            CloudFoundryClient connectedClientNoSpaceOrg = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl);

            connectedClientNoSpaceOrg.login();

            String space = connectedClientNoSpaceOrg.getSpaces().get(0).getName();
            String org = connectedClientNoSpaceOrg.getOrganizations().get(0).getName();

            connectedClientNoSpaceOrg.logout();

            logger.info("Connecting to Organization: " + org + ", Space: " + space);

            client = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl, org, space);

            client.login();

            CloudApplication cloudApplication = client.getApplication(applicationModel.getName());

            CloudApplication.AppState appState = cloudApplication.getState();

            if (null != appState && appState.equals(CloudApplication.AppState.STARTED)) {
                client.stopApplication(cloudApplication.getName());
            } else {
                // TODO
            }

            client.logout();

            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.CREATED);

            return new SPIResponse(BasicResponseCode.SUCCESS, "Application undeployed successfully!", applicationModel);

        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), applicationModel);
        }


    }

    @Override
    public SPIResponse startApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {

        logger.info("Bluemix Adapter invoked for starting application: " + applicationModel.getName() + ", for user: " + credentialsModel.getUsername());
        applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.STARTING);

        CloudFoundryClient client = null;

        try {

            URL cloudControllerUrl = new URL(credentialsModel.getPaaSOffering().getEndpointURI());
            CloudFoundryClient connectedClientNoSpaceOrg = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl);

            connectedClientNoSpaceOrg.login();

            String space = connectedClientNoSpaceOrg.getSpaces().get(0).getName();
            String org = connectedClientNoSpaceOrg.getOrganizations().get(0).getName();

            connectedClientNoSpaceOrg.logout();

            logger.info("Connecting to Organization: " + org + ", Space: " + space);

            client = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl, org, space);

            client.login();

            CloudApplication cloudApplication = client.getApplication(applicationModel.getName());

            CloudApplication.AppState appState = cloudApplication.getState();

            if (null != appState && appState.equals(CloudApplication.AppState.STARTED)) {

            } else {


                StartingInfo startingInfo = client.startApplication(cloudApplication.getName());

                logger.info("Staging info: " + startingInfo.getStagingFile());
            }

            client.logout();

            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.RUNNING);

            return new SPIResponse(BasicResponseCode.SUCCESS, "Application started successfully!", applicationModel);

        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), applicationModel);
        }

    }

    @Override
    public SPIResponse stopApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {

        logger.info("Bluemix Adapter invoked for stopping application: " + applicationModel.getName() + ", for user: " + credentialsModel.getUsername());

        applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.STOPPING);

        CloudFoundryClient client = null;

        try {

            URL cloudControllerUrl = new URL(credentialsModel.getPaaSOffering().getEndpointURI());
            CloudFoundryClient connectedClientNoSpaceOrg = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl);

            connectedClientNoSpaceOrg.login();

            String space = connectedClientNoSpaceOrg.getSpaces().get(0).getName();
            String org = connectedClientNoSpaceOrg.getOrganizations().get(0).getName();

            connectedClientNoSpaceOrg.logout();

            logger.info("Connecting to Organization: " + org + ", Space: " + space);

            client = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl, org, space);

            client.login();

            CloudApplication cloudApplication = client.getApplication(applicationModel.getName());

            CloudApplication.AppState appState = cloudApplication.getState();

            if (null != appState && appState.equals(CloudApplication.AppState.STARTED)) {

                client.stopApplication(cloudApplication.getName());

            } else {

                // TODO
            }

            client.logout();

            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.DEPLOYED);

            return new SPIResponse(BasicResponseCode.SUCCESS, "Application stopped successfully!", applicationModel);

        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
            return new SPIResponse(BasicResponseCode.SUCCESS, ex.getMessage(), applicationModel);
        }

    }

    @Override
    public SPIResponse deleteApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {

        logger.info("Bluemix Adapter invoked for deleting application: " + applicationModel.getName() + ", for user: " + credentialsModel.getUsername());

        applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.DELETING);

        CloudFoundryClient client = null;

        try {

            URL cloudControllerUrl = new URL(credentialsModel.getPaaSOffering().getEndpointURI());
            client = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl);

            client.login();

            CloudApplication cloudApplication = client.getApplication(applicationModel.getName());

            CloudApplication.AppState appState = cloudApplication.getState();

            if (null != appState && appState.equals(CloudApplication.AppState.STARTED)) {

                client.stopApplication(cloudApplication.getName());

            } else {

                // TODO
            }

            client.deleteApplication(cloudApplication.getName());

            client.logout();

            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.DELETED);

            return new SPIResponse(BasicResponseCode.SUCCESS, "Application deleted successfully!", applicationModel);

        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), applicationModel);
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

        logger.info("Bluemix Adapter invoked for getting all available services: " + credentialsModel.getUsername());
        logger.info("Endpoint: " + credentialsModel.getPaaSOffering().getEndpointURI());

        CloudFoundryClient client = null;

        try {

            URL cloudControllerUrl = new URL(credentialsModel.getPaaSOffering().getEndpointURI());
            client = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl);

            client.login();

            List<CloudServiceOffering> services = client.getServiceOfferings();

            List<ServiceModel> listOfServiceModels = new ArrayList<>();

            if (null != services && !services.isEmpty()) {

                services.stream().forEach(service -> {

                    ServiceModel srv = new ServiceModel();
                    srv.setMetaModel(new MetaModel(service.getMeta().getGuid(), service.getMeta().getCreated(), service.getMeta().getUpdated(), service.getMeta().getUrl()));
                    srv.setLabel(service.getLabel());
                    srv.setDescription(service.getDescription());
                    srv.setActive(service.isActive());
                    srv.setBindable(service.isBindable());
                    List<ServicePlanModel> plans = new ArrayList<ServicePlanModel>();

                    service.getCloudServicePlans().stream().forEach(srvPlan -> {

                        ServicePlanModel plan = new ServicePlanModel();

                        plan.setExtra(srvPlan.getExtra());
                        plan.set_public(srvPlan.isPublic());
                        plan.setFree(srvPlan.isFree());
                        plan.setDescription(srvPlan.getDescription());
                        plan.setUniqueID(srvPlan.getUniqueId());
                        plan.setName(srvPlan.getName());
                        plans.add(plan);

                    });

                    srv.setPlans(plans);
                    srv.setDescription(service.getDescription());
                    srv.setActive(service.isActive());
                    srv.setBindable(service.isBindable());
                    srv.setName(service.getName());
                    srv.setDocUrl(service.getDocumentationUrl());
                    srv.setInfoUrl(service.getInfoUrl());
                    srv.setUrl(service.getUrl());
                    srv.setUniqueId(service.getUniqueId());
                    srv.setExtra(service.getExtra());
                    srv.setProvider(service.getProvider());
                    srv.setVersion(service.getVersion());
                    listOfServiceModels.add(srv);

                });

            }

            paaSOfferingModel.setServices(listOfServiceModels);
            client.logout();
            return new SPIResponse(BasicResponseCode.SUCCESS, "Services fetched successfully!", paaSOfferingModel);
        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), paaSOfferingModel);
        }

    }

    @Override
    public SPIResponse getAvailableStacks(CredentialsModel credentialsModel, PaaSOfferingModel paaSOfferingModel) {

        logger.info("Bluemix Adapter invoked for getting all available stacks: " + credentialsModel.getUsername());
        logger.info("Endpoint: " + credentialsModel.getPaaSOffering().getEndpointURI());

        CloudFoundryClient client = null;

        try {

            URL cloudControllerUrl = new URL(credentialsModel.getPaaSOffering().getEndpointURI());
            client = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl);

            client.login();

            List<CloudStack> stacks = client.getStacks();

            List<StackModel> listOfStacks = new ArrayList<>();

            if (null != stacks && !stacks.isEmpty()) {

                stacks.stream().forEach(stack -> {

                    StackModel stk = new StackModel();
                    stk.setMetaModel(new MetaModel(stack.getMeta().getGuid(), stack.getMeta().getCreated(), stack.getMeta().getUpdated(), stack.getMeta().getUrl()));
                    stk.setName(stack.getName());
                    stk.setDescription(stack.getDescription());
                    listOfStacks.add(stk);

                });

            }

            paaSOfferingModel.setStacks(listOfStacks);

            client.logout();

            return new SPIResponse(BasicResponseCode.SUCCESS, "Stacks fetched successfully!", paaSOfferingModel);

        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), paaSOfferingModel);
        }

    }

    @Override
    public SPIResponse createServiceBinding(CredentialsModel credentialsModel, ApplicationModel applicationModel, ServiceModel serviceModel) {

        logger.info("Bluemix Adapter invoked for creating service binding: " + serviceModel.getName() + ", for application: " + applicationModel.getName());

        CloudFoundryClient client = null;

        try {

            URL cloudControllerUrl = new URL(credentialsModel.getPaaSOffering().getEndpointURI());
            CloudFoundryClient connectedClientNoSpaceOrg = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl);

            connectedClientNoSpaceOrg.login();

            String space = connectedClientNoSpaceOrg.getSpaces().get(0).getName();
            String org = connectedClientNoSpaceOrg.getOrganizations().get(0).getName();

            connectedClientNoSpaceOrg.logout();

            logger.info("Connecting to Organization: " + org + ", Space: " + space);

            client = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl, org, space);

            client.login();

            CloudApplication cloudApplication = client.getApplication(applicationModel.getName());

            CloudServiceOffering serviceOffering = client.getServiceOfferings().stream().filter(srv -> srv.getName().equals(serviceModel.getName())).collect(Collectors.toList()).get(0);

            CloudServicePlan servicePlan = null;

            List<CloudServicePlan> servicePlans = serviceOffering.getCloudServicePlans().stream().filter(srvPlan -> srvPlan.isFree()).collect(Collectors.toList());

            if (null != servicePlans && !servicePlans.isEmpty()) {
                servicePlan = servicePlans.get(0);
            } else {
                servicePlan = client.getServiceOfferings().stream().filter(srv -> srv.getName().equals(serviceModel.getName())).collect(Collectors.toList()).get(0).getCloudServicePlans().get(0);
            }

            CloudService service = new CloudService(CloudEntity.Meta.defaultMeta(), serviceOffering.getName());
            service.setName(serviceOffering.getName() + "." + Util.generateRandomString(5, Util.Mode.ALPHA));
            service.setPlan(servicePlan.getName());
            service.setLabel(serviceOffering.getLabel());

            client.createService(service);
            client.bindService(cloudApplication.getName(), service.getName());

            List<ServiceModel> serviceModels = null != applicationModel.getServices() ? applicationModel.getServices() : new ArrayList<>();

            serviceModels.add(serviceModel);
            applicationModel.setServices(serviceModels);

            client.logout();

            return new SPIResponse(BasicResponseCode.SUCCESS, "Service is bound successfully!", applicationModel);

        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), applicationModel);
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

        logger.info("Bluemix Adapter invoked for deleting service binding: " + serviceModel.getLabel() + ", for application: " + applicationModel.getName());

        CloudFoundryClient client = null;

        try {

            URL cloudControllerUrl = new URL(credentialsModel.getPaaSOffering().getEndpointURI());
            CloudFoundryClient connectedClientNoSpaceOrg = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl);

            connectedClientNoSpaceOrg.login();

            String space = connectedClientNoSpaceOrg.getSpaces().get(0).getName();
            String org = connectedClientNoSpaceOrg.getOrganizations().get(0).getName();

            connectedClientNoSpaceOrg.logout();

            logger.info("Connecting to Organization: " + org + ", Space: " + space);

            client = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl, org, space);

            client.login();

            CloudApplication cloudApplication = client.getApplication(applicationModel.getName());

            CloudService cloudService = client.getService(serviceModel.getName());

            client.unbindService(cloudApplication.getName(), cloudService.getName());

            client.deleteService(cloudService.getName());

            return new SPIResponse(BasicResponseCode.SUCCESS, "Service binding deleted successfully!", applicationModel);

        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), applicationModel);
        }

    }

    @Override
    public SPIResponse getAllBoundServices(CredentialsModel credentialsModel, ApplicationModel applicationModel) {

        logger.info("Bluemix Adapter invoked for getting all binded services for application: " + applicationModel.getName());

        CloudFoundryClient client = null;

        try {

            URL cloudControllerUrl = new URL(credentialsModel.getPaaSOffering().getEndpointURI());
            CloudFoundryClient connectedClientNoSpaceOrg = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl);

            connectedClientNoSpaceOrg.login();

            String space = connectedClientNoSpaceOrg.getSpaces().get(0).getName();
            String org = connectedClientNoSpaceOrg.getOrganizations().get(0).getName();

            connectedClientNoSpaceOrg.logout();

            logger.info("Connecting to Organization: " + org + ", Space: " + space);

            client = new CloudFoundryClient(new CloudCredentials(credentialsModel.getUsername(), credentialsModel.getPassword()),
                    cloudControllerUrl, org, space);

            client.login();

            CloudApplication cloudApplication = client.getApplication(applicationModel.getName());

            List<String> services = cloudApplication.getServices();

            List<ServiceModel> listOfServiceModels = new ArrayList<>();

            if (null != services && !services.isEmpty()) {

                services.stream().forEach(service -> {

                    ServiceModel srv = new ServiceModel();
                    srv.setName(service);
                    listOfServiceModels.add(srv);

                });

            }

            applicationModel.setServices(listOfServiceModels);

            client.logout();

            return new SPIResponse(BasicResponseCode.SUCCESS, "Bound services fetched successfully!", applicationModel);

        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), applicationModel);
        }

    }

}
