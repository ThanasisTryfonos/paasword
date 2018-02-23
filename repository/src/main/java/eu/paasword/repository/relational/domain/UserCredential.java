package eu.paasword.repository.relational.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by smantzouratos on 29/11/2016.
 */
@Entity
@Table(name = "user_credential")
public class UserCredential {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = true)
    private String username;

    @Column(nullable = true)
    private String password;

    @Column(nullable = true, name = "private_key")
    private String privateKey;

    @Column(nullable = true, name = "public_key")
    private String publicKey;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private User user;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private ProxyCloudProvider proxyCloudProvider;

    @Column(nullable = true, name = "date_created")
    private Date dateCreated;

    @UpdateTimestamp
    @Column(nullable = false, name = "last_modified", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date lastModified;

    @Column(nullable = false, columnDefinition = "bit(1) default 1")
    private boolean enabled = true;

    public UserCredential() {
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

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ProxyCloudProvider getProxyCloudProvider() {
        return proxyCloudProvider;
    }

    public void setProxyCloudProvider(ProxyCloudProvider proxyCloudProvider) {
        this.proxyCloudProvider = proxyCloudProvider;
    }
}
