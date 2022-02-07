package com.example.sensorapp;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    boolean isPermissionGranted;
    GoogleMap googleMap;
    MapView mapView;
    ImageView imageViewSearch;
    EditText inputLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mapView = findViewById(R.id.mapView);
        imageViewSearch = findViewById(R.id.imageViewSearch);
        inputLocation = findViewById(R.id.inputLocationSearch);

        checkPermission();
        if (isPermissionGranted) {
            if (checkGooglePlayServices()) {
                mapView.getMapAsync(this);
                mapView.onCreate(savedInstanceState);
            } else {
                Toast.makeText(this, "Google Play services Not Available", Toast.LENGTH_SHORT).show();
            }
        }

        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = inputLocation.getText().toString();
                if (location == null) {
                    Toast.makeText(MapActivity.this, "Type any Location", Toast.LENGTH_SHORT).show();
                } else {
                    Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
                    try {
                        List<Address> listAddress = geocoder.getFromLocationName(location, 1);
                        if (listAddress.size() > 0) {
                            LatLng latlng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.title("My Search position");
                            markerOptions.position(latlng);
                            googleMap.addMarker(markerOptions);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 5);
                            googleMap.animateCamera(cameraUpdate);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public boolean havePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApiAvailability.isUserResolvableError(result)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(this, result, 201, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    Toast.makeText(MapActivity.this, "user Canceled Dialog", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        }
        return false;
    }

    private void checkPermission() {

        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                isPermissionGranted = true;
                Toast.makeText(MapActivity.this, "Permission Granted", Toast.LENGTH_SHORT);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                permissionToken.continuePermissionRequest();
            }
        }).check();

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        this.googleMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (googleMap != null) {

            if (item.getItemId() == R.id.noneMap) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
            }
            if (item.getItemId() == R.id.NormalMap) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
            if (item.getItemId() == R.id.SatelliteMap) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
            if (item.getItemId() == R.id.MapHybrid) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
            if (item.getItemId() == R.id.MapTerrain) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            mapView.onStart();
        } catch (Exception e) {
            Toast.makeText(this, "No Permission Granted for precise location",
                    Toast.LENGTH_LONG).show();
            Intent x = new Intent(MapActivity.this, SensorInput.class);
            startActivity(x);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mapView.onResume();
        } catch (Exception e) {

            Intent x = new Intent(MapActivity.this, SensorInput.class);
            startActivity(x);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            mapView.onPause();
        } catch (Exception e) {
            Intent x = new Intent(MapActivity.this, SensorInput.class);
            startActivity(x);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            mapView.onStop();
        } catch (Exception e) {
            Intent x = new Intent(MapActivity.this, SensorInput.class);
            startActivity(x);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mapView.onDestroy();
        } catch (Exception e) {
            Intent x = new Intent(MapActivity.this, SensorInput.class);
            startActivity(x);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        try {
            mapView.onLowMemory();
        } catch (Exception e) {
            Intent x = new Intent(MapActivity.this, SensorInput.class);
            startActivity(x);
        }
    }
}