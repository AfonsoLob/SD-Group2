package clientSide.main;

import clientSide.entities.TClerk;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry; // Assuming TClerk entity exists
import serverSide.interfaces.PollingStation.IPollingStation_all;
import serverSide.interfaces.Register.IRegister;

public class ClerkClient {

    // RMI Registry connection parameters
    private static final String RMI_REGISTRY_HOSTNAME = "localhost";
    private static final int RMI_REGISTRY_PORT = 22350;
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds

    // Service names to look up via IRegister
    private static final String REGISTER_SERVICE_LOOKUP_NAME = "RegisterService";
    private static final String POLLING_STATION_SERVICE_NAME = "PollingStationService";
    
    // Typically, there's a fixed number of clerks, e.g., 1 or 2
    // This could be a constant or fetched if dynamic (though less common for clerks)
    private static final int NUM_CLERKS = 1; // Example: 1 Clerk

    public static void main(String[] args) {
        System.out.println("Clerk Client starting...");

        IRegister registerServiceStub = null;
        IPollingStation_all pollingStation = null;

        // 1. Connect to RMI Registry and look up IRegister service (with retry)
        Registry rmiRegistry = null;
        while (registerServiceStub == null) {
            try {
                System.out.println("ClerkClient: Attempting to connect to RMI Registry at " +
                                   RMI_REGISTRY_HOSTNAME + ":" + RMI_REGISTRY_PORT +
                                   " to find '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                rmiRegistry = LocateRegistry.getRegistry(RMI_REGISTRY_HOSTNAME, RMI_REGISTRY_PORT);
                registerServiceStub = (IRegister) rmiRegistry.lookup(REGISTER_SERVICE_LOOKUP_NAME);
                System.out.println("ClerkClient: Successfully connected to '" + REGISTER_SERVICE_LOOKUP_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("ClerkClient: Failed to connect to '" + REGISTER_SERVICE_LOOKUP_NAME +
                                   "' via RMI Registry: " + e.getMessage());
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

        // 2. Look up PollingStationService via IRegister (with retry)
        while (pollingStation == null) {
            try {
                System.out.println("ClerkClient: Attempting to lookup '" + POLLING_STATION_SERVICE_NAME +
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                pollingStation = (IPollingStation_all) registerServiceStub.lookup(POLLING_STATION_SERVICE_NAME);
                System.out.println("ClerkClient: Successfully looked up '" + POLLING_STATION_SERVICE_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("ClerkClient: Failed to lookup '" + POLLING_STATION_SERVICE_NAME +
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "': " + e.getMessage());
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
        
        if (Thread.currentThread().isInterrupted()) {
            System.err.println("ClerkClient: Startup interrupted before creating clerk threads. Exiting.");
            return;
        }

        System.out.println("ClerkClient: PollingStation service looked up. Creating clerk threads...");

        // 3. Create and start TClerk threads
        TClerk[] clerks = new TClerk[NUM_CLERKS];
        for (int i = 0; i < NUM_CLERKS; i++) {
            // TClerk constructor will need to be adapted to not take a logger instance.
            // It will take its ID and the pollingStation stub.
            clerks[i] = new TClerk( pollingStation,i);
            clerks[i].start();
            System.out.println("ClerkClient: TClerk " + i + " created and started.");
        }

        // Wait for all clerk threads to complete
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
