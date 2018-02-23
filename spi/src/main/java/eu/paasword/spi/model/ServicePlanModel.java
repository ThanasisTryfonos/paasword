package eu.paasword.spi.model;

import java.io.Serializable;

/**
 * Created by smantzouratos on 19/12/2016.
 */
public class ServicePlanModel implements Serializable {

    private boolean _public;
    private String description;
    private String extra;
    private boolean free;
    private String name;
    private String uniqueID;

    public ServicePlanModel() {
    }

    public boolean is_public() {
        return _public;
    }

    public void set_public(boolean _public) {
        this._public = _public;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
