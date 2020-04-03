package com.olssonhenrik.hello;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private static final long[] VIBRATOR_PATTERN = {400, 100, 200, 100};

    private TextView compassText;
    private ImageView compassImage;

    private Vibrator vibrator;
    private boolean isVibrating = false;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor rotationV;
    private Sensor magnetometer;

    private boolean hasSensor;
    private boolean hasExtraSensor;

    int azimuth;
    float[] rotationMatrix = new float[9];
    float[] orientation = new float[3];
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        compassText = findViewById(R.id.tv_compass);
        compassImage = findViewById(R.id.iv_compass);

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
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if ((sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null)) {
                Toast.makeText(this, "We can't access any sensors on this phone", Toast.LENGTH_LONG).show();
            } else {

                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                hasSensor = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
                hasExtraSensor = sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        } else {
            rotationV = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            hasSensor = sensorManager.registerListener(this, rotationV, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        if (hasSensor && hasExtraSensor) {
            sensorManager.unregisterListener(this, accelerometer);
            sensorManager.unregisterListener(this, magnetometer);
        } else {
            if (hasSensor) {
                sensorManager.unregisterListener(this, rotationV);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            azimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientation)[0]) + 360) % 360;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            lastMagnetometerSet = true;
        }
        if (lastAccelerometerSet && lastMagnetometerSet) {
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(rotationMatrix, orientation);
            azimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientation)[0]) + 360) % 360;
        }

        azimuth = Math.round(azimuth);
        compassImage.setRotation(-azimuth);

        String where = "NW";

        if ((azimuth >= 345 || azimuth <= 15)) {
            if (!isVibrating) {
                isVibrating = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(VIBRATOR_PATTERN, 0));
                } else {
                    vibrator.vibrate(VIBRATOR_PATTERN, 0);
                }
            }
        } else {
            isVibrating = false;
            vibrator.cancel();
        }

        if (azimuth >= 350 || azimuth <= 10)
            where = "N";
        if (azimuth < 350 && azimuth > 280)
            where = "NW";
        if (azimuth <= 280 && azimuth > 260)
            where = "W";
        if (azimuth <= 260 && azimuth > 190)
            where = "SW";
        if (azimuth <= 190 && azimuth > 170)
            where = "S";
        if (azimuth <= 170 && azimuth > 100)
            where = "SE";
        if (azimuth <= 100 && azimuth > 80)
            where = "E";
        if (azimuth <= 80 && azimuth > 10)
            where = "NE";


        compassText.setText(azimuth + "° " + where);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
