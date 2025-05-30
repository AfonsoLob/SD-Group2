package serverSide.main;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import interfaces.Logger.ILogger_all; // Or the specific ILogger interface MPollingStation needs
import interfaces.PollingStation.IPollingStation_all; // Assuming this is the remote interface for MPollingStation
import interfaces.Register.IRegister;
import serverSide.sharedRegions.MPollingStation;

public class PollingStationServer {

    public static final String RMI_REGISTRY_HOSTNAME = "localhost";
    public static final int RMI_REGISTRY_PORT = 22350;
    public static final String REGISTER_SERVICE_LOOKUP_NAME = "RegisterService"; // To find IRegister
    public static final String LOGGER_SERVICE_NAME = "LoggerService"; // To lookup Logger via IRegister
    public static final String POLLING_STATION_SERVICE_NAME = "PollingStationService"; // To bind MPollingStation via IRegister
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds

    public static void main(String[] args) {
        System.out.println("PollingStationServer starting...");

        IRegister registerServiceStub = null;
        ILogger_all loggerStub = null;
        IPollingStation_all pollingStation = null; // The remote object instance
        MPollingStation mPollingStation = null; // The actual implementation

        // 1. Look up the IRegister service (with retry)
        Registry rmiRegistry = null;
        while (registerServiceStub == null) {
            try {
                System.out.println("PollingStationServer: Attempting to connect to RMI Registry at " +
                                   RMI_REGISTRY_HOSTNAME + ":" + RMI_REGISTRY_PORT +
                                   " to find '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                rmiRegistry = LocateRegistry.getRegistry(RMI_REGISTRY_HOSTNAME, RMI_REGISTRY_PORT);
                registerServiceStub = (IRegister) rmiRegistry.lookup(REGISTER_SERVICE_LOOKUP_NAME);
                System.out.println("PollingStationServer: Successfully connected to '" + REGISTER_SERVICE_LOOKUP_NAME + "'.");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("PollingStationServer: Failed to connect to '" + REGISTER_SERVICE_LOOKUP_NAME +
                                   "' via RMI Registry: " + e.getMessage());
                registerServiceStub = null;
            }
            if (registerServiceStub == null) {
                System.out.println("PollingStationServer: Retrying IRegister lookup in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    System.err.println("PollingStationServer: Sleep interrupted, retrying IRegister lookup immediately.");
                    Thread.currentThread().interrupt();
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
            // Assuming MPollingStation constructor takes the logger stub.
            // And that MPollingStation itself implements IPollingStation_all or can be cast/exported.
            // If MPollingStation needs other parameters (like numVoters), they need to be obtained.
            // For now, assuming it primarily needs the logger.
            // The number of voters might be fetched by MPollingStation from the logger if needed.
            mPollingStation = MPollingStation.getInstance(loggerStub);
            pollingStation = (IPollingStation_all) mPollingStation; // Or export if not extending UnicastRemoteObject directly
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
                System.out.println("PollingStationServer: Attempting to bind '" + POLLING_STATION_SERVICE_NAME +
                                   "' via '" + REGISTER_SERVICE_LOOKUP_NAME + "'...");
                registerServiceStub.bind(POLLING_STATION_SERVICE_NAME, pollingStation);
                System.out.println("PollingStationServer: '" + POLLING_STATION_SERVICE_NAME + "' registered successfully.");
                bound = true;
            } catch (AlreadyBoundException e) {
                System.err.println("PollingStationServer: '" + POLLING_STATION_SERVICE_NAME + "' already bound in '" +
                                   REGISTER_SERVICE_LOOKUP_NAME + "'. Retrying...");
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
        // Server keeps running to serve RMI requests for MPollingStation
    }
}
