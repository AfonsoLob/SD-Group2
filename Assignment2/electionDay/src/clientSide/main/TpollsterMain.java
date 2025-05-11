package clientSide.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import clientSide.entities.TPollster;
import clientSide.interfaces.ExitPoll.IExitPoll_Pollster;
import clientSide.stubs.SExitPollStub;
import serverSide.main.SimulPar;

public class TpollsterMain {
    public static void main(String[] args) {
        SimulPar.validateParameters();

        final String CONFIG_FILE = "config.properties";               // Path to the configuration 
        int ServerExitPollPortNumber = -1;                           // port number for listening to service requests
        String hostExitPollName = "localhost";                        // name of the platform where is located the exit poll server
        String loggerHostName = "localhost";
        int loggerPortNumber = -1;

        // Load default values from properties file
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            hostExitPollName = props.getProperty("hostExitPollName");
            ServerExitPollPortNumber = Integer.parseInt(props.getProperty("ServerExitPollPortNumber"));
            loggerHostName = props.getProperty("loggerHostName");
            loggerPortNumber = Integer.parseInt(props.getProperty("loggerPortNumber"));
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error reading configuration file!");
            System.exit(1);
        }
        
        // Stub
        IExitPoll_Pollster exitPoll = new SExitPollStub(hostExitPollName, ServerExitPollPortNumber, loggerHostName, loggerPortNumber);
        
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
