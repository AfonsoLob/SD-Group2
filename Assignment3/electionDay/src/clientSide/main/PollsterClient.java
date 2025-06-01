package clientSide.main;

import clientSide.entities.TPollster; // Assuming TPollster entity exists
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import serverSide.interfaces.ExitPoll.IExitPoll_all;
import serverSide.interfaces.Register.IRegister;

public class PollsterClient {

    // RMI Registry connection parameters
    private static final String RMI_REGISTRY_HOSTNAME = "localhost";
    private static final int RMI_REGISTRY_PORT = 22350;
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds

    // Service names to look up via IRegister
    private static final String REGISTER_SERVICE_LOOKUP_NAME = "RegisterService";
    private static final String EXIT_POLL_SERVICE_NAME = "ExitPollService";
    
    // Assuming 1 Pollster for simplicity
    private static final int NUM_POLLSTERS = 1; 

    public static void main(String[] args) {
        System.out.println("Pollster Client starting...");

        IRegister registerServiceStub = null;
        IExitPoll_all exitPoll = null;

        // 1. Connect to RMI Registry and look up IRegister service (with retry)
        Registry rmiRegistry = null;
        while (registerServiceStub == null) {
            try {
                System.out.println("PollsterClient: Attempting to connect to RMI Registry at " +
                                   RMI_REGISTRY_HOSTNAME + ":" + RMI_REGISTRY_PORT +
                                   " to find '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                rmiRegistry = LocateRegistry.getRegistry(RMI_REGISTRY_HOSTNAME, RMI_REGISTRY_PORT);
                registerServiceStub = (IRegister) rmiRegistry.lookup(REGISTER_SERVICE_LOOKUP_NAME);
                System.out.println("PollsterClient: Successfully connected to '" + REGISTER_SERVICE_LOOKUP_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("PollsterClient: Failed to connect to '" + REGISTER_SERVICE_LOOKUP_NAME +
                                   "' via RMI Registry: " + e.getMessage());
                registerServiceStub = null;
            }
            if (registerServiceStub == null) {
                System.out.println("PollsterClient: Retrying IRegister lookup in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    System.err.println("PollsterClient: Sleep interrupted, retrying IRegister lookup immediately.");
                    Thread.currentThread().interrupt();
                    if (Thread.currentThread().isInterrupted()) { System.err.println("PollsterClient: Exiting due to interruption."); return; }
                }
            }
        }

        // 2. Look up ExitPollService via IRegister (with retry)
        while (exitPoll == null) {
            try {
                System.out.println("PollsterClient: Attempting to lookup '" + EXIT_POLL_SERVICE_NAME +
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                exitPoll = (IExitPoll_all) registerServiceStub.lookup(EXIT_POLL_SERVICE_NAME);
                System.out.println("PollsterClient: Successfully looked up '" + EXIT_POLL_SERVICE_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("PollsterClient: Failed to lookup '" + EXIT_POLL_SERVICE_NAME +
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "': " + e.getMessage());
                exitPoll = null;
            }
            if (exitPoll == null) {
                System.out.println("PollsterClient: Retrying ExitPollService lookup in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    System.err.println("PollsterClient: Sleep interrupted, retrying ExitPollService lookup immediately.");
                    Thread.currentThread().interrupt();
                    if (Thread.currentThread().isInterrupted()) { System.err.println("PollsterClient: Exiting due to interruption."); return; }
                }
            }
        }
        
        if (Thread.currentThread().isInterrupted()) {
            System.err.println("PollsterClient: Startup interrupted before creating pollster threads. Exiting.");
            return;
        }

        System.out.println("PollsterClient: ExitPoll service looked up. Creating pollster threads (if applicable)...");

        // 3. Create and start TPollster threads (if TPollster entity is used)
        // If PollsterClient directly interacts, this part would be different.
        TPollster[] pollsters = new TPollster[NUM_POLLSTERS];
        for (int i = 0; i < NUM_POLLSTERS; i++) {
            // TPollster constructor will need to be adapted.
            // It will take its ID (if any) and the exitPoll stub.
            pollsters[i] = TPollster.getInstance(exitPoll); // Fixed: TPollster only takes exitPoll parameter
            pollsters[i].start();
            System.out.println("PollsterClient: TPollster " + i + " created and started.");
        }

        // Wait for all pollster threads to complete
        System.out.println("PollsterClient: Waiting for all pollster threads to complete...");
        for (int i = 0; i < NUM_POLLSTERS; i++) {
            try {
                pollsters[i].join();
                System.out.println("PollsterClient: TPollster " + i + " has finished.");
            } catch (InterruptedException e) {
                System.err.println("PollsterClient: Interrupted while waiting for TPollster " + i + ".");
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        }

        System.out.println("PollsterClient: All pollster threads have completed. Exiting.");
    }
}
