package eu.paasword.spi.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by smantzouratos on 30/11/2016.
 */
public class CloudProviderModel implements Serializable {

    private Long id;
    private String name;
    private CredentialsModel credentialsModel;

    public CloudProviderModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CredentialsModel getCredentialsModel() {
        return credentialsModel;
    }

    public void setCredentialsModel(CredentialsModel credentialsModel) {
        this.credentialsModel = credentialsModel;
    }
}
