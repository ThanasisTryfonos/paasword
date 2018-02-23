package eu.paasword.adapter.heroku;

import com.heroku.api.*;
import eu.paasword.spi.adapter.PaaSAdapter;
import eu.paasword.spi.model.*;
import eu.paasword.spi.response.BasicResponseCode;
import eu.paasword.spi.response.SPIResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by smantzouratos on 21/09/16.
 */
public class HerokuAdapter implements PaaSAdapter {

    private static final Logger logger = Logger.getLogger(HerokuAdapter.class.getName());

    public static void main(String[] args) {

    }

    @Override
    public SPIResponse validateCredentials(CredentialsModel credentialsModel, PaaSOfferingModel paasOfferingModel) {

        logger.info("Heroku Adapter invoked for deployment validation of user: " + credentialsModel.getUsername());

        try {

            String herokuAPIKey = HerokuAPI.obtainApiKey(credentialsModel.getUsername(), credentialsModel.getPassword());

            if (null != herokuAPIKey && !herokuAPIKey.isEmpty()) {
                logger.info("Credentials validated successfully!");
                return new SPIResponse(BasicResponseCode.SUCCESS, "Credentials validated successfully!", paasOfferingModel);
            }

            logger.info("Credentials are invalid!");

        } catch (Exception e) {
//            logger.severe(e.getMessage());
            logger.info("Credentials are invalid!");
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Credentials are invalid!");
    }

    @Override
    public SPIResponse createApplication(CredentialsModel credentialsModel, ApplicationRequestModel applicationRequestModel) {

        logger.info("Heroku Adapter invoked for deployment for user: " + credentialsModel.getUsername() + ", application: " + applicationRequestModel.getName() + ", endpoint: " + credentialsModel.getPaaSOffering().getEndpointURI());

        String herokuAPIKey = HerokuAPI.obtainApiKey(credentialsModel.getUsername(), credentialsModel.getPassword());

        HerokuAPI api = new HerokuAPI(herokuAPIKey);

        ApplicationModel createdApp = new ApplicationModel();
        createdApp.setName(applicationRequestModel.getName());
        createdApp.setApplicationRequest(applicationRequestModel);

        if (null != api) {

            String applicationName = applicationRequestModel.getName();


            if (api.appExists(applicationName)) {
                applicationName += ".paasport";
            }

            App app = api.createApp(new App().on(Heroku.Stack.fromString(applicationRequestModel.getStackName())).named(applicationName));

//            if (!api.isMaintenanceModeEnabled(applicationName)) {
//                api.setMaintenanceMode(applicationName, true);
//            }

            String endPoint = app.getWebUrl();

            if (endPoint.indexOf("://") == -1) {
                endPoint = "http://" + endPoint;
            }

            logger.info("Running endpoint: " + endPoint);

            createdApp.setRunningEndpoint(endPoint);
            createdApp.setRemoteGitURL(app.getGitUrl());
            createdApp.setApplicationStateTypeModel(ApplicationStateTypeModel.CREATED);

            logger.info("Application created successfully!");

            return new SPIResponse(BasicResponseCode.SUCCESS, "Application created successfully!", createdApp);

        } else {
            createdApp.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
            logger.info("Application didn't create successfully!");
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Application didn't create successfully!", createdApp);

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

        logger.info("Heroku Adapter invoked for starting application: " + applicationModel.getName() + ", user: " + credentialsModel.getUsername());

        applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.STARTING);

        String herokuAPIKey = HerokuAPI.obtainApiKey(credentialsModel.getUsername(), credentialsModel.getPassword());

        HerokuAPI api = new HerokuAPI(herokuAPIKey);

        if (api.isMaintenanceModeEnabled(applicationModel.getName())) {
            api.setMaintenanceMode(applicationModel.getName(), false);
        }

        applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.STARTED);

        logger.info("Application started successfully!");

        return new SPIResponse(BasicResponseCode.SUCCESS, "Application started successfully!", applicationModel);

    }

    @Override
    public SPIResponse stopApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {

        logger.info("Heroku Adapter invoked for stopping application: " + applicationModel.getName() + ", user: " + credentialsModel.getUsername());

        applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.STOPPING);

        String herokuAPIKey = HerokuAPI.obtainApiKey(credentialsModel.getUsername(), credentialsModel.getPassword());

        HerokuAPI api = new HerokuAPI(herokuAPIKey);

        if (!api.isMaintenanceModeEnabled(applicationModel.getName())) {
            api.setMaintenanceMode(applicationModel.getName(), true);
        }

        applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.DEPLOYED);

        logger.info("Application stopped successfully!");

        return new SPIResponse(BasicResponseCode.SUCCESS, "Application stopped successfully!", applicationModel);
    }

    @Override
    public SPIResponse deleteApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {

        logger.info("Heroku Adapter invoked for deleting application: " + applicationModel.getName() + ", user: " + credentialsModel.getUsername());

        applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.DELETING);

        String herokuAPIKey = HerokuAPI.obtainApiKey(credentialsModel.getUsername(), credentialsModel.getPassword());

        HerokuAPI api = new HerokuAPI(herokuAPIKey);

        api.destroyApp(applicationModel.getName());

        applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.DELETED);

        logger.info("Application deleted successfully!");

        return new SPIResponse(BasicResponseCode.SUCCESS, "Application deleted successfully!", applicationModel);

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

        logger.info("Heroku Adapter invoked for getting available services for user: " + credentialsModel.getUsername());

        String herokuApiKey = HerokuAPI.obtainApiKey(credentialsModel.getUsername(), credentialsModel.getPassword());

        if (null != herokuApiKey && !herokuApiKey.isEmpty()) {

            HerokuAPI api = new HerokuAPI(herokuApiKey);

            List<Addon> addonList = api.listAllAddons();

            List<ServiceModel> listOfServiceModels = new ArrayList<>();

            for (Addon addon : addonList) {
                ServiceModel serviceModel = new ServiceModel();
                serviceModel.setName(addon.getName());
                serviceModel.setDescription(addon.getDescription());
                serviceModel.setPrice(String.valueOf(addon.getPriceCents()));
                serviceModel.setPriceUnit(addon.getPriceUnit());
                listOfServiceModels.add(serviceModel);
            }

            paaSOfferingModel.setServices(listOfServiceModels);

            logger.info("");
            return new SPIResponse(BasicResponseCode.SUCCESS, "Services are fetched successfully!", paaSOfferingModel);

        } else {
            logger.info("Services aren't fetched successfully!");
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Services aren't fetched successfully!", paaSOfferingModel);
        }


    }

    @Override
    public SPIResponse getAvailableStacks(CredentialsModel credentialsModel, PaaSOfferingModel paaSOfferingModel) {

        logger.info("Heroku Adapter invoked for getting available stacks for user: " + credentialsModel.getUsername());

        String herokuApiKey = HerokuAPI.obtainApiKey(credentialsModel.getUsername(), credentialsModel.getPassword());

        if (null != herokuApiKey && !herokuApiKey.isEmpty()) {

            HerokuAPI api = new HerokuAPI(herokuApiKey);

            List<StackModel> listOfStackModels = new ArrayList<>();

            for (Heroku.Stack stack : Heroku.Stack.values()) {
                StackModel stackModel = new StackModel();
                stackModel.setName(stack.value);
                listOfStackModels.add(stackModel);
            }

            paaSOfferingModel.setStacks(listOfStackModels);

            logger.info("Stacks are fetched successfully!");

            return new SPIResponse(BasicResponseCode.SUCCESS, "Stacks are fetched successfully!", paaSOfferingModel);

        } else {
            logger.info("Stacks aren't fetched successfully!");
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Stackes aren't fetched successfully!", paaSOfferingModel);
        }

    }

    @Override
    public SPIResponse createServiceBinding(CredentialsModel credentialsModel, ApplicationModel applicationModel, ServiceModel serviceModel) {

        logger.info("Heroku Adapter invoked for binding a service: " + serviceModel.getName() + ", to application: " + applicationModel.getName());

        String herokuApiKey = HerokuAPI.obtainApiKey(credentialsModel.getUsername(), credentialsModel.getPassword());

        if (null != herokuApiKey && !herokuApiKey.isEmpty()) {

            HerokuAPI api = new HerokuAPI(herokuApiKey);
            String appName = applicationModel.getName();

            AddonChange addonChange = api.addAddon(appName, serviceModel.getName());

            logger.info("Status: " + addonChange.getStatus() + ", MSG: " + addonChange.getMessage() + ", Price: " + addonChange.getPrice());

            List<ServiceModel> serviceModels = null != applicationModel.getServices() ? applicationModel.getServices() : new ArrayList<>();

            serviceModels.add(serviceModel);
            applicationModel.setServices(serviceModels);

            logger.info("Service has been bound successfully!");

            return new SPIResponse(BasicResponseCode.SUCCESS, "Service has been bound successfully!", applicationModel);

        } else {
            logger.info("Service hasn't been bound successfully!");
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Service hasn't been bound successfully!", applicationModel);
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
        logger.info("Heroku Adapter invoked for unbinding a service: " + serviceModel.getName() + ", to application: " + applicationModel.getName());

        String herokuApiKey = HerokuAPI.obtainApiKey(credentialsModel.getUsername(), credentialsModel.getPassword());

        if (null != herokuApiKey && !herokuApiKey.isEmpty()) {

            HerokuAPI api = new HerokuAPI(herokuApiKey);
            String appName = applicationModel.getName();

            AddonChange addonChange = api.removeAddon(appName, serviceModel.getName());

            logger.info("Status: " + addonChange.getStatus() + ", MSG: " + addonChange.getMessage() + ", Price: " + addonChange.getPrice());

            logger.info("Service has been unbound successfully!");

            return new SPIResponse(BasicResponseCode.SUCCESS, "Service has been unbound successfully!", applicationModel);

        } else {
            logger.info("Service hasn't been unbound successfully!");
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Service hasn't been unbound successfully!", applicationModel);
        }
    }

    @Override
    public SPIResponse getAllBoundServices(CredentialsModel credentialsModel, ApplicationModel applicationModel) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }
}
