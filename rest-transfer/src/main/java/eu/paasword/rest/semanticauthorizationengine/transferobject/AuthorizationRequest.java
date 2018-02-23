package eu.paasword.rest.semanticauthorizationengine.transferobject;

import java.util.List;

/**
 *
 * @author smantzouratos
 */
public class AuthorizationRequest {
    
    // Request Info that will be utilized to enrich the working memory (through the creation of IoCs and KTs )
    
    private String requestid;
    private String subjectinstance;
    private String objectinstance;
    private String actioninstance;
    private List<Handler>  handlers;
            
    // Additional elements that may be used by general purpose handlers
    
    private String requestContext;
    private String remoteAddress;
    
    //Parameters that will affect the session.fireAll( FILTER )
    //CONVENTION only one of the items below should be filled
    //They should be filled using a top down prioritization
    
    private List<PolicySet> policysets;
    private List<Policy> policies;
    private List<Rule> rules;

    public AuthorizationRequest() {
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getRequestid() {
        return requestid;
    }

    public void setRequestid(String requestid) {
        this.requestid = requestid;
    }

    public String getSubjectinstance() {
        return subjectinstance;
    }

    public void setSubjectinstance(String subjectinstance) {
        this.subjectinstance = subjectinstance;
    }

    public String getObjectinstance() {
        return objectinstance;
    }

    public void setObjectinstance(String objectinstance) {
        this.objectinstance = objectinstance;
    }

    public String getActioninstance() {
        return actioninstance;
    }

    public void setActioninstance(String actioninstance) {
        this.actioninstance = actioninstance;
    }

    public String getRequestContext() {
        return requestContext;
    }

    public void setRequestContext(String requestContext) {
        this.requestContext = requestContext;
    }

    public List<Handler> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<Handler> handlers) {
        this.handlers = handlers;
    }

    public List<PolicySet> getPolicysets() {
        return policysets;
    }

    public void setPolicysets(List<PolicySet> policysets) {
        this.policysets = policysets;
    }

    public List<Policy> getPolicies() {
        return policies;
    }

    public void setPolicies(List<Policy> policies) {
        this.policies = policies;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
}
