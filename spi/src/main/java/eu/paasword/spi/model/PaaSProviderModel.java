package eu.paasword.spi.model;

import java.io.Serializable;

/**
 * Created by smantzouratos on 30/11/2016.
 */
public class PaaSProviderModel implements Serializable {

    private Long id;
    private String name;
    private UserModel user;

    public PaaSProviderModel() {
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

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }
}
