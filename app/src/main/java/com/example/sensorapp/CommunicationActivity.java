package com.example.sensorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class CommunicationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);

        ImageButton setDelay = findViewById(R.id.imageButtonSet);
        EditText delayvalue = findViewById(R.id.editTextDelay);
        delayvalue.setText(Share.getDelay("delay",this));

        setDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((Integer.parseInt(String.valueOf(delayvalue.getText())))<100)){
                    Toast.makeText(CommunicationActivity.this, "Invalid Value", Toast.LENGTH_SHORT).show();
                }
                else if(((Integer.parseInt(String.valueOf(delayvalue.getText())))>1000)){
                    Toast.makeText(CommunicationActivity.this, "Invalid Value", Toast.LENGTH_SHORT).show();
                }
                else{
                    // Share sa = new Share();
                    Share.setDelay("delay", String.valueOf(delayvalue.getText()), CommunicationActivity.this);
                }
            }
        });
    }
}