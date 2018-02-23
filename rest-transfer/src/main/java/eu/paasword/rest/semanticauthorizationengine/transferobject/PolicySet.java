
package eu.paasword.rest.semanticauthorizationengine.transferobject;

import java.util.List;

/**
 *
 * @author smantzouratos
 */
public class PolicySet {
    
    private String policysetidentifier;             
    private String policysetcombiningalgorithm;     //Available options are :  XXXX
    private List<Policy> policies;                  //Policy objects

    public PolicySet() {
    }

    public PolicySet(String policysetidentifier, String policysetcombiningalgorithm, List<Policy> policies) {
        this.policysetidentifier = policysetidentifier;
        this.policysetcombiningalgorithm = policysetcombiningalgorithm;
        this.policies = policies;
    }
    
    public String getPolicysetidentifier() {
        return policysetidentifier;
    }

    public void setPolicysetidentifier(String policysetidentifier) {
        this.policysetidentifier = policysetidentifier;
    }

    public String getPolicysetcombiningalgorithm() {
        return policysetcombiningalgorithm;
    }

    public void setPolicysetcombiningalgorithm(String policysetcombiningalgorithm) {
        this.policysetcombiningalgorithm = policysetcombiningalgorithm;
    }

    public List<Policy> getPolicies() {
        return policies;
    }

    public void setPolicies(List<Policy> policies) {
        this.policies = policies;
    }     
    
}
