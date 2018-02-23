package eu.paasword.util.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Created by smantzouratos on 27/04/16.
 */
public class PrivacyConstraint implements Serializable {

    private long id;
    private String name;
    private List<String> constraints;

    public PrivacyConstraint() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<String> constraints) {
        this.constraints = constraints;
    }
}
