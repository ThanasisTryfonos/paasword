package eu.paasword.util.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Created by smantzouratos on 27/04/16.
 */
public class AnnotatedMethod implements Serializable {

    private long id;
    private String name;
    private List<AnnotatedAnnotation> methodAnnotations;
    private List<AnnotatedParameter> parameters;

    public AnnotatedMethod() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AnnotatedAnnotation> getMethodAnnotations() {
        return methodAnnotations;
    }

    public void setMethodAnnotations(List<AnnotatedAnnotation> methodAnnotations) {
        this.methodAnnotations = methodAnnotations;
    }

    public List<AnnotatedParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<AnnotatedParameter> parameters) {
        this.parameters = parameters;
    }
}
