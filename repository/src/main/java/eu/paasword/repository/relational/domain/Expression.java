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

import org.hibernate.annotations.Cascade;
import org.json.JSONArray;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author smantzouratos
 */
@Entity
@Table(name = "expression", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
public class Expression implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "name")
    private String expressionName;
    @Basic(optional = false)
    @NotNull
    @Column(name = "expression", columnDefinition="TEXT")
    private String expression;
    @Basic(optional = true)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @JoinColumn(name = "namespace_id", referencedColumnName = "id")
    @OneToOne(optional = true)
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private Namespace namespaceID;
    @Basic(optional = false)
    @Column(name = "last_modified", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private String lastModified;
    @Basic(optional = true)
    @Column(name = "friendly_data", columnDefinition="TEXT")
    private String expressionFriendlyData;
    @Basic(optional = true)
    @Column(name = "instance_set_ids", columnDefinition = "TEXT")
    private String instanceSetIDs;

    @Basic(optional = true)
    @Column(name = "referred_expressions", columnDefinition = "TEXT")
    private String referredExpressions;

    @Basic(optional = true)
    @Size(min = 1, max = 5)
    @Column(name = "expr_condition")
    private String condition;

    @Column(nullable = false, name = "date_created")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dateCreated;

    public Expression() {
    }

    public enum Condition {

        AND,
        OR
    }

    public Namespace getNamespaceID() {
        return namespaceID;
    }

    public void setNamespaceID(Namespace namespaceID) {
        this.namespaceID = namespaceID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExpressionName() {
        return expressionName;
    }

    public void setExpressionName(String expressionName) {
        this.expressionName = expressionName;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getExpressionFriendlyData() {
        return expressionFriendlyData;
    }

    public void setExpressionFriendlyData(String expressionFriendlyData) {
        this.expressionFriendlyData = expressionFriendlyData;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstanceSetIDs() {
        return instanceSetIDs;
    }

    public void setInstanceSetIDs(String instanceSetIDs) {
        this.instanceSetIDs = instanceSetIDs;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getReferredExpressions() {
        return referredExpressions;
    }

    public void setReferredExpressions(String referredExpressions) {
        this.referredExpressions = referredExpressions;
    }

    public List<String> getInstances() {

        List<String> instances = new ArrayList<>();

        JSONArray instancesArray = new JSONArray(this.getInstanceSetIDs());

        for (int i=0; i < instancesArray.length(); i++) {

            instances.add(instancesArray.getString(i));

        }

        return instances;
    }

    public List<String> getReferredExpressionsFormatted() {

        List<String> referredExpressionsFormatted = new ArrayList<>();

        JSONArray referredExpressionsFormattedArray = new JSONArray(this.getReferredExpressions());

        for (int i=0; i < referredExpressionsFormattedArray.length(); i++) {

            referredExpressionsFormatted.add(referredExpressionsFormattedArray.getString(i));

        }

        return referredExpressionsFormatted;

    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
