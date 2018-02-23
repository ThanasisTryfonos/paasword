package eu.paasword.spi.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by smantzouratos on 23/12/2016.
 */
public class VirtualMachineModel implements Serializable {

    private String instanceID;
    private CloudProviderModel cloudProvider;
    private UserModel user;
    private String state;
    private Date measurement;
    private String runningUUID;
    private String ip;
    private String name;
    private boolean usable;
    private String cpu;
    private String ram;
    private String disk;
    private String instanceType;

    public VirtualMachineModel() {
    }

    public String getInstanceID() {
        return instanceID;
    }

    public void setInstanceID(String instanceID) {
        this.instanceID = instanceID;
    }

    public CloudProviderModel getCloudProvider() {
        return cloudProvider;
    }

    public void setCloudProvider(CloudProviderModel cloudProvider) {
        this.cloudProvider = cloudProvider;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getMeasurement() {
        return measurement;
    }

    public void setMeasurement(Date measurement) {
        this.measurement = measurement;
    }

    public String getRunningUUID() {
        return runningUUID;
    }

    public void setRunningUUID(String runningUUID) {
        this.runningUUID = runningUUID;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUsable() {
        return usable;
    }

    public void setUsable(boolean usable) {
        this.usable = usable;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getDisk() {
        return disk;
    }

    public void setDisk(String disk) {
        this.disk = disk;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }
}
