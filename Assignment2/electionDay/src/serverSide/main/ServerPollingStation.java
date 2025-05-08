package serverSide.main;

import java.net.SocketTimeoutException;

import commInfra.ServerCom;
import serverSide.entities.PMPollingStationProxy;
import serverSide.sharedRegions.MPollingStation;
import serverSide.sharedRegions.PollingStationInterface;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import clientSide.interfaces.Pollingstation.IPollingStation_all;



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
      MPollingStation pollingStation;                                              // barber shop (service to be rendered)
      IPollingStation_all pStationInter;                                // interface to the barber shop
    //   GeneralReposStub reposStub;                                    // stub to the general repository
      ServerCom scon, sconi;                                         // communication channels
      int ServerPollingStationPortNumber = -1;                                             // port number for listening to service requests
      String reposServerName;                                        // name of the platform where is located the server for the general repository
      int reposPortNumb = -1;                                        // port nunber where the server for the general repository is listening to service requests

      // Load default values from properties file
      Properties props = new Properties();
      try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
          props.load(fis);
          ServerPollingStationPortNumber = Integer.parseInt(props.getProperty("portNumber"));
          reposPortNumb = Integer.parseInt(props.getProperty("reposPortNumber"));
      } catch (IOException | NumberFormatException e) {
          GenericIO.writelnString("Error reading configuration file!");
          System.exit(1);
      }
  
      // Override with command line arguments if provided
      if (args.length == 3) {
          try {
              ServerPollingStationPortNumber = Integer.parseInt(args[0]);
              reposServerName = args[1];
              reposPortNumb = Integer.parseInt(args[2]);
          } catch (NumberFormatException e) {
              GenericIO.writelnString("Invalid number format in arguments!");
              System.exit(1);
          }
      }

      // Validate port numbers
      if ((ServerPollingStationPortNumber < 4000) || (ServerPollingStationPortNumber >= 65536)) {
          GenericIO.writelnString("Invalid port number!");
          System.exit(1);
      }
      if ((reposPortNumb < 4000) || (reposPortNumb >= 65536)) {
          GenericIO.writelnString("Invalid repository port number!");
          System.exit(1);
      }

     /* service is established */

    //   reposStub = new GeneralReposStub (reposServerName, reposPortNumb);     // communication to the general repository is instantiated
    //   pollingStation = new MPollingStation (reposStub);                      // service is instantiated
      MPollingStation pollingStation = MPollingStation.getInstance(reposPortNumb, null); // Create an instance of the polling station
      pStationInter = new PollingStationInterface(pollingStation);             // interface to the service is instantiated
      scon = new ServerCom (ServerPollingStationPortNumber);                    // listening channel at the public port is established
      scon.start ();
      GenericIO.writelnString ("Service is established!");
      GenericIO.writelnString ("Server is listening for service requests.");

     /* service request processing */

      PMPollingStationProxy Proxy;                                // service provider agent

      waitConnection = true;
      while (waitConnection)
      { try
        { sconi = scon.accept ();                                    // enter listening procedure
          Proxy = new PMPollingStationProxy (sconi);                 // start a service provider agent to address
          Proxy.start ();                                         //   the request of service
        }
        catch (SocketTimeoutException e) {}
      }
      scon.end ();                                                   // operations termination
      GenericIO.writelnString ("Server was shutdown.");
   }
}
