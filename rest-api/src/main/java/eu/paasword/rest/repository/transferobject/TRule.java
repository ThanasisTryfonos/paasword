package eu.paasword.rest.repository.transferobject;

import java.io.Serializable;

/**
 * Created by smantzouratos on 24/05/16.
 */
public class TRule implements Serializable {

    private long id;
    private String ruleName;
    private String description;
    private String controlledObject;
    private String authorization;
    private String action;
    private String actor;
    private long expressionID;
    private String permissionType;
    private long namespaceID;

    public TRule() {
    }

    public String getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }

    public long getNamespaceID() {
        return namespaceID;
    }

    public void setNamespaceID(long namespaceID) {
        this.namespaceID = namespaceID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getControlledObject() {
        return controlledObject;
    }

    public void setControlledObject(String controlledObject) {
        this.controlledObject = controlledObject;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public long getExpressionID() {
        return expressionID;
    }

    public void setExpressionID(long expressionID) {
        this.expressionID = expressionID;
    }
}
