package com.example.hellowifi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ID_READ_WRITE_PERMISSION = 99;

    private void askWifiPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            //check permissions
            int wifiAccessPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
            int wifiChangePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE);
            int fineLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int coarseLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if (wifiAccessPermission != PackageManager.PERMISSION_GRANTED ||
                    wifiChangePermission != PackageManager.PERMISSION_GRANTED ||
                    fineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                //if we don't have permission, prompt user
                this.requestPermissions(
                        new String[] {Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_ID_READ_WRITE_PERMISSION
                );
                return;
            }
        }
    }

    //After result is obtained
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int grantResults[]) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ID_READ_WRITE_PERMISSION: {
                //Permission granted
                if (grantResults.length > 1 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted!!", Toast.LENGTH_LONG).show();
                }

                //Permission denied
                else {
                    Toast.makeText(this, "Permission denied!!", Toast.LENGTH_LONG).show();
                }
                break;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askWifiPermissions();

        lv = (ListView)findViewById(R.id.listView);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager.getWifiState() == wifiManager.WIFI_STATE_DISABLED) {
            wifiManager.setWifiEnabled(true);
        }

        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "The Wifi scan has started...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiScanReceiver);
    }
}