package com.example.camera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements MotionSensorManager.OnMotionSensorManagerListener {

    static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    static final int REQUEST_ID_READ_WRITE_PERMISSION = 1;

    //motion sensor manager
    private MotionSensorManager mMotionSensorManager;

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

    private void askCameraPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            //check permission
            int readPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            int camPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

            if (writePermission != PackageManager.PERMISSION_GRANTED ||
                    readPermission != PackageManager.PERMISSION_GRANTED ||
                    camPermission != PackageManager.PERMISSION_GRANTED) {
                //ask the user permission
                this.requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA},
                        REQUEST_ID_READ_WRITE_PERMISSION
                );
                return;
            }
        }
    }

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

        mMotionSensorManager = new MotionSensorManager(this);
        mMotionSensorManager.setOnMotionSensorManagerListener(this);

        askCameraPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //register sensors
        mMotionSensorManager.registerMotionSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregister sensors when unused
        mMotionSensorManager.unregisterMotionSensors();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ID_READ_WRITE_PERMISSION: {
                //permission granted
                if (grantResults.length > 1 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED) {
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

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public void takePhoto (View view) {
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
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
                galleryAddPic();
            }
            
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Image saving cancelled", Toast.LENGTH_SHORT).show();
            }
            
            else {
                Toast.makeText(this, "Task failed, please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAccValueUpdated(float[] acceleration) {
        acc_x.setText("acc_Xaxis: " + acceleration[0]);
        acc_y.setText("acc_Yaxis: " + acceleration[1]);
        acc_z.setText("acc_Zaxis: " + acceleration[2]);
    }

    @Override
    public void onGyoValueUpdated(float[] gyoscope) {
        gyro_x.setText("gyo_Xaxis: " + gyoscope[0]);
        gyro_y.setText("gyo_Yaxis: " + gyoscope[1]);
        gyro_z.setText("gyo_Zaxis: " + gyoscope[2]);
    }

    @Override
    public void onMagValueUpdated(float[] magneticfield) {
        mag_x.setText("mag_Xaxis: " + magneticfield[0]);
        mag_y.setText("mag_Yaxis: " + magneticfield[1]);
        mag_z.setText("mag_Zaxis: " + magneticfield[2]);
        mag_h.setText("magnetic_field: " + magneticfield[3]);
    }
}