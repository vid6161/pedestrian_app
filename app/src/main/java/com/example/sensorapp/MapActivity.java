package com.example.sensorapp;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.androidplot.xy.EditableXYSeries;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
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
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;

public class MapActivity extends AppCompatActivity  {

    private LocationRequest request;
    private LocationSettingsRequest.Builder builder;
    private final int REQUEST_CODE = 8990;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;

    double lng, lat;

    /* in meters*/

    //position
    private double x;
    private double y;

    //velocity
    private double xV;
    private double yV;

    //acceleration
    private double xA;
    private double yA;

    private double xOrigin;
    private double yOrigin;

    private double timestamp = 0;

    private float[] gravity;
    private float[] geomagnet;
    private double[] acceleration;

    /* Kalman filter to correct GPS readings with acceleration */
    private KalmanFilter KF;

    /* interval of GPS reading in ms */
    private final int interval = 1000;
    private final float nanosecond = 1.0f / 1000000000.0f;
    private final Handler handlerKalman = new Handler();

    private XYPlot plot;
    private EditableXYSeries correctedSeries;
    private List<Double> correctedXVals;
    private List<Double> correctedYVals;
    private SensorManager sensorManager;

    // Sensors
    private Sensor accelerationSensor;
    private Sensor gravitySensor;
    private Sensor mangeticSensor;

    // Listeners
    private AccelerationSensorListener accelerationListener;
    private GravitySensorListener gravitySensorListener;
    private MagneticSensorListener magneticSensorListener;

    private boolean isOrigin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        /* Initialize Graph Plotter*/
        plot = (XYPlot) findViewById(R.id.plot);

        correctedXVals = new ArrayList<>();
        correctedYVals = new ArrayList<>();

        correctedSeries = new SimpleXYSeries(correctedXVals, correctedYVals, "");
        plot.addSeries(correctedSeries, new LineAndPointFormatter(null, Color.BLUE, null, null));

        ImageButton set = findViewById(R.id.imageButtonStartTracking);
        ImageButton origin = findViewById(R.id.imageButtonSetOrigin);
        ImageButton stop = findViewById(R.id.imageButtonStop);

