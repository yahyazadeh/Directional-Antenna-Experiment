/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sepand;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.log4j.Logger;
import sepand.dialog.NewGatewayDialog;
import sepand.dialog.PasswordDialog;
import sepand.entities.Gateway;
import sepand.enums.SourceCode;
import sepand.util.CommandUtil;

/**
 *
 * @author daniel
 */
public class Sepand extends Application {

    final private CommandUtil cmd = new CommandUtil();
    private StringProperty defaultMonitorSrcCodePath = new SimpleStringProperty();
    private StringProperty phaserSrcCodePath = new SimpleStringProperty();
    final private String phaserInstallCommand = "make phaser upload"; // make phaser upload
    final private String phaserInstallSuccessSigniture = "Reset device";
    final private int phaserInstallCommandNumberoFRetry = 1;
    final private String moteInstallCommand = "export BSLPORT=/dev/ttyUSB[0-3] && make telosb upload";

    private TableView motesTable = new TableView();
    private ObservableList<Gateway> gateways = FXCollections.observableArrayList();
    private final ProgressIndicator phaserPI = new ProgressIndicator(0);
    private final ProgressIndicator motesPI = new ProgressIndicator(0);

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sepand");
        Group root = new Group();
        Scene scene = new Scene(root, 800, 600);

        TabPane tabPane = new TabPane();
        BorderPane mainPane = new BorderPane();

        //Create Tabs
        Tab phaserTab = new Tab();
        phaserTab.setClosable(false);
        phaserTab.setText("Phaser");

        final FileChooser phaserfileChooser = new FileChooser();

        GridPane phaserGrid = new GridPane();
//        grid.setAlignment(Pos.CENTER);
        phaserGrid.setHgap(10);
        phaserGrid.setVgap(10);
        phaserGrid.setPadding(new Insets(5, 5, 5, 5));

        GridPane configGrid = new GridPane();
//        grid.setAlignment(Pos.CENTER);
        configGrid.setHgap(10);
        configGrid.setVgap(10);
        configGrid.setPadding(new Insets(5, 5, 5, 5));

        Label label2 = new Label("Phaser's source code path:");
        configGrid.add(label2, 0, 1);

        TextField pTextField = new TextField();
        pTextField.textProperty().bindBidirectional(phaserSrcCodePath);
        configGrid.add(pTextField, 1, 1);

        Button phaserOpenDlgButton = new Button("...");
        phaserOpenDlgButton.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                File file = phaserfileChooser.showOpenDialog(primaryStage);
                if (file != null) {
                    setFilePath(file, SourceCode.PHASER);
                }
            }
        });
        configGrid.add(phaserOpenDlgButton, 2, 1);

        TitledPane titledPane = new TitledPane("Configuration", configGrid);
        titledPane.setCollapsible(false);
        titledPane.setPadding(new Insets(5, 5, 5, 5));

        phaserGrid.add(titledPane, 0, 0);
        GridPane.setHgrow(titledPane, Priority.ALWAYS);

        GridPane installationGrid = new GridPane();
//        grid.setAlignment(Pos.CENTER);
        installationGrid.setHgap(10);
        installationGrid.setVgap(10);
        installationGrid.setPadding(new Insets(5, 5, 5, 5));

        Label label3 = new Label("Status:");
        installationGrid.add(label3, 0, 0);

        Label labelStatus = new Label("");
        installationGrid.add(labelStatus, 1, 0);

        Button phaserCheckStatusButton = new Button("Check Status");
        phaserCheckStatusButton.setDisable(true);
        installationGrid.add(phaserCheckStatusButton, 2, 0);

        Button phaserInstallButton = new Button("Install");
        phaserInstallButton.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                installPhaser();
            }
        });
        installationGrid.add(phaserInstallButton, 3, 0);
        phaserPI.setVisible(false);
        installationGrid.add(phaserPI, 4, 0);

        TitledPane titledPane2 = new TitledPane("Installation", installationGrid);
        titledPane2.setCollapsible(false);
        titledPane2.setPadding(new Insets(0, 5, 5, 5));

        phaserGrid.add(titledPane2, 0, 1);

        phaserTab.setContent(phaserGrid);
        tabPane.getTabs().add(phaserTab);

        Tab motesTab = new Tab();
        motesTab.setClosable(false);
        motesTab.setText("Motes");

        final FileChooser monitorfileChooser = new FileChooser();

        GridPane motesGrid = new GridPane();
