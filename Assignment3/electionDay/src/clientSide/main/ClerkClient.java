package clientSide.main;

import clientSide.entities.TClerk;
import interfaces.ExitPoll.IExitPoll_Clerk;
import interfaces.PollingStation.IPollingStation_Clerk;
import interfaces.Register.IRegister;
import java.rmi.NotBoundException;
import java.rmi.RemoteException; // Assuming TClerk entity exists
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClerkClient {

    // RMI Registry connection parameters
    private static final String RMI_REGISTRY_HOSTNAME = "localhost";
    private static final int RMI_REGISTRY_PORT = 22350;
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds

    // Service names to look up via IRegister
    private static final String REGISTER_SERVICE_LOOKUP_NAME = "RegisterService";
    private static final String POLLING_STATION_SERVICE_NAME = "PollingStationService";
    private static final String EXIT_POLL_SERVICE_NAME = "ExitPollService";
    
    // Typically, there's a fixed number of clerks, e.g., 1 or 2
    // This could be a constant or fetched if dynamic (though less common for clerks)
    private static final int NUM_CLERKS = 1; // Example: 1 Clerk

    public static void main(String[] args) {
        System.out.println("Clerk Client starting...");

        IRegister registerServiceStub = null;
        IPollingStation_Clerk pollingStation = null;
        IExitPoll_Clerk exitPoll = null;

        while (registerServiceStub == null) {
            try {
                System.out.println("ClerkClient: Attempting to connect to RMI Registry at " + RMI_REGISTRY_HOSTNAME + ":" + RMI_REGISTRY_PORT + " to find '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                Registry rmiRegistry = LocateRegistry.getRegistry(RMI_REGISTRY_HOSTNAME, RMI_REGISTRY_PORT);
                registerServiceStub = (IRegister) rmiRegistry.lookup(REGISTER_SERVICE_LOOKUP_NAME);
                System.out.println("ClerkClient: Successfully connected to '" + REGISTER_SERVICE_LOOKUP_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("ClerkClient: Failed to connect to '" + REGISTER_SERVICE_LOOKUP_NAME + "' via RMI Registry: " + e.getMessage());
                registerServiceStub = null;
            }
            if (registerServiceStub == null) {
                System.out.println("ClerkClient: Retrying IRegister lookup in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    System.err.println("ClerkClient: Sleep interrupted, retrying IRegister lookup immediately.");
                    Thread.currentThread().interrupt();
                    if (Thread.currentThread().isInterrupted()) { System.err.println("ClerkClient: Exiting due to interruption."); return; }
                }
            }
        }

        while (pollingStation == null) {
            try {
                System.out.println("ClerkClient: Attempting to lookup '" + POLLING_STATION_SERVICE_NAME + "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                pollingStation = (IPollingStation_Clerk) registerServiceStub.lookup(POLLING_STATION_SERVICE_NAME);
                System.out.println("ClerkClient: Successfully looked up '" + POLLING_STATION_SERVICE_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("ClerkClient: Failed to lookup '" + POLLING_STATION_SERVICE_NAME + "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "': " + e.getMessage());
                pollingStation = null;
            }
            if (pollingStation == null) {
                System.out.println("ClerkClient: Retrying PollingStationService lookup in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    System.err.println("ClerkClient: Sleep interrupted, retrying PollingStationService lookup immediately.");
                    Thread.currentThread().interrupt();
                    if (Thread.currentThread().isInterrupted()) { System.err.println("ClerkClient: Exiting due to interruption."); return; }
                }
            }
        }
        
        while (exitPoll == null) {
            try {
                System.out.println("ClerkClient: Attempting to lookup '" + EXIT_POLL_SERVICE_NAME + "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                exitPoll = (IExitPoll_Clerk) registerServiceStub.lookup(EXIT_POLL_SERVICE_NAME);
                System.out.println("ClerkClient: Successfully looked up '" + EXIT_POLL_SERVICE_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("ClerkClient: Failed to lookup '" + EXIT_POLL_SERVICE_NAME + "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "': " + e.getMessage());
                exitPoll = null;
            }
            if (exitPoll == null) {
                System.out.println("ClerkClient: Retrying ExitPollService lookup in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    System.err.println("ClerkClient: Sleep interrupted, retrying ExitPollService lookup immediately.");
                    Thread.currentThread().interrupt();
                    if (Thread.currentThread().isInterrupted()) { System.err.println("ClerkClient: Exiting due to interruption."); return; }
                }
            }
        }
        
        if (Thread.currentThread().isInterrupted()) {
            System.err.println("ClerkClient: Startup interrupted before creating clerk threads. Exiting.");
            return;
        }

        System.out.println("ClerkClient: PollingStation and ExitPoll services looked up. Creating clerk threads...");

        TClerk[] clerks = new TClerk[NUM_CLERKS];
        for (int i = 0; i < NUM_CLERKS; i++) {
            // TClerk constructor takes only the pollingStation stub
            clerks[i] = TClerk.getInstance(pollingStation, exitPoll);
            clerks[i].run();
            System.out.println("ClerkClient: TClerk " + i + " created and started.");
        }

        System.out.println("ClerkClient: Waiting for all clerk threads to complete...");
        for (int i = 0; i < NUM_CLERKS; i++) {
            try {
                clerks[i].join();
                System.out.println("ClerkClient: TClerk " + i + " has finished.");
            } catch (InterruptedException e) {
                System.err.println("ClerkClient: Interrupted while waiting for TClerk " + i + ".");
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        }

        System.out.println("ClerkClient: All clerk threads have completed. Exiting.");
    }
}
