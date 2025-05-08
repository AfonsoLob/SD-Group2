package clientSide.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import clientSide.entities.TVoter;
import clientSide.stubs.MPollingStationStub;
import example.serverSide.main.SimulPar;
import clientSide.stubs.MExitPollStub;

import clientSide.interfaces.Pollingstation.*;
import clientSide.interfaces.ExitPoll.*;

public class TvoterMain {
    public static void main(String[] args) {
        SimulPar.validateParameters();

        final String CONFIG_FILE = "config.properties";               // Path to the configuration 
        int ServerPollingStationPortNumber = -1;                       // port number for listening to service requests
        int ServerExitPollPortNumber = -1;                       // port number for listening to service requests
        String hostPollingStationName = "pollingStation";
        String hostExitPollName = "exitPoll";
        String reposServerName;                                        // name of the platform where is located the server for the general repository
        int reposPortNumb = -1;                                        // port nunber where the server for the general repository is listening to service requests

        // Load default values from properties file
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            hostPollingStationName = props.getProperty("hostPollingStationName");
            hostExitPollName = props.getProperty("hostExitPollName");
            ServerPollingStationPortNumber = Integer.parseInt(props.getProperty("ServerPollingStationPortNumber"));
            ServerExitPollPortNumber = Integer.parseInt(props.getProperty("ServerExitPollPortNumber"));
            reposPortNumb = Integer.parseInt(props.getProperty("reposPortNumber"));
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error reading configuration file!");
            System.exit(1);
        }
        
        // Stubs
        IPollingStation_Voter pollingStation = new MPollingStationStub(hostPollingStationName, ServerPollingStationPortNumber);
        IExitPoll_Voter exitPoll = new MExitPollStub(hostExitPollName, ServerExitPollPortNumber);
        
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
