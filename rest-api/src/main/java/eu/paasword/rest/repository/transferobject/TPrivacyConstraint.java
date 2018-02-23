package eu.paasword.rest.repository.transferobject;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.List;

/**
 * Created by smantzouratos on 26/04/16.
 */
public class TPrivacyConstraint implements Serializable {

    private long id;
    private long applicationID;
    private String name;
    private List<String> privacyConstraint;

    public TPrivacyConstraint() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(long applicationID) {
        this.applicationID = applicationID;
    }

    public List<String> getPrivacyConstraint() {
        return privacyConstraint;
    }

    public void setPrivacyConstraint(List<String> privacyConstraint) {
        this.privacyConstraint = privacyConstraint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
