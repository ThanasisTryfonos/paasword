/*
 *  Copyright 2016 PaaSword Framework, http://www.paasword.eu/
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.paasword.repository.relational.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author smantzouratos
 */
@Entity
@Table(name = "iaas_provider_instance")
public class IaaSProviderInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    // (String serverName, String flavorId, String imageId, List<String> networkNames, String keyPairName, String path, String contents)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "friendly_name")
    private String friendlyName;

    @Basic(optional = false)
    @NotNull
    @Column(name = "flavor_id")
    private int flavorID;

    @JoinColumn(name = "iaas_provider_image_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @OneToOne(optional = true)
    private IaaSProviderImage iaasProviderImageID;

    @Basic(optional = false)
    @NotNull
    @Column(name = "network", columnDefinition = "TEXT")
    private String network;

    @Basic(optional = true)
    @Size(min = 1, max = 250)
    @Column(name = "key_pair")
    private String keyPair;

    @Basic(optional = true)
    @Size(min = 1, max = 250)
    @Column(name = "db_name")
    private String dbName;

    @Basic(optional = true)
    @Size(min = 1, max = 250)
    @Column(name = "db_user")
    private String dbUser;

    @Basic(optional = true)
    @Size(min = 1, max = 250)
    @Column(name = "db_password")
    private String dbPassword;

    @Basic(optional = true)
    @Size(min = 1, max = 250)
    @Column(name = "public_ip")
    private String publicIP;

    @Basic(optional = true)
    @Column(name = "other", columnDefinition = "TEXT")
    private String other;

    @JoinColumn(name = "iaas_provider_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @OneToOne(optional = true)
    private IaaSProvider iaasProviderID;

    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @OneToOne(optional = true)
    private User userID;

    @Column(nullable = false, name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Basic(optional = false)
    @Column(name = "last_modified", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private String lastModified;

    @Column(nullable = false, columnDefinition = "bit(1) default 1", name = "enabled")
    private boolean enabled = true;

    public IaaSProviderInstance() {
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getPublicIP() {
        return publicIP;
    }

    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public int getFlavorID() {
        return flavorID;
    }

    public void setFlavorID(int flavorID) {
        this.flavorID = flavorID;
    }

    public IaaSProviderImage getIaasProviderImageID() {
        return iaasProviderImageID;
    }

    public void setIaasProviderImageID(IaaSProviderImage iaasProviderImageID) {
        this.iaasProviderImageID = iaasProviderImageID;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(String keyPair) {
        this.keyPair = keyPair;
    }

    public IaaSProvider getIaasProviderID() {
        return iaasProviderID;
    }

    public void setIaasProviderID(IaaSProvider iaasProviderID) {
        this.iaasProviderID = iaasProviderID;
    }

    public User getUserID() {
        return userID;
    }

    public void setUserID(User userID) {
        this.userID = userID;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
