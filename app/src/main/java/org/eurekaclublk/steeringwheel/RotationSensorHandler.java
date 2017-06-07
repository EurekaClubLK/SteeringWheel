package org.eurekaclublk.steeringwheel;

import android.app.Activity;
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

    // TODO handle accuracy changes - there shouldn't be anything significant {but have a look here just in case https://stackoverflow.com/questions/32677861/onaccuracychanged-why}

    private SensorManager _rotationSensorManager;
    private RotationListener _currentListener;
    private float _startTilt = 0;
    private boolean _turningDirection=true;// false - CW true - AntiCW

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
        _rotationSensorManager = (SensorManager)activity.getSystemService(Activity.SENSOR_SERVICE);

        // create instance of listener
        _currentListener = listener;
    }

    /**
     * Start reading sensor values. The attached listener will be triggered.
     */
    void startListening() {
        _startTilt = 0;
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
        float _tilt;
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
            _tilt = (float)Math.toDegrees(_orientation[2]);

            //Set 0 point
            if (_startTilt == 0){
                _startTilt = _tilt;
            }

            // normalise between -1 and +1
            _tiltNormalised = ((_tilt-_startTilt)/ 90);

            //OverTilt protection (to an extent)
            if (_tiltNormalised < 2){
                if (_tiltNormalised < 0){
                    _turningDirection = true;
                }else{
                    _turningDirection = false;
                }
            }else if(_turningDirection){
                _tiltNormalised =-1;
            }
            if (_tiltNormalised > 1){
                _tiltNormalised =1;
            }else if (_tiltNormalised < -1){
                _tiltNormalised =-1;
            }
            // normalise between -1 and +1*
            // trigger a listener update
            _currentListener.onTiltChanged(_tiltNormalised);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // meh
        }
    };

}
