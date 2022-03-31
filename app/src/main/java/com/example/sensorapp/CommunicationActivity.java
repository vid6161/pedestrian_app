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
        EditText delayValue = findViewById(R.id.editTextDelay);
        delayValue.setText(String.valueOf(1000/Integer.parseInt(Share.getDelay("delay",this))));

        //Communication frequency
        setDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((1000/Integer.parseInt(String.valueOf(delayValue.getText())))<100)){
                    Toast.makeText(CommunicationActivity.this, "Invalid Value", Toast.LENGTH_SHORT).show();
                }
                else if(((1000/Integer.parseInt(String.valueOf(delayValue.getText())))>1000)){
                    Toast.makeText(CommunicationActivity.this, "Invalid Value", Toast.LENGTH_SHORT).show();
                }
                else{
                    Share.setDelay("delay", String.valueOf(1000/Integer.parseInt(String.valueOf(delayValue.getText()))), CommunicationActivity.this);
                }
            }
        });
    }
}