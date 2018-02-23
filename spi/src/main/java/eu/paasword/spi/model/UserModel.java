package eu.paasword.spi.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by smantzouratos on 30/11/2016.
 */
public class UserModel implements Serializable {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String username;

    private String password;

    private UserTypeModel userTypeModel;

    private UserActivationStatusModel activationStatus;

    private Date registrationDate;

    private Date activationDate;

    private Date deactivationDate;

    private String company;

    private String address;

    private String phone;

    private List<SSHKeyModel> sshKeys;

    public UserModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserTypeModel getUserTypeModel() {
        return userTypeModel;
    }

    public void setUserTypeModel(UserTypeModel userTypeModel) {
        this.userTypeModel = userTypeModel;
    }

    public UserActivationStatusModel getActivationStatus() {
        return activationStatus;
    }

    public void setActivationStatus(UserActivationStatusModel activationStatus) {
        this.activationStatus = activationStatus;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Date activationDate) {
        this.activationDate = activationDate;
    }

    public Date getDeactivationDate() {
        return deactivationDate;
    }

    public void setDeactivationDate(Date deactivationDate) {
        this.deactivationDate = deactivationDate;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<SSHKeyModel> getSshKeys() {
        return sshKeys;
    }

    public void setSshKeys(List<SSHKeyModel> sshKeys) {
        this.sshKeys = sshKeys;
    }
}
