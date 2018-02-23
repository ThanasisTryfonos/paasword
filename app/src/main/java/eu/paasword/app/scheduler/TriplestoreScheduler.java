package eu.paasword.app.scheduler;

import eu.paasword.repository.relational.service.TriplestoreService;
import eu.paasword.util.entities.ContextModel;
import eu.paasword.util.entities.PolicyModel;
import eu.paasword.validator.Validator;
import eu.paasword.validator.engine.ValidationReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.logging.Logger;

/**
 * Created by smantzouratos on 19/01/2017.
 */
@Configuration
public class TriplestoreScheduler {

    private final static int TRIPLESTORE_SCHEDULER_TIME = 600000;// Trigger scheduler every 10 minutes
    private final static Logger logger = Logger.getLogger(TriplestoreScheduler.class.getName());

    @Autowired
    TriplestoreService triplestoreService;

    @Autowired
    Validator validatorService;

    @Autowired
    Environment environment;

    @Scheduled(fixedDelay = TRIPLESTORE_SCHEDULER_TIME)
    public void triplestoreSynchronization() {

        String enabled = environment.getProperty("fuseki.triplestore.enabled");

        if (enabled.equals("true")) {

            logger.info("Starting synchronization...");
            long startTime = System.currentTimeMillis();

            // Fetching Context Model from DB
            ContextModel contextModel = triplestoreService.exportContextModel();

            if (null != contextModel) {

                // Synchronizing with Triplestore

//                if (triplestoreService.uploadContextModelToTriplestore(contextModel.getContextModelRDF())) {
//                    logger.info("Context Model synchronized successfully!");
//                } else {
//                    logger.info("Context Model didn't synchronize successfully!");
//                }

            }

            // Fetching Policy Model from DB

            PolicyModel policyModel = triplestoreService.exportPolicyModel();

            if (null != policyModel) {

                // Synchronizing with Triplestore

//                if (null != policyModel.getValidationMessage() && !policyModel.getValidationMessage().isEmpty()) {
//
////                    logger.info("Validation issue: " + policyModel.getValidationMessage());
//
//                } else {
//
//                    if (triplestoreService.uploadPolicyModelToTriplestore(policyModel.getPolicyModelRDF())) {
//                        logger.info("Policy Model synchronized successfully!");
//                    } else {
//                        logger.info("Policy Model didn't synchronize successfully!");
//                    }
//
//                }

            }


            if (null != contextModel && null != policyModel) {

                ValidationReport validationReport = validatorService.validateOnlinePolicyModel(contextModel.getContextModelRDF(), policyModel.getPolicyModelRDF());

                if (null != validationReport && validationReport.isValid()) {
                    logger.info("Context/Policy Model validated successfully!");
                } else {
                    logger.info("Context/Policy Model din't validate successfully!");
                }

            }

            logger.info(String.format("Total time for sync() %s ms", (System.currentTimeMillis() - startTime) / 1000));

        } else {
            logger.info("Triplestore synchronization is disabled!");
        }

    }

}
