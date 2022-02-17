package com.example.camera;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MotionSensorManager implements SensorEventListener {

    //listener that pass through info of the activities
    private OnMotionSensorManagerListener motionSensorManagerListener;

    //sensors and manager
    private SensorManager sensorManager;
    private Sensor Accelerometer;
    private Sensor Gyroscope;
    private Sensor mMagneticField;

    //constructor
    public MotionSensorManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        mMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    //listener
    public void setOnMotionSensorManagerListener(OnMotionSensorManagerListener motionSensorManagerListener) {
        this.motionSensorManagerListener = motionSensorManagerListener;
    }

    //register sensor
    public void registerMotionSensors(){
        sensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, Gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //unregister sensor
    public void unregisterMotionSensors(){
        sensorManager.unregisterListener(this);
    }

    //Listeners for activities when data changed
    public interface OnMotionSensorManagerListener{
        void onAccValueUpdated(float[] acceleration);
        void onGyoValueUpdated(float[] gyoscope);
        void onMagValueUpdated(float[] magneticfield);
    }

    private double h;
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
                float[] linear_acceleration = new float[3];
                linear_acceleration[0] = sensorEvent.values[0] - gravity[0];
                linear_acceleration[1] = sensorEvent.values[1] - gravity[1];
                linear_acceleration[2] = sensorEvent.values[2] - gravity[2];

                motionSensorManagerListener.onAccValueUpdated(new float[]{linear_acceleration[0], linear_acceleration[1], linear_acceleration[2]});
                break;

            case Sensor.TYPE_GYROSCOPE:
                motionSensorManagerListener.onGyoValueUpdated(new float[]{sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]});
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                //Calculate total magnetic field
                h = Math.sqrt((sensorEvent.values[0]*sensorEvent.values[0])+(sensorEvent.values[1]*sensorEvent.values[1])+(sensorEvent.values[2]*sensorEvent.values[2]));

                motionSensorManagerListener.onMagValueUpdated(new float[]{sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2], (float) h});
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}