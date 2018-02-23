package eu.paasword.util.entities;

import java.io.Serializable;

/**
 * Created by smantzouratos on 26/01/2017.
 */
public class PolicyModel implements Serializable {

    private String policyModelJSON;
    private String policyModelCache;
    private String policyModelXACML;
    private String policyModelRDF;
    private String validationMessage;

    public PolicyModel(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public PolicyModel(String policyModelJSON, String policyModelCache, String policyModelXACML, String policyModelRDF) {
        this.policyModelJSON = policyModelJSON;
        this.policyModelCache = policyModelCache;
        this.policyModelXACML = policyModelXACML;
        this.policyModelRDF = policyModelRDF;
    }

    public String getPolicyModelCache() {
        return policyModelCache;
    }

    public void setPolicyModelCache(String policyModelCache) {
        this.policyModelCache = policyModelCache;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public String getPolicyModelJSON() {
        return policyModelJSON;
    }

    public void setPolicyModelJSON(String policyModelJSON) {
        this.policyModelJSON = policyModelJSON;
    }

    public String getPolicyModelXACML() {
        return policyModelXACML;
    }

    public void setPolicyModelXACML(String policyModelXACML) {
        this.policyModelXACML = policyModelXACML;
    }

    public String getPolicyModelRDF() {
        return policyModelRDF;
    }

    public void setPolicyModelRDF(String policyModelRDF) {
        this.policyModelRDF = policyModelRDF;
    }
}
