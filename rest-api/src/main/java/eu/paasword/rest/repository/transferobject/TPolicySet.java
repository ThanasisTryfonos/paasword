package eu.paasword.rest.repository.transferobject;

import java.io.Serializable;

/**
 * Created by smantzouratos on 24/05/16.
 */
public class TPolicySet implements Serializable {

    private long id;
    private String policySetName;
    private String description;
    private String policiesCustom;
    private long policySetCombiningAlgorithmID;
    private long namespaceID;

    public TPolicySet() {
    }

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

    public String getPolicySetName() {
        return policySetName;
    }

    public void setPolicySetName(String policySetName) {
        this.policySetName = policySetName;
    }

    public String getPoliciesCustom() {
        return policiesCustom;
    }

    public void setPoliciesCustom(String policiesCustom) {
        this.policiesCustom = policiesCustom;
    }

    public long getPolicySetCombiningAlgorithmID() {
        return policySetCombiningAlgorithmID;
    }

    public void setPolicySetCombiningAlgorithmID(long policySetCombiningAlgorithmID) {
        this.policySetCombiningAlgorithmID = policySetCombiningAlgorithmID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
