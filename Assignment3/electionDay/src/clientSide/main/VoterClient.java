package clientSide.main;

import clientSide.entities.TVoter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import interfaces.ExitPoll.IExitPoll_all;
import interfaces.PollingStation.IPollingStation_all;
import interfaces.Register.IRegister; // For the registration service

public class VoterClient {

    // RMI Registry connection parameters
    private static final String RMI_REGISTRY_HOSTNAME = "localhost";
    private static final int RMI_REGISTRY_PORT = 22350;
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds

    // Service names to look up via IRegister
    private static final String REGISTER_SERVICE_LOOKUP_NAME = "RegisterService";
    private static final String POLLING_STATION_SERVICE_NAME = "PollingStationService";
    private static final String EXIT_POLL_SERVICE_NAME = "ExitPollService";

    public static void main(String[] args) {
        System.out.println("Voter Client starting...");

        IRegister registerServiceStub = null;
        IPollingStation_all pollingStation = null;
        IExitPoll_all exitPoll = null;
        int numVoters = -1;

        // 1. Connect to RMI Registry and look up IRegister service (with retry)
        Registry rmiRegistry = null;
        while (registerServiceStub == null) {
            try {
                System.out.println("VoterClient: Attempting to connect to RMI Registry at " +
                                   RMI_REGISTRY_HOSTNAME + ":" + RMI_REGISTRY_PORT +
                                   " to find '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                rmiRegistry = LocateRegistry.getRegistry(RMI_REGISTRY_HOSTNAME, RMI_REGISTRY_PORT);
                registerServiceStub = (IRegister) rmiRegistry.lookup(REGISTER_SERVICE_LOOKUP_NAME);
                System.out.println("VoterClient: Successfully connected to '" + REGISTER_SERVICE_LOOKUP_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("VoterClient: Failed to connect to '" + REGISTER_SERVICE_LOOKUP_NAME +
                                   "' via RMI Registry: " + e.getMessage());
                registerServiceStub = null;
            }
            if (registerServiceStub == null) {
                System.out.println("VoterClient: Retrying IRegister lookup in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    System.err.println("VoterClient: Sleep interrupted, retrying IRegister lookup immediately.");
                    Thread.currentThread().interrupt();
                    if (Thread.currentThread().isInterrupted()) { System.err.println("VoterClient: Exiting due to interruption."); return; }
                }
            }
        }

        // 2. Look up PollingStationService via IRegister (with retry)
        while (pollingStation == null) {
            try {
                System.out.println("VoterClient: Attempting to lookup '" + POLLING_STATION_SERVICE_NAME +
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                pollingStation = (IPollingStation_all) registerServiceStub.lookup(POLLING_STATION_SERVICE_NAME);
                System.out.println("VoterClient: Successfully looked up '" + POLLING_STATION_SERVICE_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("VoterClient: Failed to lookup '" + POLLING_STATION_SERVICE_NAME +
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "': " + e.getMessage());
                pollingStation = null;
            }
            if (pollingStation == null) {
                System.out.println("VoterClient: Retrying PollingStationService lookup in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    System.err.println("VoterClient: Sleep interrupted, retrying PollingStationService lookup immediately.");
                    Thread.currentThread().interrupt();
                    if (Thread.currentThread().isInterrupted()) { System.err.println("VoterClient: Exiting due to interruption."); return; }
                }
            }
        }

        // 3. Look up ExitPollService via IRegister (with retry)
        while (exitPoll == null) {
            try {
                System.out.println("VoterClient: Attempting to lookup '" + EXIT_POLL_SERVICE_NAME +
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                exitPoll = (IExitPoll_all) registerServiceStub.lookup(EXIT_POLL_SERVICE_NAME);
                System.out.println("VoterClient: Successfully looked up '" + EXIT_POLL_SERVICE_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("VoterClient: Failed to lookup '" + EXIT_POLL_SERVICE_NAME +
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "': " + e.getMessage());
                exitPoll = null;
            }
            if (exitPoll == null) {
                System.out.println("VoterClient: Retrying ExitPollService lookup in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    System.err.println("VoterClient: Sleep interrupted, retrying ExitPollService lookup immediately.");
                    Thread.currentThread().interrupt();
                    if (Thread.currentThread().isInterrupted()) { System.err.println("VoterClient: Exiting due to interruption."); return; }
                }
            }
        }

        // 4. Get number of voters from PollingStation (with retry)
        // This assumes PollingStation has a method like getNumberOfVotersConfigured()
        // which in turn gets it from the Logger.
        while (numVoters < 0) {
            try {
                System.out.println("VoterClient: Attempting to get number of voters from '" + POLLING_STATION_SERVICE_NAME + "'...");
                numVoters = pollingStation.getNumberOfVotersConfigured(); // Method to be added to IPollingStation_all
                if (numVoters >= 0) {
                    System.out.println("VoterClient: Number of voters configured: " + numVoters);
                } else {
                    System.out.println("VoterClient: PollingStation returned invalid number of voters (" + numVoters + "). Waiting for valid configuration...");
                    // numVoters remains < 0, loop will retry
                }
            } catch (RemoteException e) {
                System.err.println("VoterClient: Error getting number of voters from '" + POLLING_STATION_SERVICE_NAME + "': " + e.getMessage());
                numVoters = -1; // Ensure retry
            } catch (NullPointerException e) {
                 System.err.println("VoterClient: PollingStation stub is null while trying to get numVoters. This shouldn't happen if lookup succeeded.");
                 numVoters = -1; // Ensure retry, though this indicates a deeper issue.
            }
            if (numVoters < 0) {
                System.out.println("VoterClient: Retrying getNumberOfVotersConfigured in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    System.err.println("VoterClient: Sleep interrupted, retrying getNumberOfVotersConfigured immediately.");
                    Thread.currentThread().interrupt();
                    if (Thread.currentThread().isInterrupted()) { System.err.println("VoterClient: Exiting due to interruption."); return; }
                }
            }
        }

        if (Thread.currentThread().isInterrupted()) {
            System.err.println("VoterClient: Startup interrupted before creating voter threads. Exiting.");
            return;
        }

        System.out.println("VoterClient: All services looked up. Number of voters: " + numVoters + ". Creating voter threads...");

        // 5. Create and start TVoter threads
        TVoter[] voters = new TVoter[numVoters];
        for (int i = 0; i < numVoters; i++) {
            // TVoter constructor will need to be adapted to not take a logger instance.
            // For now, assuming TVoter(int, IPollingStation_all, IExitPoll_all)
            voters[i] = new TVoter(i, pollingStation, exitPoll);
            voters[i].start();
            System.out.println("VoterClient: TVoter " + i + " created and started.");
        }

        // Wait for all voter threads to complete
        System.out.println("VoterClient: Waiting for all voter threads to complete...");
        for (int i = 0; i < numVoters; i++) {
            try {
                voters[i].join();
                System.out.println("VoterClient: TVoter " + i + " has finished.");
            } catch (InterruptedException e) {
                System.err.println("VoterClient: Interrupted while waiting for TVoter " + i + ".");
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        }

        System.out.println("VoterClient: All voter threads have completed. Exiting.");
    }
}
