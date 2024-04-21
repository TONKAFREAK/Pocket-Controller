package com.tonki;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.InputStream;

import org.controlsfx.control.StatusBar;

public class BluetoothControllerApp extends Application {
    
    @Override
    public void start(@SuppressWarnings("exports") Stage primaryStage) {
        
        try {
            InputStream iconStream = getClass().getResourceAsStream("/images/icon.png");
            if (iconStream == null) {
                throw new IllegalArgumentException("Icon file not found");
            }
            Image appIcon = new Image(iconStream);
            primaryStage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.err.println("Error loading icon: " + e.getMessage());
        }

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
        BluetoothConnection bluetoothConnection = new BluetoothConnection();
        BluetoothDiscovery bluetoothDiscovery = new BluetoothDiscovery(deviceList, statusBar, bluetoothConnection);
        

        // Connect Button
        Button connectButton = new Button();

        updateConnectButton(connectButton, bluetoothConnection, deviceList, statusBar, bluetoothDiscovery);
       
        

        HBox statusLayout = new HBox(0,statusBar, connectButton);
        statusLayout.setStyle("-fx-border-color: #3F3F46; -fx-border-width: 2px 0 0 0;");
        
        root.setBottom(statusLayout);

        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
        
        bluetoothDiscovery.startPeriodicDeviceSearch();
        
        primaryStage.setOnCloseRequest(event -> bluetoothDiscovery.shutdown());


    }

    private void updateConnectButton(Button connectButton, BluetoothConnection bluetoothConnection, ListView<String> deviceList, StatusBar statusBar, BluetoothDiscovery bluetoothDiscovery) {
        if (bluetoothConnection.isConnected()) {
            connectButton.setText("Disconnect");
            connectButton.setOnAction(e -> {
                try {
                    bluetoothConnection.disconnect(statusBar);
                    updateConnectButton(connectButton, bluetoothConnection, deviceList, statusBar, bluetoothDiscovery);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        } else {
            connectButton.setText("Connect");
            connectButton.setOnAction(e -> {
                String selectedDevice = deviceList.getSelectionModel().getSelectedItem();
                if (selectedDevice == null) {
                    statusBar.setText("No device selected");
                    return;
                }
                String bluetoothAddress = bluetoothDiscovery.extractDeviceAddress(selectedDevice);
                try {
                    bluetoothConnection.connect(bluetoothAddress, statusBar);
                    updateConnectButton(connectButton, bluetoothConnection, deviceList, statusBar, bluetoothDiscovery);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
        System.setProperty("bluecove.debug", "true");
    }
}
