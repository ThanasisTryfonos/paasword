package eu.paasword.adapter.openstack.nova;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Image;

import java.util.List;

/**
 * Created by smantzouratos on 16/09/16.
 */
public class NovaImage {

    public NovaImage(OSClient.OSClientV2 os) {
        this.os = os;
    }

    private final OSClient.OSClientV2 os;

    // Querying for Images.
    /**
     * List all Images (detailed @see #list(boolean detailed) for brief).
     */
    public List<? extends Image> getAllImages() {
        List<? extends Image> images = os.compute().images().list();

        return images;

    }

    /**
     * Get an Image by ID.
     */
    public Image getImageById(String imageId) {
        Image image = os.compute().images().get(imageId);

        return image;

    }



}
