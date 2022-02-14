package com.example.partb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mMagneticField;
    private Sensor Accelerometer;

    public static final String mag_x = "emf_Xaxis: ";
    public static final String mag_y = "emf_Yaxis: ";
    public static final String mag_z = "emf_Zaxis: ";
    public static final String magnetic_field = "magnetic_field: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //assign sensor manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Determine sensor type
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //register sensor when resume the app
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    String emfX, emfY, emfZ, mag;
    private double h;
    final float alpha = (float) 0.8;
    private float[] gravity = new float[3];
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //detect movement using accelerometer
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //low-pass filter -- isolate force of gravity
            gravity[0] = (alpha * gravity[0]) + (1 - alpha) * sensorEvent.values[0];
            gravity[1] = (alpha * gravity[1]) + (1 - alpha) * sensorEvent.values[1];
            gravity[2] = (alpha * gravity[2]) + (1 - alpha) * sensorEvent.values[2];

            //high-pass filter -- remove force of gravity
            float[] linear_acceleration = new float[3];
            linear_acceleration[0] = sensorEvent.values[0] - gravity[0];
            linear_acceleration[1] = sensorEvent.values[1] - gravity[1];
            linear_acceleration[2] = sensorEvent.values[2] - gravity[2];

            //when there is movement
            if (linear_acceleration[0] != 0.0 ||
                    linear_acceleration[1] != 0.0 ||
                    linear_acceleration[2] != 0.0) {

                //re-register emf sensor
                mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
                //Calculate total magnetic field
                h = Math.sqrt((sensorEvent.values[0] * sensorEvent.values[0]) + (sensorEvent.values[1] * sensorEvent.values[1]) + (sensorEvent.values[2] * sensorEvent.values[2]));

                String emfX = "mag_Xaxis: " + sensorEvent.values[0];
                String emfY = "mag_Yaxis: " + sensorEvent.values[1];
                String emfZ = "mag_Zaxis: " + sensorEvent.values[2];
                String mag = "magnetic_field: " + h;
            }
            //no movement
            else {
                //unregister emf sensor
                mSensorManager.unregisterListener(this, mMagneticField);
            }
        }
    }

    //method to respond to button taps
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        intent.putExtra(mag_x, emfX);
        intent.putExtra(mag_y, emfY);
        intent.putExtra(mag_z, emfZ);
        intent.putExtra(magnetic_field, mag);

        //start activity
        startActivity(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}