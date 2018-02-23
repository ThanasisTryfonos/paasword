package eu.paasword.repository.relational.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by smantzouratos on 03/04/2017.
 */
@Entity
@Table(name = "application_instance_tenant_config", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
public class ApplicationInstanceTenantConfig {

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
    @Size(min = 1, max = 10024)
    @Column(name = "public_key")
    private String publicKey;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10024)
    @Column(name = "private_key")
    private String privateKey;

    public ApplicationInstanceTenantConfig() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ApplicationInstance getApplicationInstanceID() {
        return applicationInstanceID;
    }

    public void setApplicationInstanceID(ApplicationInstance applicationInstanceID) {
        this.applicationInstanceID = applicationInstanceID;
    }
}
