/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sepand;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.Logger;
import sepand.dialog.NewGatewayDialog;
import sepand.dialog.PasswordDialog;
import sepand.entities.Gateway;
import sepand.entities.GatewaysListWrapper;
import sepand.enums.SourceCode;
import sepand.util.CommandUtil;

/**
 *
 * @author daniel
 */
public class Sepand extends Application {

    final static Logger logger = Logger.getLogger(Sepand.class);
    
    final private CommandUtil cmd = new CommandUtil();
    final private String phaserInstallCommand = "make phaser upload"; // make phaser upload
    final private String phaserInstallSuccessSigniture = "Reset device";
    final private int phaserInstallCommandNumberoFRetry = 1;
    final private String motesInstallSuccessSigniture = "Reset device";
    final private int motesInstallCommandNumberoFRetry = 1;
    final private String defaultCodePathOnGateway = "/home/pi/github/MansOS/apps/santa-test/src/app_monitor";
    final private String moteInstallCommand = "export BSLPORT=/dev/ttyUSB[0-3] && make telosb upload";
    final private String xmlFilePath = "setting.xml";
    
    final private String backgroundColor = "lightgrey";

    private StringProperty defaultMonitorSrcCodePath = new SimpleStringProperty();
    private StringProperty phaserSrcCodePath = new SimpleStringProperty();
    private TableView<Gateway> motesTable = new TableView<>();
    private ObservableList<Gateway> gateways = FXCollections.observableArrayList();

    @Override
    public void stop() throws Exception {
        storeCurrentSetting();
        super.stop(); 
    }

