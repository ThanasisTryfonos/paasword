package eu.paasword.adapter.azure;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.*;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import eu.paasword.spi.adapter.PaaSAdapter;
import eu.paasword.spi.model.*;
import eu.paasword.spi.response.BasicResponseCode;
import eu.paasword.spi.response.SPIResponse;
import okhttp3.logging.HttpLoggingInterceptor;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by smantzouratos on 21/09/16.
 */
public class AzureAdapter implements PaaSAdapter {

    private static final Logger logger = Logger.getLogger(AzureAdapter.class.getName());

    @Override
    public SPIResponse validateCredentials(CredentialsModel credentialsModel, PaaSOfferingModel paasOfferingModel) {

        logger.info("Azure Adapter invoked for validating credentials: " + credentialsModel.getUsername());

        String client = null;
        String tenant = null;
        String key = null;

        try {
            ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(client, tenant, key, AzureEnvironment.AZURE);
            Azure azure = Azure.authenticate(credentials).withDefaultSubscription();

            if (null != azure) {
                logger.info("Credentials validated successfully!");
                return new SPIResponse(BasicResponseCode.SUCCESS, "Credentials validated successfully!", paasOfferingModel);
            } else {
                logger.info("Credentials are invalid!");
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Credentials are invalid!");
            }

        } catch (IOException e) {
            logger.severe(e.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, e.getMessage());
        }

    }

