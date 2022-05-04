package com.example.smartalert;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class MainActivity3 extends AppCompatActivity{
    private static final int SMS_REQ_CODE =789 ;
    SharedPreferences sharedPreferences;
    ImageView imageView;
    FirebaseStorage storage;
    StorageReference myRef;
    Random random;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference, databaseReference1,databaseReference2,databaseReference3;
    LocationManager locationManager;
    LocationListener locationListener;
    double longitude,latitude;
    TextView textView;
    SmsManager manager;
    String alert,pr1,pr2,address,Fire,Snow,Flood;
    Button button1,button2,button7,button8;
    int countFire,countSnow,countFlood,i;
    boolean sent;// true when user clicks abort button after send alert

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        imageView = findViewById(R.id.imageView3);
        firebaseDatabase = FirebaseDatabase.getInstance();
        random = new Random();
        i = random.nextInt(1000000000);
        databaseReference = firebaseDatabase.getReference("message" + i);
        databaseReference1=firebaseDatabase.getReference("countFire");
        databaseReference2=firebaseDatabase.getReference("countSnow");
        databaseReference3=firebaseDatabase.getReference("countFlood");
        storage = FirebaseStorage.getInstance();
        myRef = storage.getReference();
        Intent intent = getIntent();
        alert = intent.getStringExtra("typeOfAlert");
        //save the telephone numbers on local database
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("family1", "6999999999");
        editor.putString("family2", "6900000000");
        editor.putString("fireDepartment", "4");
        editor.putString("Police", "1");
        editor.putString("FloodEmergency", "2");
        editor.putString("SnowEmergency", "3");
        editor.putString("Ambulance", "0");
        editor.commit();
        textView=findViewById(R.id.textView6);
        button1=findViewById(R.id.button6);
        button2=findViewById(R.id.button9);
        button1.setEnabled(false);
        button7=findViewById(R.id.button7);
        button8=findViewById(R.id.button8);
        button7.setEnabled(false);
        sent=false;
        //get data for counters from database
        databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Fire= (String.valueOf(snapshot.getValue()));
                countFire=Integer.parseInt(Fire);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        databaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Snow=(String.valueOf(snapshot.getValue()));
                countSnow=Integer.parseInt(Snow);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        databaseReference3.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Flood= (String.valueOf(snapshot.getValue()));
                countFlood=Integer.parseInt(Flood);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }


    private void sendSMS(String recipient, String message) {
        manager = SmsManager.getDefault();
        manager.sendTextMessage(recipient, null, message, null, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
                //ACCESS_FINE_LOCATION permission granted
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
        if (requestCode == SMS_REQ_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                //SEND_SMS permission granted
                    return;
            }
        }
    }

    public void picture(View view) {
        //Intent to take a picture
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 345);
        button8.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 345 && resultCode == RESULT_OK) {
            //code for image capture
            Bundle extra = data.getExtras();
            //change to bitmap to update imageView
            Bitmap bitmap = (Bitmap) extra.get("data");
            imageView.setImageBitmap(bitmap);
            //change to byte[] to save picture in Firebase Storage
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();
            i = random.nextInt(1000000000);
            myRef.child("image" + i + ".jpg").putBytes(bytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), R.string.imageUploaded,
                                Toast.LENGTH_SHORT).show();
                        button1.setEnabled(true);

                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        button7.setEnabled(false);
    }

    public void location(View view){
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude=location.getLongitude();
                latitude=location.getLatitude();
                //get full address
                address= getCompleteAddressString(latitude,longitude);
                textView.setText(address);
            }

        };
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            //request permission
            ActivityCompat.requestPermissions(this,new String []{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
        }
        button8.setEnabled(false);
        button7.setEnabled(true);

    }

    public void abort2(View view){
        //timestamp to save datetime in Firebase Database
        Timestamp timestamp=new Timestamp(System.currentTimeMillis());
        databaseReference.setValue("Alert cancellation for "+alert+" alarm"+" datetime: "+timestamp.toString());
        //Delete previous image from Storage if exists
        StorageReference deleteFile = myRef.child("image"+i+".jpg");
        deleteFile.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
               Toast.makeText(getApplicationContext(), R.string.imageDeleted,Toast.LENGTH_SHORT).show();
            }
        });
        if (sent) {
            //send cancellation messages
            switch (alert) {
                case "Fire":
                    countFire-=1;
                    pr1 = sharedPreferences.getString("fireDepartment", "no value yet");
                    pr2 = sharedPreferences.getString("Police", "no value yet");
                    sms(getString(R.string.CancelEverythinsOK));
                    databaseReference1.setValue(countFire);
                    break;
                case "Snow":
                    countSnow -= 1;
                    pr1 = sharedPreferences.getString("SnowEmergency", "no value yet");
                    pr2 = sharedPreferences.getString("Police", "no value yet");
                    sms(getString(R.string.CancelEverythinsOK));
                    databaseReference2.setValue(countSnow);
                    break;
                case "Flood":
                    countFlood -= 1;
                    pr1 = sharedPreferences.getString("FloodEmergency", "no value yet");
                    pr2 = sharedPreferences.getString("Police", "no value yet");
                    sms(getString(R.string.CancelEverythinsOK));
                    databaseReference3.setValue(countFlood);
                    break;
            }
            Toast.makeText(this, R.string.EverythingsOk, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, R.string.cancellation_was_successful,Toast.LENGTH_SHORT).show();
        }
        button7.setEnabled(false);
        button8.setEnabled(false);
        button1.setEnabled(false);
        button2.setEnabled(false);
    }

    public void send(View view) {
        // //timestamp to save datetime in Firebase Database
        Timestamp timestamp=new Timestamp(System.currentTimeMillis());
        sent=true;
        //send alert messages and update Firebase values
        switch (alert) {
            case "Fire":
                countFire+=1;
                pr1 = sharedPreferences.getString("fireDepartment", "no value yet");
                pr2 = sharedPreferences.getString("Police", "no value yet");
                sms("I am at the location with latitude: "+ latitude +" and longitude: "+ longitude+" and i behold "+alert);
                Toast.makeText(this,R.string.sent,Toast.LENGTH_LONG).show();
                databaseReference1.setValue(countFire);
                break;
            case "Snow":
                countSnow += 1;
                pr1 = sharedPreferences.getString("SnowEmergency", "no value yet");
                pr2 = sharedPreferences.getString("Police", "no value yet");

                sms("I am at the location with latitude: "+ latitude +" and longitude: "+ longitude+" and i behold "+alert);
                Toast.makeText(this,R.string.sent,Toast.LENGTH_LONG).show();
                databaseReference2.setValue(countSnow);
                break;
            case "Flood":
                countFlood += 1;
                pr1 = sharedPreferences.getString("FloodEmergency", "no value yet");
                pr2 = sharedPreferences.getString("Police", "no value yet");
                sms("I am at the location with latitude: "+ latitude +" and longitude: "+ longitude+" and i behold "+alert);
                Toast.makeText(this, R.string.sent,Toast.LENGTH_LONG).show();
                databaseReference3.setValue(countFlood);
                break;
        }
        //if latitude and longitude not null
        if(latitude!=0.0 && longitude!=0.0){
            databaseReference.setValue("I am at the location with latitude: " + latitude + " and longitude: " + longitude + " and i behold " + alert + " " + "datetime: " + timestamp.toString());
        }
        button1.setEnabled(false);
        button7.setEnabled(false);
        button8.setEnabled(false);




    }
    //get the exact location using latitude and longitude
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }

    public void sms(String message){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            //request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_REQ_CODE);
        } else {
            sendSMS(pr1, message);
            sendSMS(pr2, message);
        }
    }
}