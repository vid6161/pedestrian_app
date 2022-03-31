package com.example.sensorapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    ImageButton imageButtonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageButtonStart = findViewById(R.id.imageButtonStart);
        TextView textViewWelcome = findViewById(R.id.textViewWelcome);

        MyDatabaseHelper myDB = new MyDatabaseHelper(MainActivity.this);
        Cursor cursor = myDB.readAllData();
        cursor.moveToPosition(0);

        //Welcome text
        textViewWelcome.setText("Welcome!");
        TextPaint paint = textViewWelcome.getPaint();
        float width = paint.measureText("Welcome!");
        Shader textShader = new LinearGradient(0, 0, width, textViewWelcome.getTextSize(),
                new int[]{
                        Color.parseColor("#F97C3C"),
                        Color.parseColor("#FDB54E"),
                        Color.parseColor("#64B678"),
                        Color.parseColor("#478AEA"),
                        Color.parseColor("#8446CC"),
                }, null, Shader.TileMode.CLAMP);
        textViewWelcome.getPaint().setShader(textShader);
        textViewWelcome.setTextColor(Color.parseColor("#F97C3C"));

        //Start button
        imageButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cursor.getCount()==0) {
                    Intent i = new Intent(MainActivity.this, UserInputActivity.class);
                    startActivity(i);
                    finish();
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Are you this user?");
                    builder.setMessage("Name:"+cursor.getString(1)+
                            "\nAge: "+cursor.getString(2)+
                            "\nGender: "+cursor.getString(3)+
                            "\nHeight: "+cursor.getString(4)+
                            "\nWeight: "+cursor.getString(5));
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(MainActivity.this, UserViewActivity.class);
                            startActivity(i);
                            finish();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(MainActivity.this, UserInputActivity.class);
                            startActivity(i);
                            finish();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
    }
}