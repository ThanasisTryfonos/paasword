package eu.paasword.util.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Created by smantzouratos on 27/04/16.
 */
public class AnnotatedCode implements Serializable {

    private long id;
    private String name;
    private String type;
    private List<AnnotatedMethod> methods;
    private List<AnnotatedField> fields;
    private List<AnnotatedAnnotation> annotations;

    public AnnotatedCode() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<AnnotatedMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<AnnotatedMethod> methods) {
        this.methods = methods;
    }

    public List<AnnotatedField> getFields() {
        return fields;
    }

    public void setFields(List<AnnotatedField> fields) {
        this.fields = fields;
    }

    public List<AnnotatedAnnotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotatedAnnotation> annotations) {
        this.annotations = annotations;
    }

}
