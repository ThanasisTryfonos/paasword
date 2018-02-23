package eu.paasword.rest.repository.transferobject;

import eu.paasword.repository.relational.domain.Clazz;

import java.io.Serializable;
import java.util.List;

/**
 * Created by smantzouratos on 25/01/2017.
 */
public class TApplicationInstanceHandler implements Serializable {

    private long id;
    private String handlerName;
    private Clazz hasInput;
    private Clazz hasOutput;
    private List<String> restEndpointURIs;

    public TApplicationInstanceHandler() {
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

    public Clazz getHasInput() {
        return hasInput;
    }

    public void setHasInput(Clazz hasInput) {
        this.hasInput = hasInput;
    }

    public Clazz getHasOutput() {
        return hasOutput;
    }

    public void setHasOutput(Clazz hasOutput) {
        this.hasOutput = hasOutput;
    }

    public List<String> getRestEndpointURIs() {
        return restEndpointURIs;
    }

    public void setRestEndpointURIs(List<String> restEndpointURIs) {
        this.restEndpointURIs = restEndpointURIs;
    }
}
