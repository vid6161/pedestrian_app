package com.example.sensorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class ConfirmMessageActivity extends AppCompatActivity {

    ImageButton imageButtonNextConfirmMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_message);

        imageButtonNextConfirmMessage = findViewById(R.id.imageButtonNextConfirmMessage);
        imageButtonNextConfirmMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ConfirmMessageActivity.this, UserViewActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}