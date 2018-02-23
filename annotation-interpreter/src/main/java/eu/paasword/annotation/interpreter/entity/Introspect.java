package eu.paasword.annotation.interpreter.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by smantzouratos on 20/07/16.
 */
public class Introspect implements Serializable {

    private String annotatedCode;
    private boolean hasPEP;
    private boolean hasDataModel;
    private Map<String, Class<?>> mapOfDAOs;

    public Introspect(String annotatedCode, boolean hasPEP, boolean hasDataModel) {
        this.annotatedCode = annotatedCode;
        this.hasPEP = hasPEP;
        this.hasDataModel = hasDataModel;
    }

    public Introspect() {
    }

    public Map<String, Class<?>> getMapOfDAOs() {
        return mapOfDAOs;
    }

    public void setMapOfDAOs(Map<String, Class<?>> mapOfDAOs) {
        this.mapOfDAOs = mapOfDAOs;
    }

    public String getAnnotatedCode() {
        return annotatedCode;
    }

    public void setAnnotatedCode(String annotatedCode) {
        this.annotatedCode = annotatedCode;
    }

    public boolean isHasPEP() {
        return hasPEP;
    }

    public void setHasPEP(boolean hasPEP) {
        this.hasPEP = hasPEP;
    }

    public boolean isHasDataModel() {
        return hasDataModel;
    }

    public void setHasDataModel(boolean hasDataModel) {
        this.hasDataModel = hasDataModel;
    }
}
