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
  
      // Override with command line arguments if provided
    //   if (args.length == 3) {
    //       try {
    //         ServerExitPollPortNumber = Integer.parseInt(args[0]);
    //           reposServerName = args[1];
    //           reposPortNumb = Integer.parseInt(args[2]);
    //       } catch (NumberFormatException e) {
    //           System.err.println("Invalid number format in arguments!");
    //           System.exit(1);
    //       }
    //   }

    //   // Validate port numbers
    //   if ((ServerExitPollPortNumber < 4000) || (ServerExitPollPortNumber >= 65536)) {
    //       System.err.println("Invalid port number!");
    //       System.exit(1);
    //   }
    //   if ((reposPortNumb < 4000) || (reposPortNumb >= 65536)) {
    //       System.err.println("Invalid repository port number!");
    //       System.exit(1);
    //   }

     /* service is established */

    //   reposStub = new GeneralReposStub (reposServerName, reposPortNumb);     // communication to the general repository is instantiated
    //   pollingStation = new MPollingStation (reposStub);                      // service is instantiated
    //   pStationInter = new PollingStationInterface(pollingStation);             // interface to the service is instantiated

      exitPoll = MExitPoll.getInstance(SimulPar.EXIT_POLL_PERCENTAGE);                      // service is instantiated
       
      scon = new ServerCom (ServerExitPollPortNumber);                    // listening channel at the public port is established
      scon.start();
      System.err.println ("Service is established!");
      System.err.println ("Server is listening for service requests.");

     /* service request processing */

      PExitPollProxy Proxy;                                // service provider agent

      waitConnection = true;
      while (waitConnection)
      {   sconi = scon.accept();                                    // enter listening procedure
      Proxy = new PExitPollProxy(sconi, exitPoll);           // service provider agent is generated
      Proxy.start();                                         //   the request of service
      }
      scon.end ();                                                   // operations termination
      System.err.println ("Server was shutdown.");
   }
}
