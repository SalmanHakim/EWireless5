package com.example.parta;

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
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mMagneticField;
    private Sensor Accelerometer;
    private Sensor Gyroscope;

    private TextView mag_x;
    private TextView mag_y;
    private TextView mag_z;
    private TextView mag_h;

    private TextView acc_x;
    private TextView acc_y;
    private TextView acc_z;

    private TextView gyro_x;
    private TextView gyro_y;
    private TextView gyro_z;

    public static final String EXTRA_MESSAGE = "com.example.parta.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //assign textview from layout file for emf
        mag_x = (TextView) findViewById(R.id.emf_Xaxis);
        mag_y = (TextView) findViewById(R.id.emf_Yaxis);
        mag_z = (TextView) findViewById(R.id.emf_Zaxis);
        mag_h = (TextView) findViewById(R.id.emf_magnetic_field);

        //assign textview from layout file for accelerometer
        acc_x = (TextView) findViewById(R.id.acc_Xaxis);
        acc_y = (TextView) findViewById(R.id.acc_Yaxis);
        acc_z = (TextView) findViewById(R.id.acc_Zaxis);

        //assign textview from layout file for gyroscope
        gyro_x = (TextView) findViewById(R.id.gyro_Xaxis);
        gyro_y = (TextView) findViewById(R.id.gyro_Yaxis);
        gyro_z = (TextView) findViewById(R.id.gyro_Zaxis);

        //assign sensor manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Determine sensor type
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    //method to respond to button taps
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        //get data from editText by ID
        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);

        //start activity
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //register sensor when resume the app
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, Gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //disable sensor when not using the app
        mSensorManager.unregisterListener(this);
    }

    private double h;
    final float alpha = (float) 0.8;
    private float[] gravity = new float[3];
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        switch (sensorEvent.sensor.getType()) {

            case Sensor.TYPE_MAGNETIC_FIELD:
                //Calculate total magnetic field
                h = Math.sqrt((sensorEvent.values[0]*sensorEvent.values[0])+(sensorEvent.values[1]*sensorEvent.values[1])+(sensorEvent.values[2]*sensorEvent.values[2]));
                //Set text view to show value of emf
                mag_x.setText("mag_Xaxis: " + sensorEvent.values[0]);
                mag_y.setText("mag_Yaxis: " + sensorEvent.values[1]);
                mag_z.setText("mag_Zaxis: " + sensorEvent.values[2]);
                mag_h.setText("magnetic_field: " + h);

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

                //Set text view to show value of accelerometer
                acc_x.setText("acc_Xaxis: " + linear_acceleration[0]);
                acc_y.setText("acc_Yaxis: " + linear_acceleration[1]);
                acc_z.setText("acc_Zaxis: " + linear_acceleration[2]);

            case Sensor.TYPE_GYROSCOPE:
                //Set text view to show value of gyroscope
                gyro_x.setText("gyo_Xaxis: " + sensorEvent.values[0]);
                gyro_y.setText("gyo_Yaxis: " + sensorEvent.values[1]);
                gyro_z.setText("gyo_Zaxis: " + sensorEvent.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}