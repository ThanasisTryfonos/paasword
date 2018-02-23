package eu.paasword.validator.engine;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Created by smantzouratos on 22/03/2017.
 */
public class OnlineSecurityPolicyValidator {

    private static final Logger logger = Logger.getLogger(OnlineSecurityPolicyValidator.class.getName());
    private static final String validationServiceUrl = "http://securitypolicyvalidator.herokuapp.com/rest/validateSecurityPolicy";

    static RestTemplate restTemplate;

    public static ValidationReport validatePolicy(String contextModelTTL, String policyModelTTL) {

        ValidationReport validationReport = new ValidationReport();

        logger.info("Context Model: " + contextModelTTL);

        logger.info("Policy Model: " + policyModelTTL);


        if (null == restTemplate) {
            restTemplate = new RestTemplate();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", Arrays.asList("text/plain"));

        String body = contextModelTTL + "\n\n";

        body += policyModelTTL + "\n\n";

        HttpEntity entity = new HttpEntity(body, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(validationServiceUrl, HttpMethod.POST, entity, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

            String response = responseEntity.getBody();

            validationReport.setValid(true);

            logger.info("Response (SUCCESS): " + response);

        } else {
            logger.severe("Response (FAILED): " + responseEntity.getStatusCode() + ", Body: " + responseEntity.getBody());
            validationReport.setValid(false);
        }


        return validationReport;

    }

}
