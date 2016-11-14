/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sepand.dialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import sepand.entities.Gateway;

/**
 *
 * @author daniel
 */
public class NewGatewayDialog extends Dialog<Gateway> {

    private TextField nameField;
    private TextField ipAddressField;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button connectButton;
    private Gateway gateway;

    public NewGatewayDialog() {
        setTitle("New Gateway");
        setHeaderText("Please enter the gateway information:");

        ButtonType addGatewayButtonType = new ButtonType("Add", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(addGatewayButtonType, ButtonType.CANCEL);

        GridPane gatewayGrid = new GridPane();
        gatewayGrid.setAlignment(Pos.CENTER);
        gatewayGrid.setHgap(10);
        gatewayGrid.setVgap(10);
        gatewayGrid.setPadding(new Insets(5, 5, 5, 5));

        Label label1 = new Label("Name:");
        gatewayGrid.add(label1, 0, 1);

        nameField = new TextField();
        gatewayGrid.add(nameField, 1, 1);
        
        Label label2 = new Label("IP Address:");
        gatewayGrid.add(label2, 0, 2);

        ipAddressField = new TextField();
        gatewayGrid.add(ipAddressField, 1, 2);
        
        Label label3 = new Label("Username:");
        gatewayGrid.add(label3, 0, 3);

        usernameField = new TextField();
        gatewayGrid.add(usernameField, 1, 3);
        
        Label label4 = new Label("Password:");
        gatewayGrid.add(label4, 0, 4);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        gatewayGrid.add(passwordField, 1, 4);
        
        connectButton = new Button("Connect");
        gatewayGrid.add(connectButton, 0, 5);

        getDialogPane().setContent(gatewayGrid);

        Platform.runLater(() -> passwordField.requestFocus());

        setResultConverter(dialogButton -> {
            if (dialogButton == addGatewayButtonType) {
                createNewGateway();
                return gateway;
            }
            return null;
        });
    }
    
    private void createNewGateway(){
        gateway = new Gateway();
        gateway.setName(nameField.getText());
        gateway.setIpAddress(ipAddressField.getText());
        gateway.setUsername(usernameField.getText());
        gateway.setPassword(passwordField.getText());
    }

    public Gateway getGateway() {
        return gateway;
    }
}
