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
    private boolean stepDetect = false;
    private boolean stepCounted = true;
    private int steps = -1;     //wait for sensor to stabilised
    ////threshold for steps
    final private float uThreshold = 0.108f;
    final private float lThreshold = 0.088f;

    // Sensors data value used to calculate orientation
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];

    TextView counter;
    TextView acc;
    TextView heading;
    TextView coo_x;
    TextView coo_y;
    TextView coo_lat;
    TextView coo_lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);

        counter = (TextView) findViewById(R.id.textView);
        acc = (TextView) findViewById(R.id.textView2);
        heading = (TextView) findViewById(R.id.textView3);
        coo_x = (TextView) findViewById(R.id.textView4);
        coo_y = (TextView) findViewById(R.id.textView5);
        coo_lat = (TextView) findViewById(R.id.lat);
        coo_lon = (TextView) findViewById(R.id.lon);

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

    private double magnitude;
    private float[] hpfiltered = new float[3];
    private float[] lpfiltered = new float[3];
    private float grav[] = new float[3];

    //coordinate initialise
    float prev_x = 0.0f;
    float prev_y = 0.0f;
    float x, y;

    //world coordinate initialise
    double prev_lat = 0.0;
    double prev_lon = 0.0;
    double lat, lon;
    final double lat_deg_per_m = 1/110947.2;
    final double lon_deg_per_m = 1/87843.36;

    //angle parameters
    final float min_degree = 10.0f;
    final float max_degree = 80.0f;
    float prev_degree = 0.0f;
    float degree;


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            hpfiltered = highPass(sensorEvent.values);
            lpfiltered = lowPass(hpfiltered);
            magnitude = Math.sqrt((lpfiltered[0] * lpfiltered[0]) + (lpfiltered[1] * lpfiltered[1]) + (lpfiltered[2] * lpfiltered[2]));
            acc.setText("Acc: " + magnitude);

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
        float sl = stepLength();

        //heading angle
        degree = calculateOrientation();
        heading.setText("Angle: " + degree);

        //step detector
        if (!stepDetect) {
            if (magnitude > uThreshold) {
                stepDetect = true;
                stepCounted = false;
            }
        }
        else if (magnitude < lThreshold) {
            stepDetect = false;
        }

        if (stepDetect && !stepCounted) {
            stepCounted = true;
            steps = steps + 1;
            counter.setText("Step: "+ steps);

            if (steps > 0) {
                if (Math.abs(prev_degree - degree) <= min_degree) {
                    //coordinate
                    x = (float) (prev_x + (sl * Math.sin(Math.toRadians(prev_degree))));
                    y = (float) (prev_y + (sl * Math.cos(Math.toRadians(prev_degree))));

                    //world coordinate
                    lat = prev_lat + ((sl * Math.cos(Math.toRadians(prev_degree))) * lat_deg_per_m);
                    lon = prev_lon + ((sl * Math.sin(Math.toRadians(prev_degree))) * lon_deg_per_m);
                } else if (Math.abs(prev_degree - degree) > min_degree &&
                        Math.abs(prev_degree - degree) < max_degree) {
                    //coordinate
                    x = (float) (prev_x + (sl * Math.sin(Math.toRadians(degree))));
                    y = (float) (prev_y + (sl * Math.cos(Math.toRadians(degree))));

                    //world coordinate
                    lat = prev_lat + ((sl * Math.cos(Math.toRadians(degree))) * lat_deg_per_m);
                    lon = prev_lon + ((sl * Math.sin(Math.toRadians(degree))) * lon_deg_per_m);
                } else {
                    if ((degree - prev_degree) > 0) {
                        //coordinate
                        x = (float) (prev_x + (sl * Math.sin(Math.toRadians(prev_degree + 90))));
                        y = (float) (prev_y + (sl * Math.cos(Math.toRadians(prev_degree + 90))));

                        //world coordinate
                        lat = prev_lat + ((sl * Math.cos(Math.toRadians(prev_degree + 90))) * lat_deg_per_m);
                        lon = prev_lon + ((sl * Math.sin(Math.toRadians(prev_degree + 90))) * lon_deg_per_m);

                        prev_degree = prev_degree + 90;
                    } else {
                        //coordinate
                        x = (float) (prev_x + (sl * Math.sin(Math.toRadians(prev_degree - 90))));
                        y = (float) (prev_y + (sl * Math.cos(Math.toRadians(prev_degree - 90))));

                        //world coordinate
                        lat = prev_lat + ((sl * Math.cos(Math.toRadians(prev_degree - 90))) * lat_deg_per_m);
                        lon = prev_lon + ((sl * Math.sin(Math.toRadians(prev_degree - 90))) * lon_deg_per_m);

                        prev_degree = prev_degree - 90;
                    }
                }
            }

            //set coordinate
            prev_x = x;
            prev_y = y;
            coo_x.setText("Coordinate x: " + x);
            coo_y.setText("Coordinate y: " + y);

            //set world coordinate
            prev_lat = lat;
            prev_lon = lon;
            coo_lat.setText("Latitude: " + lat);
            coo_lon.setText("Longitude: " + lon);
        }
    }

    private float stepLength () {
        float height = 1.72f;
        boolean men = true;

        if (men) {
            return (0.415f * height);
        }
        else {
            return (0.413f * height);
        }
    }


    //double mean_grav = 9.81;
    private float[] highPass(float[] mag) {
        float alpha = 0.2f;
        float[] filtered = new float[3];

        grav[0] = (alpha * grav[0]) + ((1-alpha) * mag[0]);
        grav[1] = (alpha * grav[1]) + ((1-alpha) * mag[1]);
        grav[2] = (alpha * grav[2]) + ((1-alpha) * mag[2]);

        //mean_grav = (mag * (1-alpha)) + (mean_grav*alpha);

        filtered[0] = mag[0] - grav[0];
        filtered[1] = mag[1] - grav[1];
        filtered[2] = mag[2] - grav[2];

        return filtered;
    }

    private float[] lowPass(float[] mag) {
        float alpha = 0.2f;
        float[] filtered = new float[3];

        filtered[0] = (mag[0] * alpha) + (filtered[0] * (1-alpha));
        filtered[1] = (mag[1] * alpha) + (filtered[1] * (1-alpha));
        filtered[2] = (mag[2] * alpha) + (filtered[2] * (1-alpha));
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