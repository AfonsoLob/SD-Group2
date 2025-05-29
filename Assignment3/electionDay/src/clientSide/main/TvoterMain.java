package clientSide.main;

import clientSide.entities.TVoter;
import clientSide.interfaces.ExitPoll.IExitPoll_Voter;
import clientSide.interfaces.Pollingstation.IPollingStation_Voter;
import clientSide.stubs.SExitPollStub;
import clientSide.stubs.SPollingStationStub;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import serverSide.main.SimulPar;

public class TvoterMain {
    public static void main(String[] args) {
        SimulPar.validateParameters();

        final String CONFIG_FILE = "config.properties";               // Path to the configuration 
        int ServerPollingStationPortNumber = -1;                       // port number for listening to service requests
        int ServerExitPollPortNumber = -1;                       // port number for listening to service requests
        int loggerPortNumber = -1;
        String hostPollingStationName = "localhost";       // default to localhost
        String hostExitPollName = "localhost";             // default to localhost
        String loggerHostName = "localhost";
        // String reposServerName;                                        // name of the platform where is located the server for the general repository
        // int reposPortNumb;                                        // port nunber where the server for the general repository is listening to service requests

        // Load default values from properties file
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            hostPollingStationName = props.getProperty("hostPollingStationName");
            hostExitPollName = props.getProperty("hostExitPollName");
            ServerPollingStationPortNumber = Integer.parseInt(props.getProperty("ServerPollingStationPortNumber"));
            ServerExitPollPortNumber = Integer.parseInt(props.getProperty("ServerExitPollPortNumber"));

            loggerHostName = props.getProperty("hostLoggerName");
            loggerPortNumber = Integer.parseInt(props.getProperty("reposPortNumber"));
            // reposPortNumb = Integer.parseInt(props.getProperty("reposPortNumber"));
        } catch (IOException | NumberFormatException e) {
            System.out.println("Voter main: Error reading configuration file!");
            System.exit(1);
        }
        
        // Get logger host and port from properties

        // Stubs
        IPollingStation_Voter pollingStation = new SPollingStationStub(hostPollingStationName, ServerPollingStationPortNumber, loggerHostName, loggerPortNumber);
        IExitPoll_Voter exitPoll = new SExitPollStub(hostExitPollName, ServerExitPollPortNumber, loggerHostName, loggerPortNumber);
        
        // Create and start voter threads
        Thread[] voters = new Thread[SimulPar.N];
        for (int i = 0; i < SimulPar.N; i++) {
            voters[i] = new Thread(TVoter.getInstance(i, pollingStation, exitPoll));
            voters[i].start();
        }

        // Wait for all voters to finish
        for (int i = 0; i < SimulPar.N; i++) {
            try {
                voters[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
