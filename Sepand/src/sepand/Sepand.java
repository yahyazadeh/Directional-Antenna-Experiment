/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sepand;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
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
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.Logger;
import sepand.dialog.NewGatewayDialog;
import sepand.dialog.PasswordDialog;
import sepand.entities.Gateway;
import sepand.entities.GatewaysListWrapper;
import sepand.entities.Parameter;
import sepand.enums.SourceCode;
import sepand.util.CommandUtil;
import sepand.util.RaspListener;

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
    final private String defaultCodePathOnGateway = "/home/pi/github/MansOS/apps/santa-test/src/app_monitor/";
    final private String moteInstallCommand = "export BSLPORT=/dev/ttyUSB%d && make telosb upload";
    final private String xmlFilePath = "setting.xml";
    final private String startParameterSection = "start parameter section";
    final private String endParameterSection = "end parameter section";

    final private String backgroundColor = "lightgrey";

    private StringProperty defaultMonitorSrcCodePath = new SimpleStringProperty();
    private StringProperty phaserSrcCodePath = new SimpleStringProperty();
    private StringProperty phaserSrcCodeMainFilePath = new SimpleStringProperty();
    private TableView<Gateway> motesTable = new TableView<>();
    private ObservableList<Gateway> gateways = FXCollections.observableArrayList();
    private ObservableList<Parameter> parameters = FXCollections.observableArrayList();
    private ObservableMap<String, ObservableList<Parameter>> parametersMap = FXCollections.observableHashMap();
    private GridPane parametersPane = new GridPane();
    private IntegerProperty fromPort = new SimpleIntegerProperty();
    private IntegerProperty toPort = new SimpleIntegerProperty();
    private StringProperty destListenerFilePath = new SimpleStringProperty();
    private StringProperty suffixFileName = new SimpleStringProperty();
    private List<String> keySet = new ArrayList<String>();

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
                    loadParametersFromFile();
                    refreshParameterSection();
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