//        grid.setAlignment(Pos.CENTER);
        motesGrid.setHgap(10);
        motesGrid.setVgap(10);
        motesGrid.setPadding(new Insets(5, 5, 5, 5));

        GridPane moteconfigGrid = new GridPane();
        moteconfigGrid.setHgap(10);
        moteconfigGrid.setVgap(10);
        moteconfigGrid.setPadding(new Insets(5, 5, 5, 5));

        Label label1 = new Label("Default Monitor:");
        moteconfigGrid.add(label1, 0, 0);

        TextField dmTextField = new TextField();
        dmTextField.textProperty().bindBidirectional(defaultMonitorSrcCodePath);
        moteconfigGrid.add(dmTextField, 1, 0);

        Button monitorOpenDlgButton = new Button("...");
        monitorOpenDlgButton.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                File file = monitorfileChooser.showOpenDialog(primaryStage);
                if (file != null) {
                    setFilePath(file, SourceCode.DEFAULT_MONITOR);
                }
            }
        });
        moteconfigGrid.add(monitorOpenDlgButton, 2, 0);

        TitledPane titledPane3 = new TitledPane("Configuration", moteconfigGrid);
        titledPane3.setCollapsible(false);
        titledPane3.setPadding(new Insets(5, 5, 5, 5));

        motesGrid.add(titledPane3, 0, 0);
        GridPane.setHgrow(titledPane3, Priority.ALWAYS);

        GridPane motesInstGrid = new GridPane();
        motesInstGrid.setHgap(10);
        motesInstGrid.setVgap(10);
        motesInstGrid.setPadding(new Insets(5, 5, 5, 5));

        TableColumn nameCol = new TableColumn("Gateway Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<Gateway,String>("name"));
        TableColumn ipCol = new TableColumn("Gateway IP");
        ipCol.setCellValueFactory(new PropertyValueFactory<Gateway,String>("ipAddress"));
        motesTable.getColumns().addAll(nameCol, ipCol);
        motesTable.setItems(gateways);
        motesInstGrid.add(motesTable, 0, 0);

        Button addGatewayButton = new Button("Add");
        addGatewayButton.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                addGateway();
            }
        });
        motesInstGrid.add(addGatewayButton, 0, 1);

        TitledPane titledPane4 = new TitledPane("Installation", motesInstGrid);
        titledPane4.setCollapsible(false);
        titledPane4.setPadding(new Insets(0, 5, 5, 5));

        motesGrid.add(titledPane4, 0, 1);

        motesTab.setContent(motesGrid);
        tabPane.getTabs().add(motesTab);

        mainPane.setCenter(tabPane);

        mainPane.prefHeightProperty().bind(scene.heightProperty());
        mainPane.prefWidthProperty().bind(scene.widthProperty());

        root.getChildren().add(mainPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setFilePath(File file, SourceCode sourceCode) {
        String path = file.getParent();
        if (sourceCode == SourceCode.DEFAULT_MONITOR) {
            defaultMonitorSrcCodePath.set(path);
            System.out.println(defaultMonitorSrcCodePath);
        }
        if (sourceCode == SourceCode.PHASER) {
            phaserSrcCodePath.set(path);
            System.out.println(phaserSrcCodePath);
        }
    }

    private void installPhaser() {
        PasswordDialog pd = new PasswordDialog();
        Optional<String> result = pd.showAndWait();
        result.ifPresent(password -> runInstallPhaserCommand(password));
        //        System.out.println(cmd.executeCommand(command));
    }

    private void addGateway() {
        NewGatewayDialog pd = new NewGatewayDialog();
        Optional<Gateway> result = pd.showAndWait();
        result.ifPresent(gateway -> gateways.add(gateway));
    }

    private void runInstallPhaserCommand(String password) {
        
        Dialog pleaseWaitDialog = new Dialog();
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                String output = "";
                int retry = phaserInstallCommandNumberoFRetry;
                do {
                    output = cmd.executeCommand(phaserSrcCodePath.get(), phaserInstallCommand, true, password);
                    retry--;
                } while (!output.contains(phaserInstallSuccessSigniture) && retry >= 0);

                output = cmd.executeCommand(phaserSrcCodePath.get(), phaserInstallCommand, true, password);

                if (!output.contains(phaserInstallSuccessSigniture)) {
                    return false;
                } else {
                    return true;
                }
            }
        };

        task.setOnRunning((e) -> {
            pleaseWaitDialog.setContentText("Please Wait...");
            pleaseWaitDialog.show();
        });
        task.setOnSucceeded((e) -> {
            try {
                // Dummy close button
                pleaseWaitDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
                pleaseWaitDialog.close();
                Boolean result = task.get();
                if (!result) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error Dialog");
                    alert.setHeaderText("Error!");
                    alert.setContentText("Ooops, there was an error!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Information Dialog");
                    alert.setHeaderText("Success!");
                    alert.setContentText("Installation has done successfully!");
                    alert.showAndWait();
                }
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(Sepand.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                java.util.logging.Logger.getLogger(Sepand.class.getName()).log(Level.SEVERE, null, ex);
            }

        });
        new Thread(task).start();

    }

    private void runInstallForAllMotesCommand() {
        // TODO: Should be completed
        String command = moteInstallCommand;
        for (Gateway gateway : gateways) {
            installOnMotesOfGateway(gateway, command);
        }
    }

    private void installOnMotesOfGateway(Gateway gateway, String command) {
        // TODO: Should be completed
//        cmd.executeSSHCommand(gateway.getIpAddress(), gateway.getUsername(), gateway.getPassword(), command);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
