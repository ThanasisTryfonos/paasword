package eu.paasword.adapter.openstack.util;

/**
 * Created by smantzouratos on 16/09/16.
 */
public class KeyPairObj {

    @Override
    public String toString() {
        return "KeyPairObj{" +
                "publicKey='" + publicKey + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", name='" + name + '\'' +
                '}';

    }

    /**
     * Setters & Getters
     */
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

    public String getName() {
        return name;

    }

    public void setName(String name) {
        this.name = name;

    }

    /**
     * Constructors
     */
    //Default Constructor
    public KeyPairObj() {
        // Default

    }

    public KeyPairObj(String publicKey, String privateKey, String name) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.name = name;

    }

    // Internal variables
    private String publicKey;
    private String privateKey;
    private String name;

}
