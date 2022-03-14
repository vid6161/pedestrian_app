package com.example.sensorapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

public class SensorInputActivity extends AppCompatActivity implements SensorEventListener {
    private TextView textview_accelerometer, textview_gyroscope, textview_location, textview_speed, textview_magnetic;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor, gyrometerSensor, magnetometerSensor;
    private boolean isAccelerometerAvailable, isGyrometerAvailable, isMagnetometerAvailable;
    private final String[] accelerometerChangedValue = new String[3];
    private final String[] gyrometerChangedValue = new String[3];
    private final String[] magnetometerChangedValue = new String[3];

    private ImageButton imageButton_accelerometer, imageButton_gyroscope, imageButton_location, imageButton_speed, imageButton_magnetic;
    private LocationRequest request;
    private LocationSettingsRequest.Builder builder;
    private final int REQUEST_CODE = 8990;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermission = true;

    long lastUpdateAc = 0;
    long lastUpdateGy = 0;
    long lastUpdateMg = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_input);
        initialWork();
        getGPS();
        buttonActions();
    }

    private void buttonActions() {
        //open google maps
        imageButton_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapView = new Intent(SensorInputActivity.this, MapActivity.class);
                startActivity(mapView);
            }
        });

        //open graph
        imageButton_accelerometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent graphview = new Intent(SensorInputActivity.this, GraphViewActivity.class);
                startActivity(graphview);
            }
        });

        imageButton_gyroscope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent graphview = new Intent(SensorInputActivity.this, GraphViewActivity.class);
                startActivity(graphview);
            }
        });

        imageButton_magnetic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent graphview = new Intent(SensorInputActivity.this, GraphViewActivity.class);
                startActivity(graphview);
            }
        });

        //open speedometer
        imageButton_speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent speedometer = new Intent(SensorInputActivity.this, SpeedoMeterActivity.class);
                startActivity(speedometer);
            }
        });

    }

    private void getGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        request = LocationRequest.create().setFastestInterval(300)
                .setInterval(300)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        builder = new LocationSettingsRequest.Builder().addLocationRequest(request);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(SensorInputActivity.this, REQUEST_CODE);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }
                }
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    textview_accelerometer.setText("Null");
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    textview_location.setText(String.valueOf("Longitude:   " + location.getLongitude() + "\nLatitude:   " + location.getLatitude() + "\nAccuracy:   " + location.getAccuracy() + " m"));
                    textview_speed.setText(String.valueOf("Speed:   " + location.getSpeed() + " m/s"));
                }
            }
        };
    }

    private void initialWork() {
        textview_accelerometer = findViewById(R.id.textview_accelerometer);
        textview_gyroscope = findViewById(R.id.textview_gyroscope);
        textview_location = findViewById(R.id.textview_location);
        textview_speed = findViewById(R.id.textview_speed);
        textview_magnetic = findViewById(R.id.textview_magnetic);

        imageButton_accelerometer = findViewById(R.id.button_accelerometer);
        imageButton_gyroscope = findViewById(R.id.button_gyroscope);
        imageButton_location = findViewById(R.id.button_location);
        imageButton_speed = findViewById(R.id.button_speed);
        imageButton_magnetic = findViewById(R.id.button_magnetic);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        setSensor(sensorManager);
    }

    private void setSensor(SensorManager sensorManager) {

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            isAccelerometerAvailable = true;
        } else {
            textview_accelerometer.setText("Sensor not available");
            isAccelerometerAvailable = false;
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyrometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            isGyrometerAvailable = true;
        } else {
            textview_accelerometer.setText("Sensor not available");
            isGyrometerAvailable = false;
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            isMagnetometerAvailable = true;
        } else {
            textview_accelerometer.setText("Sensor not available");
            isMagnetometerAvailable = false;
        }

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long actualTime = sensorEvent.timestamp;
        if (sensorEvent.sensor == accelerometerSensor) {

            //get the event's timestamp
            if (actualTime - lastUpdateAc > 500000000) {
                accelerometerChangedValue[0] = String.valueOf(sensorEvent.values[0]);
                accelerometerChangedValue[1] = String.valueOf(sensorEvent.values[1]);
                accelerometerChangedValue[2] = String.valueOf(sensorEvent.values[2]);
                String accelerometer = "x:  " + accelerometerChangedValue[0] + " m/s²\n" +
                        "y:  " + accelerometerChangedValue[1] + " m/s²\n" +
                        "z:  " + accelerometerChangedValue[2] + " m/s²";

                textview_accelerometer.setText(accelerometer);
                lastUpdateAc = actualTime;
            }} else if (sensorEvent.sensor == gyrometerSensor && actualTime - lastUpdateGy > 200000000 ) {
            gyrometerChangedValue[0] = String.valueOf(sensorEvent.values[0]);
            gyrometerChangedValue[1] = String.valueOf(sensorEvent.values[1]);
            gyrometerChangedValue[2] = String.valueOf(sensorEvent.values[2]);
            String gyrometer = "x:    " + gyrometerChangedValue[0] + " rad/s\n" +
                    "y:    " + gyrometerChangedValue[1] + " rad/s\n" +
                    "z:    " + gyrometerChangedValue[2] + " rad/s";

            textview_gyroscope.setText(gyrometer);
            lastUpdateGy =actualTime;
        } else if (sensorEvent.sensor == magnetometerSensor && actualTime - lastUpdateMg > 400000000) {
            magnetometerChangedValue[0] = String.valueOf(sensorEvent.values[0]);
            magnetometerChangedValue[1] = String.valueOf(sensorEvent.values[1]);
            magnetometerChangedValue[2] = String.valueOf(sensorEvent.values[2]);
            String magnetometer = "x:    " + magnetometerChangedValue[0] + " μT\n" +
                    "y:    " + magnetometerChangedValue[1] + " μT\n" +
                    "z:    " + magnetometerChangedValue[2] + " μT";

            textview_magnetic.setText(magnetometer);
            lastUpdateMg =actualTime;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(SensorInputActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startGPS();

        if (isAccelerometerAvailable)
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        if (isGyrometerAvailable)
            sensorManager.registerListener(this, gyrometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        if (isMagnetometerAvailable)
            sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        if (isAccelerometerAvailable)
            sensorManager.unregisterListener(this, accelerometerSensor);

        if (isGyrometerAvailable)
            sensorManager.unregisterListener(this, gyrometerSensor);

        if (isMagnetometerAvailable)
            sensorManager.unregisterListener(this, magnetometerSensor);

    }

    public void stopGPS() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "GPS permission not granted", Toast.LENGTH_SHORT).show();
                    stopGPS();
                }
        }
    }

    public void startGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, getMainLooper());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLocationUpdates();
    }
}
