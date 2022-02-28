package com.example.hellolocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TextView lat;
    TextView lon;

    LocationManager locationManager;
    LocationListener locationListener;

    private static final int REQUEST_ID_LOCATION_PERMISSION = 99;

    private void askLocationPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //check permission
            int CoarseLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int FineLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int InternetPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET);

            if (CoarseLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    InternetPermission != PackageManager.PERMISSION_GRANTED ||
                    FineLocationPermission != PackageManager.PERMISSION_GRANTED) {
                //dont have permission prompt
                this.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.INTERNET},
                        REQUEST_ID_LOCATION_PERMISSION
                );
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askLocationPermission();

        lat = (TextView) findViewById(R.id.lat);
        lon = (TextView) findViewById(R.id.lon);

        //define location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new myLocationListener();

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Open GPS", Toast.LENGTH_SHORT).show();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    //to verify permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permission, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permission, grantResults);

        switch (requestCode) {
            case REQUEST_ID_LOCATION_PERMISSION: {
                if (grantResults.length > 1 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show();
                }
                //if permission not granted
                else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
                }
                break;
            }
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