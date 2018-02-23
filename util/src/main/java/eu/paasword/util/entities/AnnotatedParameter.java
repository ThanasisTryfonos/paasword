package eu.paasword.util.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Created by smantzouratos on 27/04/16.
 */
public class AnnotatedParameter implements Serializable {

    private long id;
    private String name;
    private String type;

    public AnnotatedParameter() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
