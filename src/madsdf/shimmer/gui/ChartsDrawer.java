package madsdf.shimmer.gui;

import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * Draw the chart on the main frame and save the data received from the
 * Bluetooth device.
 * 
 * Java version : JDK 1.6.0_21
 * IDE : Netbeans 7.1.1
 * 
 * @author Gregoire Aubert
 * @version 1.0
 */
public class ChartsDrawer {

   private TimeSeries accelX;
   private TimeSeries accelY;
   private TimeSeries accelZ;
   private TimeSeries gyroX;
   private TimeSeries gyroY;
   private TimeSeries gyroZ;
   private JFreeChart accelChart;
   private JFreeChart gyroChart;
   private LinkedList<AccelGyroSample> lastData;
   private CopyOnWriteArrayList<AccelGyroSample> receivedValues;
   private boolean drawing = false;
   private Second secondNow = new Second(new Date());
   private int valuePos = Integer.MIN_VALUE;
   private Color chartColor;
   private final int SAMPLE_SIZE = 100;

   /**
    * Constructor
    * @param panAccel will contain the acceleration chart
    * @param panGyro will contain the gyroscope chart
    */
   public ChartsDrawer(ChartPanel panAccel, ChartPanel panGyro) {

      lastData = new LinkedList<AccelGyroSample>();
      receivedValues = new CopyOnWriteArrayList<AccelGyroSample>();

      createChart();
      chartColor = panAccel.getBackground();
      panAccel.setChart(accelChart);
      panGyro.setChart(gyroChart);

   }

   /**
    * Create the charts
    */
   private void createChart() {

      // Define the times series
      accelX = new TimeSeries("Accel X");
      accelY = new TimeSeries("Accel Y");
      accelZ = new TimeSeries("Accel Z");
      gyroX = new TimeSeries("Gyro X");
      gyroY = new TimeSeries("Gyro Y");
      gyroZ = new TimeSeries("Gyro Z");

      // Create the colletions of times series, used to plot multiple series on 1 chart
      TimeSeriesCollection datasetAccel = new TimeSeriesCollection();
      datasetAccel.addSeries(accelX);
      datasetAccel.addSeries(accelY);
      datasetAccel.addSeries(accelZ);
      TimeSeriesCollection datasetGyro = new TimeSeriesCollection();
      datasetGyro.addSeries(gyroX);
      datasetGyro.addSeries(gyroY);
      datasetGyro.addSeries(gyroZ);

      // Create the charts with the time series collections
      accelChart = ChartFactory.createTimeSeriesChart(
              "Accelerometer",
              "Lastest " + SAMPLE_SIZE + " values",
              "Acc m/s^2",
              datasetAccel,
              true,
              false,
              false);
      accelChart.setBackgroundPaint(chartColor);

      XYPlot plotAcc = accelChart.getXYPlot();
      plotAcc.setRangeGridlinesVisible(false);     // Hide the grid in the graph
      plotAcc.setDomainGridlinesVisible(false);
      plotAcc.setBackgroundPaint(Color.WHITE);
      ValueAxis axisAcc = plotAcc.getDomainAxis();
      axisAcc.setTickMarksVisible(true);    // Define the tick count
      axisAcc.setMinorTickCount(10);
      axisAcc.setAutoRange(true);
      axisAcc.setFixedAutoRange(SAMPLE_SIZE);     // Define the number of visible value
      axisAcc.setTickLabelsVisible(false);  // Hide the axis labels

      gyroChart = ChartFactory.createTimeSeriesChart(
              "Gyroscope",
              "Lastest " + SAMPLE_SIZE + " values",
              "Angular speed rad/s",
              datasetGyro,
              true,
              false,
              false);
      gyroChart.setBackgroundPaint(chartColor);

      XYPlot plotGyro = gyroChart.getXYPlot();
      plotGyro.setRangeGridlinesVisible(false);
      plotGyro.setDomainGridlinesVisible(false);
      plotGyro.setBackgroundPaint(Color.WHITE);
      ValueAxis axisGyro = plotGyro.getDomainAxis();
      axisGyro.setAutoRange(true);
      axisGyro.setFixedAutoRange(SAMPLE_SIZE);
      axisGyro.setMinorTickCount(10);
      axisGyro.setTickLabelsVisible(false);
   }
   
   @Subscribe
   public void onSample(AccelGyroSample sample) {
      // Add the sample in the complete list
      receivedValues.add(sample);
      
      if(receivedValues.size() > 20 * SAMPLE_SIZE)
         receivedValues.retainAll(receivedValues.subList(receivedValues.size() - 3 * SAMPLE_SIZE, receivedValues.size() - 1));
      
      // If live drawing, add the sample to the chart
      if (drawing) {
         synchronized (lastData) {
            addSampleToChart(sample);
         }
      }
   }
   
   /**
    * Draw the last SAMPLE_SIZE values on the charts
    */
   public void drawLatestHundred() {
      synchronized (lastData) {
         // Iterate from the last SAMPLE_SIZE sample received
         int start = Math.max(getReceivedValues().size() - SAMPLE_SIZE, 0);
         ListIterator<AccelGyroSample> iterator = getReceivedValues().listIterator(start);
         
         // Add each sample in order to the chart
         while (iterator.hasNext()) {
            addSampleToChart(iterator.next());
         }
      }
   }

   /**
    * Add a single sample of values to the charts
    * @param sample to be added
    */
   private void addSampleToChart(AccelGyroSample sample) {
      
      // Add the sample in the list, maintening SAMPLE_SIZE sample
      if (getLastHundred().size() == SAMPLE_SIZE) {
         getLastHundred().remove();
      }
      getLastHundred().add(sample);
      
      // Add the sample in the series
      valuePos++;
      accelX.add(new Millisecond(valuePos, secondNow), sample.accel[0]);
      accelY.add(new Millisecond(valuePos, secondNow), sample.accel[1]);
      accelZ.add(new Millisecond(valuePos, secondNow), sample.accel[2]);
      gyroX.add(new Millisecond(valuePos, secondNow), sample.gyro[0]);
      gyroY.add(new Millisecond(valuePos, secondNow), sample.gyro[1]);
      gyroZ.add(new Millisecond(valuePos, secondNow), sample.gyro[2]);
   }

   /**
    * @return the last SAMPLE_SIZE values displayed on the charts
    */
   public LinkedList<AccelGyroSample> getLastHundred() {
      return lastData;
   }

   /**
    * @return all the received values
    */
   public CopyOnWriteArrayList<AccelGyroSample> getReceivedValues() {
      return receivedValues;
   }

   /**
    * Switch on/off the live drawing of the sample
    * @param drawing set the live drawing or not
    */
   public void setDrawing(boolean drawing) {
      // Draw the last hundred sample and then draw live
      if (this.drawing == false && drawing == true) {
         drawLatestHundred();
      }
      this.drawing = drawing;
   }
}
