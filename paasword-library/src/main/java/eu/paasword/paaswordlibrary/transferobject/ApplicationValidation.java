package eu.paasword.paaswordlibrary.transferobject;

import java.io.Serializable;

/**
 * Created by smantzouratos on 07/03/2017.
 */
public class ApplicationValidation implements Serializable {

    private String appKey;
    private boolean validated;
    private boolean paaSwordJPAEnabled;

    public ApplicationValidation(String appKey, boolean validated, boolean paaSwordJPAEnabled) {
        this.appKey = appKey;
        this.validated = validated;
        this.paaSwordJPAEnabled = paaSwordJPAEnabled;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public boolean isPaaSwordJPAEnabled() {
        return paaSwordJPAEnabled;
    }

    public void setPaaSwordJPAEnabled(boolean paaSwordJPAEnabled) {
        this.paaSwordJPAEnabled = paaSwordJPAEnabled;
    }
}
