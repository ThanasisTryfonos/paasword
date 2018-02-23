package eu.paasword.rest.dbproxy.transferobject;


import java.io.Serializable;

/**
 * Created by smantzouratos on 26/04/16.
 */

public class TQuery implements Serializable{

    private String appAPIKey;
    private String appInstanceAPIKey;
    private String userKey;
    private String userPrincipal;
    private String query;
    private String transactionid;
    private String resid;

    public TQuery() {
    }        

    public TQuery(String appAPIKey, String appInstanceAPIKey, String query) {
        this.appAPIKey = appAPIKey;
        this.appInstanceAPIKey = appInstanceAPIKey;
        this.query = query;
    }

    public TQuery(String appAPIKey, String appInstanceAPIKey, String query, String transactionid) {
        this.appAPIKey = appAPIKey;
        this.appInstanceAPIKey = appInstanceAPIKey;
        this.query = query;
        this.transactionid = transactionid;
    }    
    
    public TQuery(String appAPIKey, String appInstanceAPIKey, String query, String resid, String transactionid) {
        this.appAPIKey = appAPIKey;
        this.appInstanceAPIKey = appInstanceAPIKey;
        this.query = query;
        this.resid = resid;
        this.transactionid = transactionid;
    }

    public String getUserPrincipal() {
        return userPrincipal;
    }

    public void setUserPrincipal(String userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getTransactionid() {
        return transactionid;
    }

    public void setTransactionid(String transactionid) {
        this.transactionid = transactionid;
    }


    public String getAppAPIKey() {
        return appAPIKey;
    }

    public void setAppAPIKey(String appAPIKey) {
        this.appAPIKey = appAPIKey;
    }

    public String getAppInstanceAPIKey() {
        return appInstanceAPIKey;
    }

    public void setAppInstanceAPIKey(String appInstanceAPIKey) {
        this.appInstanceAPIKey = appInstanceAPIKey;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getResid() {
        return resid;
    }

    public void setResid(String resid) {
        this.resid = resid;
    }
           
}
