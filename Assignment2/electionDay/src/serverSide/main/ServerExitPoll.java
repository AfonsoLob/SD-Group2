package serverSide.main;

import java.io.FileInputStream;
import java.io.IOException;
// import java.net.SocketTimeoutException;
import java.util.Properties;

import serverSide.interfaces.ExitPoll.IExitPoll_all;
import commInfra.ServerCom;
import serverSide.entities.PExitPollProxy;
import serverSide.sharedRegions.MExitPoll;
// import serverSide.main.SimulPar;


public class ServerExitPoll {
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
      IExitPoll_all exitPoll;                                // interface to the barber shop
      ServerCom scon;                                         // communication channels
      ServerCom.ServerComHandler sconi;                       // communication channel
      int ServerExitPollPortNumber = -1;                                             // port number for listening to service requests

      // Load default values from properties file
      Properties props = new Properties();
      try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
          props.load(fis);
          ServerExitPollPortNumber = Integer.parseInt(props.getProperty("ServerExitPollPortNumber"));
      } catch (IOException | NumberFormatException e) {
          System.err.println("Exitpoll main: Error reading configuration file!");
          System.exit(1);
      }
  
    
      exitPoll = MExitPoll.getInstance(SimulPar.EXIT_POLL_PERCENTAGE);                      // service is instantiated
       
      scon = new ServerCom (ServerExitPollPortNumber);                    // listening channel at the public port is established
      scon.start();
      System.err.println ("Service is established!");
      System.err.println ("Server is listening for service requests.");

     /* service request processing */

      PExitPollProxy Proxy;                                // service provider agent

      waitConnection = true;
      while (waitConnection)
      {   
        sconi = scon.accept();                                    // enter listening procedure
        Proxy = new PExitPollProxy(sconi, exitPoll);           // service provider agent is generated
        Proxy.start();                                         //   the request of service
        // System.out.println("New client connection accepted and started proxy thread ExitPoll: " + Proxy.getName());
   }
      scon.end ();                                                   // operations termination
      System.err.println ("Server was shutdown.");
   }
}
