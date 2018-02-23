package eu.paasword.rest.bounce;

import java.io.Serializable;

public class EmailImage implements Serializable {

    public String type;
    public String name;
    public String content;

    public EmailImage() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
