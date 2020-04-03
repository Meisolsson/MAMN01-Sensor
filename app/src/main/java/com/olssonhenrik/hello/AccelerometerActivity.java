package com.olssonhenrik.hello;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener {

    private TextView xText;
    private TextView yText;
    private TextView zText;
    private TextView directionText;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean hasSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        xText = findViewById(R.id.tv_x);
        yText = findViewById(R.id.tv_y);
        zText = findViewById(R.id.tv_z);
        directionText = findViewById(R.id.tv_direction);

        start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    private void start () {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            Toast.makeText(this, "We can't access any sensors on this phone", Toast.LENGTH_LONG).show();
        } else {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            hasSensor = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        if (hasSensor) {
            sensorManager.unregisterListener(this, accelerometer);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        xText.setText("X: " + event.values[0]);
        yText.setText("Y: " + event.values[1]);
        zText.setText("Z: " + event.values[2]);

        double angle = Math.atan2(event.values[0], -event.values[1]);
        String text = "";

        if (angle < 0.75 * Math.PI && angle > 0.25 * Math.PI) {
            text = "VÄNSTER";
        } else if (angle < 0.25 * Math.PI && angle > -0.25 * Math.PI) {
            text = "FRAMÅT";
        } else if (angle < -0.25 * Math.PI && angle > -0.75 * Math.PI) {
            text = "HÖGER";
        } else if (angle < -0.75 * Math.PI || angle > 0.75 * Math.PI) {
            text = "BAKÅT";
        }
        directionText.setText(text);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
