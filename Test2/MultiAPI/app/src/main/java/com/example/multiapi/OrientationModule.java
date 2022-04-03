package com.example.multiapi;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationModule implements SensorEventListener {

    //listener that pass through info of the activities
    private OnOrientationModule orientationModule;

    //sensors and manager
    private SensorManager sensorManager;
    private Sensor Accelerometer;
    private Sensor mMagneticField;

    public OrientationModule(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        mMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    //listener
    public void setOnOrientationModule(OnOrientationModule orientationModule) {
        this.orientationModule = orientationModule;
    }

    //register sensor
    public void registerOrientationSensors(){
        sensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //unregister sensor
    public void unregisterOrientationSensors(){
        sensorManager.unregisterListener(this);
    }

    public interface OnOrientationModule{
        void onDirectionUpdated(String compass, float degree);
    }

    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];

    final float alpha = (float) 0.8;
    private float[] gravity = new float[3];
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                //low-pass filter -- isolate force of gravity
                gravity[0] = (alpha * gravity[0]) + (1-alpha) * sensorEvent.values[0];
                gravity[1] = (alpha * gravity[1]) + (1-alpha) * sensorEvent.values[1];
                gravity[2] = (alpha * gravity[2]) + (1-alpha) * sensorEvent.values[2];

                //high-pass filter -- remove force of gravity
                accelerometerValues[0] = sensorEvent.values[0] - gravity[0];
                accelerometerValues[1] = sensorEvent.values[1] - gravity[1];
                accelerometerValues[2] = sensorEvent.values[2] - gravity[2];

                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticFieldValues[0] = sensorEvent.values[0];
                magneticFieldValues[1] = sensorEvent.values[1];
                magneticFieldValues[2] = sensorEvent.values[2];

                break;
        }

        calculateOrientation();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    String compass;
    private void calculateOrientation() {

        float[] values = new float[3];

        float [] R = new float[9];

        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);

        SensorManager.getOrientation(R, values);

        float degree = (float) Math.toDegrees(values[0]);

        if (degree >= -5 && degree < 5) {
            compass = "North";
        }
        else if (degree >= 5 && degree < 85) {
            compass = "North-East";
        }
        else if (degree >= 85 && degree < 95) {
            compass = "East";
        }
        else if (degree >= 95 && degree < 175) {
            compass = "South-East";
        }
        else if (degree >= 175 && degree < -175) {
            compass = "South";
        }
        else if (degree >= -85 && degree < -5) {
            compass = "North-West";
        }
        else if (degree >= -95 && degree < -85) {
            compass = "West";
        }
        else if (degree >= -175 && degree < -95) {
            compass = "South-West";
        }

        orientationModule.onDirectionUpdated(compass, degree);
    }
}
