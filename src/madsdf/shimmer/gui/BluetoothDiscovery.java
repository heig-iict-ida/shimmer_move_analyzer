package madsdf.shimmer.gui;

import java.util.LinkedList;
import java.util.List;
import javax.bluetooth.*;

/**
 * Implement the DiscoveryListener to discover the Bluetooth device and their
 * RFCOMM services.
 * 
 * Java version : JDK 1.6.0_21
 * IDE : Netbeans 7.1.1
 * 
 * @author Gregoire Aubert
 * @version 1.0
 */
public class BluetoothDiscovery implements DiscoveryListener {

   // Bluetooth devices list
   private List<MyRemoteDevice> devicesDiscovered = new LinkedList<MyRemoteDevice>();
   // Bluetooth RFCOMM services
   private List<String> servicesUrl = new LinkedList<String>();
   // Lock object
   private final Object lock = new Object();
   // RFCOMM service ID
   private final String RFCOMM_ID = "1101";

   /**
    * Constructor
    */
   public BluetoothDiscovery() {
      devicesDiscovered.clear();
   }

   @Override
   public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
      getDevicesDiscovered().add(new MyRemoteDevice(btDevice));
   }

   @Override
   public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
      if (servRecord != null && servRecord.length > 0) {
         for (int i = 0; i < servRecord.length; i++) {
            getServicesUrl().add(servRecord[i].getConnectionURL(0, false));
         }
      }
      synchronized (getLock()) {
         getLock().notify();
      }
   }

   @Override
   public void serviceSearchCompleted(int transID, int respCode) {
      synchronized (getLock()) {
         getLock().notifyAll();
      }
   }

   @Override
   public void inquiryCompleted(int discType) {
      synchronized (getLock()) {
         getLock().notifyAll();
      }
   }
   
   /**
    * Start the discovery of the devices
    * @return the list of the discovered devices
    */
   public List<MyRemoteDevice> launchDevicesDiscovery() {
      devicesDiscovered.clear();
      synchronized (lock) {
         try {
            boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, this);
            if (started) {
               lock.wait();
            }
         }
         catch (BluetoothStateException ex) {
            System.err.println("BluetoothDiscovery.launchDevicesDiscovery : " + ex);
         }
         catch (InterruptedException ex){
            System.err.println("BluetoothDiscovery.launchDevicesDiscovery : " + ex);
         }
      }
      return devicesDiscovered;
   }
   
   /**
    * Start the discovery of the services for a given device
    * @param remoteDevice is the device to discover the services
    * @return the list of the discovered services
    */
   public List<String> launchServicesDiscovery(RemoteDevice remoteDevice){
      
      servicesUrl.clear();
      
      UUID[] uuidSet = new UUID[1];
      uuidSet[0] = new UUID(RFCOMM_ID, true);
      
      try {
         LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(null, uuidSet, remoteDevice, this);
         
         synchronized (lock) {
            lock.wait();
         }
      }
      catch (BluetoothStateException ex) {
         System.err.println("BluetoothDiscovery.launchServicesDiscovery : " + ex);
      }
      catch (InterruptedException ex) {
         System.err.println("BluetoothDiscovery.launchServicesDiscovery : " + ex);
      }
      return servicesUrl;
   }

   /**
    * @return the list of the discovered devices
    */
   public List<MyRemoteDevice> getDevicesDiscovered() {
      return devicesDiscovered;
   }

   /**
    * @return the list of the services Url
    */
   public List<String> getServicesUrl() {
      return servicesUrl;
   }

   /**
    * @return the lock
    */
   public Object getLock() {
      return lock;
   }
}
