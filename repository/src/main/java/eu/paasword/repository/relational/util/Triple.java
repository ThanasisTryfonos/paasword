package eu.paasword.repository.relational.util;

import eu.paasword.repository.relational.domain.Clazz;
import eu.paasword.repository.relational.domain.Instance;
import eu.paasword.repository.relational.domain.Property;

import java.io.Serializable;

/**
 * Created by smantzouratos on 06/03/2017.
 */
public class Triple implements Serializable {

    private Property property;
    private Clazz domainClazz;
    private Instance domainInstance;
    private Clazz rangeClazz;
    private Instance rangeInstance;
    private String condition; // equals, not equals

    public Triple() {
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public Clazz getDomainClazz() {
        return domainClazz;
    }

    public void setDomainClazz(Clazz domainClazz) {
        this.domainClazz = domainClazz;
    }

    public Instance getDomainInstance() {
        return domainInstance;
    }

    public void setDomainInstance(Instance domainInstance) {
        this.domainInstance = domainInstance;
    }

    public Clazz getRangeClazz() {
        return rangeClazz;
    }

    public void setRangeClazz(Clazz rangeClazz) {
        this.rangeClazz = rangeClazz;
    }

    public Instance getRangeInstance() {
        return rangeInstance;
    }

    public void setRangeInstance(Instance rangeInstance) {
        this.rangeInstance = rangeInstance;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
