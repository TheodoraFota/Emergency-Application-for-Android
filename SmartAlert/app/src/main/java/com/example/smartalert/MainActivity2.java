package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.sql.Timestamp;
import java.util.Random;


public class MainActivity2 extends AppCompatActivity {
    private static final int SMS_REQ_CODE = 123;
    TextView textView;
    MediaPlayer mediaPlayer;
    CountDownTimer countDownTimer;
    SharedPreferences preferences;
    SmsManager manager;
    String pr1, pr2, pr3;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference databaseReference1;
    String Fall;
    int countFall,i;
    Timestamp timestamp;
    Random random;
    Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        timestamp = new Timestamp(System.currentTimeMillis());
        //textView for countDown
        textView = findViewById(R.id.textView4);
        //mediaPlayer for sound
        mediaPlayer = MediaPlayer.create(this, R.raw.sound1);
        //get the appropriate phones stored in SharedPreferences
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        pr1 = preferences.getString("family1", "no value yet");
        pr2 = preferences.getString("Ambulance", "no value yet");
        pr3 = preferences.getString("family2", "no value yet");
        firebaseDatabase = FirebaseDatabase.getInstance();
        random = new Random();
        i = random.nextInt(1000000000);
        //Random name for each message
        databaseReference = firebaseDatabase.getReference("message" + i);
        databaseReference1 = firebaseDatabase.getReference("countFall");
        //get the countFall everytime value changes
        databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Fall = String.valueOf(snapshot.getValue());
                countFall = Integer.parseInt(Fall);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        mediaPlayer.start();
        countDownTimer = new CountDownTimer(30000, 1000) {
            int count = 30;

            @Override
            public void onTick(long l) {
                count--;
                textView.setText(String.valueOf(count));
            }

            @Override
            public void onFinish() {
                mediaPlayer.stop();
                sms("Alert!!!");
                showMessage("message:",R.string.ambulanceAndRelatives);
                countFall += 1;
                MainActivity.flag = false;
                //write to Firebase database if latitude and longitude not null
                if(MainActivity.latitude!=0.0 && MainActivity.longitude!=0.0) {
                    databaseReference.setValue("I am at the location with latitude: " + MainActivity.latitude + " and longitude: " + MainActivity.longitude + " and i fell" + " " + "datetime: " + timestamp.toString());
                }
                //change the value of countFall in Firebase
                databaseReference1.setValue(countFall);
                button2.setEnabled(false);
                MainActivity.flag=false;
            };
        };
        countDownTimer.start();
        button2 = findViewById(R.id.button2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_REQ_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
    }


    private void sendSMS(String recipient, String message) {
        manager = SmsManager.getDefault();
        manager.sendTextMessage(recipient, null, message, null, null);
    }

    public void abort(View view) {
        countDownTimer.cancel();
        mediaPlayer.stop();
        Toast.makeText(this, R.string.cancellation_was_successful, Toast.LENGTH_SHORT).show();
        //write to Firebase database
        databaseReference.setValue("Alert cancellation for Fall alarm" + " datetime: " +timestamp.toString());
        MainActivity.flag = false;
        button2.setEnabled(false);
    }

    public void sms(String message) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            //request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_REQ_CODE);
        } else {
            //send sms to each recipient
            sendSMS(pr1, message);
            sendSMS(pr2, message);
            sendSMS(pr3, message);
        }
    }

    //method for displaying a message on screen
    void showMessage(String title, int message){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .show();
    }
}


