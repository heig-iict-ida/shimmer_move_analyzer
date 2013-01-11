/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madsdf.shimmer.glview;

import madsdf.shimmer.gui.AccelGyroSample;
import madsdf.shimmer.filter.ComplementaryFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JTextArea;
import madsdf.shimmer.event.Globals;

/**
 * Class that receive Shimmer samples, filter them to extract angle and dispatch
 * angles to various components for display
 * @author julien
 */
public class ShimmerAngleController implements Observer {
    public static class AngleEvent {
        // Angles in degrees
        public final float roll, pitch, yaw;
        public AngleEvent(float roll, float pitch, float yaw) {
            this.roll = roll;
            this.pitch = pitch;
            this.yaw = yaw;
        }
    }
    
    private ComplementaryFilter cf = new ComplementaryFilter();
    private JTextArea logArea;
    private DecimalFormat fmt = new DecimalFormat("#.##");
    
    public ShimmerAngleController(JTextArea logArea) {
        this.logArea = logArea;
    }

    @Override
    public void update(Observable o, Object arg) {
      AccelGyroSample sample = (AccelGyroSample) (arg);
      cf.update(sample);
      
      
      final float roll = (float) Math.toDegrees(cf.angles[0]);
      final float pitch = (float) Math.toDegrees(cf.angles[1]);
      final float yaw = (float) Math.toDegrees(cf.angles[2]);
      
      String txt = "roll  :" + fmt.format(roll) + "\n"
                 + "pitch :" + fmt.format(pitch) + "\n"
                 + "yaw   :" + fmt.format(yaw) + "\n";
      txt = detectMovements(txt, roll, pitch, yaw);
      
      Globals.eventBus.post(new AngleEvent(roll, pitch, yaw));
      logArea.setText(txt);
    }
    
    // Detect movementif v is inside [sign*lower, sign*upper]
    // Divide the detection interval in three to get three movements level
    private String detectIf(String movementName, float v, int sign, float lower, float upper) {
        // Define 3 thresholds
        final float interval = Math.abs((upper - lower) / 2.0f);
        final float thresh = interval / 3.0f;
        String log = "";
        if (sign > 0) { // positive case
            if (v > lower && v < upper) {
                log += movementName + " ";
                if (v > upper - thresh) { log += " 3 "; }
                else if (v > upper - 2*thresh) { log += " 2 "; }
                else { log += " 1 "; }
            }
        } else { // negative case
            if (v < lower && v > upper) {
                log += movementName + " ";
                if (v < upper + thresh) { log += " 3 "; }
                else if (v < upper + 2*thresh) { log += " 2 "; }
                else { log += " 1 "; }
            }
        }
        return log;
    }
    
    // Quick hack to detect movements and print them in log
    private String detectMovements(String log, float roll, float pitch, float yaw) {
        /*if (pitch > -60 && pitch < -20) {
            log += "LEFT";
        }
        if (pitch < 60 && pitch > 20) {
            log += "RIGHT\n";
        }
        if (roll > -60 && roll < -20) {
            log += "FORWARD\n";
        }
        if (roll < 60 && roll > 20) {
            log += "BACKWARD\n";
        }*/
        log += detectIf("LEFT", pitch, -1, -20, -60);
        log += detectIf("RIGHT", pitch, 1, 20, 60);
        log += detectIf("FORWARD", roll, -1, -20, -60);
        log += detectIf("BACKWARD", roll, 1, 20, 60);
        return log;
    }
}
