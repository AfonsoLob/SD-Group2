package serverSide.main;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import serverSide.interfaces.Logger.ILogger_all; // Or the specific ILogger interface MPollingStation needs
import serverSide.interfaces.PollingStation.IPollingStation_all; // Assuming this is the remote interface for MPollingStation
import serverSide.interfaces.Register.IRegister;
import serverSide.sharedRegions.MPollingStation;

public class PollingStationServer {

    public static final String RMI_REGISTRY_HOSTNAME = "localhost";
    public static final int RMI_REGISTRY_PORT = 22350;
    public static final String REGISTER_SERVICE_LOOKUP_NAME = "RegisterService"; // To find IRegister
    public static final String LOGGER_SERVICE_NAME = "LoggerService"; // To lookup Logger via IRegister
    public static final String POLLING_STATION_SERVICE_NAME = "PollingStationService"; // To bind MPollingStation via IRegister
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds

    // Static references for cleanup
    private static IRegister registerServiceStub = null;
    private static IPollingStation_all pollingStation = null;
    private static volatile boolean shutdownRequested = false;

    public static void main(String[] args) {
        System.out.println("PollingStationServer starting...");

        // Add shutdown hook for proper cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("PollingStationServer: Shutdown hook triggered, performing cleanup...");
            performShutdown();
        }));

        ILogger_all loggerStub = null;

        // 1. Look up the IRegister service (with retry)
        Registry rmiRegistry = null;
        while (registerServiceStub == null && !shutdownRequested) {
            try {
                System.out.println("PollingStationServer: Attempting to connect to RMI Registry at " + RMI_REGISTRY_HOSTNAME + ":" + RMI_REGISTRY_PORT + " to find '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                rmiRegistry = LocateRegistry.getRegistry(RMI_REGISTRY_HOSTNAME, RMI_REGISTRY_PORT);
                registerServiceStub = (IRegister) rmiRegistry.lookup(REGISTER_SERVICE_LOOKUP_NAME);
                System.out.println("PollingStationServer: Successfully connected to '" + REGISTER_SERVICE_LOOKUP_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("PollingStationServer: Failed to connect to '" + REGISTER_SERVICE_LOOKUP_NAME +
                                   "' via RMI Registry: " + e.getMessage());
                registerServiceStub = null;
            }
            if (registerServiceStub == null && !shutdownRequested) {
                System.out.println("PollingStationServer: Retrying IRegister lookup in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    System.err.println("PollingStationServer: Sleep interrupted, retrying IRegister lookup immediately.");
                    Thread.currentThread().interrupt();
                    shutdownRequested = true;
                    return;
                }
            }
        }

        // 2. Look up LoggerService via IRegister (with retry)
        while (loggerStub == null) {
            try {
                System.out.println("PollingStationServer: Attempting to lookup '" + LOGGER_SERVICE_NAME +
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                // Assuming ILogger_all is the correct interface type for the logger service
                loggerStub = (ILogger_all) registerServiceStub.lookup(LOGGER_SERVICE_NAME);
                System.out.println("PollingStationServer: Successfully looked up '" + LOGGER_SERVICE_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("PollingStationServer: Failed to lookup '" + LOGGER_SERVICE_NAME +
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "': " + e.getMessage());
                loggerStub = null;
            }
            if (loggerStub == null) {
                System.out.println("PollingStationServer: Retrying LoggerService lookup in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    System.err.println("PollingStationServer: Sleep interrupted, retrying LoggerService lookup immediately.");
                    Thread.currentThread().interrupt();
                }
            }
        }

        // 3. Instantiate MPollingStation
        try {
            System.out.println("PollingStationServer: Creating MPollingStation instance...");
            
            pollingStation = MPollingStation.getInstance(loggerStub); // Or export if not extending UnicastRemoteObject directly
            System.out.println("PollingStationServer: MPollingStation instance created.");
        } catch (RemoteException e) { // If MPollingStation constructor throws RemoteException (e.g. UnicastRemoteObject)
            System.err.println("PollingStationServer: CRITICAL - Failed to instantiate MPollingStation: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // Cannot proceed
        }


        // 4. Bind MPollingStation to IRegister (with retry)
        boolean bound = false;
        while (!bound) {
            try {
                System.out.println("PollingStationServer: Attempting to bind '" + POLLING_STATION_SERVICE_NAME + "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                registerServiceStub.bind(POLLING_STATION_SERVICE_NAME, pollingStation);
                System.out.println("PollingStationServer: '" + POLLING_STATION_SERVICE_NAME + "' registered successfully.");
                bound = true;
            } catch (AlreadyBoundException e) {
                System.err.println("PollingStationServer: '" + POLLING_STATION_SERVICE_NAME + "' already bound in '" + REGISTER_SERVICE_LOOKUP_NAME + "'. Retrying...");
            } catch (RemoteException e) {
                System.err.println("PollingStationServer: RemoteException during RMI binding of '" +
                                   POLLING_STATION_SERVICE_NAME + "': " + e.getMessage());
            }

            if (!bound) {
                System.out.println("PollingStationServer: RMI binding for '" + POLLING_STATION_SERVICE_NAME +
                                   "' failed. Retrying in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    System.err.println("PollingStationServer: Sleep interrupted, retrying RMI binding immediately.");
                    Thread.currentThread().interrupt();
                }
            }
        }

        System.out.println("PollingStationServer: Service is now bound and running.");
        
        // Keep server running until shutdown is requested
        while (!shutdownRequested) {
            try {
                Thread.sleep(1000); // Check every second
            } catch (InterruptedException e) {
                System.out.println("PollingStationServer: Interrupted, shutting down...");
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("PollingStationServer: Main thread exiting.");
    }
    
    /**
     * Perform clean shutdown of the PollingStation service
     */
    private static void performShutdown() {
        shutdownRequested = true;
        
        try {
            if (registerServiceStub != null && pollingStation != null) {
                System.out.println("PollingStationServer: Unbinding " + POLLING_STATION_SERVICE_NAME + "...");
                try {
                    registerServiceStub.unbind(POLLING_STATION_SERVICE_NAME);
                    System.out.println("PollingStationServer: Successfully unbound " + POLLING_STATION_SERVICE_NAME);
                } catch (Exception e) {
                    System.err.println("PollingStationServer: Error unbinding service: " + e.getMessage());
                }
                
                System.out.println("PollingStationServer: Unexporting PollingStation object...");
                try {
                    java.rmi.server.UnicastRemoteObject.unexportObject(pollingStation, true);
                    System.out.println("PollingStationServer: Successfully unexported PollingStation object");
                } catch (Exception e) {
                    System.err.println("PollingStationServer: Error unexporting object: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("PollingStationServer: Error during shutdown: " + e.getMessage());
        }
        
        System.out.println("PollingStationServer: Shutdown complete.");
    }
}
