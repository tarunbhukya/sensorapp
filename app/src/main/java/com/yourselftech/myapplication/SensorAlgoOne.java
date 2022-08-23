package com.yourselftech.myapplication;

import android.hardware.SensorEvent;

public class SensorAlgoOne {
    static final float NS2S = 1.0f / 1000000000.0f;
    long last_timestamp = 0;
    float[] last_position, last_velocity, last_acceleration, coordinates;
    int xCounter=0;
    int stillCounter=0;
    int xDirection;

    public SensorAlgoOne() {

    }

    public float[] getPosition(SensorEvent event) {
        float[] current_velocity = new float[3];
        float[] current_position = new float[3];

        final float alpha = (float) 0.8;

        // Isolate the force of gravity with the low-pass filter.
        float[] gravity = new float[3];
        float[] linear_acceleration = new float[3];
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        if(last_position == null) {
            last_acceleration = new float[3];
            last_position = new float[3];
            last_velocity = new float[3];
            coordinates = new float[3];
            last_velocity[0] = last_velocity[1] = last_velocity[2] = 0f;
            last_position[0] = last_position[1] = last_position[2] = 0f;
            coordinates[0] = coordinates[1] = coordinates[2] = 0f;
        }

        float dt = (event.timestamp - last_timestamp) * NS2S;

        getXDirection(event.values[0]);

        for(int index = 0; index < 3;++index){
            if(index == 0) {
                current_velocity[index] = (Math.abs(event.values[index]) + Math.abs(last_acceleration[index]))/2 * dt;
                current_position[index] = (Math.abs(last_velocity[index]) + Math.abs(current_velocity[index]))/2 * dt;
                if(xDirection == 0) {
                    coordinates[0] = coordinates[0];
                }
                if(xDirection < 0) {
                    coordinates[0] = coordinates[0] + current_position[index];
                }
                if(xDirection > 0) {
                    coordinates[0] = coordinates[0] - current_position[index];
                }
            }else {
                current_velocity[index] = (event.values[index] + last_acceleration[index])/2 * dt;
                current_position[index] = (last_velocity[index] + current_velocity[index])/2 * dt;
                coordinates[index] = current_position[index];
            }
        }

        last_acceleration = event.values;
        last_velocity = current_velocity;
        last_position = current_position;
        last_timestamp = event.timestamp;
        return coordinates;
    }

    private void getXDirection(float value) {
        // Straight line with no acceleration, basically a reset condition
        if ((xCounter == 0 || xCounter ==3 || stillCounter == 20) && value == 0) {
            xCounter = 0;
            xDirection = 0;
            return;
        }

        if(value == 0) {
            stillCounter += 1;
        }

        // starting with negative values, + direction
        if (xCounter == 0 && value < 0) {
            xCounter = 1;
            xDirection = 1;
            stillCounter = 0;
            return;
        }

        // starting with positive values, - direction
        if (xCounter == 0 && value > 0) {
            xCounter = 1;
            xDirection = -1;
            stillCounter = 0;
            return;
        }

        // + direction and and negative values do nothing
        if (xCounter == 1 && value < 0 && xDirection == 1) {
            // Do Nothing
            stillCounter = 0;
        }

        // - direction and + values do nothing
        if (xCounter == 1 && value > 0 && xDirection == -1) {
            // Do Nothing
            stillCounter = 0;
        }

        if (xCounter == 1 && value == 0 && xDirection == 1) {
            xCounter = 2;
            stillCounter = 0;
        }

        if (xCounter == 1 && value == 0 && xDirection == -1) {
            xCounter = 2;
            stillCounter = 0;
        }

        if (xCounter == 2 && value == 0 && xDirection == 1) {
            xCounter = 0;
            xDirection = 0;
            stillCounter = 0;
        }

        if (xCounter == 2 && value == 0 && xDirection == -1) {
            xCounter = 0;
            xDirection = 0;
            stillCounter = 0;
        }

        if (xCounter == 2 && value > 0 && xDirection == 1) {
            xCounter = 3;
            stillCounter = 0;
        }

        if (xCounter == 2 && value < 0 && xDirection == -1) {
            xCounter = 3;
            stillCounter = 0;
        }

        if (xCounter == 3 && value > 0 && xDirection == 1) {
            // Do Nothing
            stillCounter = 0;
        }

        if (xCounter == 3 && value < 0 && xDirection == -1) {
            // Do Nothing
            stillCounter = 0;
        }
    }
}
