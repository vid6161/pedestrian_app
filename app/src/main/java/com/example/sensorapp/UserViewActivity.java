package com.example.sensorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.Transliterator;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class UserViewActivity extends AppCompatActivity {

    int myValue;
    int decision;
    int delay;

    ImageButton imageButtonDeveloper;
    ToggleButton toggleButton;

    Button btnSend;
    EditText txtIP;
    TextView textView;
    public static String wifiModuleIp = "";
    public static int wifiModulePort = 0;
    private final Handler handler = new Handler();
    public byte[] buf = ("GOIT").getBytes();

    private LocationRequest request;
    private LocationSettingsRequest.Builder builder;
    private final int REQUEST_CODE = 8990;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermission = true;

    public String locationvalue;
    public double latitude = 0;
    public double longitude = 0;
    public float speed = 0;
    public float bearing = 0;

    TextView editTextIpUser;
    TextView editTextPortUser;

    public boolean isFirsttime=true;
    public Socket_AsyncTask senddata;

    //KALMAN START
    private LocationManager locationManager;
    private LocationListener locationListener;
    private SensorManager sensorManager;
    // Sensors
    private Sensor accelerationSensor;
    private Sensor gravitySensor;
    private Sensor mangeticSensor;

    // Listeners
    private UserViewActivity.AccelerationSensorListener accelerationListener;
    private UserViewActivity.GravitySensorListener gravitySensorListener;
    private UserViewActivity.MagneticSensorListener magneticSensorListener;

    /* in radians*/
    private double lat;
    private double lng;

    /* in meters*/
    private double x;
    private double y;

    private double xV;
    private double yV;
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

    /* interval of GPS reading in ms 4000*/
    private final int interval = 1000;

    /* sampling interval of SENSOR_DELAY_GAME is 20ms */
    private final double sensorSamplingInterval = 0.1;

    private final float nanosecond = 1.0f / 1000000000.0f;

    private final Handler handlerKalman = new Handler();
    double u1,u2;
    //KALMAN END
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view);
        //TextView textViewValue = findViewById(R.id.textViewValue);
        //Button batton = findViewById(R.id.buttonClick);

        //Database start
        MyDatabaseHelper myDB = new MyDatabaseHelper(UserViewActivity.this);
        cursor = myDB.readAllData();
        cursor.moveToPosition(0);
        //Database end

        Share sa = new Share();
        delay = Integer.parseInt(Share.getDelay("delay",this));

        String matt = Share.getDefaults("VIP",this);
        if(matt.equals("1")){
            decision= 1;
        }
        else if(matt.equals("2")) {
            decision=2;
        }

        //batton.setOnClickListener(new View.OnClickListener() {
        //    @Override
         //   public void onClick(View v) {
         //       textViewValue.setText(String.valueOf(decision));
         //   }
        //});

        imageButtonDeveloper = findViewById(R.id.imageButtonDeveloper);
        toggleButton = findViewById(R.id.toggleButton);

        Log.d("f", String.valueOf(matt));

        editTextIpUser = findViewById(R.id.editTextIpUser);
        editTextPortUser = findViewById(R.id.editTextPortUser);

        imageButtonDeveloper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(senddata!=null ) {
                    senddata.cancel(true);
                }
                Intent i = new Intent(UserViewActivity.this, DeveloperViewActivity.class);
                startActivity(i);
                //finish();
            }
        });
        //getIPandPort();

        toggleButton.setText(null);
        toggleButton.setTextOn(null);
        toggleButton.setTextOff(null);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String check1 =editTextIpUser.getText().toString();
                String check2 =editTextPortUser.getText().toString();
                if(check1.matches("")){
                    Toast.makeText(UserViewActivity.this, "Enter a valid IP", Toast.LENGTH_SHORT).show();
                }
                else if(check2.matches("")){
                    Toast.makeText(UserViewActivity.this, "Enter a valid Port", Toast.LENGTH_SHORT).show();
                }

                else {
                    getIPandPort();
                    if (toggleButton.isChecked()) {
                        senddata = new Socket_AsyncTask();
                        senddata.execute();
                    } else {
                        senddata.cancel(true);
                    }
                }
            }
        });

        //KALMAN START
        /* assume stationary when app opens*/
        xV = 0D;
        yV = 0D;
        xA = 0D;
        yA = 0D;

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        request = LocationRequest.create().setFastestInterval(1000)
                .setInterval(1000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        builder = new LocationSettingsRequest.Builder().addLocationRequest(request);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(UserViewActivity.this, REQUEST_CODE);
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
                    if(decision==1) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        speed = location.getSpeed();
                        bearing = location.getBearing();
                    }
                    else if(decision==2) {
                        lng = Math.toRadians(location.getLongitude());
                        lat = Math.toRadians(location.getLatitude());
                        speed = location.getSpeed();
                        bearing = location.getBearing();
                        getXY(lat, lng);
                    }
                }
            }
        };

        //end
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        /* Initialize Accelerometer */
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        accelerationListener = new UserViewActivity.AccelerationSensorListener();

        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        gravitySensorListener = new UserViewActivity.GravitySensorListener();

        mangeticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magneticSensorListener = new UserViewActivity.MagneticSensorListener();

        sensorManager.registerListener(accelerationListener, accelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(gravitySensorListener, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(magneticSensorListener, mangeticSensor, SensorManager.SENSOR_DELAY_NORMAL);

        if(decision==2){
            onClick_StartTracking();
        }
        //KALMAN END
    }
    //EXIT & INFO BUTTON
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.exit){
            AlertDialog.Builder builder = new AlertDialog.Builder(UserViewActivity.this);
            builder.setMessage("Do You Want To Exit?");
            builder.setCancelable(true);
            builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            builder.setPositiveButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        if(id==R.id.info){
            AlertDialog.Builder builder = new AlertDialog.Builder(UserViewActivity.this);
            builder.setTitle("Information");
            builder.setMessage("This is an app for collecting the Vru sensor data." +
                    "Please press the ON/OFF Button for sending the data."+
                    "\nSettings Button: Sensor Data, Sensor Fusion, Communication"+
                    "\nSensor Data: Smartphone sensor data and visualization"+
                    "\nSensor Fusion: Sensor fusion method selection"+
                    "\nCommunication: Communication delay and IP & Port selection");
            builder.setCancelable(true);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        return true;
    }
    //EXIT & INFO BUTTON

    public void getIPandPort() {
        //String iPandPort = editTextIpPortUser.getText().toString();
        //String temp[] = iPandPort.split(":");
        //wifiModuleIp = temp[0];
        //wifiModulePort = Integer.valueOf(temp[1]);
        wifiModuleIp = editTextIpUser.getText().toString();
        wifiModulePort = Integer.valueOf(editTextPortUser.getText().toString());
    }

    public class Socket_AsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                DatagramSocket udpSocket = new DatagramSocket(null);
                udpSocket.setReuseAddress(true);
                udpSocket.bind(new InetSocketAddress(wifiModulePort));
                InetAddress serverAddr = InetAddress.getByName(wifiModuleIp);
                //Log.d("x", String.valueOf(isCancelled()));
                while (!isCancelled()) {
                    byte[] buf1 = getbuf();
                    DatagramPacket packet = new DatagramPacket(buf1, buf1.length, serverAddr, wifiModulePort);
                    udpSocket.send(packet);
                    Thread.sleep(delay);
                }
            } catch (IOException | InterruptedException socketException) {
                socketException.printStackTrace();
            }
            return null;
        }
    }
    private byte[] getbuf() {
        Person person;
        if(cursor.getCount()!=0) {
            person = Person.newBuilder().setName(cursor.getString(1))
                    .setAge(Integer.parseInt(cursor.getString(2)))
                    .setGender(cursor.getString(3))
                    .setHeight(Integer.parseInt(cursor.getString(4)))
                    .setWeight(Integer.parseInt(cursor.getString(5))).build();
        }
        else{
            person = Person.newBuilder().setName("Null").setAge(0).setGender("Null").setHeight(0).setWeight(0).build();
        }
        Position position = Position.newBuilder().setLatitude(latitude).setLongitude(longitude).build();
        SensorData sensor = SensorData.newBuilder().setPerson(person).setPosition(position).setSpeed(speed).setHeading(bearing).build();
        sensor.toByteArray();
        return sensor.toByteArray();
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(UserViewActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, getMainLooper());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startLocationUpdates();
        startGPS();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
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
                    //finish();
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

    //KALMAN START

    public void onClick_StartTracking() {

        /* Initialize Kalman Filter */
        KF = new KalmanFilter(new UserViewActivity.MyProcessModel(), new UserViewActivity.MyMeasurementModel());

        handlerKalman.postDelayed(new Runnable() {
            @Override
            public synchronized void run() {

                double[] measuredState = new double[]{x, y, xV, yV};
                KF.correct(measuredState);
                KF.predict();
                double[] stateEstimate = KF.getStateEstimation();

                Double longitudecorrection= (2*Math.PI)/40075160;
                Double latitudecorrection= (2*Math.PI)/40008000;
                longitude = Math.toDegrees(stateEstimate[0]*longitudecorrection);
                latitude = Math.toDegrees(stateEstimate[1]*latitudecorrection);
                handlerKalman.postDelayed(this, interval);
            }
        }, 0);
    }

    /* uses radian */
    private synchronized void getXY(double lat, double lng) {
        double deltaLat = lat - yOrigin;
        double deltaLng = lng - xOrigin;

        double latCircumference = 40075160 * Math.cos(yOrigin);
        x = deltaLng * latCircumference / 2 / Math.PI;
        y = deltaLat * 40008000 / 2 / Math.PI;
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
            acceleration[0] += 0.20;//original
            acceleration[1] -= 0.08;

            phoneAcceleration = MatrixUtils.createRealVector(acceleration);

            if (!(gravity == null || geomagnet == null)) {
                SensorManager.getRotationMatrix(rotationMatrix, inclineMatrix, gravity, geomagnet);

                R = MatrixUtils.createRealMatrix(resize3by3(castFloatToDouble(rotationMatrix)));
                I = MatrixUtils.createRealMatrix(resize3by3(castFloatToDouble(inclineMatrix)));

                phoneAcceleration = R.preMultiply(phoneAcceleration);
                phoneAcceleration = I.preMultiply(phoneAcceleration);


                double newXA = 0.8 * xA + 0.2 * phoneAcceleration.getEntry(0);//original
                double newYA = 0.8 * yA + 0.2 * phoneAcceleration.getEntry(1);

                u1= newXA;
                u2= newYA;

                if (timestamp != 0) {
                    dT = (sensorEvent.timestamp - timestamp) * nanosecond;

                    xV += (xA + newXA) * dT / 2;//original
                    yV += (yA + newYA) * dT / 2;

                }

                timestamp = sensorEvent.timestamp;

                xA = newXA;
                yA = newYA;

                if (Math.abs(xV) > 2.0) xV *= 0.6;//original
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
            double[] control = {0, 0, 0, 0};//original
            return MatrixUtils.createRealDiagonalMatrix(control);
        }

        @Override
        public RealMatrix getInitialErrorCovariance() {
            double[] diagon = {0.01, 0.01, 1, 1};
            return MatrixUtils.createRealDiagonalMatrix(diagon);
        }

        @Override
        public RealMatrix getProcessNoise() {
            // assume no process noise first
            double[] processNoise = { 0.001,0.001,5,5};//original
            return MatrixUtils.createRealDiagonalMatrix(processNoise);
        }

        @Override
        public RealMatrix getStateTransitionMatrix() {
            /* assume constant velocity during interval*/
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
            double[] measurementNoise = {0.2, 0.2, 0.5, 0.5};//original
            return MatrixUtils.createRealDiagonalMatrix(measurementNoise);
        }
    }
}