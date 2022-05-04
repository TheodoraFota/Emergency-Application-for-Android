package com.example.smartalert;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    SensorManager sensorManager;
    Sensor sensor;
    ImageView imageView;
    String alertType;
    static boolean flag;//true when fall is detected
    LocationManager locationManager;
    LocationListener locationListener;
    static double longitude, latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        imageView=findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.alert);
        sensorManager.registerListener(this,sensor,0);
        flag=false;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //permission granted
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            double accelValuesX = sensorEvent.values[0];
            double accelValuesY = sensorEvent.values[1];
            double accelValuesZ = sensorEvent.values[2];
            double rootSquare = Math.sqrt(Math.pow(accelValuesX, 2) + Math.pow(accelValuesY, 2) + Math.pow(accelValuesZ, 2));
            //if rootSquare is near to 0.0 a fall is detected
            if (rootSquare < 0.3 && !flag) {
                locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                    }
                };
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //request permission
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    //permission is granted
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
                }
                //start MainActivity2
                Intent intent=new Intent(this,MainActivity2.class);
                startActivity(intent);
                alertType="Fall";
                flag=true;// fall detected

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    public void fire_alert(View view){
        flag=true;
        alertType="Fire";
        //start MainActivity3.java and pass the alertType
        Intent intent=new Intent(this,MainActivity3.class);
        intent.putExtra("typeOfAlert",alertType);
        startActivity(intent);
    }

    public void snow_alert(View view){
        flag=true;
        alertType="Snow";
        //start MainActivity3.java and pass the alertType
        Intent intent=new Intent(this,MainActivity3.class);
        intent.putExtra("typeOfAlert",alertType);
        startActivity(intent);
    }

    public void flood_alert(View view){
        flag=true;
        alertType="Flood";
        //start MainActivity3.java and pass the alertType
        Intent intent=new Intent(this,MainActivity3.class);
        intent.putExtra("typeOfAlert",alertType);
        startActivity(intent);
    }

    public void openStatistics(View view){
        //start Statistics.java
        Intent intent=new Intent(this,Statistics.class);
        startActivity(intent);
    }
}