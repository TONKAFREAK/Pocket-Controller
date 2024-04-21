package com.tonki;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;

public class BluetoothControllerApp extends Application {
    
    @Override
    public void start(@SuppressWarnings("exports") Stage primaryStage) {
        primaryStage.setTitle("Pocket Controller");
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.setOnCloseRequest(e -> System.exit(0));
    
        // Device List
        ListView<String> deviceList = new ListView<>();
        deviceList.setPlaceholder(new Label("No devices found"));  

        // Layout setup
        VBox centerLayout = new VBox(10, deviceList);
        BorderPane root = new BorderPane();
        root.setCenter(centerLayout);

        // Status Bar
        StatusBar statusBar = new StatusBar();
        statusBar.setMinWidth(520);

        BluetoothDiscovery bluetoothDiscovery = new BluetoothDiscovery(deviceList, statusBar);

        // Connect Button
        Button connectButton = new Button("Connect");
        connectButton.setOnAction(e -> {
            String selectedDevice = deviceList.getSelectionModel().getSelectedItem();
            if (selectedDevice != null) {
                bluetoothDiscovery.setConnected(true);
                bluetoothDiscovery.connectToDevice(selectedDevice);  
                statusBar.setText("Attempting to connect to " + selectedDevice);
            } else {
                bluetoothDiscovery.setConnected(false);
                statusBar.setText("No device selected. Please select a device from the list.");
            }
        });

        HBox statusLayout = new HBox(0,statusBar, connectButton);
        statusLayout.setStyle("-fx-border-color: #3F3F46; -fx-border-width: 2px 0 0 0;");
        
        root.setBottom(statusLayout);

        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        // Bluetooth discovery
        if (bluetoothDiscovery.isConnected() == false) {
            bluetoothDiscovery.startPeriodicDeviceSearch();
        }
        primaryStage.setOnCloseRequest(event -> bluetoothDiscovery.shutdown());


    }


    public static void main(String[] args) {
        launch(args);
        //System.setProperty("bluecove.debug", "true");
    }
}
