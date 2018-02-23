package eu.paasword.rest.repository.transferobject;

import java.io.Serializable;

/**
 * Created by smantzouratos on 31/08/16.
 */
public class TIaaSProviderInstance implements Serializable {

    private long id;
    private String friendlyName;
    private long imageID;
    private int flavorID;
    private long iaasProviderID;

    public TIaaSProviderInstance() {
    }

    public int getFlavorID() {
        return flavorID;
    }

    public void setFlavorID(int flavorID) {
        this.flavorID = flavorID;
    }

    public long getImageID() {
        return imageID;
    }

    public void setImageID(long imageID) {
        this.imageID = imageID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public long getIaasProviderID() {
        return iaasProviderID;
    }

    public void setIaasProviderID(long iaasProviderID) {
        this.iaasProviderID = iaasProviderID;
    }
}
