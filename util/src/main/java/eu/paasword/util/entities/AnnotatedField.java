package eu.paasword.util.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Created by smantzouratos on 27/04/16.
 */
public class AnnotatedField implements Serializable {

    private long id;
    private String name;
    private String type;
    private List<AnnotatedAnnotation> fieldAnnotations;

    public AnnotatedField() {
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

    public List<AnnotatedAnnotation> getFieldAnnotations() {
        return fieldAnnotations;
    }

    public void setFieldAnnotations(List<AnnotatedAnnotation> fieldAnnotations) {
        this.fieldAnnotations = fieldAnnotations;
    }
}
