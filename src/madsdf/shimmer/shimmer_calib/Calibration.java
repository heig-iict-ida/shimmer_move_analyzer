/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madsdf.shimmer.shimmer_calib;

import static com.google.common.base.Preconditions.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import madsdf.shimmer.gui.BluetoothDeviceCom;

/**
 *
 * @author julien
 */
public class Calibration {
    private final static Logger Log = Logger.getLogger(Calibration.class.getName());
    public static boolean loadGyroCalibration(String shimmerID, float[] outOffset, float[] outGain) throws IOException {
        final String fname = "/madsdf/shimmer/shimmer_calib/1_5_" + shimmerID + ".gyro.properties";
        return loadCalibration(fname, outOffset, outGain);
    }

    public static boolean loadAccelCalibration(String shimmerID, float[] outOffset, float[] outGain) throws IOException {
        final String fname = "/madsdf/shimmer/shimmer_calib/1_5_" + shimmerID + ".accel.properties";
        if (!loadCalibration(fname, outOffset, outGain)) {
            return false;
        }
        // Transform from g to m/s^2
        outGain[0] /= 9.81;
        outGain[1] /= 9.81;
        outGain[2] /= 9.81;
        return true;
    }
    
    private static boolean loadCalibration(String filename, float[] outOffset, float[] outGain) throws IOException {
        Properties prop = new Properties();
        final InputStream stream = BluetoothDeviceCom.class.getResourceAsStream(filename);
        if (stream != null) {
            prop.load(stream);
            outOffset[0] = Float.parseFloat(prop.getProperty("offset_x"));
            outOffset[1] = Float.parseFloat(prop.getProperty("offset_y"));
            outOffset[2] = Float.parseFloat(prop.getProperty("offset_z"));
            outGain[0] = Float.parseFloat(prop.getProperty("gain_x"));
            outGain[1] = Float.parseFloat(prop.getProperty("gain_y"));
            outGain[2] = Float.parseFloat(prop.getProperty("gain_z"));
            return true;
        } else {
            // In case calibration is not found, fallback on dummy offset/gain
            outOffset[0] = outOffset[1] = outOffset[2] = 0;
            outGain[0] = outGain[1] = outGain[2] = 1;
            Log.log(Level.WARNING, "Couldn't load calibration from " + filename);
            return false;
        }
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
