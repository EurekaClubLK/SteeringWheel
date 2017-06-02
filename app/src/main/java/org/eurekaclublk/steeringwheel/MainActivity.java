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

import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;

public class MainActivity extends AppCompatActivity {
    //Class wide Variables
    private BluetoothHandler bluetoothHandler;
    float StartTilt = 0;
    float Tilt = 1;
    //Class wide Variables
    /**
     * When the activity is first created (opened app from scratch).
     *
     * @param savedInstanceState Any state to be restored for the appearance of continuity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Register Sensor Manager
        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        //Register Sensor Manager
        //Init the driving
        StartTilt=0;
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),SensorManager.SENSOR_DELAY_GAME);
        //Init the driving

        // temporary button to send a string
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

    /**
     * When the Activity is displayed (opened app from scratch or switched back from another app).
     * Redirects to Bluetooth settings page if Bluetooth is not enabled.
     * Displays dialog to select the paired device.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // redirect user to Bluetooth settings if functionality is off
        if (!BluetoothHandler.isBluetoothEnabled())
            BluetoothHandler.enableBluetooth(this);

        // show device selection dialog
        BluetoothDevice[] devices = BluetoothHandler.getPairedDevices();
        showBluetoothSelectionDialog(devices);
    }
    @Override
    protected void onResume(){
        super.onResume();
        //Init the driving
        StartTilt=0;
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),SensorManager.SENSOR_DELAY_GAME);
        //Init the driving
    }
    /**
     * When the Activity is removed from view (closed app or switched to another app).
     * Forces disconnect from device.
     */
    @Override
    protected void onPause() {
        //Kill the Drviing
        mSensorManager.unregisterListener(mSensorListener);
        //Kill the Drviing
        super.onPause();
        // disconnect from device before closing
        if (bluetoothHandler != null)
            if (bluetoothHandler.isConnected())
                disconnectFromDevice();
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
            bluetoothHandler = BluetoothHandler.newConnection(device);
            showSnackbar(R.string.activity_main_snackbar_connected);
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
            bluetoothHandler.disconnect();
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


    //Get Steering Tilt
    private SensorManager mSensorManager;
    private final SensorEventListener mSensorListener = new SensorEventListener() {
        //Place Holder Arrays
        float mRotationMatrixFromVector[] = new float[16];
        float mRotationMatrix[] = new float[16];
        float orientationVals[] = new float[3];

        //Place Holder Arrays
        public void onSensorChanged(SensorEvent se) {
            SensorManager.getRotationMatrixFromVector(mRotationMatrixFromVector, se.values);
            SensorManager.remapCoordinateSystem(mRotationMatrixFromVector,
                    SensorManager.AXIS_X, SensorManager.AXIS_Z,
                    mRotationMatrix);
            SensorManager.getOrientation(mRotationMatrix, orientationVals);
            if (StartTilt == 0){
                StartTilt = (int)Math.toDegrees(orientationVals[2]);
            }

            Tilt = ((float)Math.toDegrees(orientationVals[2]) - StartTilt)/90; //Convert to Value between -1 & 1
            /*Figure out some sort of over tilt protection
            if (Tilt > 1){
                Tilt -=1;
            }else if (Tilt < -1){
                Tilt +=1;
            }*/
            //Send Tilt + Speed Value here
            Log.d("Tilt", Float.toString(Tilt));
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //Do nothing
        }
    };
    //Get Steering Tilt


}


