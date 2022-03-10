package com.example.hellobt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ID_READ_WRITE_PERMISSION = 99;

    private void askBTPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            //check permission
            int btPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH);
            int btAdminPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN);
            int btAdvertisePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE);
            int btConnectPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
            int fineLocationAccess = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int coarseLocationAccess = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if (btPermission != PackageManager.PERMISSION_GRANTED ||
                    btAdminPermission != PackageManager.PERMISSION_GRANTED ||
                    btAdvertisePermission != PackageManager.PERMISSION_GRANTED ||
                    btConnectPermission != PackageManager.PERMISSION_GRANTED ||
                    fineLocationAccess != PackageManager.PERMISSION_GRANTED ||
                    coarseLocationAccess != PackageManager.PERMISSION_GRANTED) {
                //if we don't have permission, prompt user
                this.requestPermissions(
                        new String[] {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_ID_READ_WRITE_PERMISSION
                );
                return;
            }
        }
    }

    //After permission result is obtained
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
                        grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[4] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[5] == PackageManager.PERMISSION_GRANTED) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}