package eu.paasword.spi.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by smantzouratos on 23/12/2016.
 */
public class UsageModel implements Serializable {

    private CloudProviderModel cloudProvider;
    private String usageID;
    private UserModel user;
    private String startTimestamp;
    private String endTimestamp;
    private String computeTimestamp;
    private String frequency;
    private double disk;
    private double vm;
    private double ram;
    private double cpu;
    private double instanceTypeMicro;

    public UsageModel() {
    }

    public String getUsageID() {
        return usageID;
    }

    public void setUsageID(String usageID) {
        this.usageID = usageID;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public double getInstanceTypeMicro() {
        return instanceTypeMicro;
    }

    public void setInstanceTypeMicro(double instanceTypeMicro) {
        this.instanceTypeMicro = instanceTypeMicro;
    }

    public CloudProviderModel getCloudProvider() {
        return cloudProvider;
    }

    public void setCloudProvider(CloudProviderModel cloudProvider) {
        this.cloudProvider = cloudProvider;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(String startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(String endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getComputeTimestamp() {
        return computeTimestamp;
    }

    public void setComputeTimestamp(String computeTimestamp) {
        this.computeTimestamp = computeTimestamp;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public double getDisk() {
        return disk;
    }

    public void setDisk(double disk) {
        this.disk = disk;
    }

    public double getVm() {
        return vm;
    }

    public void setVm(double vm) {
        this.vm = vm;
    }

    public double getRam() {
        return ram;
    }

    public void setRam(double ram) {
        this.ram = ram;
    }

    public double getCpu() {
        return cpu;
    }

    public void setCpu(double cpu) {
        this.cpu = cpu;
    }
}
