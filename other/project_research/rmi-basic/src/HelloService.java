import java.rmi.Remote;
import java.rmi.RemoteException;

// This interface defines the methods that can be called remotely.
public interface HelloService extends Remote {

    // This method can be called remotely.
    // It must declare throwing RemoteException.
    String sayHello(String name) throws RemoteException;
}
