package com.example.sensorapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class UserInputActivity extends AppCompatActivity {

    ImageButton imageButtonNextUserInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_input);

        ImageButton skip = findViewById(R.id.imageButtonSkip);
        EditText name = findViewById(R.id.editTextUserName);
        EditText age = findViewById(R.id.editTextUserAge);
        RadioGroup gender = findViewById(R.id.radioGroupGender);
        EditText height = findViewById(R.id.editTextUserHeight);
        EditText weight = findViewById(R.id.editTextUserWeight);

        MyDatabaseHelper myDB = new MyDatabaseHelper(UserInputActivity.this);

        imageButtonNextUserInput = findViewById(R.id.imageButtonNextUserInput);
        imageButtonNextUserInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(name.getText()) || TextUtils.isEmpty(age.getText()) || TextUtils.isEmpty(height.getText()) || TextUtils.isEmpty(weight.getText()) || (gender.getCheckedRadioButtonId() == -1)) {
                    Toast.makeText(UserInputActivity.this, "No Empty field Allowed", Toast.LENGTH_SHORT).show();
                }
                else if(Integer.valueOf(String.valueOf(age.getText()))>100||Integer.valueOf(String.valueOf(age.getText()))<0){
                    Toast.makeText(UserInputActivity.this, "Enter age(0-100)", Toast.LENGTH_SHORT).show();
                }
                else if(Integer.valueOf(String.valueOf(height.getText()))>250||Integer.valueOf(String.valueOf(height.getText()))<0){
                    Toast.makeText(UserInputActivity.this, "Enter height(0-250)", Toast.LENGTH_SHORT).show();
                }
                else if(Integer.valueOf(String.valueOf(weight.getText()))>100||Integer.valueOf(String.valueOf(height.getText()))<0){
                    Toast.makeText(UserInputActivity.this, "Enter weight(0-100)", Toast.LENGTH_SHORT).show();
                }
                else {

                    String radio;
                    if (gender.getCheckedRadioButtonId() == R.id.radioButton1) {
                        radio = "Male";
                    } else if (gender.getCheckedRadioButtonId() == R.id.radioButton2) {
                        radio = "Female";
                    } else {
                        radio = "Other";
                    }
                    Cursor cursor = myDB.readAllData();
                    cursor.moveToPosition(0);

                    AlertDialog.Builder builder = new AlertDialog.Builder(UserInputActivity.this);
                    builder.setTitle("Do you want to save this user?");
                    builder.setMessage("Name: "+ name.getText()+"\n"+
                            "Age: "+ age.getText()+"\n"+
                            "Gender: "+radio+"\n"+
                            "Height: "+height.getText()+"\n"+
                            "Weight: "+weight.getText());
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (cursor.getCount() == 0) {
                                myDB.addData(name.getText().toString().trim(), Integer.valueOf(age.getText().toString().trim()), radio.trim(),
                                        Integer.valueOf(height.getText().toString().trim()), Integer.valueOf(weight.getText().toString().trim()));
                                Intent i = new Intent(UserInputActivity.this, ConfirmMessageActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                myDB.UpdateData("1", name.getText().toString().trim(), Integer.valueOf(age.getText().toString().trim()), radio.trim(),
                                        Integer.valueOf(height.getText().toString().trim()), Integer.valueOf(weight.getText().toString().trim()));
                                Intent i = new Intent(UserInputActivity.this, ConfirmMessageActivity.class);
                                startActivity(i);
                                finish();
                            }
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UserInputActivity.this,UserViewActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}