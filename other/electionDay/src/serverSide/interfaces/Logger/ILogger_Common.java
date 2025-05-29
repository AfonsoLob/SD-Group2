package serverSide.interfaces.Logger;

<<<<<<< HEAD
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Common Logger interface containing general operational methods
 */
public interface ILogger_Common extends Remote { // Added extends Remote
    // File management methods
    void saveCloseFile() throws RemoteException; // Added throws RemoteException
    void clear() throws RemoteException; // Added throws RemoteException
    
    // It's a good place for these common methods if they are used by multiple client types
    // or for general control, ensure they are also in ILogger_all if needed by the LoggerServer directly.
    int getNumVoters() throws RemoteException; // Added throws RemoteException
    void logGeneral(String message) throws RemoteException; // Added throws RemoteException
=======
/**
 * Common Logger interface containing general operational methods
 */
public interface ILogger_Common {
    // File management methods
    void saveCloseFile();
    void clear();
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
}
