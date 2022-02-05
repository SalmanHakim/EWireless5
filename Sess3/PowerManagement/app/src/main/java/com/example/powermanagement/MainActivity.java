package com.example.powermanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private IntentFilter intentFilter;

    //batt level
    private int BatteryL;
    //batt voltage
    private int BatteryV;
    //batt temp
    private double BatteryT;
    //batt tech
    private String BatteryTech;
    //batt status
    private String BatteryStatus;
    //batt health
    private String BatteryHealth;
    //batt plugged
    private String BatteryPlugged;

    public TextView TV;

    private BroadcastReceiver mBatInforReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //get data from intent
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                BatteryL = intent.getIntExtra("level", 0);
                BatteryV = intent.getIntExtra("voltage", 0);
                BatteryT = intent.getIntExtra("temperature", 0);
                BatteryTech = intent.getStringExtra("technology");

                switch (intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN)) {

                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        BatteryStatus = "Charging";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        BatteryStatus = "Discharging";
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        BatteryStatus = "Not charging";
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        BatteryStatus = "Full";
                        break;
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        BatteryStatus = "Battery status unknown";
                        break;
                }

                switch (intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN)) {

                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        BatteryHealth = "Unknown battery health";
                        break;
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        BatteryHealth = "Good";
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        BatteryHealth = "Dead";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        BatteryHealth = "Over voltage";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        BatteryHealth = "Overheat";
                        break;
                }

                switch (intent.getIntExtra("plugged", 0)) {

                    case BatteryManager.BATTERY_PLUGGED_AC:
                        BatteryPlugged = "Plugged to AC";
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        BatteryPlugged = "Plugged to USB";
                        break;
                    case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                        BatteryPlugged = "Connected to a wireless charger";
                        break;
                    default:
                        BatteryPlugged = "-----";
                }
            }

            TV.setText("Battery level: " + BatteryL + "%\n\n" +
                    "Battery status: " + BatteryStatus + "\n\n" +
                    "Battery plugged: " + BatteryPlugged + "\n\n" +
                    "Battery health: " + BatteryHealth + "\n\n" +
                    "Battery voltage: " + BatteryV + "mV\n\n" +
                    "Battery temperature: " + BatteryT*0.1 + "C\n\n" +
                    "Battery technology: " + BatteryTech);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Define intent for battery
        intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatInforReceiver, intentFilter);

        TV = (TextView) findViewById(R.id.TV);
    }
}