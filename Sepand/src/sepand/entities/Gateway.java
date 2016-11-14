/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sepand.entities;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author daniel
 */
public class Gateway {
    private SimpleStringProperty name;
    private SimpleStringProperty ipAddress;
    private SimpleStringProperty username;
    private SimpleStringProperty password;

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getIpAddress() {
        return ipAddress.get();
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress.set(ipAddress);
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }
}
