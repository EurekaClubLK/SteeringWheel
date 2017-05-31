package org.eurekaclublk.steeringwheel;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private BluetoothHandler bluetoothHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnTest = (Button)findViewById(R.id.activity_main_btnSend);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (bluetoothHandler.isConnected()) {
                        final String text = "ABC123";
                        bluetoothHandler.write(text);
                        showSnackbar(R.string.activity_main_snackbar_sent, text);
                    } else {
                        showSnackbar(R.string.activity_main_snackbar_notconnected);
                    }
                } catch (IOException ex) {
                    showErrorDialog(R.string.activity_main_error_cannotwrite);
                    Log.e("btnTest", ex.getMessage(), ex);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        BluetoothHandler.enableBluetooth(this);

        BluetoothDevice[] devices = BluetoothHandler.getPairedDevices();
        showBluetoothSelectionDialog(devices);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (bluetoothHandler != null)
            if (bluetoothHandler.isConnected())
                disconnectFromDevice();
    }

    private void showBluetoothSelectionDialog(final BluetoothDevice[] devices) {
        String[] deviceNames = new String[devices.length];
        for (int i = 0; i < devices.length; i++)
            deviceNames[i] = devices[i].getName();

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(getString(R.string.activity_main_dialog_title));
        alertBuilder.setItems(deviceNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                connectToDevice(devices[which]);
            }
        });
        alertBuilder.show();
    }

    private void connectToDevice(BluetoothDevice device) {
        try {
            bluetoothHandler = BluetoothHandler.newConnection(device);
            showSnackbar(R.string.activity_main_snackbar_connected);
        } catch (IOException ex) {
            showErrorDialog(R.string.activity_main_error_cannotconnect);
            Log.e("connectToDevice", ex.getMessage(), ex);
        }
    }

    private void disconnectFromDevice() {
        try {
            bluetoothHandler.disconnect();
        } catch (IOException ex) {
            showErrorDialog(R.string.activity_main_error_cannotdisconnect);
            Log.e("disconnectFromDevice", ex.getMessage(), ex);
        }
    }

    private void showSnackbar(int message, Object... args) {
        String messageString = getString(message, args);
        View coordinatorLayout = findViewById(R.id.activity_main_coordinatorlayout);
        Snackbar.make(coordinatorLayout, messageString, Snackbar.LENGTH_SHORT).show();
    }

    private void showErrorDialog(int errorMessage) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.activity_main_error_title);
        alertBuilder.setMessage(errorMessage);
        alertBuilder.show();
    }

}
