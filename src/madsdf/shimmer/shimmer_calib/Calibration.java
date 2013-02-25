/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madsdf.shimmer.shimmer_calib;

import static com.google.common.base.Preconditions.*;
import java.io.IOException;
import java.util.Properties;
import madsdf.shimmer.gui.BluetoothDeviceCom;

/**
 *
 * @author julien
 */
public class Calibration {
    public static void loadGyroCalibration(String shimmerID, float[] outOffset, float[] outGain) throws IOException {
        final String fname = "/madsdf/shimmer/shimmer_calib/1_5_" + shimmerID + ".gyro.properties";
        loadCalibration(fname, outOffset, outGain);
    }

    public static void loadAccelCalibration(String shimmerID, float[] outOffset, float[] outGain) throws IOException {
        final String fname = "/madsdf/shimmer/shimmer_calib/1_5_" + shimmerID + ".accel.properties";
        loadCalibration(fname, outOffset, outGain);
        // Transform from g to m/s^2
        outGain[0] /= 9.81;
        outGain[1] /= 9.81;
        outGain[2] /= 9.81;
        
    }
    
    private static void loadCalibration(String filename, float[] outOffset, float[] outGain) throws IOException {
        Properties prop = new Properties();
        prop.load(BluetoothDeviceCom.class.getResourceAsStream(filename));
        outOffset[0] = Float.parseFloat(prop.getProperty("offset_x"));
        outOffset[1] = Float.parseFloat(prop.getProperty("offset_y"));
        outOffset[2] = Float.parseFloat(prop.getProperty("offset_z"));
        outGain[0] = Float.parseFloat(prop.getProperty("gain_x"));
        outGain[1] = Float.parseFloat(prop.getProperty("gain_y"));
        outGain[2] = Float.parseFloat(prop.getProperty("gain_z"));
    }
    
    public final static int CALIB_ACCEL = 0;
    public final static int CALIB_GYRO = 1;

    // modified data; data is 3xN array
    public static float[][] calibrate(String shimmerID, float[][] data, int type) throws IOException {
        checkState(data.length == 3);
        float[] offset = new float[3];
        float[] gain = new float[3];
        
        float[][] outData = new float[3][];
        
        if (type == CALIB_ACCEL) {
            loadAccelCalibration(shimmerID, offset, gain);
        } else {
            loadGyroCalibration(shimmerID, offset, gain);
        }
        
        for (int i = 0; i < 3; ++i) {
            outData[i] = new float[data[i].length];
            for (int j = 0; j < data[i].length; ++j) {
                outData[i][j] = (data[i][j] - offset[i]) / gain[i];
            }
        }
        return outData;
    }
    
    
}
