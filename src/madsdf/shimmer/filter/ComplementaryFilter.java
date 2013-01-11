/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madsdf.shimmer.filter;

import madsdf.shimmer.gui.AccelGyroSample;

/**
 *
 * @author julien
 */
public class ComplementaryFilter {
    // Gravity extracted from acceleration (low pass filtered)
    private float[] gravity = new float[3];
    // Gyro high frequency components (high pass filtered)
    private float[] hp_gyro = new float[3];
    public float[] angles = new float[3];
    
    // Constant used for low/high pass filtering
    private final static float ALPHA_1 = 0.8f;
    // Constant used for complementary filter proper
    private final static float ALPHA_2 = 0.8f;
    
    private long prevUpdateMillis = 0;
    
    public void update(AccelGyroSample sample) {
        // Compute dt
        if (prevUpdateMillis == 0) {
            prevUpdateMillis = sample.receivedTimestampMillis;
            return;  // Can't update on first sample
        }
        final float dt = (sample.receivedTimestampMillis - prevUpdateMillis) / 1000.0f;
        prevUpdateMillis = sample.receivedTimestampMillis;
        
        // http://developer.android.com/reference/android/hardware/SensorEvent.html
        // Gravity / gyro extraction
        for (int i = 0; i < 3; ++i) {
            gravity[i] = ALPHA_1 * gravity[i] + (1 - ALPHA_1) * sample.accel[i];
        }
        for (int i = 0; i < 3; ++i) {
            hp_gyro[i] = (1 - ALPHA_1) * hp_gyro[i] + ALPHA_1 * sample.gyro[i];
        }

        // https://sites.google.com/site/myimuestimationexperience/filters/complementary-filter
        // Complementary filter
        // normalize gravity
        final float l = (float) Math.sqrt(gravity[0]*gravity[0] +
                gravity[1]*gravity[1] + gravity[2]*gravity[2]);
        final float ngx = gravity[0] / l;
        final float ngy = gravity[1] / l;
        final float ngz = gravity[2] / l;
        angles[0] = complementaryFilterAngle(angles[0], accelRoll(ngx, ngy, ngz),
                hp_gyro[0], dt);
        angles[1] = complementaryFilterAngle(angles[1], accelPitch(ngx, ngy, ngz),
                hp_gyro[1], dt);
        // We don't have angles from accelerometer for last angle (yaw)
        // We can either force it to 0 or integrate it using only the gyro
        // (drift will occur)
        /*if (Math.abs(hp_gyro[2]) > 0.4) { // Only integrate if clearly non-noisy
            angles[2] = angles[2] + hp_gyro[2] * dt;
        }*/
        angles[2] = 0;
    }
    
    // Apply the complementary filter on an angle given it past value and
    // angle measured from accelerometer and angular speed from gyro
    private float complementaryFilterAngle(float oldangle, float accel_angle,
            float gyro, float dt) {
        return (1 - ALPHA_2) * (oldangle + gyro * dt) + ALPHA_2 * accel_angle;
    }
    
    // Compute accel roll angle with accel given NORMALIZED (nax**2 + nay**2 + naz**2 == 1)
    private float accelRoll(float nax, float nay, float naz) {
        // Eqn 38 of "Tilt sensing using linear accelerometers"
        final float mu = 0.01f;
        // TODO: Should we use atan2 ? 
        return (float) Math.atan(nay / Math.sqrt(naz*naz + mu*nay*nay));
    }
    
    private float accelPitch(float nax, float nay, float naz) {
        // Eqn 26 of "Tilt sensing using linear accelerometers"
        return (float) Math.atan(-nax / Math.sqrt(nay*nay + naz*naz));
    }
}
