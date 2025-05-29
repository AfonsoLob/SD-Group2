
import java.rmi.Naming; // For looking up the object in the registry
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException; // If the name is not found in the registry

public class Client {
    public static void main(String[] args) {
        // The URL to the remote service in the RMI registry.
        // Assumes the server and registry are running on localhost.
        String serviceUrl = "rmi://localhost/HelloService";

        try {
            // Look up the remote object by its URL in the registry.
            // The result is cast to the remote interface type.
            HelloService helloService = (HelloService) Naming.lookup(serviceUrl);

            // Call the remote method! This is the magic of RMI.
            String message = helloService.sayHello("World");

            // Print the result received from the server.
            System.out.println("Received message from server: " + message);

        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
