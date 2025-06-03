package serverSide.main;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import serverSide.interfaces.ExitPoll.IExitPoll_all; // Assuming this is the remote interface for MExitPoll
import serverSide.interfaces.PollingStation.IPollingStation_ExitPoll; // For polling station reference
import serverSide.interfaces.Logger.ILogger_all; // Or the specific ILogger interface MExitPoll needs
import serverSide.interfaces.Register.IRegister;
import serverSide.sharedRegions.MExitPoll;

public class ExitPollServer {

    public static final String RMI_REGISTRY_HOSTNAME = "localhost";
    public static final int RMI_REGISTRY_PORT = 22350;
    public static final String REGISTER_SERVICE_LOOKUP_NAME = "RegisterService"; // To find IRegister
    public static final String LOGGER_SERVICE_NAME = "LoggerService"; // To lookup Logger via IRegister
    public static final String POLLING_STATION_SERVICE_NAME = "PollingStationService"; // To lookup PollingStation via IRegister
    public static final String EXIT_POLL_SERVICE_NAME = "ExitPollService"; // To bind MExitPoll via IRegister
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds

    // Static references for cleanup
    private static IRegister registerServiceStub = null;
    private static IExitPoll_all exitPoll = null;
    private static volatile boolean shutdownRequested = false;

