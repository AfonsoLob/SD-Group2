package serverSide.main;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.SwingUtilities;
import serverSide.GUI.Gui;
import interfaces.Logger.ILogger_all;
import interfaces.Register.IRegister; // Import IRegister
import serverSide.sharedRegions.Logger;

public class LoggerServer {

    public static final String RMI_REGISTRY_HOSTNAME = "localhost";
    public static final int RMI_REGISTRY_PORT = 22350;
    public static final String LOGGER_SERVICE_NAME = "LoggerService";
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds

    // Name of the RegisterService, must match RegisterRemoteObject.REGISTER_SERVICE_NAME
    public static final String REGISTER_SERVICE_LOOKUP_NAME = "RegisterService";

    // Shared instance of the logger, to be configured by GUI then bound by RMI setup
    private static volatile Logger loggerInstance = null;
    private static volatile boolean parametersSet = false;
    private static final Object lock = new Object();

    public static void main(String[] args) {
        System.out.println("Main Simulation Orchestrator (LoggerServer) starting...");

        // Launch GUI. The GUI will be responsible for instantiating the Logger
        // once parameters are entered, and then notifying LoggerServer to bind it.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("Launching Election Day GUI...");
                Gui.window();
            }
        });

        System.out.println("GUI launched. Waiting for parameters to be set via GUI and Logger to be instantiated...");

        // Wait for GUI to signal that parameters are set and Logger is instantiated
        synchronized (lock) {
            while (loggerInstance == null || !parametersSet) {
                try {
                    System.out.println("LoggerServer: Waiting for GUI to provide Logger instance and confirm parameters...");
                    lock.wait(RETRY_DELAY_MS); // Wait with a timeout to periodically print status
                } catch (InterruptedException e) {
                    System.err.println("LoggerServer: Interrupted while waiting for GUI. Exiting.");
                    Thread.currentThread().interrupt();
                    return;
                }
                if (loggerInstance != null && parametersSet) {
                    System.out.println("LoggerServer: Logger instance received from GUI and parameters are set.");
                }
            }
        }

        // At this point, loggerInstance should be non-null and configured.
        final ILogger_all rmiLoggerService = loggerInstance;

        // 1. Look up the IRegister service (with retry)
        IRegister registerServiceStub = null;
        Registry rmiRegistry = null; // Standard RMI registry
        while (registerServiceStub == null) {
            try {
                System.out.println("LoggerServer: Attempting to connect to RMI Registry at " +
                                   RMI_REGISTRY_HOSTNAME + ":" + RMI_REGISTRY_PORT + 
                                   " to find '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                rmiRegistry = LocateRegistry.getRegistry(RMI_REGISTRY_HOSTNAME, RMI_REGISTRY_PORT);
                registerServiceStub = (IRegister) rmiRegistry.lookup(REGISTER_SERVICE_LOOKUP_NAME);
                System.out.println("LoggerServer: Successfully connected to '" + REGISTER_SERVICE_LOOKUP_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("LoggerServer: Failed to connect to '" + REGISTER_SERVICE_LOOKUP_NAME + 
                                   "' via RMI Registry: " + e.getMessage());
                registerServiceStub = null; // Ensure retry
            }

            if (registerServiceStub == null) {
                System.out.println("LoggerServer: Retrying connection to '" + REGISTER_SERVICE_LOOKUP_NAME + 
                                   "' in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    System.err.println("LoggerServer: Sleep interrupted, retrying IRegister lookup immediately.");
                    Thread.currentThread().interrupt();
                }
            }
        }

        // 2. Bind LoggerService using the IRegister service (with retry)
        boolean bound = false;
        while (!bound) {
            try {
                System.out.println("LoggerServer: Attempting to bind '" + LOGGER_SERVICE_NAME + 
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                registerServiceStub.bind(LOGGER_SERVICE_NAME, rmiLoggerService);
                System.out.println("LoggerServer: '" + LOGGER_SERVICE_NAME + "' bound successfully via '" + 
                                   REGISTER_SERVICE_LOOKUP_NAME + "'.");
                System.out.println("LoggerServer: Logger Service is now registered and ready.");
                bound = true; 
            } catch (AlreadyBoundException e) {
                System.err.println("LoggerServer: '" + LOGGER_SERVICE_NAME + 
                                   "' already bound in '" + REGISTER_SERVICE_LOOKUP_NAME + 
                                   "'. Assuming another instance is active or shutting down. Retrying...");
            } catch (RemoteException e) {
                System.err.println("LoggerServer: RemoteException during binding '" + LOGGER_SERVICE_NAME + 
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "': " + e.getMessage());
            }

            if (!bound) {
                System.out.println("LoggerServer: Binding of '" + LOGGER_SERVICE_NAME + 
                                   "' failed. Retrying in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    System.err.println("LoggerServer: Sleep interrupted, retrying binding immediately.");
                    Thread.currentThread().interrupt();
                }
            }
        }

        System.out.println("\n[[ Orchestrator Note ]]");
        System.out.println("Ensure RegisterRemoteObject service is running.");
        System.out.println("Then ensure PollingStationServer and ExitPollServer are started. Dependant services:");
        System.out.println(" - PollingStationServer (needs RegisterService, LoggerService)");
        System.out.println(" - ExitPollServer (needs RegisterService, LoggerService)");
        System.out.println("\nThen, start the client applications. Dependant services:");
        System.out.println(" - VoterClient (needs RegisterService, PollingStationService, ExitPollService)");
        System.out.println(" - ClerkClient (needs RegisterService, PollingStationService)");
        System.out.println(" - PollsterClient (needs RegisterService, ExitPollService)");
        System.out.println("\nMain Simulation Orchestrator (LoggerServer) setup complete. Logger is registered and running.");
    }

    // Method to be called by the GUI after parameters are set and Logger is instantiated
    public static void initializeLoggerService(Logger instance, boolean paramsAreSet) {
        synchronized (lock) {
            loggerInstance = instance;
            parametersSet = paramsAreSet;
            if (loggerInstance != null && parametersSet) {
                System.out.println("LoggerServer.initializeLoggerService: Logger instance and parameters have been set. Notifying main thread.");
                lock.notifyAll();
            }
        }
    }
}
