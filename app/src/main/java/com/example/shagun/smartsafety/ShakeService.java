package com.example.shagun.smartsafety;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class ShakeService extends Service implements ShakeListener.OnShakeListener {
    private ShakeListener mShaker;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    public int check;
    public String no1, no2, message;
    private Location mCurrentLocation;
    private final String currentLatitude = "30.7678197";
    private final String currentLongitude = "76.7859737";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {

        super.onCreate();
        this.mSensorManager = ((SensorManager)getSystemService(Context.SENSOR_SERVICE));
        this.mAccelerometer = this.mSensorManager.getDefaultSensor(1);
        mShaker = new ShakeListener(this);
        mShaker.setOnShakeListener(this);
        Toast.makeText(ShakeService.this, "Service is created!",Toast.LENGTH_LONG).show();
        Log.d(getPackageName(), "Created the Service!");
        check=1;
    }

    @Override
    public void onShake() {
        if(check == 1) {

            Toast.makeText(ShakeService.this, "EVENT HAPPENED!", Toast.LENGTH_SHORT).show();
            final Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(500);

            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            builder.setTitle("EMERGENCY ALERT");
            builder.setMessage("Sending SMS to Emergency Contacts");
            builder.setCancelable(true);
            builder.setNeutralButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            alert.show();
            SharedPreferences shared = getSharedPreferences("MyPreferences", MODE_PRIVATE);
            no1 = shared.getString("contact1","");
            no2 = shared.getString("contact2","");
            Toast.makeText(this, ""+no1+no2, Toast.LENGTH_SHORT).show();
            message = "Hey! I am in trouble, please reach out to me at:\n"+"https://maps.google.com/?q=";
            final SmsManager sms = SmsManager.getDefault();
            if(!no1.isEmpty()) {
                sms.sendTextMessage(no1,null,message+currentLatitude+","+currentLongitude+"\n\n-Smart Safety",null,null);
            }

            if(!no2.isEmpty()) {
                sms.sendTextMessage(no2,null,message+currentLatitude+","+currentLongitude+"\n\n-Smart Safety",null,null);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }
    public void onDestroy(){
        super.onDestroy();
        check=0;
        Log.d(getPackageName(),"Service Destroyed.");
    }
}
