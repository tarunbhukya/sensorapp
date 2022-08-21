package com.yourselftech.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.TextView;

import java.net.URISyntaxException;

import io.socket.client.Socket;
import io.socket.client.IO;


public class MainActivity extends Activity implements SensorEventListener {

    SensorManager sensorManager;
    Sensor sensorAcc, sensorGra, sensorMag;
    float[] accelerometerData = new float[3];
    float[] accelerometerWorldData = new float[3];
    float[] gravityData = new float[3];
    float[] magneticData = new float[3];
    float[] rotationMatrix = new float[9];
    TextView x,y,z, xa, ya, za;

    static final float NS2S = 1.0f / 1000000000.0f;
    float[] last_values = null;
    float[] velocity = null;
    float[] position = null;
    long last_timestamp = 0;

    private Socket mSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorGra = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorMag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        x = (TextView) findViewById(R.id.x);
        y = (TextView) findViewById(R.id.y);
        z = (TextView) findViewById(R.id.z);
        xa = (TextView) findViewById(R.id.xa);
        ya = (TextView) findViewById(R.id.ya);
        za = (TextView) findViewById(R.id.za);
        try {
            mSocket = IO.socket("http://192.168.1.8:3000");
            mSocket.connect();
        } catch (URISyntaxException e) {
            Log.d("Error", "Cannot connect to socket");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorAcc, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, sensorGra, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, sensorMag, SensorManager.SENSOR_DELAY_UI);
        if(!mSocket.connected()) {
            mSocket.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        mSocket.close();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        int i = sensor.getType();

        if (i == Sensor.TYPE_LINEAR_ACCELERATION) {
            accelerometerData = event.values;
        } else if (i == Sensor.TYPE_GRAVITY) {
            gravityData = event.values;
        } else if (i == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticData = event.values;
        }

        //Calculate rotation matrix from gravity and magnetic sensor data
        SensorManager.getRotationMatrix(rotationMatrix, null, gravityData, magneticData);
        float[] latestData = new float[3];


        //World coordinate system transformation for acceleration
        latestData[0] = rotationMatrix[0] * accelerometerData[0] + rotationMatrix[1] * accelerometerData[1] + rotationMatrix[2] * accelerometerData[2];
        latestData[1] = rotationMatrix[3] * accelerometerData[0] + rotationMatrix[4] * accelerometerData[1] + rotationMatrix[5] * accelerometerData[2];
        latestData[2] = rotationMatrix[6] * accelerometerData[0] + rotationMatrix[7] * accelerometerData[1] + rotationMatrix[8] * accelerometerData[2];

//        Log.d("Tarun", String.valueOf(event.values[0]));
        latestData[0] = accelerometerData[0];
        latestData[1] = accelerometerData[1];
        latestData[2] = accelerometerData[2];


        if(last_values == null) {
            last_values = new float[3];
            velocity = new float[3];
            position = new float[3];
            velocity[0] = velocity[1] = velocity[2] = 0f;
            position[0] = position[1] = position[2] = 0f;
            calculatePosition(latestData, event);
            last_timestamp = event.timestamp;
            accelerometerWorldData = latestData;
        } else if(Math.abs(last_values[0] - latestData[0]) > 0.3) {
            calculatePosition(latestData, event);
            last_timestamp = event.timestamp;
            accelerometerWorldData = latestData;
        }
    }

    private void calculatePosition(float[] values, SensorEvent event) {

        float dt = (event.timestamp - last_timestamp) * NS2S;

        for(int index = 0; index < 3;++index){
            float prevVelocity = velocity[index];
            velocity[index] = (values[index] + last_values[index])/2 * dt;
            position[index] = (prevVelocity + velocity[index])/2 * dt;
        }


        last_values = values;
        x.setText(String.valueOf(position[0]));
        y.setText(String.valueOf(position[1]));
        z.setText(String.valueOf(position[2]));

        xa.setText(String.valueOf(values[0]));
        ya.setText(String.valueOf(values[1]));
        za.setText(String.valueOf(values[2]));

//        if(mSocket.connected()) {
        mSocket.emit("sensor", "" + position[0] + "," +  position[1] + "," + position[2]);
//        }else {
//            Log.d("Tarun", "socket not connected " + mSocket.isActive() + mSocket);
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}