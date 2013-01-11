package madsdf.shimmer.gui;

import java.util.LinkedList;

/**
 * A sample received from the shimmer containing accel / gyro measurements
 * @author Gregoire Aubert
 * @version 1.0
 */
public class AccelGyroSample {
    public final long receivedTimestampMillis;
    public final float accel[] = new float[3];
    public final float gyro[] = new float[3];

    /**
     * Constructor
     *
     * @param buffer is the data to convert
     * @throws Exception if the buffer is malformed
     */
    public AccelGyroSample(long time, float[] accel, float[] gyro) {
        this.receivedTimestampMillis = time;
        System.arraycopy(accel, 0, this.accel, 0, 3);
        System.arraycopy(gyro, 0, this.gyro, 0, 3);
    }
}
