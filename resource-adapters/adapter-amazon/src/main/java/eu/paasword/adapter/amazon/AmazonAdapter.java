package eu.paasword.adapter.amazon;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.*;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.CreateDBInstanceRequest;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import eu.paasword.spi.adapter.PaaSAdapter;
import eu.paasword.spi.model.*;
import eu.paasword.spi.response.BasicResponseCode;
import eu.paasword.spi.response.SPIResponse;
import eu.paasword.spi.util.Util;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Created by smantzouratos on 21/09/16.
 */
public class AmazonAdapter implements PaaSAdapter {

    private static final Logger logger = Logger.getLogger(AmazonAdapter.class.getName());

    public static void main(String[] args) {

    }

    @Override
    public SPIResponse validateCredentials(CredentialsModel credentialsModel, PaaSOfferingModel paasOfferingModel) {
        try {

            BasicAWSCredentials aWSCredentials = new BasicAWSCredentials(credentialsModel.getPublicKey(), credentialsModel.getPrivateKey());
            AWSElasticBeanstalkClient elasticBeanstalkClient = new AWSElasticBeanstalkClient(aWSCredentials);

            if (null != elasticBeanstalkClient) {
                elasticBeanstalkClient.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));

                if (null != elasticBeanstalkClient.listAvailableSolutionStacks()) {
                    logger.info("Credentials validated successfully!");

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Credentials validated successfully!", paasOfferingModel);

                } else {
                    logger.info("Credentials are invalid!");
                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Credentials are invalid!");
                }

            } else {
                logger.info("Credentials are invalid!");
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Credentials are invalid!");
            }


        } catch (AmazonClientException ex) {
            logger.severe(ex.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Credentials are invalid!");
        }

    }

    @Override
    public SPIResponse createApplication(CredentialsModel credentialsModel, ApplicationRequestModel applicationRequestModel) {

        logger.info("Amazon Adapter invoked: " + credentialsModel.getPublicKey() + ", Name: " + applicationRequestModel.getName());
        ApplicationModel applicationModel = null;

        // Research on how to let user select those values
        String stackName = applicationRequestModel.getStackName(); //"64bit Amazon Linux 2015.03 v2.0.0 running Tomcat 7 Java 7";
        Region region = Region.getRegion(Regions.fromName(applicationRequestModel.getRegion())); //Regions.EU_CENTRAL_1);
        try {

            BasicAWSCredentials aWSCredentials = new BasicAWSCredentials(credentialsModel.getPublicKey(), credentialsModel.getPrivateKey());
            AWSElasticBeanstalkClient elasticBeanstalkClient = new AWSElasticBeanstalkClient(aWSCredentials);
            // Setting Region to a default value: Considering to put
            elasticBeanstalkClient.setRegion(region);
            // Check name
            CheckDNSAvailabilityRequest availabilityRequest = new CheckDNSAvailabilityRequest(applicationRequestModel.getName());
            CheckDNSAvailabilityResult checkDNSAvailabilityResult = elasticBeanstalkClient.checkDNSAvailability(availabilityRequest);
            // if (checkDNSAvailabilityResult.isAvailable()) {

            CreateApplicationRequest createApplicationRequest = new CreateApplicationRequest(applicationRequestModel.getName());
            CreateApplicationResult createApplication = elasticBeanstalkClient.createApplication(createApplicationRequest);
            String realApplicationName = createApplication.getApplication().getApplicationName();

            // Create Environment
            CreateEnvironmentRequest cr = new CreateEnvironmentRequest();
            cr.setApplicationName(applicationRequestModel.getName());
            cr.setEnvironmentName(applicationRequestModel.getName());
            cr.setSolutionStackName(stackName);
            cr.setCNAMEPrefix(applicationRequestModel.getName());

            CreateEnvironmentResult createEnvironmentResult = elasticBeanstalkClient.createEnvironment(cr);
            String endPoint = createEnvironmentResult.getEndpointURL();

            if (endPoint.indexOf("://") == -1) {
                endPoint = "http://" + endPoint;
            }

            applicationModel = new ApplicationModel();
            applicationModel.setRunningEndpoint(endPoint);
            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.CREATED);
            applicationModel.setName(createEnvironmentResult.getApplicationName());

            logger.info("Application created successfully!");

            return new SPIResponse(BasicResponseCode.SUCCESS, "Application created successfully!", applicationModel);

        } catch (AmazonClientException ex) {
            logger.severe(ex.getMessage());
            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), applicationModel);
        }

    }

    @Override
    public SPIResponse deployApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel, PackageLocatorModel packageLocatorModel) {

        logger.info("Amazon Adapter invoked for deployment for user: " + credentialsModel.getPublicKey() + ", Filename: " + packageLocatorModel.getFilename());

        String bucketName = "bucket" + packageLocatorModel.getFilename().substring(0, packageLocatorModel.getFilename().lastIndexOf(".")).replace("_", "");

        Region region = Region.getRegion(Regions.fromName(applicationModel.getApplicationRequest().getRegion()));

        String originalFilename = null;
        String suffix = null;
        File fileApp = null;

        try {

            BasicAWSCredentials aWSCredentials = new BasicAWSCredentials(credentialsModel.getPublicKey(), credentialsModel.getPrivateKey());
            AWSElasticBeanstalkClient elasticBeanstalkClient = new AWSElasticBeanstalkClient(aWSCredentials);
            AmazonS3Client amazonS3Client = new AmazonS3Client(aWSCredentials);
            amazonS3Client.setRegion(region);

            // Setting Region to a default value: Considering to put
            elasticBeanstalkClient.setRegion(region);

            // Create applicationModel version- upload war
            String applicationName = applicationModel.getName();

            CreateApplicationVersionRequest applicationVersionRequest = new CreateApplicationVersionRequest(applicationName, "v1");

            // Upload war - or use what is needed
            // S3BucketResource bucketResource = new S3BucketResource(bucketName);

            if (!amazonS3Client.doesBucketExist(bucketName)) {
                amazonS3Client.createBucket(bucketName);
            }

            if (null != packageLocatorModel.getFilename() && !packageLocatorModel.getFilename().isEmpty() && null != packageLocatorModel.getFile()) {

                originalFilename = packageLocatorModel.getFilename().substring(0, packageLocatorModel.getFilename().lastIndexOf("."));

                suffix = packageLocatorModel.getFilename().substring(packageLocatorModel.getFilename().lastIndexOf("."));

                fileApp = File.createTempFile(originalFilename, suffix);

                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(fileApp));
                FileCopyUtils.copy(packageLocatorModel.getFile(), stream);
                stream.close();

            } else if (null != packageLocatorModel.getFileURL()) {

                fileApp = new File(packageLocatorModel.getFileURL().getFile());

            } else {
                // TODO
                // throw error that file doesn't exist
            }

            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileApp.getName(), fileApp));

            S3Location warLocation = new S3Location(bucketName, fileApp.getName());
            applicationVersionRequest.setSourceBundle(warLocation);

            CreateApplicationVersionResult applicationResult = elasticBeanstalkClient.createApplicationVersion(applicationVersionRequest);

            UpdateEnvironmentRequest updateEnvironmentRequest = new UpdateEnvironmentRequest();
            updateEnvironmentRequest.setApplicationName(applicationName);
            updateEnvironmentRequest.setEnvironmentName(applicationName);
            updateEnvironmentRequest.setVersionLabel("v1");

            UpdateEnvironmentResult result = elasticBeanstalkClient.updateEnvironment(updateEnvironmentRequest);

            applicationModel.setRunningEndpoint(result.getEndpointURL());

            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.RUNNING);
            logger.info("Application deployed/is running successfully!");

            return new SPIResponse(BasicResponseCode.SUCCESS, "Application deployed/is running successfully!", applicationModel);

        } catch (AmazonClientException ex) {
            logger.severe(ex.getMessage());
            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), applicationModel);
        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.ERROR);
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), applicationModel);
        }
    }

    @Override
    public SPIResponse undeployApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }

    @Override
    public SPIResponse startApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }

    @Override
    public SPIResponse stopApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }

    @Override
    public SPIResponse deleteApplication(CredentialsModel credentialsModel, ApplicationModel applicationModel) {
        try {

            BasicAWSCredentials aWSCredentials = new BasicAWSCredentials(credentialsModel.getPublicKey(), credentialsModel.getPrivateKey());
            AWSElasticBeanstalkClient elasticBeanstalkClient = new AWSElasticBeanstalkClient(aWSCredentials);
            TerminateEnvironmentRequest termEnvironmentRequest = new TerminateEnvironmentRequest();
            termEnvironmentRequest.setEnvironmentName(applicationModel.getName());
            elasticBeanstalkClient.terminateEnvironment(termEnvironmentRequest);

            logger.info("Application deleted successfully!");

            applicationModel.setApplicationStateTypeModel(ApplicationStateTypeModel.DELETED);

            return new SPIResponse(BasicResponseCode.SUCCESS, "Application  deleted successfully!", applicationModel);

        } catch (Exception ex) {
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

        logger.info("Amazon Adapter invoked: " + credentialsModel.getPublicKey());

        List<String> listOfAvailableSolutionStacks = null;

        Region region = Region.getRegion(Regions.EU_CENTRAL_1);
        try {

            BasicAWSCredentials aWSCredentials = new BasicAWSCredentials(credentialsModel.getPublicKey(), credentialsModel.getPrivateKey());
            AWSElasticBeanstalkClient elasticBeanstalkClient = new AWSElasticBeanstalkClient(aWSCredentials);
            // Setting Region to a default value: Considering to put
            elasticBeanstalkClient.setRegion(region);

            listOfAvailableSolutionStacks = elasticBeanstalkClient.listAvailableSolutionStacks().getSolutionStacks();

            List<StackModel> stacks = new ArrayList<>();

            listOfAvailableSolutionStacks.stream().forEach(stack -> {

                StackModel stk = new StackModel();
                stk.setName(stack);
                stacks.add(stk);

            });

            paaSOfferingModel.setStacks(stacks);

            return new SPIResponse(BasicResponseCode.SUCCESS, "Stacks fetched successfully!", paaSOfferingModel);

        } catch (AmazonClientException ex) {
            logger.severe(ex.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), paaSOfferingModel);
        }
    }

    @Override
    public SPIResponse createServiceBinding(CredentialsModel credentialsModel, ApplicationModel applicationModel, ServiceModel serviceModel) {

        logger.info("Amazon Adapter invoked: " + credentialsModel.getPublicKey());

        try {

            BasicAWSCredentials aWSCredentials = new BasicAWSCredentials(credentialsModel.getPublicKey(), credentialsModel.getPrivateKey());
            AmazonRDSClient rdsClient = new AmazonRDSClient(aWSCredentials);

            if (null != rdsClient) {

                String dbName = applicationModel.getName();
                String dbUser = Util.generateRandomString(10, Util.Mode.ALPHA);
                String dbPassword = Util.generateRandomString(10, Util.Mode.ALPHANUMERIC);
                String dbIdentifier = dbName + "-" + dbUser;
                int dbSize = 5;
                String dbType = serviceModel.getName();

                CreateDBInstanceRequest dbInstanceRequest = new CreateDBInstanceRequest();
                dbInstanceRequest.setDBName(dbName);
                dbInstanceRequest.setEngine(dbType);
                dbInstanceRequest.setMasterUsername(dbUser);
                dbInstanceRequest.setMasterUserPassword(dbPassword);
                dbInstanceRequest.setDBInstanceIdentifier(dbIdentifier);
                dbInstanceRequest.setAllocatedStorage(dbSize);

                //TODO
                dbInstanceRequest.setDBInstanceClass("db.m1.small");

//                String group = dbUser;
//
//                Vector sec_groups = new Vector();
//                sec_groups.add(group);
//
//                dbInstanceRequest.setDBSecurityGroups(sec_groups);

                DBInstance dbInstance = new DBInstance();
                dbInstance = rdsClient.createDBInstance(dbInstanceRequest);

                if (null != dbInstance) {
                    logger.info("Address: " + dbInstance.getEndpoint().getAddress());
                    logger.info("Port: " + dbInstance.getEndpoint().getPort());

                    String extra = "{\"dbHost\":\"" + dbInstance.getEndpoint().getAddress() + "\",\"dbPort\":\"" + dbInstance.getEndpoint().getPort() + "\",\"dbName\":\"" + dbName + "\",\"dbUser\":\"" + dbUser + "\",\"dbPassword\":\"" + dbPassword +"\"}";

                    List<ServiceModel> serviceModels = null != applicationModel.getServices() ? applicationModel.getServices() : new ArrayList<>();

                    serviceModel.setExtra(extra);

                    serviceModels.add(serviceModel);
                    applicationModel.setServices(serviceModels);
                    logger.info("Service is bound successfully!");

                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Service is bound successfully!", applicationModel);

                } else {
                    logger.info("Service isn't bound successfully!");
                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Service isn't bound successfully!", applicationModel);
                }

            } else {
                logger.info("Service isn't bound successfully!");
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Service isn't bound successfully!", applicationModel);
            }

        } catch (AmazonClientException ex) {
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
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }

    @Override
    public SPIResponse getAllBoundServices(CredentialsModel credentialsModel, ApplicationModel applicationModel) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "This method is not supported!");
    }
}
