package eu.paasword.spi.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created by smantzouratos on 08/12/2016.
 */
public class MetaModel implements Serializable {

    private UUID guid;
    private Date created;
    private Date updated;
    private String url;

    public MetaModel(UUID guid, Date created, Date updated, String url) {
        this.guid = guid;
        this.created = created;
        this.updated = updated;
        this.url = url;
    }

    public UUID getGuid() {
        return guid;
    }

    public void setGuid(UUID guid) {
        this.guid = guid;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public MetaModel() {
    }
}
