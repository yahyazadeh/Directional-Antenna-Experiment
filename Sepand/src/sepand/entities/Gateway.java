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
    private SimpleStringProperty mote0InstallationStatus;
    private SimpleStringProperty mote1InstallationStatus;
    private SimpleStringProperty mote2InstallationStatus;
    private SimpleStringProperty mote3InstallationStatus;

    public Gateway() {
        name = new SimpleStringProperty();
        ipAddress = new SimpleStringProperty();
        username = new SimpleStringProperty();
        password = new SimpleStringProperty();
        mote0InstallationStatus = new SimpleStringProperty();
        mote1InstallationStatus = new SimpleStringProperty();
        mote2InstallationStatus = new SimpleStringProperty();
        mote3InstallationStatus = new SimpleStringProperty();
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

    public final String getMote0InstallationStatus() {
        return mote0InstallationStatus.getValue();
    }
    
    public StringProperty mote0InstallationStatusProperty() {
        return mote0InstallationStatus;
    }

    public final void setMote0InstallationStatus(String mote0InstallationStatus) {
        this.mote0InstallationStatus.setValue(mote0InstallationStatus);
    }

    public final String getMote1InstallationStatus() {
        return mote1InstallationStatus.getValue();
    }
    
    public StringProperty mote1InstallationStatusProperty() {
        return mote1InstallationStatus;
    }

    public final void setMote1InstallationStatus(String mote1InstallationStatus) {
        this.mote1InstallationStatus.setValue(mote1InstallationStatus);
    }

    public final String getMote2InstallationStatus() {
        return mote2InstallationStatus.getValue();
    }
    
    public StringProperty mote2InstallationStatusProperty() {
        return mote2InstallationStatus;
    }

    public final void setMote2InstallationStatus(String mote2InstallationStatus) {
        this.mote2InstallationStatus.setValue(mote2InstallationStatus);
    }

    public final String getMote3InstallationStatus() {
        return mote3InstallationStatus.getValue();
    }
    
    public StringProperty mote3InstallationStatusProperty() {
        return mote3InstallationStatus;
    }

    public final void setMote3InstallationStatus(String mote3InstallationStatus) {
        this.mote3InstallationStatus.setValue(mote3InstallationStatus);
    }

}
