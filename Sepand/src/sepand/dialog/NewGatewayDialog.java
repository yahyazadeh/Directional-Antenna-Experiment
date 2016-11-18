/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sepand.dialog;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import sepand.entities.Gateway;
import sepand.util.CommandUtil;

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
    private Label connectionStatus;
    private Gateway gateway;
    private CommandUtil cmd;

    public NewGatewayDialog() {
        gateway = new Gateway();
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
        nameField.textProperty().bindBidirectional(gateway.nameProperty());
        nameField.setPromptText("e.g. My Gateway");
        gatewayGrid.add(nameField, 1, 1);

        Label label2 = new Label("IP Address:");
        gatewayGrid.add(label2, 0, 2);

        ipAddressField = new TextField();
        ipAddressField.textProperty().bindBidirectional(gateway.ipAddressProperty());
        ipAddressField.setPromptText("e.g. 192.168.0.2");
        gatewayGrid.add(ipAddressField, 1, 2);

        Label label3 = new Label("Username:");
        gatewayGrid.add(label3, 0, 3);

        usernameField = new TextField();
        usernameField.textProperty().bindBidirectional(gateway.usernameProperty());
        usernameField.setPromptText("Username");
        gatewayGrid.add(usernameField, 1, 3);

        Label label4 = new Label("Password:");
        gatewayGrid.add(label4, 0, 4);

        passwordField = new PasswordField();
        passwordField.textProperty().bindBidirectional(gateway.passwordProperty());
        passwordField.setPromptText("Password");
        gatewayGrid.add(passwordField, 1, 4);

        connectButton = new Button("Connect");
        connectButton.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                if (checkConnectivity(ipAddressField.getText(), usernameField.getText(), passwordField.getText())) {
                    connectionStatus.setText("Successful!");
                }
                else {
                    connectionStatus.setText("Connection Error!");
                }
            }
        });
        gatewayGrid.add(connectButton, 0, 5);
        
        connectionStatus = new Label();
        gatewayGrid.add(connectionStatus, 1, 5);

        getDialogPane().setContent(gatewayGrid);

        Platform.runLater(() -> nameField.requestFocus());

        setResultConverter(dialogButton -> {
            if (dialogButton == addGatewayButtonType) {
                return gateway;
            }
            return null;
        });
    }

    private boolean checkConnectivity(String host, String username, String password) {
        cmd = new CommandUtil();
        if (cmd.executeSSHCommand(host, username, password, "lscpu").contains("Architecture:")) {
            return true;
        } else {
            return false;
        }
    }

    public Gateway getGateway() {
        return gateway;
    }
}