    @Override
    public SPIResponse createApplication(CredentialsModel credentialsModel, ApplicationRequestModel applicationRequestModel) {

        logger.info("Amazon Adapter invoked: " + credentialsModel.getUsername() + ", Name: " + applicationRequestModel.getName());
        ApplicationModel applicationModel = new ApplicationModel();

        String stackName = applicationRequestModel.getStackName();
        String client = null;
        String tenant = null;
        String key = null;
        String planName = "paasportplan"; // ResourceNamer.randomResourceName("paasportplan", 15);
        String rgName = "paasportrg"; //ResourceNamer.randomResourceName("paasportrg", 24);

        try {
            ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(client, tenant, key, AzureEnvironment.AZURE);
            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                    .authenticate(credentials)
                    .withDefaultSubscription();

            if (null != azure) {

                logger.info("Selected subscription: " + azure.subscriptionId());

                logger.info("Creating application " + applicationRequestModel.getName() + " in resource group " + rgName);

                WebApp azureApp = azure.webApps()
                        .define(applicationRequestModel.getName())
                        .withNewResourceGroup(rgName)
                        .withNewAppServicePlan(planName)
                        .withRegion(Region.US_WEST)
                        .withPricingTier(AppServicePricingTier.FREE_F1)
                        .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                        .withWebContainer(WebContainer.TOMCAT_8_0_NEWEST)
                        .create();

                if (null != azureApp) {

                    StringBuilder builder = new StringBuilder().append("Web app: ").append(azureApp.id())
                            .append("Name: ").append(azureApp.name())
                            .append("\n\tState: ").append(azureApp.state())
                            .append("\n\tResource group: ").append(azureApp.resourceGroupName())
                            .append("\n\tRegion: ").append(azureApp.region())
                            .append("\n\tDefault hostname: ").append(azureApp.defaultHostName())
                            .append("\n\tApp service plan: ").append(azureApp.appServicePlanId())
                            .append("\n\tHost name bindings: ");

                    for (HostNameBinding binding : azureApp.getHostNameBindings().values()) {
                        builder = builder.append("\n\t\t" + binding.toString());
                    }
                    builder = builder.append("\n\tSSL bindings: ");
                    for (HostNameSslState binding : azureApp.hostNameSslStates().values()) {
                        builder = builder.append("\n\t\t" + binding.name() + ": " + binding.sslState());
                        if (binding.sslState() != null && binding.sslState() != SslState.DISABLED) {
                            builder = builder.append(" - " + binding.thumbprint());
                        }
                    }
                    builder = builder.append("\n\tApp settings: ");
                    for (AppSetting setting : azureApp.appSettings().values()) {
                        builder = builder.append("\n\t\t" + setting.key() + ": " + setting.value() + (setting.sticky() ? " - slot setting" : ""));
                    }
                    builder = builder.append("\n\tConnection strings: ");
                    for (ConnectionString conn : azureApp.connectionStrings().values()) {
                        builder = builder.append("\n\t\t" + conn.name() + ": " + conn.value() + " - " + conn.type() + (conn.sticky() ? " - slot setting" : ""));
                    }

                    logger.info(builder.toString());

                    applicationModel.setRunningEndpoint(azureApp.defaultHostName());
                    applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.CREATED);
                    applicationModel.setName(azureApp.name());

                    logger.info("Application created successfully!");

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Application created successfully!", applicationModel);

                } else {

                    logger.info("Application isn't created successfully!");
                    applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Application isn't created successfully!", applicationModel);

                }

            } else {
                logger.info("Application isn't created successfully!");
                applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Application isn't created successfully!", applicationModel);
            }

        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), applicationModel);
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
        logger.info("Amazon Adapter invoked for starting application: " + credentialsModel.getUsername() + ", Name: " + applicationModel.getName());

        String client = null;
        String tenant = null;
        String key = null;
        String planName = "paasportplan"; // ResourceNamer.randomResourceName("paasportplan", 15);
        String rgName = "paasportrg"; //ResourceNamer.randomResourceName("paasportrg", 24);

        try {
            ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(client, tenant, key, AzureEnvironment.AZURE);
            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                    .authenticate(credentials)
                    .withDefaultSubscription();

            if (null != azure) {

                logger.info("Selected subscription: " + azure.subscriptionId());

                logger.info("Starting application: " + applicationModel.getName() + " in resource group: " + rgName);

                WebApp azureApp = azure.webApps().listByGroup(rgName).get(0);

                if (null != azureApp) {

                    StringBuilder builder = new StringBuilder().append("Web app: ").append(azureApp.id())
                            .append("Name: ").append(azureApp.name())
                            .append("\n\tState: ").append(azureApp.state())
                            .append("\n\tResource group: ").append(azureApp.resourceGroupName())
                            .append("\n\tRegion: ").append(azureApp.region())
                            .append("\n\tDefault hostname: ").append(azureApp.defaultHostName())
                            .append("\n\tApp service plan: ").append(azureApp.appServicePlanId())
                            .append("\n\tHost name bindings: ");

                    for (HostNameBinding binding : azureApp.getHostNameBindings().values()) {
                        builder = builder.append("\n\t\t" + binding.toString());
                    }
                    builder = builder.append("\n\tSSL bindings: ");
                    for (HostNameSslState binding : azureApp.hostNameSslStates().values()) {
                        builder = builder.append("\n\t\t" + binding.name() + ": " + binding.sslState());
                        if (binding.sslState() != null && binding.sslState() != SslState.DISABLED) {
                            builder = builder.append(" - " + binding.thumbprint());
                        }
                    }
                    builder = builder.append("\n\tApp settings: ");
                    for (AppSetting setting : azureApp.appSettings().values()) {
                        builder = builder.append("\n\t\t" + setting.key() + ": " + setting.value() + (setting.sticky() ? " - slot setting" : ""));
                    }
                    builder = builder.append("\n\tConnection strings: ");
                    for (ConnectionString conn : azureApp.connectionStrings().values()) {
                        builder = builder.append("\n\t\t" + conn.name() + ": " + conn.value() + " - " + conn.type() + (conn.sticky() ? " - slot setting" : ""));
                    }

                    logger.info(builder.toString());

                    azureApp.start();

                    applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.RUNNING);

                    logger.info("Application started successfully!");

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Application started successfully!", applicationModel);

                } else {

                    logger.info("Application isn't created successfully!");
                    applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Application isn't started successfully!", applicationModel);

                }

            } else {
                logger.info("Application isn't started successfully!");
                applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Application isn't started successfully!", applicationModel);
            }

        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), applicationModel);
        }
    }

    @Override
    public SPIResponse stopApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {
        logger.info("Amazon Adapter invoked for stopping application: " + credentialsModel.getUsername() + ", Name: " + applicationModel.getName());

        String client = null;
        String tenant = null;
        String key = null;
        String planName = "paasportplan"; // ResourceNamer.randomResourceName("paasportplan", 15);
        String rgName = "paasportrg"; //ResourceNamer.randomResourceName("paasportrg", 24);

        try {
            ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(client, tenant, key, AzureEnvironment.AZURE);
            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                    .authenticate(credentials)
                    .withDefaultSubscription();

            if (null != azure) {

                logger.info("Selected subscription: " + azure.subscriptionId());

                logger.info("Stopping application: " + applicationModel.getName() + " in resource group: " + rgName);

                WebApp azureApp = azure.webApps().listByGroup(rgName).get(0);

                if (null != azureApp) {

                    StringBuilder builder = new StringBuilder().append("Web app: ").append(azureApp.id())
                            .append("Name: ").append(azureApp.name())
                            .append("\n\tState: ").append(azureApp.state())
                            .append("\n\tResource group: ").append(azureApp.resourceGroupName())
                            .append("\n\tRegion: ").append(azureApp.region())
                            .append("\n\tDefault hostname: ").append(azureApp.defaultHostName())
                            .append("\n\tApp service plan: ").append(azureApp.appServicePlanId())
                            .append("\n\tHost name bindings: ");

                    for (HostNameBinding binding : azureApp.getHostNameBindings().values()) {
                        builder = builder.append("\n\t\t" + binding.toString());
                    }
                    builder = builder.append("\n\tSSL bindings: ");
                    for (HostNameSslState binding : azureApp.hostNameSslStates().values()) {
                        builder = builder.append("\n\t\t" + binding.name() + ": " + binding.sslState());
                        if (binding.sslState() != null && binding.sslState() != SslState.DISABLED) {
                            builder = builder.append(" - " + binding.thumbprint());
                        }
                    }
                    builder = builder.append("\n\tApp settings: ");
                    for (AppSetting setting : azureApp.appSettings().values()) {
                        builder = builder.append("\n\t\t" + setting.key() + ": " + setting.value() + (setting.sticky() ? " - slot setting" : ""));
                    }
                    builder = builder.append("\n\tConnection strings: ");
                    for (ConnectionString conn : azureApp.connectionStrings().values()) {
                        builder = builder.append("\n\t\t" + conn.name() + ": " + conn.value() + " - " + conn.type() + (conn.sticky() ? " - slot setting" : ""));
                    }

                    logger.info(builder.toString());

                    azureApp.stop();

                    applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.DEPLOYED);

                    logger.info("Application stopped successfully!");

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Application stopped successfully!", applicationModel);

                } else {

                    logger.info("Application isn't created successfully!");
                    applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Application isn't stopped successfully!", applicationModel);

                }

            } else {
                logger.info("Application isn't stopped successfully!");
                applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Application isn't stopped successfully!", applicationModel);
            }

        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), applicationModel);
        }
    }

    @Override
    public SPIResponse deleteApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {
        logger.info("Amazon Adapter invoked for stopping application: " + credentialsModel.getUsername() + ", Name: " + applicationModel.getName());

        String client = null;
        String tenant = null;
        String key = null;
        String planName = "paasportplan"; // ResourceNamer.randomResourceName("paasportplan", 15);
        String rgName = "paasportrg"; //ResourceNamer.randomResourceName("paasportrg", 24);

        try {
            ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(client, tenant, key, AzureEnvironment.AZURE);
            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                    .authenticate(credentials)
                    .withDefaultSubscription();

            if (null != azure) {

                logger.info("Selected subscription: " + azure.subscriptionId());

                logger.info("Deleting application: " + applicationModel.getName() + " in resource group: " + rgName);

                azure.webApps().deleteByGroup(rgName, applicationModel.getName());

                applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.DELETED);

                logger.info("Application deleted successfully!");

                return new SPIResponse(BasicResponseCode.SUCCESS, "Application deleted successfully!", applicationModel);

            } else {
                logger.info("Application isn't deleted successfully!");
                applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Application isn't deleted successfully!", applicationModel);
            }

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
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }

    @Override
    public SPIResponse getAvailableStacks(CredentialsModel credentialsModel, PaaSOfferingModel paaSOfferingModel) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }

    @Override
    public SPIResponse createServiceBinding(CredentialsModel credentialsModel, ApplicationModel applicationModel, ServiceModel serviceModel) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
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
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }

    @Override
    public SPIResponse getAllBoundServices(CredentialsModel credentialsModel, ApplicationModel applicationModel) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }
}
