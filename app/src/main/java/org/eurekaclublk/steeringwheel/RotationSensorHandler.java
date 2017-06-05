package org.eurekaclublk.steeringwheel;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * bleh
 *
 * @author  bleh
 * @since   29/05/2017
 *
 */
class RotationSensorHandler {

    // TODO detect initial tilt if not horizontal
    // TODO overtilt protection
    // TODO handle accuracy changes

    private SensorManager _rotationSensorManager;
    private RotationListener _currentListener;

    /**
     * A listener triggered by sensor readings.
     */
    interface RotationListener {
        /**
         * Called when a new tilt reading is available.
         *
         * @param tilt Current tilt of device, normalised between -1 and +1
         */
        void onTiltChanged(float tilt);
    }

    /**
     * Creates a new instance of RotationSensorHandler.
     *
     * @param activity Calling activity
     * @param listener An instance of RotationListener that will be updated with sensor readings
     */
    RotationSensorHandler(Activity activity, RotationListener listener) {
        // get a reference to the sensor service
        _rotationSensorManager = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);

        // create instance of listener
        _currentListener = listener;
    }

    /**
     * Start reading sensor values. The attached listener will be triggered.
     */
    void startListening() {
        Sensor rotationSensor = _rotationSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        _rotationSensorManager.registerListener(_rotationSensorListener, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Stop reading sensor values.
     */
    void stopListening() {
        _rotationSensorManager.unregisterListener(_rotationSensorListener);
    }

    /**
     * Internal listener object that gets sensor values and triggers the external listener.
     */
    private final SensorEventListener _rotationSensorListener = new SensorEventListener() {
        // predefine variables for efficiency
        float[] _rotationMatrix = new float[16];
        float[] _rotationMatrixRemapped = new float[16];
        float[] _orientation = new float[3];
        double _tilt;
        float _tiltNormalised;

        @Override
        public void onSensorChanged(SensorEvent event) {
            // converts the rotation vector into a rotation matrix
            SensorManager.getRotationMatrixFromVector(_rotationMatrix, event.values);

            // map the rotation values to X and Z coordinates
            SensorManager.remapCoordinateSystem(_rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, _rotationMatrixRemapped);

            // convert coordinates to azimuth, pitch and roll
            SensorManager.getOrientation(_rotationMatrixRemapped, _orientation);

            // convert roll to degrees
            _tilt = Math.toDegrees(_orientation[2]);

            // normalise between -1 and +1
            _tiltNormalised = (float)(_tilt / 90);

            // trigger a listener update
            _currentListener.onTiltChanged(_tiltNormalised);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // meh
        }
    };

}
