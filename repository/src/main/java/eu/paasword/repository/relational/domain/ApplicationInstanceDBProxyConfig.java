package eu.paasword.repository.relational.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by smantzouratos on 03/04/2017.
 */
@Entity
@Table(name = "application_instance_db_proxy_config", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
public class ApplicationInstanceDBProxyConfig {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @JoinColumn(name = "application_instance_id", referencedColumnName = "id")
    @OneToOne(optional = true)
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @JsonBackReference
    private ApplicationInstance applicationInstanceID;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "db_proxy_id")
    private String dbProxyID;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10024)
    @Column(name = "db_proxy_public_key")
    private String dbProxyPublicKey;

    @Basic(optional = true)
    @Size(min = 1, max = 10024)
    @Column(name = "secretKey")
    private String secretKey;

    @Basic(optional = false)
    @Size(min = 1, max = 250)
    @Column(name = "db_proxy_url", nullable = true)
    private String dbProxyURL;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10024)
    @Column(name = "application_instance_public_key")
    private String applicationInstancePublicKey;

    @Basic(optional = false)
    @Size(min = 1, max = 250)
    @Column(name = "application_instance_url", nullable = true)
    private String applicationInstanceURL;

    public ApplicationInstanceDBProxyConfig() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDbProxyID() {
        return dbProxyID;
    }

    public void setDbProxyID(String dbProxyID) {
        this.dbProxyID = dbProxyID;
    }

    public String getDbProxyPublicKey() {
        return dbProxyPublicKey;
    }

    public void setDbProxyPublicKey(String dbProxyPublicKey) {
        this.dbProxyPublicKey = dbProxyPublicKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getDbProxyURL() {
        return dbProxyURL;
    }

    public void setDbProxyURL(String dbProxyURL) {
        this.dbProxyURL = dbProxyURL;
    }

    public String getApplicationInstancePublicKey() {
        return applicationInstancePublicKey;
    }

    public void setApplicationInstancePublicKey(String applicationInstancePublicKey) {
        this.applicationInstancePublicKey = applicationInstancePublicKey;
    }

    public String getApplicationInstanceURL() {
        return applicationInstanceURL;
    }

    public void setApplicationInstanceURL(String applicationInstanceURL) {
        this.applicationInstanceURL = applicationInstanceURL;
    }

    public ApplicationInstance getApplicationInstanceID() {
        return applicationInstanceID;
    }

    public void setApplicationInstanceID(ApplicationInstance applicationInstanceID) {
        this.applicationInstanceID = applicationInstanceID;
    }
}
