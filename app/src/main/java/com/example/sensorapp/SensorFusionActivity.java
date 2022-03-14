package com.example.sensorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;

public class SensorFusionActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String VALUE = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_fusion);
        RadioGroup fusion = findViewById(R.id.radioGroupfusion);

        Share sap = new Share();
        String mxx = sap.getDefaults("VIP",this);

        if(mxx.equals("1")){
            fusion.check(R.id.radioButtonNo);
        }
        else if(mxx.equals("2")){
            fusion.check(R.id.radioButtonKalman);
        }
        fusion.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(fusion.getCheckedRadioButtonId() == R.id.radioButtonNo){
                    Share.setDefaults("VIP","1",SensorFusionActivity.this);
                }
                else if(fusion.getCheckedRadioButtonId() == R.id.radioButtonKalman){
                    Share.setDefaults("VIP","2",SensorFusionActivity.this);
                }

            }
        });
    }
}