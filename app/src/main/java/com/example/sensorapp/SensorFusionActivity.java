package com.example.sensorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;

public class SensorFusionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_fusion);
        RadioGroup fusion = findViewById(R.id.radioGroupfusion);

        Share sap = new Share();
        String decision = sap.getDefaults("decision",this);

        //No Fusion
        if(decision.equals("1")){
            fusion.check(R.id.radioButtonNo);
        }

        //Kalman Filter
        else if(decision.equals("2")){
            fusion.check(R.id.radioButtonKalman);
        }

        fusion.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(fusion.getCheckedRadioButtonId() == R.id.radioButtonNo){
                    Share.setDefaults("decision","1",SensorFusionActivity.this);
                }
                else if(fusion.getCheckedRadioButtonId() == R.id.radioButtonKalman){
                    Share.setDefaults("decision","2",SensorFusionActivity.this);
                }

            }
        });
    }
}