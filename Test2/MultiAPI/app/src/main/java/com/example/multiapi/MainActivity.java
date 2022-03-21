package com.example.multiapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //declare sensors
    private SensorManager mSensorManager;
    private Sensor Accelerometer;
    private Sensor mMagneticField;

    //declare text view
    TextView lat;
    TextView lon;
    TextView tv;
    TextView deg;

    //declare location manager and listener
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermissions();

        lat = (TextView) findViewById(R.id.lat);
        lon = (TextView) findViewById(R.id.lon);
        tv = (TextView) findViewById(R.id.textView4);
        deg = (TextView) findViewById(R.id.textView5);

        lv = (ListView)findViewById(R.id.listView);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager.getWifiState() == wifiManager.WIFI_STATE_DISABLED) {
            wifiManager.setWifiEnabled(true);
        }

        //define location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new myLocationListener();

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Open GPS", Toast.LENGTH_SHORT).show();
        }

        //assign sensor type
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void wifi(View view) {
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "The Wifi scan has started...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //register sensors when using app
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //disable sensor when not using the app
        unregisterReceiver(wifiScanReceiver);
        mSensorManager.unregisterListener(this);
    }

    static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    static final int REQUEST_ID_PERMISSION = 99;

    private void askPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            //check permission
            int readPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int camPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

            int coarseLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int fineLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int internetPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET);

            int wifiAccessPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
            int wifiChangePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE);

            if (writePermission != PackageManager.PERMISSION_GRANTED ||
                    readPermission != PackageManager.PERMISSION_GRANTED ||
                    camPermission != PackageManager.PERMISSION_GRANTED ||
                    coarseLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    internetPermission != PackageManager.PERMISSION_GRANTED ||
                    fineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    wifiAccessPermission != PackageManager.PERMISSION_GRANTED ||
                    wifiChangePermission != PackageManager.PERMISSION_GRANTED) {
                //ask the user permission
                this.requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET,
                                Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE},
                        REQUEST_ID_PERMISSION
                );
                return;
            }
        }
    }

    //upon getting permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ID_PERMISSION: {
                //permission granted
                if (grantResults.length > 1 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[4] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[5] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[6] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[7] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show();
                }

                //permission denied or cancelled
                else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                }

                break;
            }
        }
    }

    //naming of the picture file
    String currentPhotoPath;

    private File createImageFile() throws IOException {
        //create image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        //save
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //add picture to phone gallery
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public void takePhoto(View view) {
        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //create file where the pics go
        File photoFile = null;
        try {
            photoFile = createImageFile();
            Log.e("CameraApp", photoFile.getPath());
        } catch (IOException ex) {
        }

        //continue if successful
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                    "com.example.camera",
                    photoFile);
            takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePicIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
                galleryAddPic();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Image saving cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Task failed, please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    ListView lv;
    String wifis[];
    WifiManager wifiManager;
    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> wifiScanList = wifiManager.getScanResults();
            unregisterReceiver(this);

            wifis = new String[wifiScanList.size()];
            Log.e("WiFi", String.valueOf(wifiScanList.size()));
            for (int i = 0; i < wifiScanList.size(); i++) {
                wifis[i] = wifiScanList.get(i).SSID + ", " + wifiScanList.get(i).BSSID + ", " + String.valueOf(wifiScanList.get(i).level);
                Log.e("WiFi", String.valueOf(wifis[i]));
            }

            lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, wifis));
        }
    };

    public void displayLocation(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void orientation (View view) {
        calculateOrientation();
    }

    private void calculateOrientation() {

        float[] values = new float[3];

        float [] R = new float[9];

        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);

        SensorManager.getOrientation(R, values);

        float degree = (float) Math.toDegrees(values[0]);

        if (degree >= -5 && degree < 5) {
            tv.setText("North");
            //deg.setText((int)degree);
        }
        else if (degree >= 5 && degree < 85) {
            tv.setText("North-East");
            //deg.setText((int) degree);
        }
        else if (degree >= 85 && degree < 95) {
            tv.setText("East");
            //deg.setText((int) degree);
        }
        else if (degree >= 95 && degree < 175) {
            tv.setText("South-East");
            //deg.setText((int) degree);
        }
        else if (degree >= 175 && degree < -175) {
            tv.setText("South");
            //deg.setText((int) degree);
        }
        else if (degree >= -85 && degree < -5) {
            tv.setText("North-West");
            //deg.setText((int) degree);
        }
        else if (degree >= -95 && degree < -85) {
            tv.setText("West");
            //deg.setText((int) degree);
        }
        else if (degree >= -175 && degree < -95) {
            tv.setText("South-West");
            //deg.setText((int) degree);
        }
    }

    class myLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(@NonNull Location location) {
            if (location != null) {
                double tlat = location.getLatitude();
                double tlon = location.getLongitude();
                lat.setText(Double.toString(tlat));
                lon.setText(Double.toString(tlon));
            }
        }
    }
}