//By Salman Saiful Redzuan

package com.example.testbutton;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class StepActivity extends AppCompatActivity implements SensorEventListener {

    // get access to sensors
    private SensorManager sensorManager;

    // represent a sensor
    private Sensor magneticField;
    private Sensor accelerometer;

    // initialise parameters
    private boolean detectFlag = false;
    private boolean counted = true;
    private int steps = 0;
    ////threshold for steps
    private double uThreshold = 0.108;
    private double lThreshold = 0.088;

    // Sensors data value used to calculate orientation
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];

    TextView counter;
    TextView acc;
    TextView heading;
    TextView coo_x;
    TextView coo_y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);

        counter = (TextView) findViewById(R.id.textView);
        acc = (TextView) findViewById(R.id.textView2);
        heading = (TextView) findViewById(R.id.textView3);
        coo_x = (TextView) findViewById(R.id.textView4);
        coo_y = (TextView) findViewById(R.id.textView5);

        // initialise sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magneticField = (Sensor) sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = (Sensor) sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    double magnitude = 0;
    double hpfiltered = 0;
    double lpfiltered = 0;

    double prev_x = 0.0;
    double prev_y = 0.0;
    double x, y;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            magnitude = Math.sqrt((sensorEvent.values[0] * sensorEvent.values[0]) + (sensorEvent.values[1] * sensorEvent.values[1]) + (sensorEvent.values[2] * sensorEvent.values[2]));
            hpfiltered = highPass(magnitude);
            lpfiltered = lowPass(hpfiltered);
            acc.setText("Acc: " + lpfiltered);

            // for degree calculation
            accelerometerValues[0] = sensorEvent.values[0];
            accelerometerValues[1] = sensorEvent.values[1];
            accelerometerValues[2] = sensorEvent.values[2];
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            // for degree calculation
            magneticFieldValues[0] = sensorEvent.values[0];
            magneticFieldValues[1] = sensorEvent.values[1];
            magneticFieldValues[2] = sensorEvent.values[2];
        }

        //step length
        double sl = stepLength();

        //heading angle
        float degree = calculateOrientation();
        heading.setText("Angle: " + degree);

        //step detector
        if (!detectFlag) {
            if (lpfiltered > uThreshold) {
                detectFlag = true;
                counted = false;
            }
        }
        else if (lpfiltered < lThreshold) {
            detectFlag = false;
        }

        if (detectFlag && !counted) {
            counted = true;
            steps = steps + 1;
            counter.setText("Step: "+ steps);

            //coordinate
            double rad = Math.toRadians(degree);
            x = prev_x + (sl * Math.sin(rad));
            y = prev_y + (sl * Math.cos(rad));
            prev_x = x;
            prev_y = y;
            coo_x.setText("Coordinate x: " + x);
            coo_y.setText("Coordinate y: " + y);
        }
    }

    private double stepLength () {
        double height = 1.72;
        boolean men = true;

        if (men) {
            return (0.415 * height);
        }
        else {
            return (0.413 * height);
        }
    }

    double mean_grav = 9.81;
    private double highPass(double mag) {
        double alpha = 0.9;

        mean_grav = (mag * (1-alpha)) + (mean_grav*alpha);
        return (mag - mean_grav);
    }

    double filtered = 0;
    private double lowPass(double mag) {
        double alpha = 0.2;

        filtered = (mag * alpha) + (filtered * (1-alpha));
        return filtered;
    }

    private float calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];

        // calculate degrees
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);

        float degree = (float) Math.toDegrees(values[0]);

        return degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}