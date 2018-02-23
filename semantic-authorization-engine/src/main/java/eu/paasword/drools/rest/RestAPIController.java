package eu.paasword.drools.rest;

import eu.paasword.drools.service.RulesEngineService;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordObjectResponse;
import eu.paasword.rest.semanticauthorizationengine.transferobject.AuthorizationRequest;
import eu.paasword.rest.semanticauthorizationengine.transferobject.AuthorizationResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import eu.paasword.drools.util.Message;
/*
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
@RestController
@RequestMapping("/api/semanticpolicyengine")
public class RestAPIController {

    private static final Logger logger = Logger.getLogger(RestAPIController.class.getName());

    @Autowired
    RulesEngineService res;

    @RequestMapping(method = RequestMethod.GET)
    public String test() {
        logger.info("Rest Test Request");
        return "echo";
    }

    @RequestMapping(value = "/loadontology", method = RequestMethod.GET)
    public PaaSwordObjectResponse loadOntology() {
        logger.info("REST - loadOntology");
        try {
            res.loadOntology();
            return new PaaSwordObjectResponse(BasicResponseCode.SUCCESS.name(), Message.SUCCESS, Optional.empty());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), Message.GENERAL_ERROR, Optional.empty());
        }
    }//EoM
    
    @RequestMapping(value = "/loadrules", method = RequestMethod.GET)
    public PaaSwordObjectResponse loadRules() {
        logger.info("REST - loadOntology");
        try {
            res.refreshKnowledgebase();
            res.loadOntology();
            return new PaaSwordObjectResponse(BasicResponseCode.SUCCESS.name(), Message.SUCCESS, Optional.empty());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), Message.GENERAL_ERROR, Optional.empty());
        }
    }//EoM

    @RequestMapping(value = "/handlerequest", method = RequestMethod.POST,consumes =  "application/json", produces = "application/json")
    public PaaSwordObjectResponse handleRequest(@RequestBody AuthorizationRequest authrequest) {
        logger.info("REST - handleRequest");
        try {
            logger.info("Handle request: ");
            AuthorizationResponse authresp = new AuthorizationResponse();
            String advice;
            advice = res.handleRequest(authrequest);
            authresp.setRequestid(authrequest.getRequestid());
            
            authresp.setAdvice(advice.equalsIgnoreCase("positive")?Message.REQUEST_ALLOW:Message.REQUEST_DENY);
            return new PaaSwordObjectResponse(BasicResponseCode.SUCCESS.name(), Message.SUCCESS, authresp);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), Message.GENERAL_ERROR, Optional.empty());
        }
    }//EoM

        
}//EoC
