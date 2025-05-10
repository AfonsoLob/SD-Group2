package clientSide.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import clientSide.entities.TPollster;
import clientSide.stubs.MExitPollStub;
import serverSide.main.SimulPar;
import clientSide.interfaces.ExitPoll.IExitPoll_Pollster;

public class TpollsterMain {
    public static void main(String[] args) {
        SimulPar.validateParameters();

        final String CONFIG_FILE = "config.properties";               // Path to the configuration 
        int ServerExitPollPortNumber = -1;                           // port number for listening to service requests
        String hostExitPollName = "exitPoll";                        // name of the platform where is located the exit poll server

        // Load default values from properties file
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            hostExitPollName = props.getProperty("hostExitPollName");
            ServerExitPollPortNumber = Integer.parseInt(props.getProperty("ServerExitPollPortNumber"));
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error reading configuration file!");
            System.exit(1);
        }
        
        // Stub
        IExitPoll_Pollster exitPoll = new MExitPollStub(hostExitPollName, ServerExitPollPortNumber);
        
        // Create and start pollster thread
        Thread pollster = new Thread(TPollster.getInstance(exitPoll));
        pollster.start();

        // Wait for pollster to finish
        try {
            pollster.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
