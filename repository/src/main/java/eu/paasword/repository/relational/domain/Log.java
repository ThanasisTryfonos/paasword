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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import eu.paasword.util.parser.ParserUtil;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author smantzouratos
 */
@Entity
@Table(name = "log", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
public class Log implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @JoinColumn(name = "application_id", referencedColumnName = "id")
    @OneToOne(optional = true)
    @Cascade(CascadeType.SAVE_UPDATE)
    @JsonBackReference
    private Application applicationID;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 500)
    @Column(name = "object")
    private String object;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "actor")
    private String actor;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "remote_address")
    private String remoteAddress;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "local_address")
    private String localAddress;

    @Basic(optional = false)
    @NotNull
    @Column(name = "header", columnDefinition = "TEXT")
    private String header;

    @Basic(optional = true)
    @Column(name = "annotated_code", columnDefinition = "TEXT")
    private String annotatedCode;

    @Column(nullable = false, name = "invocation_timestamp")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date invocationTimestamp;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 15)
    @Column(name = "permission")
    private String permission;

    @Basic(optional = true)
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Basic(optional = false)
    @Column(name = "date_registered", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private String dateRegistered;

    public Log() {
    }

    public Log(Application applicationID, String object, String actor, String remoteAddress, String localAddress, String header, String annotatedCode, Date invocationTimestamp) {
        this.applicationID = applicationID;
        this.object = object;
        this.actor = actor;
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.header = header;
        this.annotatedCode = annotatedCode;
        this.invocationTimestamp = invocationTimestamp;
    }

    public Log(long id, String object, String actor, String remoteAddress, String localAddress, String header, String annotatedCode, Date invocationTimestamp, String permission) {
        this.id = id;
        this.object = object;
        this.actor = actor;
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.header = header;
        this.annotatedCode = annotatedCode;
        this.invocationTimestamp = invocationTimestamp;
        this.permission = permission;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Application getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(Application applicationID) {
        this.applicationID = applicationID;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getAnnotatedCode() {
        return annotatedCode;
    }

    public void setAnnotatedCode(String annotatedCode) {
        this.annotatedCode = annotatedCode;
    }

    public Date getInvocationTimestamp() {
        return invocationTimestamp;
    }

    public void setInvocationTimestamp(Date invocationTimestamp) {
        this.invocationTimestamp = invocationTimestamp;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(String dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public String getAgent() {

        return ParserUtil.parseHTTPHeader(this.getHeader()).getUserAgent();
    }

    public String getReferrer() {

        return ParserUtil.parseHTTPHeader(this.getHeader()).getReferer();
    }

    public String getPolicies() {

        String policies = "";

        JSONArray policiesArray = new JSONArray(this.getAnnotatedCode());

        for (int i =0; i < policiesArray.length(); i++) {

            policies += policiesArray.get(i).toString() + ", ";
        }

        return policies.substring(0, policies.length() - 2);
    }

    public String getTimestamp() {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return formatter.format(this.invocationTimestamp);
    }

}

