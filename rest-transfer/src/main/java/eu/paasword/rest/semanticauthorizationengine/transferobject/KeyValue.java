package eu.paasword.rest.semanticauthorizationengine.transferobject;

import java.io.Serializable;

/**
 * Created by smantzouratos on 18/11/2016.
 */
public class KeyValue implements Serializable {

    private String key;
    private String value;

    public KeyValue() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
