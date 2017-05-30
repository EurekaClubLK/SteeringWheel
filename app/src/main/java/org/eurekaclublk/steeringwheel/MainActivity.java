package org.eurekaclublk.steeringwheel;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private BluetoothHandler bluetoothHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothHandler.enableBluetooth(this);

        final BluetoothDevice[] devices = BluetoothHandler.getPairedDevices();
        for (BluetoothDevice device : devices)
            Log.e("device", device.getName());

        final TextView lblStatus = (TextView)findViewById(R.id.activity_main_lblStatus);

        Button btnConnect = (Button)findViewById(R.id.activity_main_btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    lblStatus.setText("Connecting");
                    bluetoothHandler = BluetoothHandler.newConnection(devices[0]);
                    lblStatus.setText("Connected");
                } catch (IOException ex) {
                    Log.e("error", ex.getMessage(), ex);
                    lblStatus.setText(ex.getMessage());
                }
            }
        });

        Button btnTest = (Button)findViewById(R.id.activity_main_btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (bluetoothHandler.isConnected()) {
                        final String text = "ABC123";
                        bluetoothHandler.write(text);
                        lblStatus.setText(text);
                    }
                    else
                        lblStatus.setText("Not connected");
                } catch (IOException ex) {
                    Log.e("error", ex.getMessage(), ex);
                    lblStatus.setText(ex.getMessage());
                }
            }
        });

    }

}
