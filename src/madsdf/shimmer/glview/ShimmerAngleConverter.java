/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madsdf.shimmer.glview;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import madsdf.shimmer.gui.AccelGyro;
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
public class ShimmerAngleConverter {
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
    private EventBus ebus;
    
    public ShimmerAngleConverter(EventBus ebus, JTextArea logArea) {
        this.logArea = logArea;
        this.ebus = ebus;
    }
    
    @Subscribe
    public void onSample(AccelGyro.CalibratedSample sample) {
      cf.update(sample);
      
      float roll = (float) Math.toDegrees(cf.angles[0]);
      float pitch = (float) Math.toDegrees(cf.angles[1]);
      float yaw = (float) Math.toDegrees(cf.angles[2]);
      
      // When pitch becomes too big, roll estimation becomes wrong. So
      // simply force the angles
      // TODO: Not sure this is the best way to do it... the filter might
      // be a bit too steep 
      if (pitch < -70 || pitch > 70) {
          roll = 0;
          pitch = pitch < -70 ? -70 : 70;
      }
      
      String txt = "roll  :" + fmt.format(roll) + "\n"
                 + "pitch :" + fmt.format(pitch) + "\n"
                 + "yaw   :" + fmt.format(yaw) + "\n";
      //txt = detectMovements(txt, roll, pitch, yaw);
      
      ebus.post(new AngleEvent(roll, pitch, yaw));
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
