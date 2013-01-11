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
import javax.swing.SwingUtilities;

/**
 *
 * @author julien
 */
public class Main {
    private static final int CANVAS_WIDTH = 320;  // width of the drawable
    private static final int CANVAS_HEIGHT = 240; // height of the drawable
    private static final int FPS = 60; // animator's target frames per second

    public static void main(String[] args) {
        // Run the GUI codes in the event-dispatching thread for thread safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Create a animator that drives canvas' display() at the specified FPS. 
                //final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);

                // Create the top-level container
                final JFrame frame = new JFrame(); // Swing's JFrame or AWT's Frame
                ShimmerCanvas canvas = ShimmerCanvas.createCanvas(frame);
                canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
                frame.getContentPane().add(canvas);
                frame.setTitle("Shimmer");
                frame.pack();
                frame.setVisible(true);
                //animator.start(); // start the animation loop
            }
        });
    }
}
