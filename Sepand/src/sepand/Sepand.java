/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sepand;

import java.io.File;
import java.util.Optional;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import sepand.dialog.PasswordDialog;
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
    final private String phaserInstallCommand = "ls"; // make phaser upload

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

        Label label1 = new Label("Default Monitor:");
        motesGrid.add(label1, 0, 0);

        TextField dmTextField = new TextField();
        dmTextField.textProperty().bindBidirectional(defaultMonitorSrcCodePath);
        motesGrid.add(dmTextField, 1, 0);

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
        motesGrid.add(monitorOpenDlgButton, 2, 0);

        final TextField testTF = new TextField();
        motesGrid.add(testTF, 0, 1);

        Button testBtn = new Button("Run");
        testBtn.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
//                System.out.println(cmd.executeCommand("/",testTF.getText(),true,"D@nijo0n2771"));
                PasswordDialog pd = new PasswordDialog();
                Optional<String> result = pd.showAndWait();
                result.ifPresent(password -> System.out.println(password));
            }
        });
        motesGrid.add(testBtn, 1, 1);

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
        String path = file.getAbsolutePath();
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
        result.ifPresent(password ->  System.out.println(cmd.executeCommand(phaserSrcCodePath.get(), phaserInstallCommand, true, password))); 
        //        System.out.println(cmd.executeCommand(command));
    }
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
