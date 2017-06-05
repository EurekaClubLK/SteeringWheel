package org.eurekaclublk.steeringwheel;

import android.app.Activity;
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

import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements RotationSensorHandler.RotationListener {

    private BluetoothHandler _bluetoothHandler;
    private RotationSensorHandler _rotationSensorHandler;

    private Switch tglBluetooth;
    private Button btnTest;
    private TextView lblTilt;

    /**
     * When the activity is first created (opened app from scratch).
     * Sets functionality of UI elements.
     *
     * @param savedInstanceState Any state to be restored for the appearance of continuity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Activity self = this;

        lblTilt = (TextView)findViewById(R.id.activity_main_lblTilt);
        lblTilt.setText("?");

        // toggle bluetooth
        tglBluetooth = (Switch)findViewById(R.id.activity_main_tglBluetooth);
        tglBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // redirect user to Bluetooth settings if functionality is off
                    if (!BluetoothHandler.isBluetoothEnabled())
                        BluetoothHandler.enableBluetooth(self);

                    // show device selection dialog
                    BluetoothDevice[] devices = BluetoothHandler.getPairedDevices();
                    showBluetoothSelectionDialog(devices);
                } else {
                    // disconnect from device before closing
                    if (_bluetoothHandler != null)
                        if (_bluetoothHandler.isConnected())
                            disconnectFromDevice();
                    btnTest.setEnabled(false);
                }
            }
        });

        // temporary button to send a string
        btnTest = (Button)findViewById(R.id.activity_main_btnSend);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (_bluetoothHandler.isConnected()) {
                        final String text = "ABC123";
                        _bluetoothHandler.write(text);
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

    /**
     * When the Activity is first displayed (opened app from scratch or switch back from another app).
     * Start listening for tilt readings.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // start listening for tilt
        _rotationSensorHandler = new RotationSensorHandler(this, this);
        _rotationSensorHandler.startListening();
    }

    /**
     * When the Activity is removed from view (closed app or switched to another app).
     * Forces disconnect from device. Stops listening for tilt readings.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // stop listening for tilt
        if (_rotationSensorHandler != null)
            _rotationSensorHandler.stopListening();

        // disconnect from device before closing
        if (_bluetoothHandler != null)
            if (_bluetoothHandler.isConnected())
                disconnectFromDevice();

        // UI
        tglBluetooth.setChecked(false);
        btnTest.setEnabled(false);
    }

    /**
     * Called when a new tilt reading is obtained.
     * Displays the value in a TextView.
     *
     * @param tilt Current tilt of device, normalised between -1 and +1
     */
    @Override
    public void onTiltChanged(float tilt) {
        lblTilt.setText(Float.toString(tilt));
    }

    /**
     * Displays an AlertDialog with a list of paired device names.
     * Connects to the selected device.
     *
     * @param devices Array of BluetoothDevice objects for paired devices
     */
    private void showBluetoothSelectionDialog(final BluetoothDevice[] devices) {
        // generate an array of device names to be displayed
        String[] deviceNames = new String[devices.length];
        for (int i = 0; i < devices.length; i++)
            deviceNames[i] = devices[i].getName();

        // display the list
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(getString(R.string.activity_main_dialog_title));
        alertBuilder.setItems(deviceNames, new DialogInterface.OnClickListener() {
            // on user selection
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // close the dialog
                dialog.dismiss();

                // try to connect
                connectToDevice(devices[which]);
            }
        });
        alertBuilder.show();
    }

    /**
     * Connects to the specified device. Displays and logs error on failure. Displays Snackbar on success.
     *
     * @param device BluetoothDevice object corresponding to the desired paired device
     */
    private void connectToDevice(BluetoothDevice device) {
        try {
            _bluetoothHandler = BluetoothHandler.newConnection(device);
            showSnackbar(R.string.activity_main_snackbar_connected);
            btnTest.setEnabled(true);
        } catch (IOException ex) {
            showErrorDialog(R.string.activity_main_error_cannotconnect);
            Log.e("connectToDevice", ex.getMessage(), ex);
        }
    }

    /**
     * Disconnects from connected device. Displays and logs error on failure.
     */
    private void disconnectFromDevice() {
        try {
            _bluetoothHandler.disconnect();
        } catch (IOException ex) {
            showErrorDialog(R.string.activity_main_error_cannotdisconnect);
            Log.e("disconnectFromDevice", ex.getMessage(), ex);
        }
    }

    /**
     * Shows a Snackbar for the desired string.
     *
     * @param message Identifier of the desired string
     * @param args Any arguments to be injected into the message string
     */
    private void showSnackbar(int message, Object... args) {
        String messageString = getString(message, args);
        View coordinatorLayout = findViewById(R.id.activity_main_coordinatorlayout);
        Snackbar.make(coordinatorLayout, messageString, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Shows an AlertDialog for the specific error string.
     *
     * @param errorMessage Identifier of the desired error string
     */
    private void showErrorDialog(int errorMessage) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.activity_main_error_title);
        alertBuilder.setMessage(errorMessage);
        alertBuilder.show();
    }

}


