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

/**
 * Interfaces with the BluetoothAdapter class to create a SPP byte stream.
 * Connects, disconnects, sends and receives strings.
 * Uses default UUID for the HC-06 receiver.
 *
 * @author  Chamath Ellawala
 * @since   29/05/2017
 *
 */
class BluetoothHandler {

    // TODO authentication
    // TODO fixed format
    // TODO input stream

    private static final UUID HC06_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final BluetoothAdapter BLUETOOTH_ADAPTER = BluetoothAdapter.getDefaultAdapter();

    private BluetoothSocket _socket;
    private BufferedInputStream _socketIn;
    private PrintWriter _socketOut;

    /**
     * Teleports user to Bluetooth settings page if Bluetooth is not enabled.
     *
     * @param activity Calling activity
     */
    static void enableBluetooth(Activity activity) {
        if (!isBluetoothEnabled()) {
            Intent bIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(bIntent, 0);
        }
    }

    /**
     * Checks if Bluetooth is enabled.
     *
     * @return Bluetooth is enabled
     */
    static boolean isBluetoothEnabled() {
        return BLUETOOTH_ADAPTER.isEnabled();
    }

    /**
     * Gets a list of paired devices from BluetoothAdapter.
     *
     * @return Array of BluetoothDevice
     */
    static BluetoothDevice[] getPairedDevices() {
        Set<BluetoothDevice> devices = BLUETOOTH_ADAPTER.getBondedDevices();
        return devices.toArray(new BluetoothDevice[devices.size()]);
    }

    /**
     * Generates a new instance of BluetoothHandler with an active connection.
     *
     * @param device BluetoothDevice object corresponding to the desired paired device
     * @return Instance of self with active connection
     * @throws IOException On connection failure
     */
    static BluetoothHandler newConnection(BluetoothDevice device) throws IOException {
        BluetoothHandler bh = new BluetoothHandler();
        bh.connect(device);
        return bh;
    }

    /**
     * Connects to the specified device.
     *
     * @param device Instance of BluetoothDevice corresponding to the desired paired device
     */
    private void connect(BluetoothDevice device) throws IOException {
        _socket = device.createRfcommSocketToServiceRecord(HC06_UUID);
        _socket.connect();

        _socketIn = new BufferedInputStream(_socket.getInputStream());
        _socketOut = new PrintWriter(new BufferedOutputStream(_socket.getOutputStream()));
    }

    /**
     * Checks if this instance of BluetoothHandler already has an active connection.
     *
     * @return Connected to a device
     */
    boolean isConnected() {
        return _socket.isConnected();
    }

    /**
     * Disconnects from the active device. This instance of BluetoothHandler should not be reused.
     *
     * @throws IOException On failure to disconnect
     */
    void disconnect() throws IOException {
        if (isConnected()) {
            _socketIn.close();
            _socketOut.close();
            _socket.close();
        }
    }

    /**
     * Send a string to the active device.
     *
     * @param text String to be sent to the device
     * @throws IOException On failure to send
     */
    void write(String text) throws IOException {
        if (isConnected()) {
            _socketOut.write(text);
            _socketOut.flush();
        }
    }

}
