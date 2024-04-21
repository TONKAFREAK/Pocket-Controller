package com.tonki;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javax.bluetooth.*;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.Connector;

import java.io.IOException;
import javafx.util.Duration;
import java.util.*;
import java.util.UUID;
import java.util.concurrent.*;

import javafx.scene.control.ListView;
import org.controlsfx.control.StatusBar;

public class BluetoothDiscovery  {
    private ListView<String> deviceList;
    private StatusBar statusBar;
    private Set<String> discoveredDevices = new HashSet<>();
    private ScheduledExecutorService scheduler;

    private boolean connected = false;

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public BluetoothDiscovery(ListView<String> deviceList, StatusBar statusBar) {
        this.deviceList = deviceList;
        this.statusBar = statusBar;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void startPeriodicDeviceSearch() {
        final Runnable searcher = () -> {
            try {
                startDeviceSearch();
            } catch (BluetoothStateException e) {
                e.printStackTrace();
            }
        };
        scheduler.scheduleAtFixedRate(searcher, 0, 30, TimeUnit.SECONDS); 
    }

    public void startDeviceSearch() throws BluetoothStateException {

        discoveredDevices.clear(); 

        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            DiscoveryAgent agent = localDevice.getDiscoveryAgent();
            agent.startInquiry(DiscoveryAgent.GIAC, listener);
            if (!statusBar.getText().equals("Ready to connect") && connected == false){
                Platform.runLater(() -> statusBar.setText("Searching for devices..."));
            }
        } catch (BluetoothStateException ex) {
            if (!statusBar.getText().equals("Ready to connect") && connected == false){
                Platform.runLater(() -> statusBar.setText("Failed to start Bluetooth search. " + ex.getMessage()));
            }
            ex.printStackTrace();
        }
    }

    private final DiscoveryListener listener = new DiscoveryListener() {
        @Override
        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            try {
                String deviceName = btDevice.getFriendlyName(false);
                String deviceInfo = deviceName != null ? deviceName + " ("+ btDevice.getBluetoothAddress() + ")" : "Unknown device (" + btDevice.getBluetoothAddress() + ")";
                discoveredDevices.add(deviceInfo);
            } catch (IOException e) {
                System.out.println("Error retrieving device name");
                discoveredDevices.add("Unknown device (" + btDevice.getBluetoothAddress() + ")");
            }
        }

        @Override
        public void inquiryCompleted(int discType) {
            Platform.runLater(() -> {
                updateDeviceListView();
                
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(event -> statusBar.setText("Ready to connect"));
                pause.play();  
            });
        }

        private void updateDeviceListView() {
            Set<String> currentItems = new HashSet<>(deviceList.getItems());
            
            discoveredDevices.stream().filter(device -> !currentItems.contains(device))
                             .forEach(device -> deviceList.getItems().add(device));
            
            deviceList.getItems().removeIf(device -> !discoveredDevices.contains(device));
        }

        @Override
        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            
        }

        @Override
        public void serviceSearchCompleted(int transID, int respCode) {
            
        }
    };

    public void connectToDevice(String deviceInfo) {
        if (connected) {
            System.out.println("Already connected. Disconnect before trying to connect to a new device.");
            return;
        }
    
        try {
            String deviceAddress = extractDeviceAddress(deviceInfo);
            
    
            UUID sppUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            UUID[] uuidSet = new UUID[]{sppUUID};
    
            int[] attrSet = null; // Default attributes or specify IDs of attributes you want to retrieve
    
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            localDevice.getDiscoveryAgent().searchServices(attrSet, uuidSet, device, new DiscoveryListener() {
                public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                    if (servRecord != null && servRecord.length > 0) {
                        String connectionURL = servRecord[0].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                        connectService(connectionURL);
                    }
                }
    
                public void serviceSearchCompleted(int transID, int respCode) {
                    if (respCode == SERVICE_SEARCH_COMPLETED) {
                        System.out.println("Search completed.");
                    } else {
                        System.out.println("Service search failed.");
                        Platform.runLater(() -> statusBar.setText("Service search failed."));
                    }
                }
    
                public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {}
                public void inquiryCompleted(int discType) {}
            });
    
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> statusBar.setText("Connection error: " + e.getMessage()));
        }
    }
    
    private void connectService(String connectionURL) {
        try {
            StreamConnection connection = (StreamConnection) Connector.open(connectionURL);
            System.out.println("Connected successfully to " + connectionURL);
    
            connected = true;
            Platform.runLater(() -> statusBar.setText("Connected successfully."));
    
            
        } catch (IOException e) {
            System.out.println("Connection failed: " + e.getMessage());
            Platform.runLater(() -> statusBar.setText("Connection failed: " + e.getMessage()));
        }
    }
    
      
    
    private String extractDeviceAddress(String deviceInfo) {
       
        int startIndex = deviceInfo.indexOf("(") + 1;
        int endIndex = deviceInfo.indexOf(")");
        return deviceInfo.substring(startIndex, endIndex);
    }    

    public void shutdown() {
        scheduler.shutdownNow();
        try {
            scheduler.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
