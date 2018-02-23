package eu.paasword.spi.model;

import java.io.Serializable;
import java.net.URL;

/**
 * Created by smantzouratos on 30/11/2016.
 */
public class PackageLocatorModel implements Serializable {

    private String filename;
    private byte[] file;
    private URL fileURL;
    private String gitURL;

    public PackageLocatorModel() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public URL getFileURL() {
        return fileURL;
    }

    public void setFileURL(URL fileURL) {
        this.fileURL = fileURL;
    }

    public String getGitURL() {
        return gitURL;
    }

    public void setGitURL(String gitURL) {
        this.gitURL = gitURL;
    }
}
