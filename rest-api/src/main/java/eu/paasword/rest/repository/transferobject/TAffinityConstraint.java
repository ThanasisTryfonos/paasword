package eu.paasword.rest.repository.transferobject;

import java.io.Serializable;
import java.util.List;

/**
 * Created by smantzouratos on 26/04/16.
 */
public class TAffinityConstraint implements Serializable {

    private long id;
    private long applicationID;
    private String name;
    private List<String> affinityConstraint;

    public TAffinityConstraint() {
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

    public List<String> getAffinityConstraint() {
        return affinityConstraint;
    }

    public void setAffinityConstraint(List<String> affinityConstraint) {
        this.affinityConstraint = affinityConstraint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
