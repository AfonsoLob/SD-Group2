import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// This class implements the remote interface.
// It extends UnicastRemoteObject to make it a remote object.
public class HelloServiceImpl extends UnicastRemoteObject implements HelloService {

    // The constructor must throw RemoteException as it calls the super constructor.
    protected HelloServiceImpl() throws RemoteException {
        super(); // Call the parent constructor to export the object
    }

    // Implement the remote method defined in the interface.
    @Override
    public String sayHello(String name) throws RemoteException {
        System.out.println("Server received call for: " + name);
        return "Hello, " + name + " from the RMI server!";
    }
}
