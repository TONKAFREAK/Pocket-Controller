package com.tonki;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.controlsfx.control.StatusBar;

public class BluetoothConnection {
    
    private StreamConnection connection;
    private OutputStream outStream;
    private InputStream inStream;
    private boolean connected = false;

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void connect(String address, @SuppressWarnings("exports") StatusBar status) {
        try {

            String connectionString = "btspp://" + address + ":1;authenticate=false;encrypt=false;master=false";
            StreamConnection connection = (StreamConnection) Connector.open(connectionString);
            outStream = connection.openOutputStream();
            inStream = connection.openInputStream();
            setConnected(true);
            System.out.println("Connected to " + address);
            status.setText("Connected");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect(@SuppressWarnings("exports") StatusBar status) throws IOException {
        if (outStream != null) {
            outStream.close();
        }
        if (inStream != null) {
            inStream.close();
        }
        if (connection != null) {
            connection.close();
        }
        setConnected(false);
        System.out.println("Disconnected");
        status.setText("Ready to connect");
    }

    public void sendMessage(String message) throws IOException {
        outStream.write(message.getBytes());
    }

    public String receiveMessage() throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead = inStream.read(buffer);
        return new String(buffer, 0, bytesRead);
    }
}
