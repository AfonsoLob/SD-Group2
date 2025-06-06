package serverSide.main;

import interfaces.ExitPoll.IExitPoll_all;
import interfaces.Logger.ILogger_ExitPoll;
import interfaces.Register.IRegister;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import serverSide.sharedRegions.MExitPoll;

public class ExitPollServer {

    public static final String RMI_REGISTRY_HOSTNAME = "localhost";
    public static final int RMI_REGISTRY_PORT = 22350;
    public static final String REGISTER_SERVICE_LOOKUP_NAME = "RegisterService"; // To find IRegister
    public static final String LOGGER_SERVICE_NAME = "LoggerService"; // To lookup Logger via IRegister
    public static final String EXIT_POLL_SERVICE_NAME = "ExitPollService"; // To bind MExitPoll via IRegister
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds

    // Static references for cleanup
    private static IRegister registerServiceStub = null;
    private static IExitPoll_all exitPoll = null;
    private static volatile boolean shutdownRequested = false;

    public static void main(String[] args) {
        System.out.println("ExitPollServer starting...");


        ILogger_ExitPoll loggerStub = null;

        while (registerServiceStub == null && !shutdownRequested) {
            try {
                System.out.println("ExitPollServer: Attempting to connect to RMI Registry at " + RMI_REGISTRY_HOSTNAME + ":" + RMI_REGISTRY_PORT + " to find '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                Registry rmiRegistry = LocateRegistry.getRegistry(RMI_REGISTRY_HOSTNAME, RMI_REGISTRY_PORT);
                registerServiceStub = (IRegister) rmiRegistry.lookup(REGISTER_SERVICE_LOOKUP_NAME);
                System.out.println("ExitPollServer: Successfully connected to '" + REGISTER_SERVICE_LOOKUP_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("ExitPollServer: Failed to connect to '" + REGISTER_SERVICE_LOOKUP_NAME + "' via RMI Registry: " + e.getMessage());
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

        while (loggerStub == null && !shutdownRequested) {
            try {
                System.out.println("ExitPollServer: Attempting to lookup '" + LOGGER_SERVICE_NAME + "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                loggerStub = (ILogger_ExitPoll) registerServiceStub.lookup(LOGGER_SERVICE_NAME);
                System.out.println("ExitPollServer: Successfully looked up '" + LOGGER_SERVICE_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("ExitPollServer: Failed to lookup '" + LOGGER_SERVICE_NAME + "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "': " + e.getMessage());
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

        try {
            // Assuming MExitPoll constructor takes the logger stub.
            // And MExitPoll itself implements IExitPoll_all or can be cast/exported.
            MExitPoll mExitPoll = MExitPoll.getInstance(loggerStub);
            exitPoll = (IExitPoll_all) mExitPoll; // Or export if not extending UnicastRemoteObject directly
            System.out.println("ExitPollServer: MExitPoll instance created.");
        } catch (RemoteException e) { // If MExitPoll constructor throws RemoteException
            System.err.println("ExitPollServer: CRITICAL - Failed to instantiate MExitPoll: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // Cannot proceed
        }

        // 4. Bind MExitPoll to IRegister (with retry)
        boolean bound = false;
        while (!bound) {
            try {
                System.out.println("ExitPollServer: Attempting to bind '" + EXIT_POLL_SERVICE_NAME + "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                registerServiceStub.bind(EXIT_POLL_SERVICE_NAME, exitPoll);
                System.out.println("ExitPollServer: '" + EXIT_POLL_SERVICE_NAME + "' registered successfully.");
                bound = true;
            } catch (AlreadyBoundException e) {
                System.err.println("ExitPollServer: '" + EXIT_POLL_SERVICE_NAME + "' already bound in '" + REGISTER_SERVICE_LOOKUP_NAME + "'. Retrying...");
            } catch (RemoteException e) {
                System.err.println("ExitPollServer: RemoteException during RMI binding of '" + EXIT_POLL_SERVICE_NAME + "': " + e.getMessage());
            }

            if (!bound) {
                System.out.println("ExitPollServer: RMI binding for '" + EXIT_POLL_SERVICE_NAME + "' failed. Retrying in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    System.err.println("ExitPollServer: Sleep interrupted, retrying RMI binding immediately.");
                    Thread.currentThread().interrupt();
                }
            }
        }

        System.out.println("ExitPollServer: Service is now bound and running.");
        
        // Keep server running until shutdown is requested
        while (!shutdownRequested) {
            try {
                Thread.sleep(1000); // Check every second
                try {
                    boolean pollOpen = exitPoll.isOpen();
                    System.out.println("ExitPollServer: Checking exit poll status: " + (pollOpen ? "OPEN" : "CLOSED"));
                    if (!pollOpen) {
                        System.out.println("ExitPollServer: Exit poll is closed.");
                        shutdownRequested = true;
                    }
                } catch (RemoteException e) {
                    System.err.println("ExitPollServer: Error checking exit poll status: " + e.getMessage());
                }
            } catch (InterruptedException e) {
                System.out.println("ExitPollServer: Interrupted, shutting down...");
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("ExitPollServer: Main thread exiting.");
        performShutdown();
    }
    
    /**
     * Perform clean shutdown of the ExitPoll service
     */
    private static void performShutdown() {
        shutdownRequested = false; // Reset shutdown flag
        
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
