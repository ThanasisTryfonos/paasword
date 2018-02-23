package eu.paasword.spi.model;

import java.io.Serializable;

/**
 * Created by smantzouratos on 30/11/2016.
 */
public class SSHKeyModel implements Serializable {

    private Long id;
    private String sshFriendlyName;
    private String sshPubKey;

    public SSHKeyModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSshFriendlyName() {
        return sshFriendlyName;
    }

    public void setSshFriendlyName(String sshFriendlyName) {
        this.sshFriendlyName = sshFriendlyName;
    }

    public String getSshPubKey() {
        return sshPubKey;
    }

    public void setSshPubKey(String sshPubKey) {
        this.sshPubKey = sshPubKey;
    }
}
