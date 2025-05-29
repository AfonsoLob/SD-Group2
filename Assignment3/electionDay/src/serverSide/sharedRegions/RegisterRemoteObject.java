package serverSide.main;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import serverSide.interfaces.Register.IRegister;

/**
 * Implementation of the {@link IRegister} interface.
 * This service acts as a custom RMI registry, allowing other services to bind
 * themselves and be looked up by clients or other services.
 * It itself is bound to the standard RMI registry.
 */
public class RegisterRemoteObject extends UnicastRemoteObject implements IRegister {

    private static final long serialVersionUID = 1L; // Standard for UnicastRemoteObject
    private final HashMap<String, Remote> bindings;
    private static final String RMI_REGISTRY_HOSTNAME = "localhost"; // Or get from config
    private static final int RMI_REGISTRY_PORT = 22350; // Standard RMI registry port used by this project
    public static final String REGISTER_SERVICE_NAME = "RegisterService"; // Name for this service in RMI registry
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds for retry

    /**
     * Constructs a new RegisterRemoteObject.
     * @throws RemoteException if the UnicastRemoteObject constructor fails.
     */
    public RegisterRemoteObject() throws RemoteException {
        super();
        this.bindings = new HashMap<>();
        System.out.println("RegisterRemoteObject: Instance created.");
    }

    @Override
    public synchronized void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException {
        if (bindings.containsKey(name)) {
            throw new AlreadyBoundException("Name '" + name + "' is already bound.");
        }
        bindings.put(name, obj);
        System.out.println("RegisterRemoteObject: Bound '" + name + "'.");
    }

    @Override
    public synchronized Remote lookup(String name) throws RemoteException, NotBoundException {
        Remote obj = bindings.get(name);
        if (obj == null) {
            throw new NotBoundException("Name '" + name + "' is not bound.");
        }
        System.out.println("RegisterRemoteObject: Lookup for '" + name + "' successful.");
        return obj;
    }

    @Override
    public synchronized void unbind(String name) throws RemoteException, NotBoundException {
        if (bindings.remove(name) == null) {
            throw new NotBoundException("Name '" + name + "' is not bound, cannot unbind.");
        }
        System.out.println("RegisterRemoteObject: Unbound '" + name + "'.");
    }

    @Override
    public synchronized String[] list() throws RemoteException {
        System.out.println("RegisterRemoteObject: Listing bindings.");
        return bindings.keySet().toArray(new String[0]);
    }

    public static void main(String[] args) {
        System.out.println("RegisterRemoteObject (Main RMI Registration Service) starting...");

        // Optional: Set RMI security manager if not already handled globally or by rmiregistry itself
        // if (System.getSecurityManager() == null) {
        //     System.setSecurityManager(new SecurityManager());
        //     System.out.println("RegisterRemoteObject: Security Manager installed.");
        // }

        RegisterRemoteObject registerService = null;
        try {
            registerService = new RegisterRemoteObject();
        } catch (RemoteException e) {
            System.err.println("RegisterRemoteObject: CRITICAL - Failed to instantiate RegisterRemoteObject: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // Cannot proceed
        }

        Registry registry = null;
        boolean bound = false;

        while (!bound) {
            try {
                System.out.println("RegisterRemoteObject: Attempting to locate/create RMI Registry at " +
                                   RMI_REGISTRY_HOSTNAME + ":" + RMI_REGISTRY_PORT + "...");
                try {
                    registry = LocateRegistry.getRegistry(RMI_REGISTRY_HOSTNAME, RMI_REGISTRY_PORT);
                    registry.list(); // Test if registry is active
                    System.out.println("RegisterRemoteObject: RMI Registry located.");
                } catch (RemoteException e) {
                    System.out.println("RegisterRemoteObject: RMI Registry not found or not responding. Attempting to create...");
                    registry = LocateRegistry.createRegistry(RMI_REGISTRY_PORT);
                    System.out.println("RegisterRemoteObject: RMI Registry created on port " + RMI_REGISTRY_PORT + ".");
                }

                System.out.println("RegisterRemoteObject: Attempting to bind '" + REGISTER_SERVICE_NAME + "'...");
                registry.bind(REGISTER_SERVICE_NAME, registerService);
                System.out.println("RegisterRemoteObject: '" + REGISTER_SERVICE_NAME + "' bound successfully to RMI Registry.");
                System.out.println("RegisterRemoteObject: Service is now running and ready to accept registrations.");
                bound = true;
            } catch (AlreadyBoundException e) {
                System.err.println("RegisterRemoteObject: '" + REGISTER_SERVICE_NAME + "' already bound in RMI Registry. Assuming another instance is active or shutting down. Retrying...");
                // If another instance is running, this one might wait or you might choose to exit.
                // For simplicity, we retry, assuming the other might unbind.
            } catch (RemoteException e) {
                System.err.println("RegisterRemoteObject: RemoteException during RMI setup with RMI Registry: " + e.getMessage());
                e.printStackTrace(); // More detailed error
                registry = null; // Reset registry in case of connection issues
            }

            if (!bound) {
                System.out.println("RegisterRemoteObject: RMI binding to RMI Registry failed. Retrying in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    System.err.println("RegisterRemoteObject: Sleep interrupted, retrying RMI binding immediately.");
                    Thread.currentThread().interrupt();
                }
            }
        }
        // Server keeps running to serve RMI requests for IRegister
    }
}
