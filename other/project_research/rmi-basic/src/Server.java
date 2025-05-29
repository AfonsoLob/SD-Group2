import java.rmi.Naming; // For binding the object in the registry
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry; // Optional: for starting the registry
import java.net.MalformedURLException; // For Naming exceptions

public class Server {
    public static void main(String[] args) {
        try {
            // Optional: Start the RMI registry locally on the default port (1099)
            // If you run rmiregistry from the command line separately, you don't need this line.
            LocateRegistry.createRegistry(1099);

            // Create an instance of the remote object implementation
            HelloService helloService = new HelloServiceImpl();

            // Bind the remote object instance to a name in the RMI registry.
            // Clients will use this name to look up the object.
            // The format is rmi://host:port/name (port 1099 is default if omitted)
            Naming.rebind("HelloService", helloService);

            System.out.println("HelloService is registered and ready.");

        } catch (RemoteException | MalformedURLException e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
