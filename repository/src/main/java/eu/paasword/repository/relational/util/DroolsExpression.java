package eu.paasword.repository.relational.util;

import eu.paasword.repository.relational.domain.Instance;
import eu.paasword.repository.relational.domain.Property;

import java.io.Serializable;
import java.util.List;

/**
 * Created by smantzouratos on 06/03/2017.
 */
public class DroolsExpression implements Serializable {

    private Long expressionID;
    private List<Instance> instances;
    private List<Property> predicates;
    private List<Triple> triples;
    private String eval;
    private String nested;

    public DroolsExpression() {
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }

    public Long getExpressionID() {
        return expressionID;
    }

    public void setExpressionID(Long expressionID) {
        this.expressionID = expressionID;
    }

    public List<Property> getPredicates() {
        return predicates;
    }

    public void setPredicates(List<Property> predicates) {
        this.predicates = predicates;
    }

    public List<Triple> getTriples() {
        return triples;
    }

    public void setTriples(List<Triple> triples) {
        this.triples = triples;
    }

    public String getEval() {
        return eval;
    }

    public void setEval(String eval) {
        this.eval = eval;
    }

    public String getNested() {
        return nested;
    }

    public void setNested(String nested) {
        this.nested = nested;
    }
}