    public static void main(String[] args) {
        System.out.println("ExitPollServer starting...");

        // Add shutdown hook for proper cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("ExitPollServer: Shutdown hook triggered, performing cleanup...");
            performShutdown();
        }));

        ILogger_all loggerStub = null;

        // 1. Look up the IRegister service (with retry)
        while (registerServiceStub == null && !shutdownRequested) {
            try {
                System.out.println("ExitPollServer: Attempting to connect to RMI Registry at " +
                                   RMI_REGISTRY_HOSTNAME + ":" + RMI_REGISTRY_PORT +
                                   " to find '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                Registry rmiRegistry = LocateRegistry.getRegistry(RMI_REGISTRY_HOSTNAME, RMI_REGISTRY_PORT);
                registerServiceStub = (IRegister) rmiRegistry.lookup(REGISTER_SERVICE_LOOKUP_NAME);
                System.out.println("ExitPollServer: Successfully connected to '" + REGISTER_SERVICE_LOOKUP_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("ExitPollServer: Failed to connect to '" + REGISTER_SERVICE_LOOKUP_NAME +
                                   "' via RMI Registry: " + e.getMessage());
                registerServiceStub = null;
            }
            if (registerServiceStub == null && !shutdownRequested) {
                System.out.println("ExitPollServer: Retrying IRegister lookup in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    System.err.println("ExitPollServer: Sleep interrupted, retrying IRegister lookup immediately.");
                    Thread.currentThread().interrupt();
                    shutdownRequested = true;
                    return;
                }
            }
        }

        // 2. Look up LoggerService via IRegister (with retry)
        while (loggerStub == null && !shutdownRequested) {
            try {
                System.out.println("ExitPollServer: Attempting to lookup '" + LOGGER_SERVICE_NAME +
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                loggerStub = (ILogger_all) registerServiceStub.lookup(LOGGER_SERVICE_NAME);
                System.out.println("ExitPollServer: Successfully looked up '" + LOGGER_SERVICE_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("ExitPollServer: Failed to lookup '" + LOGGER_SERVICE_NAME +
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "': " + e.getMessage());
                loggerStub = null;
            }
            if (loggerStub == null && !shutdownRequested) {
                System.out.println("ExitPollServer: Retrying LoggerService lookup in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    System.err.println("ExitPollServer: Sleep interrupted, retrying LoggerService lookup immediately.");
                    Thread.currentThread().interrupt();
                    shutdownRequested = true;
                    return;
                }
            }
        }

        // 3. Instantiate MExitPoll
        try {
            // Assuming MExitPoll constructor takes the logger stub.
            // And MExitPoll itself implements IExitPoll_all or can be cast/exported.
            MExitPoll mExitPoll = MExitPoll.getInstance(loggerStub);
            exitPoll = (IExitPoll_all) mExitPoll; // Or export if not extending UnicastRemoteObject directly
            System.out.println("ExitPollServer: MExitPoll instance created.");
            
            // 3.1. Look up PollingStation service and set it on the exit poll
            IPollingStation_ExitPoll pollingStationStub = null;
            int retryCount = 0;
            int maxRetries = 10; // Try for up to 50 seconds (10 * 5 seconds)
            
            while (pollingStationStub == null && retryCount < maxRetries && !shutdownRequested) {
                try {
                    System.out.println("ExitPollServer: Attempting to lookup '" + POLLING_STATION_SERVICE_NAME + 
                                       "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "' (attempt " + (retryCount + 1) + "/" + maxRetries + ")...");
                    pollingStationStub = (IPollingStation_ExitPoll) registerServiceStub.lookup(POLLING_STATION_SERVICE_NAME);
                    System.out.println("ExitPollServer: Successfully looked up '" + POLLING_STATION_SERVICE_NAME + "'.");
                    
                    // Set the polling station reference on the exit poll for monitoring
                    mExitPoll.setPollingStationReference(pollingStationStub);
                    System.out.println("ExitPollServer: Polling station reference set on exit poll for monitoring.");
                    
                } catch (RemoteException | NotBoundException e) {
                    System.err.println("ExitPollServer: Failed to lookup '" + POLLING_STATION_SERVICE_NAME + 
                                       "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "': " + e.getMessage());
                    pollingStationStub = null;
                    retryCount++;
                    
                    if (retryCount < maxRetries) {
                        System.out.println("ExitPollServer: Retrying PollingStation lookup in " + RETRY_DELAY_MS / 1000 + " seconds...");
                        try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                            System.err.println("ExitPollServer: Sleep interrupted, stopping PollingStation lookup.");
                            Thread.currentThread().interrupt();
                            shutdownRequested = true;
                            break;
                        }
                    }
                }
            }
            
            if (pollingStationStub == null) {
                System.err.println("ExitPollServer: WARNING - Could not establish connection to PollingStation after " + 
                                   maxRetries + " attempts. Exit poll will not automatically close when polling station closes.");
            }
            
        } catch (RemoteException e) { // If MExitPoll constructor throws RemoteException
            System.err.println("ExitPollServer: CRITICAL - Failed to instantiate MExitPoll: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // Cannot proceed
        }

        // 4. Bind MExitPoll to IRegister (with retry)
        boolean bound = false;
        while (!bound) {
            try {
                System.out.println("ExitPollServer: Attempting to bind '" + EXIT_POLL_SERVICE_NAME +
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                registerServiceStub.bind(EXIT_POLL_SERVICE_NAME, exitPoll);
                System.out.println("ExitPollServer: '" + EXIT_POLL_SERVICE_NAME + "' registered successfully.");
                bound = true;
            } catch (AlreadyBoundException e) {
                System.err.println("ExitPollServer: '" + EXIT_POLL_SERVICE_NAME + "' already bound in '" +
                                   REGISTER_SERVICE_LOOKUP_NAME + "'. Retrying...");
            } catch (RemoteException e) {
                System.err.println("ExitPollServer: RemoteException during RMI binding of '" +
                                   EXIT_POLL_SERVICE_NAME + "': " + e.getMessage());
            }

            if (!bound) {
                System.out.println("ExitPollServer: RMI binding for '" + EXIT_POLL_SERVICE_NAME +
                                   "' failed. Retrying in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    System.err.println("ExitPollServer: Sleep interrupted, retrying RMI binding immediately.");
                    Thread.currentThread().interrupt();
                }
            }
        }

        System.out.println("ExitPollServer: Service is now bound and running.");
        
        // Keep server running until shutdown is requested or exit poll closes
        while (!shutdownRequested) {
            try {
                Thread.sleep(1000); // Check every second
                
                // Check if exit poll is closed and shutdown accordingly
                if (exitPoll != null) {
                    try {
                        if (!exitPoll.isOpen()) {
                            System.out.println("ExitPollServer: Exit poll is closed, initiating shutdown...");
                            shutdownRequested = true;
                            break;
                        }
                    } catch (RemoteException e) {
                        System.err.println("ExitPollServer: Error checking exit poll status: " + e.getMessage());
                        // Continue running even if we can't check status
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("ExitPollServer: Interrupted, shutting down...");
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("ExitPollServer: Main thread exiting.");
    }
    
    /**
     * Perform clean shutdown of the ExitPoll service
     */
    private static void performShutdown() {
        shutdownRequested = true;
        
        try {
            if (registerServiceStub != null && exitPoll != null) {
                System.out.println("ExitPollServer: Unbinding " + EXIT_POLL_SERVICE_NAME + "...");
                try {
                    registerServiceStub.unbind(EXIT_POLL_SERVICE_NAME);
                    System.out.println("ExitPollServer: Successfully unbound " + EXIT_POLL_SERVICE_NAME);
                } catch (Exception e) {
                    System.err.println("ExitPollServer: Error unbinding service: " + e.getMessage());
                }
                
                System.out.println("ExitPollServer: Unexporting ExitPoll object...");
                try {
                    java.rmi.server.UnicastRemoteObject.unexportObject(exitPoll, true);
                    System.out.println("ExitPollServer: Successfully unexported ExitPoll object");
                } catch (Exception e) {
                    System.err.println("ExitPollServer: Error unexporting object: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("ExitPollServer: Error during shutdown: " + e.getMessage());
        }
        
        System.out.println("ExitPollServer: Shutdown complete.");
    }
}
