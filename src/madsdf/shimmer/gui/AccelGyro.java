package madsdf.shimmer.gui;

import java.util.LinkedList;

/**
 * A sample received from the shimmer containing accel / gyro measurements
 * You can choose if you want calibrated or uncalibrated data by listening
 * for the correct class on the eventbus
 */
public class AccelGyro {
    public static class CalibratedSample {
        public final long receivedTimestampMillis;
        public final float accel[] = new float[3];
        public final float gyro[] = new float[3];

        /**
         * Constructor
         *
         * @param buffer is the data to convert
         * @throws Exception if the buffer is malformed
         */
        public CalibratedSample(long time, float[] accel, float[] gyro) {
            this.receivedTimestampMillis = time;
            System.arraycopy(accel, 0, this.accel, 0, 3);
            System.arraycopy(gyro, 0, this.gyro, 0, 3);
        }
    }
    
    public static class UncalibratedSample {
        public final long receivedTimestampMillis;
        public final float accel[] = new float[3];
        public final float gyro[] = new float[3];

        /**
         * Constructor
         *
         * @param buffer is the data to convert
         * @throws Exception if the buffer is malformed
         */
        public UncalibratedSample(long time, float[] accel, float[] gyro) {
            this.receivedTimestampMillis = time;
            System.arraycopy(accel, 0, this.accel, 0, 3);
            System.arraycopy(gyro, 0, this.gyro, 0, 3);
        }
        
        // TODO: Remove : this is only for backward-compatibility
        public float getVal(int i) {
            if (i < 1 || i > 6) {
                throw new IllegalArgumentException("Invaild i : " + i);
            }
            if (i <= 3) {
                return accel[i - 1];
            } else {
                return accel[i - 4];
            }
            
        }
    }
}
