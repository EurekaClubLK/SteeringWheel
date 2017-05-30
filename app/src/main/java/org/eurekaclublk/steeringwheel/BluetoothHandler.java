package org.eurekaclublk.steeringwheel;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

class BluetoothHandler {

    private static final UUID HC06_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final BluetoothAdapter BLUETOOTH_ADAPTER = BluetoothAdapter.getDefaultAdapter();

    private BluetoothSocket _socket;
    private BufferedInputStream _socketIn;
    private PrintWriter _socketOut;

    static void enableBluetooth(Activity context) {
        if (!isBluetoothEnabled()) {
            Intent bIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(bIntent, 0);
        }
    }

    static boolean isBluetoothEnabled() {
        return BLUETOOTH_ADAPTER.isEnabled();
    }

    static BluetoothDevice[] getPairedDevices() {
        Set<BluetoothDevice> devices = BLUETOOTH_ADAPTER.getBondedDevices();
        return devices.toArray(new BluetoothDevice[devices.size()]);
    }

    static BluetoothHandler newConnection(BluetoothDevice device) throws IOException {
        BluetoothHandler bh = new BluetoothHandler();
        bh.connect(device);
        return bh;
    }

    void connect(BluetoothDevice device) throws IOException {
        _socket = device.createRfcommSocketToServiceRecord(HC06_UUID);
        _socket.connect();

        _socketIn = new BufferedInputStream(_socket.getInputStream());
        _socketOut = new PrintWriter(new BufferedOutputStream(_socket.getOutputStream()));
    }

    boolean isConnected() {
        return _socket.isConnected();
    }

    void disconnect() throws IOException {
        if (isConnected()) {
            _socketIn.close();
            _socketOut.close();
            _socket.close();
        }
    }

    void write(String text) throws IOException {
        if (isConnected()) {
            _socketOut.write(text);
            _socketOut.flush();
        }
    }

}
