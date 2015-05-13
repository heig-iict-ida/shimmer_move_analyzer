package madsdf.shimmer.gui;

import static com.google.common.base.Preconditions.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.eventbus.Subscribe;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.ARangePolicy;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * Draw the chart on the main frame and save the data received from the
 * Bluetooth device.
 *
 * Java version : JDK 1.6.0_21 IDE : Netbeans 7.1.1
 *
 * @author Gregoire Aubert
 * @version 1.0
 */
public class ChartsDrawer {
    // Range policy that returns the all-time max/min

    public static class RangePolicyMaxSeen extends ARangePolicy {
        private double min;
        private double max;
        
        public RangePolicyMaxSeen() {
            this(Double.MAX_VALUE, Double.MIN_VALUE);
        }
        
        public RangePolicyMaxSeen(double min, double max) {
            super();
            this.min = min;
            this.max = max;
        }

        @Override
        public double getMax(double chartMin, double chartMax) {
            max = Math.max(max, chartMax);
            return max;
        }

        @Override
        public double getMin(double chartMin, double chartMax) {
            min = Math.min(min, chartMin);
            return min;
        }
    }
    private final ConcurrentLinkedDeque<AccelGyro.Sample> receivedValues =
            new ConcurrentLinkedDeque();
    final int N_KEPT = 400;
    
    // Pairs of (color, stroke) to use to get consistent drawing based on serie
    // id across application
    public final static Color[] colors = new Color[]{
        Color.RED, Color.GREEN, Color.BLUE, Color.BLACK,
        Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.YELLOW,
        ChartColor.VERY_DARK_RED, ChartColor.VERY_DARK_GREEN,
        ChartColor.VERY_DARK_BLUE, ChartColor.GRAY,
        ChartColor.VERY_DARK_CYAN
    };
    private final static Stroke contStroke = new BasicStroke(1.0f);
    private final static Stroke dashStroke = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
            new float[]{10.0f}, 0.0f);
    public final static Stroke[] strokes = new Stroke[]{
        contStroke, contStroke, contStroke, contStroke,
        contStroke, contStroke, contStroke,
        contStroke, contStroke,
        dashStroke, dashStroke,
        dashStroke, dashStroke,
        dashStroke
    };
    
    
    private ITrace2D[] accelTraces = new ITrace2D[]{
        new Trace2DLtd(100),
        new Trace2DLtd(100),
        new Trace2DLtd(100),};
    private ITrace2D[] gyroTraces = new ITrace2D[]{
        new Trace2DLtd(100),
        new Trace2DLtd(100),
        new Trace2DLtd(100),};

    /**
     * Constructor
     *
     * @param panAccel will contain the acceleration chart
     * @param panGyro will contain the gyroscope chart
     */
    public ChartsDrawer(Chart2D chartAccel, Chart2D chartGyro) {
        createChart(chartAccel, accelTraces, "accel", "Acc m/s^2");
        createChart(chartGyro, gyroTraces, "gyro", "Angular speed rad/s");
    }

    private static void createChart(Chart2D chart, ITrace2D[] traces,
            String name, String yAxisLabel) {
        // Cleanup - necessary when we switch between calibrated and uncalibrated
        chart.removeAllTraces();
        
        final String[] axesNames = new String[]{"x", "y", "z"};
        for (int i = 0; i < 3; ++i) {
            chart.addTrace(traces[i]);
            traces[i].setName(name + "_" + axesNames[i]);
            traces[i].setColor(colors[i]);
            traces[i].setStroke(strokes[i]);
        }

        IAxis yAxis = chart.getAxisY();
        //yAxis.setAxisTitle(new AxisTitle(yAxisLabel));
        yAxis.setPaintScale(true);
        yAxis.getAxisTitle().setTitle(null);
        yAxis.setRangePolicy(new RangePolicyMaxSeen());
        IAxis xAxis = chart.getAxisX();
        xAxis.setPaintScale(false);
        xAxis.getAxisTitle().setTitle(null);
    }

    public void addSample(AccelGyro.Sample sample) {
        // Add the sample in the complete list
        receivedValues.addLast(sample);
        while (receivedValues.size() > N_KEPT) {
            receivedValues.removeFirst();
        }
        
        final long now = System.currentTimeMillis();
        accelTraces[0].addPoint(now, sample.accel[0]);
        accelTraces[1].addPoint(now, sample.accel[1]);
        accelTraces[2].addPoint(now, sample.accel[2]);

        gyroTraces[0].addPoint(now, sample.gyro[0]);
        gyroTraces[1].addPoint(now, sample.gyro[1]);
        gyroTraces[2].addPoint(now, sample.gyro[2]);
    }
    
    static class Data {
        public float[][] accel;
        public float[][] gyro;
        
        public Data(float[][] accel, float[][] gyro) {
            this.accel = accel;
            this.gyro = gyro;
        }
    }

    public Data getRecentData() {
        float[][] accelData = new float[3][];
        float[][] gyroData = new float[3][];
        for (int i = 0; i < 3; ++i) {
            accelData[i] = new float[N_KEPT];
            gyroData[i] = new float[N_KEPT];
        }

        ArrayList<AccelGyro.Sample> allSamples = Lists.newArrayList(receivedValues);

        int start = Math.max(0, allSamples.size() - N_KEPT);
        int datai = 0;
        for (int i = start; i < allSamples.size(); ++i) {
            for (int j = 0; j < 3; ++j) {
                accelData[j][datai] = allSamples.get(i).accel[j];
                gyroData[j][datai] = allSamples.get(i).gyro[j];
            }
            ++datai;
        }
        return new Data(accelData, gyroData);
    }
}
