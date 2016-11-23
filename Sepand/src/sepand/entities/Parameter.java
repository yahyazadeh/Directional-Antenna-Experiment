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
public class Parameter {

    private SimpleStringProperty variable;
    private SimpleStringProperty value;

    public Parameter() {
        variable = new SimpleStringProperty();
        value = new SimpleStringProperty();
    }

    public final String getVariable() {
        return variable.getValue();
    }
    
    public StringProperty variableProperty() {
        return variable;
    }

    public final void setVariable(String variable) {
        this.variable.setValue(variable);
    }

    public final String getValue() {
        return value.getValue();
    }
    
    public StringProperty valueProperty() {
        return value;
    }

    public final void setValue(String value) {
        this.value.setValue(value);
    }

}
