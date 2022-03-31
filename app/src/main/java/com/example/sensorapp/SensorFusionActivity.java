package com.example.sensorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;

public class SensorFusionActivity extends AppCompatActivity {
    private String decision;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_fusion);
        RadioGroup fusion = findViewById(R.id.radioGroupfusion);

        Share FM = new Share();
        decision = FM.getDefaults("FM",this);

        //No Fusion
        if(decision.equals("1")){
            fusion.check(R.id.radioButtonNo);
        }
        else if(decision.equals("2")){
            fusion.check(R.id.radioButtonKalman);
        }

        fusion.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(fusion.getCheckedRadioButtonId() == R.id.radioButtonNo){
                    Share.setDefaults("FM","1",SensorFusionActivity.this);
                }
                else if(fusion.getCheckedRadioButtonId() == R.id.radioButtonKalman){
                    Share.setDefaults("FM","2",SensorFusionActivity.this);
                }

            }
        });
    }
}