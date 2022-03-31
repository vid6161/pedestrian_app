package com.example.sensorapp;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class GraphViewActivity extends AppCompatActivity implements SensorEventListener {
    private final String TAG = "GraphSensors";
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private com.jjoe64.graphview.GraphView mGraphGyro;
    private com.jjoe64.graphview.GraphView mGraphAccel;
    private com.jjoe64.graphview.GraphView mGraphMag;
    private LineGraphSeries<DataPoint> mSeriesGyroX, mSeriesGyroY, mSeriesGyroZ;
    private LineGraphSeries<DataPoint> mSeriesAccelX, mSeriesAccelY, mSeriesAccelZ;
    private LineGraphSeries<DataPoint> mSeriesMagX, mSeriesMagY, mSeriesMagZ;
    private double graphLastGyroXValue = 5d;
    private double graphLastAccelXValue = 5d;
    private double graphLastMagXValue = 5d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGraphGyro = initGraph(R.id.graphGyro, "Sensor.TYPE_GYROSCOPE");
        mGraphAccel = initGraph(R.id.graphAccel, "Sensor.TYPE_ACCELEROMETER");
        mGraphMag = initGraph(R.id.graphMag, "Sensor.TYPE_MAGNETIC_FIELD");

        //Gyroscope
        mSeriesGyroX = initSeries(Color.BLUE, "X");
        mSeriesGyroY = initSeries(Color.RED, "Y");
        mSeriesGyroZ = initSeries(Color.GREEN, "Z");

        mGraphGyro.addSeries(mSeriesGyroX);
        mGraphGyro.addSeries(mSeriesGyroY);
        mGraphGyro.addSeries(mSeriesGyroZ);

        startGyro();

        // Accelerometer
        mSeriesAccelX = initSeries(Color.BLUE, "X");
        mSeriesAccelY = initSeries(Color.RED, "Y");
        mSeriesAccelZ = initSeries(Color.GREEN, "Z");

        mGraphAccel.addSeries(mSeriesAccelX);
        mGraphAccel.addSeries(mSeriesAccelY);
        mGraphAccel.addSeries(mSeriesAccelZ);

        startAccel();

        // Magnetic
        mSeriesMagX = initSeries(Color.BLUE, "X");
        mSeriesMagY = initSeries(Color.RED, "Y");
        mSeriesMagZ = initSeries(Color.GREEN, "Z");

        mGraphMag.addSeries(mSeriesMagX);
        mGraphMag.addSeries(mSeriesMagY);
        mGraphMag.addSeries(mSeriesMagZ);

        startMag();
    }

    public com.jjoe64.graphview.GraphView initGraph(int id, String title) {
        com.jjoe64.graphview.GraphView graph = (com.jjoe64.graphview.GraphView) findViewById(id);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(5);
        graph.getGridLabelRenderer().setLabelVerticalWidth(100);
        graph.setTitle(title);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        return graph;
    }

    public LineGraphSeries<DataPoint> initSeries(int color, String title) {
        LineGraphSeries<DataPoint> series;
        series = new LineGraphSeries<>();
        series.setDrawDataPoints(true);
        series.setDrawBackground(false);
        series.setColor(color);
        series.setTitle(title);
        return series;
    }

    public void startGyro() {
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void startAccel() {
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void startMag() {
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            graphLastGyroXValue += 0.15d;
            mSeriesGyroX.appendData(new DataPoint(graphLastGyroXValue, event.values[0]), true, 33);
            mSeriesGyroY.appendData(new DataPoint(graphLastGyroXValue, event.values[1]), true, 33);
            mSeriesGyroZ.appendData(new DataPoint(graphLastGyroXValue, event.values[2]), true, 33);
        } else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            graphLastAccelXValue += 0.15d;
            mSeriesAccelX.appendData(new DataPoint(graphLastAccelXValue, event.values[0]), true, 33);
            mSeriesAccelY.appendData(new DataPoint(graphLastAccelXValue, event.values[1]), true, 33);
            mSeriesAccelZ.appendData(new DataPoint(graphLastAccelXValue, event.values[2]), true, 33);
        }else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            graphLastMagXValue += 0.15d;
            mSeriesMagX.appendData(new DataPoint(graphLastMagXValue, event.values[0]), true, 33);
            mSeriesMagY.appendData(new DataPoint(graphLastMagXValue, event.values[1]), true, 33);
            mSeriesMagZ.appendData(new DataPoint(graphLastMagXValue, event.values[2]), true, 33);
        }
        String dataString = String.valueOf(event.accuracy) + "," + String.valueOf(event.timestamp) + "," + String.valueOf(event.sensor.getType()) + "\n";
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


}