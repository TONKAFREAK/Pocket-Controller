package com.tonki;

import javafx.application.Platform;
import javax.bluetooth.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import javafx.scene.control.ListView;
import org.controlsfx.control.StatusBar;

public class BluetoothDiscovery  {

    private ListView<String> deviceList;
    private StatusBar statusBar;

    private Set<String> discoveredDevices = new HashSet<>();

    private ScheduledExecutorService scheduler;

    private BluetoothConnection bluetoothConnection;

    @SuppressWarnings("exports")
    public BluetoothDiscovery(ListView<String> deviceList, StatusBar statusBar, BluetoothConnection bluetoothConnection) {
        this.deviceList = deviceList;
        this.statusBar = statusBar;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.bluetoothConnection = bluetoothConnection;
    }

    public void startPeriodicDeviceSearch() {
        final Runnable searcher = () -> {
            if (!bluetoothConnection.isConnected()) { 
            try {
                startDeviceSearch();
            } catch (BluetoothStateException e) {
                e.printStackTrace();
            }
        }
        };
        scheduler.scheduleAtFixedRate(searcher, 0, 1, TimeUnit.MINUTES); 
    }

    public void startDeviceSearch() throws BluetoothStateException {

        if (bluetoothConnection.isConnected() == true) {
            cancelPeriodicSearch();
        }

        discoveredDevices.clear();
        System.out.println("Searching for devices...");

        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            DiscoveryAgent agent = localDevice.getDiscoveryAgent();
            agent.startInquiry(DiscoveryAgent.GIAC, listener);
            if (deviceList.getItems().isEmpty()){
                Platform.runLater(() -> statusBar.setText("Searching for devices..."));
            }
        } catch (BluetoothStateException ex) {
            if (!statusBar.getText().equals("Ready to connect") ){
                Platform.runLater(() -> statusBar.setText("Failed to start Bluetooth search. " + ex.getMessage()));
            }
            ex.printStackTrace();
        }
    }

    private final DiscoveryListener listener = new DiscoveryListener() {
        @Override
        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            try {
                String deviceName = btDevice.getFriendlyName(true);
                String deviceInfo = null;
                if (deviceName == null) {
                    deviceInfo = "Unknown device (" + btDevice.getBluetoothAddress() + ")";
                }
                if (deviceName != null){
                    deviceInfo = deviceName + " ("+ btDevice.getBluetoothAddress() + ")";
                
                }
                discoveredDevices.add(deviceInfo);
                System.out.println("device found: " + btDevice.getFriendlyName(true));
            } catch (IOException e) {
                System.out.println("Error retrieving device name");
                discoveredDevices.add("Unknown device (" + btDevice.getBluetoothAddress() + ")");
            }
        }

        @Override
        public void inquiryCompleted(int discType) {
            Platform.runLater(() -> {
                updateDeviceListView();
            });
        }

        private void updateDeviceListView() {
            Set<String> currentItems = new HashSet<>(deviceList.getItems());
            
            discoveredDevices.stream().filter(device -> !currentItems.contains(device))
                             .forEach(device -> deviceList.getItems().add(device));
            
            deviceList.getItems().removeIf(device -> !discoveredDevices.contains(device));
            statusBar.setText("Ready to connect");
        }

        @Override
        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            
        }

        @Override
        public void serviceSearchCompleted(int transID, int respCode) {
            
        }
    }; 
    
    String extractDeviceAddress(String deviceInfo) {
       
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

    public void cancelPeriodicSearch() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    System.out.println("Scheduler did not terminate.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
