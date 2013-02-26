/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madsdf.shimmer.glview;

import com.jogamp.opengl.util.FPSAnimator;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author julien
 */
public class Main {
    private static final int CANVAS_WIDTH = 320;  // width of the drawable
    private static final int CANVAS_HEIGHT = 240; // height of the drawable

    public static void main(String[] args) {
        // Run the GUI codes in the event-dispatching thread for thread safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Create the top-level container
                final JFrame frame = new JFrame(); // Swing's JFrame or AWT's Frame
                final JPanel panel = new JPanel();
                frame.getContentPane().add(panel);
                ShimmerCanvas canvas = ShimmerCanvas.createCanvas(panel);
                canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
                frame.getContentPane().add(canvas);
                frame.setTitle("Shimmer");
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
}
