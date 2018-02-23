package eu.paasword.spi.model;

import java.io.Serializable;

/**
 * Created by smantzouratos on 30/11/2016.
 */
public class CredentialsModel implements Serializable {

    private Long id;

    private String username;

    private String password;

    private String publicKey;

    private String privateKey;

    private PaaSOfferingModel paaSOffering;

    public CredentialsModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public PaaSOfferingModel getPaaSOffering() {
        return paaSOffering;
    }

    public void setPaaSOffering(PaaSOfferingModel paaSOffering) {
        this.paaSOffering = paaSOffering;
    }
}
