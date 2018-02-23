package eu.paasword.rest.repository.transferobject;

import java.io.Serializable;

/**
 * Created by smantzouratos on 31/08/16.
 */
public class TIaaSProviderImage implements Serializable {

    private long id;
    private String friendlyName;
    private String imageID;
    private long iaasProviderID;

    public TIaaSProviderImage() {
    }

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
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