        origin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick_SetOrigin();
                isOrigin = true;
            }
        });

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOrigin) {
                    onClick_StartTracking();
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });

        getGPS();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        accelerationListener = new AccelerationSensorListener();

        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        gravitySensorListener = new GravitySensorListener();

        mangeticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magneticSensorListener = new MagneticSensorListener();

        sensorManager.registerListener(accelerationListener, accelerationSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(gravitySensorListener, gravitySensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(magneticSensorListener, mangeticSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    private void getGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        request = LocationRequest.create().setFastestInterval(0)
                .setInterval(0)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        builder = new LocationSettingsRequest.Builder().addLocationRequest(request);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapActivity.this, REQUEST_CODE);
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
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    lng = Math.toRadians(location.getLongitude());
                    lat = Math.toRadians(location.getLatitude());
                    getXY(lat, lng);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startGPS();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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

    public void onClick_StartTracking() {

        /* Initialize Kalman Filter */
        KF = new KalmanFilter(new MapActivity.MyProcessModel(), new MapActivity.MyMeasurementModel());

        handlerKalman.postDelayed(new Runnable() {
            @Override
            public synchronized void run() {
                Double correction = (2 * Math.PI) / 40075170;
                double[] measuredState = new double[]{x, y, xV, yV};
                KF.correct(measuredState);
                KF.predict();
                double[] stateEstimate = KF.getStateEstimation();
                correctedXVals.add(Math.toDegrees(stateEstimate[0] * correction));
                correctedYVals.add(Math.toDegrees(stateEstimate[1] * correction));
                redrawSeries();
                handlerKalman.postDelayed(this, interval);
            }
        }, 0);
    }

    public void redrawSeries() {
        plot.removeSeries(correctedSeries);
        correctedSeries = new SimpleXYSeries(correctedXVals, correctedYVals, "");
        plot.addSeries(correctedSeries, new LineAndPointFormatter(Color.BLUE, Color.BLUE, null, null));
        plot.redraw();
    }

    private synchronized void getXY(double lat, double lng) {
        double deltaLat = lat - yOrigin;
        double deltaLng = lng - xOrigin;

        x = deltaLng * 40075170 / 2 / Math.PI;
        y = deltaLat * 40075170 / 2 / Math.PI;
    }

    public void onClick_SetOrigin() {
        xOrigin = lng;
        yOrigin = lat;
    }

    private class AccelerationSensorListener implements SensorEventListener {
        float[] rotationMatrix = new float[9];
        float[] inclineMatrix = new float[9];
        RealVector phoneAcceleration;
        RealMatrix R;
        RealMatrix I;

        double dT;

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

        @Override
        public synchronized void onSensorChanged(SensorEvent sensorEvent) {

            acceleration = castFloatToDouble(sensorEvent.values);
            acceleration[0] += 0.20;
            acceleration[1] -= 0.08;

            phoneAcceleration = MatrixUtils.createRealVector(acceleration);

            if (!(gravity == null || geomagnet == null)) {
                SensorManager.getRotationMatrix(rotationMatrix, inclineMatrix, gravity, geomagnet);

                R = MatrixUtils.createRealMatrix(resize3by3(castFloatToDouble(rotationMatrix)));
                I = MatrixUtils.createRealMatrix(resize3by3(castFloatToDouble(inclineMatrix)));

                phoneAcceleration = R.preMultiply(phoneAcceleration);
                phoneAcceleration = I.preMultiply(phoneAcceleration);

                double newXA = 0.8 * xA + 0.2 * phoneAcceleration.getEntry(0);
                double newYA = 0.8 * yA + 0.2 * phoneAcceleration.getEntry(1);

                if (timestamp != 0) {
                    dT = (sensorEvent.timestamp - timestamp) * nanosecond;
                    xV += (xA + newXA) * dT / 2;
                    yV += (yA + newYA) * dT / 2;
                }

                timestamp = sensorEvent.timestamp;

                xA = newXA;
                yA = newYA;

                if (Math.abs(xV) > 2.0) xV *= 0.6;
                if (Math.abs(yV) > 2.0) yV *= 0.6;

            }
        }

        private double[][] resize3by3(double[] input) {
            if (input.length != 9) return null;

            double[][] result = new double[3][3];
            int index = 0;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    result[i][j] = input[index++];
                }
            }
            return result;
        }

        private double[] castFloatToDouble(float[] input) {
            double[] result = new double[input.length];
            int i = 0;
            for (float f : input) {
                result[i++] = (double) f;
            }
            return result;
        }
    }

    private class GravitySensorListener implements SensorEventListener {
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        @Override
        public synchronized void onSensorChanged(SensorEvent sensorEvent) {
            gravity = sensorEvent.values;
        }
    }

    private class MagneticSensorListener implements SensorEventListener {
        @Override
        public synchronized void onSensorChanged(SensorEvent sensorEvent) {
            geomagnet = sensorEvent.values;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    }

    private class MyProcessModel implements ProcessModel {

        @Override
        public RealMatrix getControlMatrix() {
            double[] control = {0, 0, 0, 0};
            return MatrixUtils.createRealDiagonalMatrix(control);
        }

        @Override
        public RealMatrix getInitialErrorCovariance() {
            double[] diagon = {0.6, 0.6, 5.4, 5.4};
            return MatrixUtils.createRealDiagonalMatrix(diagon);
        }

        @Override
        public RealMatrix getProcessNoise() {
            double[] processNoise = { 0.001,0.001,5,5};
            return MatrixUtils.createRealDiagonalMatrix(processNoise);
        }

        @Override
        public RealMatrix getStateTransitionMatrix() {
            double[][] stateTransitionMatrixData = {
                    {1, 0, interval / 1000.0, 0},
                    {0, 1, 0, interval / 1000.0},
                    {0, 0, 1, 0},
                    {0, 0, 0, 1}
            };

            return MatrixUtils.createRealMatrix(stateTransitionMatrixData);
        }

        @Override
        public synchronized RealVector getInitialStateEstimate() {
            double[] initialStateEstimate = {x, y, xV, yV};
            return MatrixUtils.createRealVector(initialStateEstimate);
        }
    }

    private class MyMeasurementModel implements MeasurementModel {
        @Override
        public RealMatrix getMeasurementMatrix() {
            return MatrixUtils.createRealIdentityMatrix(4);
        }

        @Override
        public RealMatrix getMeasurementNoise() {
            double[] measurementNoise = {0.2, 0.2, 0.5, 0.5};
            return MatrixUtils.createRealDiagonalMatrix(measurementNoise);
        }
    }
}