//        parametersGridPane.setPadding(new Insets(10, 0, 10, 0));
        Button refreshParameterButton = new Button("Refresh");
        refreshParameterButton.setMinWidth(100);
        refreshParameterButton.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                loadParametersFromFile();
                refreshParameterSection();
            }
        });

        Button applyParameterButton = new Button("Apply");
        applyParameterButton.setMinWidth(100);
        applyParameterButton.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                saveParametersToFile();
            }
        });

        HBox parameterButtonsHBox = new HBox(refreshParameterButton, applyParameterButton);
        parameterButtonsHBox.setStyle("-fx-spacing: 5");
        parameterButtonsHBox.setAlignment(Pos.CENTER_RIGHT);

        VBox parentBox = new VBox(parametersPane, parameterButtonsHBox);
        parentBox.setSpacing(10);
        TitledPane parametersTP = new TitledPane("Parameters", parentBox);
        parametersTP.setCollapsible(false);
        parametersTP.setPadding(new Insets(0, 5, 5, 5));

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

        VBox phaserVBox = new VBox(titledPane, parametersTP, titledPane2);

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
        TableColumn mote0InstStatusCol = new TableColumn("Mote 0 Inst. Status");
        mote0InstStatusCol.setCellValueFactory(new PropertyValueFactory<Gateway, String>("mote0InstallationStatus"));
        TableColumn mote1InstStatusCol = new TableColumn("Mote 1 Inst. Status");
        mote1InstStatusCol.setCellValueFactory(new PropertyValueFactory<Gateway, String>("mote1InstallationStatus"));
        TableColumn mote2InstStatusCol = new TableColumn("Mote 2 Inst. Status");
        mote2InstStatusCol.setCellValueFactory(new PropertyValueFactory<Gateway, String>("mote2InstallationStatus"));
        TableColumn mote3InstStatusCol = new TableColumn("Mote 3 Inst. Status");
        mote3InstStatusCol.setCellValueFactory(new PropertyValueFactory<Gateway, String>("mote3InstallationStatus"));
        motesTable.getColumns().addAll(nameCol, ipCol, mote0InstStatusCol, mote1InstStatusCol, mote2InstStatusCol, mote3InstStatusCol);
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

        Tab listenerTab = new Tab();
        listenerTab.setClosable(false);
        listenerTab.setText("Listeners");

        Label rangeLabel = new Label("Port range    from:");
        TextField fromTextField = new TextField();
        fromTextField.textProperty().bindBidirectional(fromPort, new NumberStringConverter());
        fromTextField.setMaxWidth(70);
        Label toLabel = new Label(" to:");
        TextField toTextField = new TextField();
        toTextField.textProperty().bindBidirectional(toPort, new NumberStringConverter());
        toTextField.setMaxWidth(70);
        Pane lstnPane = new Pane();
        Button stopStartButton = new Button("Start");
        stopStartButton.setMinWidth(80);
        stopStartButton.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                if (inputCheck()) {
                    startAllListeners();
                }
            }
        });
        Label destLabel = new Label("Destination file(s):");
        TextField destTextField = new TextField();
        destTextField.textProperty().bindBidirectional(destListenerFilePath);
        Label suffixLabel = new Label(" Suffix:");
        TextField suffixTextField = new TextField();
        suffixTextField.textProperty().bindBidirectional(suffixFileName);
        Label numLabel = new Label("<Number>");

        HBox listenerConfig1HBox = new HBox(destLabel, destTextField, suffixLabel, suffixTextField, numLabel);
        listenerConfig1HBox.setStyle("-fx-spacing: 5");
        listenerConfig1HBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(destTextField, Priority.ALWAYS);

        HBox listenerConfig2HBox = new HBox(rangeLabel, fromTextField, toLabel, toTextField, lstnPane, stopStartButton);
        listenerConfig2HBox.setStyle("-fx-spacing: 5");
        listenerConfig2HBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(lstnPane, Priority.ALWAYS);

        VBox configVBox = new VBox(listenerConfig1HBox, listenerConfig2HBox);
        configVBox.setSpacing(10);

        TitledPane configListenerTP = new TitledPane("Configuration", configVBox);
        configListenerTP.setCollapsible(false);
        configListenerTP.setPadding(new Insets(5, 5, 5, 5));

        VBox lstnrsVBox = new VBox();
        lstnrsVBox.setSpacing(10);

        TitledPane listenersTP = new TitledPane("Listeners", lstnrsVBox);
        listenersTP.setCollapsible(false);
        listenersTP.setPadding(new Insets(0, 5, 5, 5));

        VBox listenerVBox = new VBox(configListenerTP, listenersTP);

        listenerTab.setContent(listenerVBox);
        tabPane.getTabs().add(listenerTab);

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
        }
        if (sourceCode == SourceCode.PHASER) {
            phaserSrcCodePath.set(path);
            phaserSrcCodeMainFilePath.set(file.getAbsolutePath());
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
            for (int i = 0; i <= 3; i++) {
                installOnMote(gateway, command, i);
            }
        }
    }

    private void installOnMote(Gateway gateway, String command, int moteNumber) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String scpResult = "";
                String sshResult = "";
                String moteCommand;
                moteCommand = String.format(command, moteNumber);
                scpResult = cmd.executeSCPCommand(gateway.getIpAddress(), gateway.getUsername(),
                        gateway.getPassword(), defaultMonitorSrcCodePath.get() + "/*", defaultCodePathOnGateway);
                if (scpResult.contains("")) {
                    int retry = motesInstallCommandNumberoFRetry;
                    do {
                        sshResult = cmd.executeSSHCommand(gateway.getIpAddress(), gateway.getUsername(),
                                gateway.getPassword(), moteCommand);
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
            switch (moteNumber) {
                case 0:
                    gateway.setMote0InstallationStatus("Please Wait...");
                    break;
                case 1:
                    gateway.setMote1InstallationStatus("Please Wait...");
                    break;
                case 2:
                    gateway.setMote2InstallationStatus("Please Wait...");
                    break;
                case 3:
                    gateway.setMote3InstallationStatus("Please Wait...");
                    break;
            }
            gateway.setMote0InstallationStatus("Please Wait...");
        });
        task.setOnSucceeded((e) -> {
            try {
                Boolean result = task.get();
                if (!result) {
                    switch (moteNumber) {
                        case 0:
                            gateway.setMote0InstallationStatus("Error!");
                            break;
                        case 1:
                            gateway.setMote1InstallationStatus("Error!");
                            break;
                        case 2:
                            gateway.setMote2InstallationStatus("Error!");
                            break;
                        case 3:
                            gateway.setMote3InstallationStatus("Error!");
                            break;
                    }
                } else {
                    switch (moteNumber) {
                        case 0:
                            gateway.setMote0InstallationStatus("Successful");
                            break;
                        case 1:
                            gateway.setMote1InstallationStatus("Successful");
                            break;
                        case 2:
                            gateway.setMote2InstallationStatus("Successful");
                            break;
                        case 3:
                            gateway.setMote3InstallationStatus("Successful");
                            break;
                    }
                }
            } catch (InterruptedException ex) {
                logger.error("Error Line: " + ex);
            } catch (ExecutionException ex) {
                logger.error("Error Line: " + ex);
            }
        });
        new Thread(task).start();
    }

    private void startAllListeners() {
        String filePath = destListenerFilePath.get();
        String[] parts = filePath.split("\\.");
        if (parts[1].equals("")) {
            parts[1] = "txt";
        }
        for (int i = fromPort.get(), j = 0; i <= toPort.get(); i++, j++) {
            startListener(i, parts[0] + suffixFileName.get() + String.valueOf(j) + "." + parts[1]);
        }
    }

    private void startListener(int portNum, String filePath) {

        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                RaspListener raspListener = new RaspListener();
                raspListener.start(portNum, filePath);
                return null;
            }
        };

        task.setOnRunning((e) -> {
            System.out.println("Running " + portNum);
        });
        task.setOnSucceeded((e) -> {
            System.out.println("Success " + portNum);
        });
        new Thread(task).start();
    }

    private void loadParametersFromFile() {
        BufferedReader bufferedReader;
        keySet.clear();
        try {
            bufferedReader = new BufferedReader(
                    new FileReader(phaserSrcCodeMainFilePath.get()));

            List<Parameter> loadedParams = new ArrayList<>();
            String line = bufferedReader.readLine();

            boolean found = false;
            String key = "";
            while (line != null) {
                if (line.toLowerCase().contains(startParameterSection)) {
                    ObservableList<Parameter> parameterList = FXCollections.observableArrayList();
                    found = true;
                    line = bufferedReader.readLine();
                    if (line.contains("description:")) {
                        String[] parts = line.split(":");
                        key = parts[1].trim();
                        keySet.add(key);
                        parametersMap.put(key, parameterList);
                    }
                } else if (line.toLowerCase().contains(endParameterSection)) {
                    found = false;
                    parametersMap.get(key).clear();
                    parametersMap.get(key).addAll(loadedParams);
                    loadedParams.clear();
                } else if (found) {
                    if (line.contains("=")) {
                        Parameter myParameter = new Parameter();
                        String paramString = line.trim();
                        if (paramString.charAt(paramString.length() - 1) == ',') {
                            paramString = paramString.substring(0, paramString.length() - 1);
                        }
                        String[] parts = paramString.split("=");
                        myParameter.setVariable(parts[0]);
                        myParameter.setValue(parts[1]);
                        loadedParams.add(myParameter);
                    }
                }

                line = bufferedReader.readLine();
            }

//            parameters.clear();
//            parameters.addAll(loadedParams);
            bufferedReader.close();

        } catch (Exception ex) {
            logger.error("Error Line: " + ex);
        }
    }

    private void saveParametersToFile() {
        String oldFileName = phaserSrcCodeMainFilePath.get();
        String tmpFileName = phaserSrcCodeMainFilePath.get() + "_tmp";

        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new FileReader(oldFileName));
            bw = new BufferedWriter(new FileWriter(tmpFileName));
            String line = br.readLine();
            String startParameterSectionLine = "";
            String descriptionLine = "";
            String endParameterSectionLine = "";
            String key = "";
            boolean found = false;
            while (line != null) {

                if (line.toLowerCase().contains(startParameterSection)) {
                    found = true;
                    startParameterSectionLine = line;
                    line = br.readLine();
                    descriptionLine = line;
                    String parts[] = descriptionLine.split(":");
                    key = parts[1].trim();
                } else if (line.toLowerCase().contains(endParameterSection)) {
                    found = false;
                    endParameterSectionLine = line;
                    line = startParameterSectionLine + "\n";
                    line = line.concat(descriptionLine + "\n");
                    for (Parameter param : parametersMap.get(key)) {
                        if (param.equals(parametersMap.get(key).get(parametersMap.get(key).size() - 1))) {
                            line = line.concat(param.getVariable() + "=" + param.getValue() + "\n");
                        } else {
                            line = line.concat(param.getVariable() + "=" + param.getValue() + ",\n");
                        }
                    }
                    line = line.concat(endParameterSectionLine);
                } else if (found) {
                    line = "";
                }

                if (!found) {
                    bw.write(line + "\n");
                }
                line = br.readLine();
            }
        } catch (Exception ex) {
            logger.error("Error Line: " + ex);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                logger.error("Error Line: " + ex);
            }
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException ex) {
                logger.error("Error Line: " + ex);
            }
        }

        File oldFile = new File(oldFileName);
        oldFile.delete();

        File newFile = new File(tmpFileName);
        newFile.renameTo(oldFile);
    }

    private void refreshParameterSection() {
        parametersPane.getChildren().clear();
        parametersPane.getColumnConstraints().clear();
        TabPane parametersTabPane = new TabPane();
        parametersTabPane.setStyle("-fx-background-color: gainsboro");
        for (String key : keySet) {
            Tab myTab = new Tab();
            myTab.setClosable(false);
            myTab.setText(key);
            GridPane parametersGridPane = new GridPane();
            parametersGridPane.setPadding(new Insets(10, 10, 10, 10));
            parametersGridPane.setHgap(10);
            parametersGridPane.setVgap(7);
//            parametersGridPane.getChildren().clear();
//            parametersGridPane.getColumnConstraints().clear();
            int columnIndex = 0;
            int rowIndex = 0;
            int itemIndex = 1;
            for (Parameter param : parametersMap.get(key)) {
                Label label = new Label();
                label.textProperty().bindBidirectional(param.variableProperty());
                TextField textField = new TextField();
                textField.textProperty().bindBidirectional(param.valueProperty());
                parametersGridPane.add(label, columnIndex, rowIndex);
                columnIndex++;
                parametersGridPane.add(textField, columnIndex, rowIndex);
                columnIndex++;
                if (itemIndex % 2 == 0) {
                    rowIndex++;
                    columnIndex = 0;
                }
                itemIndex++;
            }
            ColumnConstraints column1 = new ColumnConstraints();
            column1.setPercentWidth(20);
            parametersGridPane.getColumnConstraints().add(column1);

            ColumnConstraints column2 = new ColumnConstraints();
            column2.setPercentWidth(30);
            parametersGridPane.getColumnConstraints().add(column2);

            ColumnConstraints column3 = new ColumnConstraints();
            column3.setPercentWidth(20);
            parametersGridPane.getColumnConstraints().add(column3);

            ColumnConstraints column4 = new ColumnConstraints();
            column4.setPercentWidth(30);
            parametersGridPane.getColumnConstraints().add(column4);

            myTab.setContent(parametersGridPane);
            parametersTabPane.getTabs().add(myTab);
        }

        parametersTabPane.setCenterShape(true);
        parametersPane.add(parametersTabPane, 0, 0);
        ColumnConstraints parentCol = new ColumnConstraints();
        parentCol.setPercentWidth(100);
        parametersPane.getColumnConstraints().add(parentCol);
    }

    private boolean inputCheck() {
        if (fromPort.get() <= toPort.get()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Loads data from the specified file.
     *
     * @param file
     */
    public void loadDataFromFile(File file) {
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

        } catch (Exception ex) { // catches ANY exception
            logger.error("Error Line: " + ex);
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
    public void saveDataToFile(File file) {
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

        } catch (Exception ex) { // catches ANY exception
            logger.error("Error Line: " + ex);
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
        prefs.put("phaserSrcCodeMainFilePath", phaserSrcCodeMainFilePath.get());
        prefs.put("defaultMonitorSrcCodePath", defaultMonitorSrcCodePath.get());
        File file = new File(xmlFilePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                logger.error("Error Line: " + ex);
            }
        }
        saveDataToFile(file);
    }

    private void restoreCurrentSetting() {
        Preferences prefs = Preferences.userNodeForPackage(Sepand.class);
        File file = new File(xmlFilePath);
        if (file.exists()) {
            loadDataFromFile(file);
        }
        phaserSrcCodePath.set(prefs.get("phaserSrcCodePath", ""));
        phaserSrcCodeMainFilePath.set(prefs.get("phaserSrcCodeMainFilePath", ""));
        defaultMonitorSrcCodePath.set(prefs.get("defaultMonitorSrcCodePath", ""));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
