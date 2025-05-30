package serverSide.main;

// import clientSide.interfaces.Pollingstation.IPollingStation_all;
import serverSide.interfaces.Pollingstation.IPollingStation_all;
import commInfra.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import serverSide.entities.PPollingStationProxy;
import serverSide.sharedRegions.MPollingStation;


public class ServerPollingStation {
    /**
   *  Flag signaling the service is active.
   */

   public static boolean waitConnection;

  /**
   *  Main method.
   *
   *    @param args runtime arguments
   *        args[0] - port nunber for listening to service requests
   *        args[1] - name of the platform where is located the server for the general repository
   *        args[2] - port nunber where the server for the general repository is listening to service requests
   */

   public static void main (String [] args)
   {
      final String CONFIG_FILE = "config.properties";               // Path to the configuration file
      IPollingStation_all pollingStation;                                // interface to the barber shop
      ServerCom scon;                                         // communication channels
      ServerCom.ServerComHandler sconi;                       // communication channel
      int ServerPollingStationPortNumber = -1;                                             // port number for listening to service requests

      // Load default values from properties file
      Properties props = new Properties();
      try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
          props.load(fis);
          ServerPollingStationPortNumber = Integer.parseInt(props.getProperty("ServerPollingStationPortNumber"));
      } catch (IOException | NumberFormatException e) {
          System.err.println("Polling Station main: Error reading configuration file!");
          System.exit(1);
      }
  
    
      pollingStation = (IPollingStation_all) MPollingStation.getInstance(SimulPar.K);                      // service is instantiated
       
      scon = new ServerCom (ServerPollingStationPortNumber);                    // listening channel at the public port is established
      scon.start();
      System.out.println("Service is established!");
      System.out.println("Server is listening for service requests.");

     /* service request processing */

      PPollingStationProxy Proxy;                                // service provider agent

      waitConnection = true;
      while (waitConnection)
      {   
        sconi = scon.accept();                                    // enter listening procedure
        Proxy = new PPollingStationProxy(sconi,pollingStation);  // start a service provider agent to address
        Proxy.start();                                         //   the request of service
        // System.out.println("New client connection accepted and started proxy thread PollingStation: " + Proxy.getName());
      }
      scon.end ();                                                   // operations termination
        System.err.println("Server was shutdown.");
   }
}
