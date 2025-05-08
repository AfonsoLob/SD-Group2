package serverSide.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Properties;

import example.commInfra.ServerCom;
import example.commInfra.ServerCom.ServerComHandler;
import serverSide.entities.PLoggerProxy;
import serverSide.sharedRegions.Logger;
import serverSide.interfaces.Logger.ILogger_all;


public class ServerLogger {

    /**
     *  Flag signaling the service is active.
    */

    public static boolean waitConnection;

    /**
   *  Main method.
   *
   *    @param args runtime arguments
   *        args[0] - port nunber for listening to service requests
   */
    public static void main (String [] args)
    {
        final String CONFIG_FILE = "config.properties";

        ILogger_all repos;                                            // general repository of information (service to be rendered)
        ServerCom scon;
        ServerComHandler sconi;                                         // communication channels
        int portNumb = -1;                                             // port number for listening to service requests

        // Load default values from properties file
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            portNumb = Integer.parseInt(props.getProperty("reposPortNumber"));
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error reading configuration file!");
            System.exit(1);
        }
     /* service is established */

        repos = (ILogger_all) Logger.getInstance(1,1,1);                                   // service is instantiated

        scon = new ServerCom (portNumb);                               // listening channel at the public port is established
        scon.start ();
        System.out.println ("Service is established!");
        System.out.println ("Server is listening for service requests.");

     /* service request processing */

        PLoggerProxy cliProxy;                                  // service provider agent

        waitConnection = true;
        while (waitConnection){
            try{
                sconi = scon.accept ();                                              // enter listening procedure
                cliProxy = PLoggerProxy.getInstanceLoggerProxy(sconi, repos);          // start a service provider agent to address
                cliProxy.start ();                                                   //   the request of service
            }catch (SocketTimeoutException e) {
                System.out.println("Socket timeout exception: " + e.getMessage());
            }
        }
        scon.end ();                                                   // operations termination
        System.out.println ("Server was shutdown.");
    }
}
