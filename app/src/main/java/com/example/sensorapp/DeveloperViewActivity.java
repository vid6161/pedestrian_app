package com.example.sensorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class DeveloperViewActivity extends AppCompatActivity {

    ImageButton imageButtonSensorData;
    ImageButton imageButtonSensorFusion;
    ImageButton imageButtonComm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_view);

        imageButtonSensorData = findViewById(R.id.imageButtonSensorData);
        imageButtonSensorFusion = findViewById(R.id.imageButtonSensorFusion);
        imageButtonComm = findViewById(R.id.imageButtonCommunication);

        imageButtonSensorData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DeveloperViewActivity.this, SensorInputActivity.class);
                startActivity(i);
            }
        });

        imageButtonSensorFusion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DeveloperViewActivity.this,SensorFusionActivity.class);
                startActivity(i);
            }
        });

        imageButtonComm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DeveloperViewActivity.this,CommunicationActivity.class);
                startActivity(i);
            }
        });
    }
}