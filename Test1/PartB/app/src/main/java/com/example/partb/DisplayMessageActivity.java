package com.example.partb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

public class DisplayMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        //get intent and string
        Intent intent = getIntent();
        String emfX = intent.getStringExtra(MainActivity.mag_x);
        String emfY = intent.getStringExtra(MainActivity.mag_y);
        String emfZ = intent.getStringExtra(MainActivity.mag_z);
        String mag = intent.getStringExtra(MainActivity.magnetic_field);

        //set textview as emf values
        TextView valuex = findViewById(R.id.textView2);
        TextView valuey = findViewById(R.id.textView3);
        TextView valuez = findViewById(R.id.textView4);
        TextView valuemag = findViewById(R.id.textView5);
        valuex.setText(emfX);
        valuey.setText(emfY);
        valuez.setText(emfZ);
        valuemag.setText(mag);
    }
}