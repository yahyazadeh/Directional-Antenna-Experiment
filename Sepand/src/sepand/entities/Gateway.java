/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sepand.entities;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author daniel
 */
public class Gateway {

    private SimpleStringProperty name;
    private SimpleStringProperty ipAddress;
    private SimpleStringProperty username;
    private SimpleStringProperty password;
    private SimpleStringProperty installationStatus;

    public Gateway() {
        name = new SimpleStringProperty();
        ipAddress = new SimpleStringProperty();
        username = new SimpleStringProperty();
        password = new SimpleStringProperty();
        installationStatus = new SimpleStringProperty();
    }

    public final String getName() {
        return name.getValue();
    }
    
    public StringProperty nameProperty() {
        return name;
    }

    public final void setName(String name) {
        this.name.setValue(name);
    }

    public final String getIpAddress() {
        return ipAddress.getValue();
    }
    
    public StringProperty ipAddressProperty() {
        return ipAddress;
    }

    public final void setIpAddress(String ipAddress) {
        this.ipAddress.setValue(ipAddress);
    }

    public final String getUsername() {
        return username.getValue();
    }
    
    public StringProperty usernameProperty() {
        return username;
    }

    public final void setUsername(String username) {
        this.username.setValue(username);
    }

    public final String getPassword() {
        return password.getValue();
    }
    
    public StringProperty passwordProperty() {
        return password;
    }

    public final void setPassword(String password) {
        this.password.setValue(password);
    }

    public final String getInstallationStatus() {
        return installationStatus.getValue();
    }
    
    public StringProperty installationStatusProperty() {
        return installationStatus;
    }

    public final void setInstallationStatus(String installationStatus) {
        this.installationStatus.setValue(installationStatus);
    }

}
