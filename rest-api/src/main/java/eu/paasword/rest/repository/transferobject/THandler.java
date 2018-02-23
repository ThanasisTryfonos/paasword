package eu.paasword.rest.repository.transferobject;

import java.io.Serializable;

/**
 * Created by smantzouratos on 26/04/16.
 */
public class THandler implements Serializable {

    private long id;
    private String handlerName;
    private long hasInputID;
    private long hasOutputID;
    private String restEndpointURI;
    private long namespaceID;

    public THandler() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public long getHasInputID() {
        return hasInputID;
    }

    public void setHasInputID(long hasInputID) {
        this.hasInputID = hasInputID;
    }

    public long getHasOutputID() {
        return hasOutputID;
    }

    public void setHasOutputID(long hasOutputID) {
        this.hasOutputID = hasOutputID;
    }

    public String getRestEndpointURI() {
        return restEndpointURI;
    }

    public void setRestEndpointURI(String restEndpointURI) {
        this.restEndpointURI = restEndpointURI;
    }

    public long getNamespaceID() {
        return namespaceID;
    }

    public void setNamespaceID(long namespaceID) {
        this.namespaceID = namespaceID;
    }
}
