package eu.paasword.util.entities;

import java.io.Serializable;

/**
 * Created by smantzouratos on 26/01/2017.
 */
public class ContextModel implements Serializable {

    private String contextModelJSON;
    private String contextModelRDF;

    public ContextModel(String contextModelJSON, String contextModelRDF) {
        this.contextModelJSON = contextModelJSON;
        this.contextModelRDF = contextModelRDF;
    }

    public String getContextModelJSON() {
        return contextModelJSON;
    }

    public void setContextModelJSON(String contextModelJSON) {
        this.contextModelJSON = contextModelJSON;
    }

    public String getContextModelRDF() {
        return contextModelRDF;
    }

    public void setContextModelRDF(String contextModelRDF) {
        this.contextModelRDF = contextModelRDF;
    }
}
