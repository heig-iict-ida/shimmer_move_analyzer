/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madsdf.shimmer.gui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import info.monitorenter.gui.chart.Chart2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SpinnerNumberModel;
import madsdf.shimmer.event.Globals;
import madsdf.shimmer.glview.ShimmerAngleConverter;
import madsdf.shimmer.glview.ShimmerCanvas;
import madsdf.shimmer.gui.ChartsDrawer.Data;

/**
 *
 * @author julien
 */
public class ShimmerDataPanel extends javax.swing.JPanel {
    private static final Logger log = Logger.getLogger(ShimmerDataPanel.class.getName());
    
    private final int DEFAULT_MOVEMENT_LENGTH = 75;
    
    private BluetoothDeviceCom connectedDevice;
    private final EventBus eventBus;
    private final String btid;
    
    private SpinnerNumberModel lenSpinnerModel;

    // The class drawing the chart
    private ChartsDrawer chartsDrawer;
    // Controller for OpenGL display
    private ShimmerAngleConverter angleConverter;
    
    private ShimmerCanvas shimmerCanvas;
    
    private Object sampleListener;

    public ShimmerDataPanel(String btid) {
        initComponents();
        
        shimmerCanvas = (ShimmerCanvas)panGL;
        eventBus = Globals.getBusForShimmer(btid);
        angleConverter = new ShimmerAngleConverter(eventBus, txtLog);
        
        lenSpinnerModel = (SpinnerNumberModel) lenSpinner.getModel();
        lenSpinnerModel.setValue(DEFAULT_MOVEMENT_LENGTH);
        
        // Eventbus registration
        eventBus.register(shimmerCanvas);
        eventBus.register(angleConverter);
        
        txtLog.setEditable(false);
        
        labBtId.setText(btid);
        
        this.btid = btid;
        new Thread(){
            @Override
            public void run() {
                connect();
            }
        }.start();
        
        setCalibratedChart(cbCalibrated.isSelected());
    }
    
    public void shutdown() {
        connectedDevice.stop();
        shimmerCanvas.stop();
    }
    
    private void connect() {
        try {
            log.info("Connecting to shimmer " + btid + "...");
            final String btServiceID = "btspp://00066646" + btid + ":1;authenticate=false;encrypt=false;master=false";
            connectedDevice = new BluetoothDeviceCom(eventBus, btid);
            connectedDevice.connect(btServiceID);
            
            if (!connectedDevice.isCalibrated()) {
                log.info("No calibration available");
                cbCalibrated.setSelected(false);
                setCalibratedChart(false);
                cbCalibrated.setEnabled(false);
            } else {
                log.info("Calibration available");
                cbCalibrated.setEnabled(true);
            }
            
            log.info("Connected to shimmer");
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Couldn't connect to : " + btid + " : " + ex.getMessage());
        }
    }
    
    
    private void setCalibratedChart(boolean calibrated) {
        // Replace sample listener
        if (sampleListener != null) {
            eventBus.unregister(sampleListener);
        }
        
        chartsDrawer = new ChartsDrawer((Chart2D) panAccel, (Chart2D) panGyro);
        
        if (calibrated) {
            sampleListener = new Object() {
                @Subscribe
                public void onSample(AccelGyro.CalibratedSample sample) {
                    chartsDrawer.addSample(sample);
                }
            };
        } else {
            sampleListener = new Object() {
                @Subscribe
                public void onSample(AccelGyro.UncalibratedSample sample) {
                    chartsDrawer.addSample(sample);
                }
            };
        }
        eventBus.register(sampleListener);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        panAccel = new Chart2D();
        panGyro = new Chart2D();
        panGL = ShimmerCanvas.createCanvas(this);
        panLog = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        cbCalibrated = new javax.swing.JCheckBox();
        labBtId = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        lenSpinner = new javax.swing.JSpinner();

        jPanel2.setLayout(new java.awt.GridLayout(2, 2, 5, 5));

        panAccel.setName("panAccel"); // NOI18N

        javax.swing.GroupLayout panAccelLayout = new javax.swing.GroupLayout(panAccel);
        panAccel.setLayout(panAccelLayout);
        panAccelLayout.setHorizontalGroup(
            panAccelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 245, Short.MAX_VALUE)
        );
        panAccelLayout.setVerticalGroup(
            panAccelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 207, Short.MAX_VALUE)
        );

        jPanel2.add(panAccel);

        panGyro.setName("panAccel"); // NOI18N

        javax.swing.GroupLayout panGyroLayout = new javax.swing.GroupLayout(panGyro);
        panGyro.setLayout(panGyroLayout);
        panGyroLayout.setHorizontalGroup(
            panGyroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 245, Short.MAX_VALUE)
        );
        panGyroLayout.setVerticalGroup(
            panGyroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 207, Short.MAX_VALUE)
        );

        jPanel2.add(panGyro);

        javax.swing.GroupLayout panGLLayout = new javax.swing.GroupLayout(panGL);
        panGL.setLayout(panGLLayout);
        panGLLayout.setHorizontalGroup(
            panGLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 245, Short.MAX_VALUE)
        );
        panGLLayout.setVerticalGroup(
            panGLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 207, Short.MAX_VALUE)
        );

        jPanel2.add(panGL);

        txtLog.setColumns(20);
        txtLog.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        txtLog.setRows(5);
        panLog.setViewportView(txtLog);

        jPanel2.add(panLog);

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        cbCalibrated.setText("Calibrated");
        cbCalibrated.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCalibratedActionPerformed(evt);
            }
        });

        labBtId.setText("jLabel1");

        jLabel1.setText("Movement length");

        lenSpinner.setModel(new javax.swing.SpinnerNumberModel());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lenSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(btnSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labBtId)
                        .addGap(96, 96, 96)
                        .addComponent(cbCalibrated)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lenSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(cbCalibrated)
                    .addComponent(labBtId))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        final int length = (Integer)lenSpinnerModel.getValue();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FileWriter output = null;
                final Data data = chartsDrawer.getRecentData();
                new CaptureEditFrame("movements", btid, data.accel, data.gyro, length).setVisible(true);
            }
        });
    }//GEN-LAST:event_btnSaveActionPerformed

    private void cbCalibratedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCalibratedActionPerformed
        setCalibratedChart(cbCalibrated.isSelected());
    }//GEN-LAST:event_cbCalibratedActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSave;
    private javax.swing.JCheckBox cbCalibrated;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel labBtId;
    private javax.swing.JSpinner lenSpinner;
    private javax.swing.JPanel panAccel;
    private javax.swing.JPanel panGL;
    private javax.swing.JPanel panGyro;
    private javax.swing.JScrollPane panLog;
    private javax.swing.JTextArea txtLog;
    // End of variables declaration//GEN-END:variables
}
