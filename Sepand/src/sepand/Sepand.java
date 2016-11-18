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
import javafx.scene.layout.Priority;
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
    final private String motesInstallSuccessSigniture = "****";
    final private int motesInstallCommandNumberoFRetry = 1;
    final private String defaultCodePathOnGateway = "/home/pi/github/MansOS/apps/santa-test/";
    final private String moteInstallCommand = "export BSLPORT=/dev/ttyUSB[0-3] && make telosb upload";
    final private String xmlFilePath = "setting.xml";

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
        nameCol.setCellValueFactory(new PropertyValueFactory<Gateway, String>("name"));
        TableColumn ipCol = new TableColumn("Gateway IP");
        ipCol.setCellValueFactory(new PropertyValueFactory<Gateway, String>("ipAddress"));
        TableColumn isCol = new TableColumn("Installation Status");
        isCol.setCellValueFactory(new PropertyValueFactory<Gateway, String>("installationStatus"));
        motesTable.getColumns().addAll(nameCol, ipCol, isCol);
        motesTable.setItems(gateways);
        motesInstGrid.add(motesTable, 0, 0);

        Button newGatewayButton = new Button("New");
        newGatewayButton.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                newGateway();
            }
        });
        motesInstGrid.add(newGatewayButton, 0, 1);
        
        Button deleteGatewayButton = new Button("Delete");
        deleteGatewayButton.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                deleteGateway();
            }
        });
        motesInstGrid.add(deleteGatewayButton, 1, 1);

        Button installPhaserButton = new Button("Install");
        installPhaserButton.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                runInstallForAllMotesCommand();
            }
        });
        motesInstGrid.add(installPhaserButton, 2, 1);

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
                        gateway.getPassword(), defaultMonitorSrcCodePath.get(), defaultCodePathOnGateway);
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
            gateway.setInstallationStatus("Please Waite...");
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
            gateways.addAll(wrapper.getGateways());

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
