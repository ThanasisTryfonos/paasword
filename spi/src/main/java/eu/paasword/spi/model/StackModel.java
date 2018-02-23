package eu.paasword.spi.model;

import java.io.Serializable;

/**
 * Created by smantzouratos on 08/12/2016.
 */
public class StackModel implements Serializable {

    private String name;
    private String description;
    private String url;
    private boolean active;
    private MetaModel metaModel;

    public StackModel() {
    }

    public MetaModel getMetaModel() {
        return metaModel;
    }

    public void setMetaModel(MetaModel metaModel) {
        this.metaModel = metaModel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
