package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Statistics extends AppCompatActivity {
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference,databaseReference1,databaseReference2,databaseReference3;
    TextView textView;
    String Fall,Fire,Snow,Flood;
    ImageButton imageButton;
    SpeechRecognizer speechRecognizer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        imageButton=findViewById(R.id.imageButton);
        speechRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
        textView=findViewById(R.id.textView9);
        firebaseDatabase = FirebaseDatabase.getInstance();
        //get data from Firebase Database
        databaseReference=firebaseDatabase.getReference("countFall");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Fall=String.valueOf(snapshot.getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        databaseReference1=firebaseDatabase.getReference("countFire");
        databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Fire=String.valueOf(snapshot.getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        databaseReference2=firebaseDatabase.getReference("countSnow");
        databaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Snow=String.valueOf(snapshot.getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        databaseReference3=firebaseDatabase.getReference("countFlood");
        databaseReference3.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Flood=String.valueOf(snapshot.getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    //set text to textView

    @SuppressLint("SetTextI18n")
    public void Fall(View view){
        textView.setText(getString(R.string.Fall_alert_called)+" "+Fall+" "+getString(R.string.times));
    }
    @SuppressLint("SetTextI18n")
    public void Fire(View view){
        textView.setText(getString(R.string.Fire_alert_called)+" "+Fire+" "+getString(R.string.times));
    }
    @SuppressLint("SetTextI18n")
    public void Snow(View view){
        textView.setText(getString(R.string.Snow_alert_called)+" "+Snow+" "+getString(R.string.times));
    }
    @SuppressLint("SetTextI18n")
    public void Flood(View view){
        textView.setText(getString(R.string.Flood_alert_called)+" "+Flood+" "+getString(R.string.times));
    }
    @SuppressLint("SetTextI18n")


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==888 && resultCode==RESULT_OK){
            //If voice recognized
            imageButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_off_24));
            ArrayList<String> results =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //show results to screen
            showMessage("Recognized text",results.toString());
            //check if there are any keywords
            if(results.toString().contains("fire")||results.toString().contains("φωτιά")||results.toString().contains("incendie")){
                textView.setText(getString(R.string.Fire_alert_called)+" "+Fire+" "+getString(R.string.times));
            }
            else if(results.toString().contains("flood")||results.toString().contains("πλημμύρα")|| results.toString().contains("inondation")){
                textView.setText(getString(R.string.Flood_alert_called)+" "+Flood+" "+getString(R.string.times));
            }
            else if(results.toString().contains("Snow")||results.toString().contains("χιόνι")||results.toString().contains("neige")) {
                textView.setText(getString(R.string.Snow_alert_called)+" "+Snow+" "+getString(R.string.times));
            }
            else if(results.toString().contains("Fall")||results.toString().contains("πτώση")||results.toString().contains("chute")){
                textView.setText(getString(R.string.Fall_alert_called)+" "+Fall+" "+getString(R.string.times));
            }
            else{
                Toast.makeText(this,"Please try again...",Toast.LENGTH_SHORT).show();
            }
        }
        else{

               Toast.makeText(this,"Please try again...",Toast.LENGTH_SHORT).show();
        }

    }

    void showMessage(String title,String message){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .show();
    }

    public void speak(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            //request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
        imageButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_24));
        //Intent for Speech Recognition
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Please say something!");
        startActivityForResult(intent, 888);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1 && grantResults[0]!=PackageManager.PERMISSION_GRANTED){
           return;
        }
    }
}