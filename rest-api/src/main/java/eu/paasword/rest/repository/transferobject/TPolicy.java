package eu.paasword.rest.repository.transferobject;

import java.io.Serializable;

/**
 * Created by smantzouratos on 24/05/16.
 */
public class TPolicy implements Serializable {

    private long id;
    private String policyName;
    private long policyCombiningAlgorithmID;
    private String rulesCustom;
    private long namespaceID;
    private String description;

    public long getNamespaceID() {
        return namespaceID;
    }

    public void setNamespaceID(long namespaceID) {
        this.namespaceID = namespaceID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public long getPolicyCombiningAlgorithmID() {
        return policyCombiningAlgorithmID;
    }

    public void setPolicyCombiningAlgorithmID(long policyCombiningAlgorithmID) {
        this.policyCombiningAlgorithmID = policyCombiningAlgorithmID;
    }

    public String getRulesCustom() {
        return rulesCustom;
    }

    public void setRulesCustom(String rulesCustom) {
        this.rulesCustom = rulesCustom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TPolicy() {
    }
}