    @Override
    public void init() throws Exception {
        restoreCurrentSetting();
        super.init();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sepand");
        Group root = new Group();
        Scene scene = new Scene(root, 800, 600);

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: " + backgroundColor);
        BorderPane mainPane = new BorderPane();

        //Create Tabs
        Tab phaserTab = new Tab();
        phaserTab.setClosable(false);
        phaserTab.setText("Phaser");

        final FileChooser phaserfileChooser = new FileChooser();

        Label label2 = new Label("Phaser's source code path:");

        TextField pTextField = new TextField();
        pTextField.textProperty().bindBidirectional(phaserSrcCodePath);

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
        
        HBox phaserConfigHBox = new HBox(label2, pTextField, phaserOpenDlgButton);
        phaserConfigHBox.setStyle("-fx-spacing: 5");
        phaserConfigHBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(pTextField, Priority.ALWAYS);

        TitledPane titledPane = new TitledPane("Configuration", phaserConfigHBox);
        titledPane.setCollapsible(false);
        titledPane.setPadding(new Insets(5, 5, 5, 5));


        Label label3 = new Label("Status:");
        
        Label labelStatus = new Label(" ");
        
        Pane pane = new Pane();

        Button phaserCheckStatusButton = new Button("Check Status");
        phaserCheckStatusButton.setMinWidth(100);
        phaserCheckStatusButton.setDisable(true);

        Button phaserInstallButton = new Button("Install");
        phaserInstallButton.setMinWidth(100);
        phaserInstallButton.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                installPhaser();
            }
        });

        HBox phaserInstallationHBox = new HBox(label3, labelStatus, pane, 
                phaserCheckStatusButton, phaserInstallButton);
        phaserInstallationHBox.setStyle("-fx-spacing: 5");
        phaserInstallationHBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(pane, Priority.ALWAYS);
        
        TitledPane titledPane2 = new TitledPane("Installation", phaserInstallationHBox);
        titledPane2.setCollapsible(false);
        titledPane2.setPadding(new Insets(0, 5, 5, 5));

        
        VBox phaserVBox = new VBox(titledPane, titledPane2);

        phaserTab.setContent(phaserVBox);
        tabPane.getTabs().add(phaserTab);

        Tab motesTab = new Tab();
        motesTab.setClosable(false);
        motesTab.setText("Motes");

        final FileChooser monitorfileChooser = new FileChooser();


        Label label1 = new Label("Default Monitor:");

        TextField dmTextField = new TextField();
        dmTextField.textProperty().bindBidirectional(defaultMonitorSrcCodePath);

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
        
        HBox motesConfigHBox = new HBox(label1, dmTextField, monitorOpenDlgButton);
        motesConfigHBox.setStyle("-fx-spacing: 5");
        motesConfigHBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(dmTextField, Priority.ALWAYS);

        TitledPane titledPane3 = new TitledPane("Configuration", motesConfigHBox);
        titledPane3.setCollapsible(false);
        titledPane3.setPadding(new Insets(5, 5, 5, 5));


        TableColumn nameCol = new TableColumn("Gateway Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<Gateway, String>("name"));
        TableColumn ipCol = new TableColumn("Gateway IP");
        ipCol.setCellValueFactory(new PropertyValueFactory<Gateway, String>("ipAddress"));
        TableColumn isCol = new TableColumn("Installation Status");
        isCol.setCellValueFactory(new PropertyValueFactory<Gateway, String>("installationStatus"));
        motesTable.getColumns().addAll(nameCol, ipCol, isCol);
        motesTable.setItems(gateways);


        Button newGatewayButton = new Button("New");
        newGatewayButton.setMinWidth(80);
        newGatewayButton.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                newGateway();
            }
        });
        
        Button deleteGatewayButton = new Button("Delete");
        deleteGatewayButton.setMinWidth(80);
        deleteGatewayButton.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                deleteGateway();
            }
        });

        Button installPhaserButton = new Button("Install");
        installPhaserButton.setMinWidth(80);
        installPhaserButton.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                runInstallForAllMotesCommand();
            }
        });
        
        HBox motesInstallButtonsHBox = new HBox(newGatewayButton, deleteGatewayButton, installPhaserButton);
        motesInstallButtonsHBox.setStyle("-fx-spacing: 5");
        motesInstallButtonsHBox.setAlignment(Pos.CENTER_RIGHT);
        
        VBox moteInstallVBox = new VBox(motesTable, motesInstallButtonsHBox);
        moteInstallVBox.setSpacing(10);

        TitledPane titledPane4 = new TitledPane("Installation", moteInstallVBox);
        titledPane4.setCollapsible(false);
        titledPane4.setPadding(new Insets(0, 5, 5, 5));

        VBox motesVBox = new VBox(titledPane3, titledPane4);
        

        motesTab.setContent(motesVBox);
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

    private void newGateway() {
        NewGatewayDialog pd = new NewGatewayDialog();
        Optional<Gateway> result = pd.showAndWait();
        result.ifPresent(gateway -> gateways.add(gateway));
    }
    
    private void deleteGateway() {
        Gateway selectedGateway = motesTable.getSelectionModel().getSelectedItem();
        if (selectedGateway != null) {
            gateways.remove(selectedGateway);
        }
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
                logger.error("Error Line: " + ex);
            } catch (ExecutionException ex) {
                logger.error("Error Line: " + ex);
            }

        });
        new Thread(task).start();

    }

    private void runInstallForAllMotesCommand() {
        String command = moteInstallCommand;
        command = "cd " + defaultCodePathOnGateway + " ; " + command;
        for (Gateway gateway : gateways) {
            installOnMotesOfGateway(gateway, command);
        }
    }

    private void installOnMotesOfGateway(Gateway gateway, String command) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String scpResult = "";
                String sshResult = "";
                scpResult = cmd.executeSCPCommand(gateway.getIpAddress(), gateway.getUsername(),
                        gateway.getPassword(), defaultMonitorSrcCodePath.get() + "/*", defaultCodePathOnGateway);
                if (scpResult.contains("")) {
                    int retry = motesInstallCommandNumberoFRetry;
                    do {
                        sshResult = cmd.executeSSHCommand(gateway.getIpAddress(), gateway.getUsername(),
                                gateway.getPassword(), command);
                        retry--;
                    } while (!sshResult.contains(motesInstallSuccessSigniture) && retry >= 0);
                }
                if (!sshResult.contains(motesInstallSuccessSigniture)) {
                    return false;
                } else {
                    return true;
                }
            }
        };

        task.setOnRunning((e) -> {
            gateway.setInstallationStatus("Please Wait...");
        });
        task.setOnSucceeded((e) -> {
            try {
                Boolean result = task.get();
                if (!result) {
                    gateway.setInstallationStatus("Error!");
                } else {
                    gateway.setInstallationStatus("Successful");
                }
            } catch (InterruptedException ex) {
                logger.error("Error Line: " + ex);
            } catch (ExecutionException ex) {
                logger.error("Error Line: " + ex);
            }
        });
        new Thread(task).start();
    }

    /**
     * Loads data from the specified file.
     *
     * @param file
     */
    public void loadPersonDataFromFile(File file) {
        try {
            JAXBContext context = JAXBContext
                    .newInstance(GatewaysListWrapper.class);
            Unmarshaller um = context.createUnmarshaller();

            // Reading XML from the file and unmarshalling.
            GatewaysListWrapper wrapper = (GatewaysListWrapper) um.unmarshal(file);

            gateways.clear();
            if (wrapper.getGateways() != null) {
                gateways.addAll(wrapper.getGateways());
            }

        } catch (Exception e) { // catches ANY exception
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load data");
            alert.setContentText("Could not load data from file:\n" + file.getPath());

            alert.showAndWait();
        }
    }

    /**
     * Saves the current data to the specified file.
     *
     * @param file
     */
    public void savePersonDataToFile(File file) {
        try {
            JAXBContext context = JAXBContext
                    .newInstance(GatewaysListWrapper.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Wrapping gateway data.
            GatewaysListWrapper wrapper = new GatewaysListWrapper();
            wrapper.setGateways(gateways);

            // Marshalling and saving XML to the file.
            m.marshal(wrapper, file);

        } catch (Exception e) { // catches ANY exception
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save data");
            alert.setContentText("Could not save data to file:\n" + file.getPath());

            alert.showAndWait();
        }
    }

    private void storeCurrentSetting() {
        Preferences prefs = Preferences.userNodeForPackage(Sepand.class);
        prefs.put("phaserSrcCodePath", phaserSrcCodePath.get());
        prefs.put("defaultMonitorSrcCodePath", defaultMonitorSrcCodePath.get());
        File file = new File(xmlFilePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                logger.error("Error Line: " + ex);
            }
        }
        savePersonDataToFile(file);
    }

    private void restoreCurrentSetting() {
        Preferences prefs = Preferences.userNodeForPackage(Sepand.class);
        File file = new File(xmlFilePath);
        if (file.exists()) {
            loadPersonDataFromFile(file);
        }
        phaserSrcCodePath.set(prefs.get("phaserSrcCodePath", ""));
        defaultMonitorSrcCodePath.set(prefs.get("defaultMonitorSrcCodePath", ""));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
