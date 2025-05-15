package clientSide.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import clientSide.entities.TClerk;
import clientSide.stubs.SPollingStationStub;
import serverSide.main.SimulPar;
import clientSide.stubs.SExitPollStub;
import clientSide.interfaces.Pollingstation.IPollingStation_Clerk;
import clientSide.interfaces.ExitPoll.IExitPoll_Clerk;

public class TClerkmain {
    public static void main(String[] args) {
        SimulPar.validateParameters();

        final String CONFIG_FILE = "config.properties";               // Path to the configuration 
        int ServerPollingStationPortNumber = -1;                     // port number for listening to service requests
        int ServerExitPollPortNumber = -1;                           // port number for listening to service requests
        String hostPollingStationName = "localhost";                // name of the platform where is located the polling station server
        String hostExitPollName = "localhost";                        // name of the platform where is located the exit poll server

        String hostLoggerName = "localhost";                          // name of the platform where is located the logger server
        int hostLoggerPortNumber = 0;                                // port number for listening to service requests

        // Load default values from properties file
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            hostPollingStationName = props.getProperty("hostPollingStationName");
            hostExitPollName = props.getProperty("hostExitPollName");
            ServerPollingStationPortNumber = Integer.parseInt(props.getProperty("ServerPollingStationPortNumber"));
            ServerExitPollPortNumber = Integer.parseInt(props.getProperty("ServerExitPollPortNumber"));
            
            hostLoggerName = props.getProperty("hostLoggerName");
            hostLoggerPortNumber = Integer.parseInt(props.getProperty("reposPortNumber"));

        } catch (IOException | NumberFormatException e) {
            System.out.println("Clerk main: Error reading configuration file!");
            System.exit(1);
        }
        
        // Stubs
        IPollingStation_Clerk pollingStation = new SPollingStationStub(hostPollingStationName, ServerPollingStationPortNumber, hostLoggerName, hostLoggerPortNumber);
        IExitPoll_Clerk exitPoll = new SExitPollStub(hostExitPollName, ServerExitPollPortNumber, hostLoggerName, hostLoggerPortNumber);
        
        // Create and start clerk thread
        Thread clerk = new Thread(TClerk.getInstance(SimulPar.MAX_VOTES, pollingStation, exitPoll));
        clerk.start();

        // Wait for clerk to finish
        try {
            clerk.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
} 