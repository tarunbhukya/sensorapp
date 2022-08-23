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
    TextView x,y,z, xa, ya, za;
    private Socket mSocket;
    SensorAlgoOne algo_one;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGra = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorMag = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        algo_one = new SensorAlgoOne();
        x = (TextView) findViewById(R.id.x);
        y = (TextView) findViewById(R.id.y);
        z = (TextView) findViewById(R.id.z);
        xa = (TextView) findViewById(R.id.xa);
        ya = (TextView) findViewById(R.id.ya);
        za = (TextView) findViewById(R.id.za);
        try {
            mSocket = IO.socket("http://192.168.0.167:3000");
            mSocket.connect();
        } catch (URISyntaxException e) {
            Log.d("Error", "Cannot connect to socket");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorAcc, SensorManager.SENSOR_DELAY_GAME);
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
       float[] position = algo_one.getPosition(event);
       x.setText(String.valueOf(position[0]));
       y.setText(String.valueOf(position[1]));
       z.setText(String.valueOf(position[2]));


       String sensor_position_text = String.valueOf(position[0]) + "," + String.valueOf(position[1]) + "," + String.valueOf(position[2]);
       String sensor_acc_text = String.valueOf(event.values[0]) + "," + String.valueOf(event.values[1]) + "," + String.valueOf(event.values[2]);

       mSocket.emit("sensor", sensor_position_text + "," + sensor_acc_text);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}