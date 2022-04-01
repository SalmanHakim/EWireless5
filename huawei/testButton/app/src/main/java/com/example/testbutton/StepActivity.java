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
    private int steps = 0;
    ////threshold for steps
    private double uThreshold = 0.108;
    private double lThreshold = 0.088;

    // Sensors data value used to calculate orientation
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];

    TextView counter;
    //TextView acc;
    //TextView heading;
    TextView coo_x;
    TextView coo_y;
    TextView coo_lat;
    TextView coo_lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);

        counter = (TextView) findViewById(R.id.textView);
        //acc = (TextView) findViewById(R.id.textView2);
        //heading = (TextView) findViewById(R.id.textView3);
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

    double magnitude = 0;
    double hpfiltered = 0;
    double lpfiltered = 0;

    //coordinate
    double prev_x = 0.0;
    double prev_y = 0.0;
    double x, y;

    //world coordinate
    double prev_lat = 0.0;
    double prev_lon = 0.0;
    double lat, lon;
    final double convert = 0.000008997;

    //angle parameters
    final float min_degree = 10.0f;
    final float max_degree = 80.0f;
    float prev_degree = 0.0f;
    float degree;


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            magnitude = Math.sqrt((sensorEvent.values[0] * sensorEvent.values[0]) + (sensorEvent.values[1] * sensorEvent.values[1]) + (sensorEvent.values[2] * sensorEvent.values[2]));
            hpfiltered = highPass(magnitude);
            lpfiltered = lowPass(hpfiltered);
            //acc.setText("Acc: " + lpfiltered);

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
        degree = calculateOrientation();
        //heading.setText("Angle: " + degree);

        //step detector
        if (!stepDetect) {
            if (lpfiltered > uThreshold) {
                stepDetect = true;
                stepCounted = false;
            }
        }
        else if (lpfiltered < lThreshold) {
            stepDetect = false;
        }

        if (stepDetect && !stepCounted) {
            stepCounted = true;
            steps = steps + 1;
            counter.setText("Step: "+ steps);

            if (Math.abs(prev_degree - degree) <= min_degree) {
                //coordinate
                x = prev_x + (sl * Math.sin(Math.toRadians(prev_degree)));
                y = prev_y + (sl * Math.cos(Math.toRadians(prev_degree)));

                //world coordinate
                lat = prev_lat + ((sl * Math.cos(Math.toRadians(prev_degree)))*convert);
                lon = prev_lon + ((sl * Math.sin(Math.toRadians(prev_degree)))*convert);
            }
            else if (Math.abs(prev_degree - degree) > min_degree &&
                    Math.abs(prev_degree - degree) < max_degree) {
                //coordinate
                x = prev_x + (sl * Math.sin(Math.toRadians(degree)));
                y = prev_y + (sl * Math.cos(Math.toRadians(degree)));

                //world coordinate
                lat = prev_lat + ((sl * Math.cos(Math.toRadians(degree)))*convert);
                lon = prev_lon + ((sl * Math.sin(Math.toRadians(degree)))*convert);
            }
            else {
                if ((degree - prev_degree) > 0) {
                    //coordinate
                    x = prev_x + (sl * Math.sin(Math.toRadians(prev_degree+90)));
                    y = prev_y + (sl * Math.cos(Math.toRadians(prev_degree+90)));

                    //world coordinate
                    lat = prev_lat + ((sl * Math.cos(Math.toRadians(prev_degree+90)))*convert);
                    lon = prev_lon + ((sl * Math.sin(Math.toRadians(prev_degree+90)))*convert);

                    prev_degree = prev_degree + 90;
                }
                else {
                    //coordinate
                    x = prev_x + (sl * Math.sin(Math.toRadians(prev_degree-90)));
                    y = prev_y + (sl * Math.cos(Math.toRadians(prev_degree-90)));

                    //world coordinate
                    lat = prev_lat + ((sl * Math.cos(Math.toRadians(prev_degree-90)))*convert);
                    lon = prev_lon + ((sl * Math.sin(Math.toRadians(prev_degree-90)))*convert);

                    prev_degree = prev_degree - 90;
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