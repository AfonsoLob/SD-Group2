package serverSide.interfaces.Logger;

<<<<<<< HEAD
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Logger interface containing methods needed by the GUI
 */
public interface ILogger_GUI extends Remote {
    // Methods for getting state information needed by the GUI
    String getVoteCounts() throws RemoteException;
    int getVotersProcessed() throws RemoteException;
    boolean isStationOpen() throws RemoteException;
    String getCurrentVoterInBooth() throws RemoteException;
    int getCurrentQueueSize() throws RemoteException;
=======
/**
 * Logger interface containing methods needed by the GUI
 */
public interface ILogger_GUI {
    // Methods for getting state information needed by the GUI
    String getVoteCounts();
    int getVotersProcessed();
    boolean isStationOpen();
    String getCurrentVoterInBooth();
    int getCurrentQueueSize();
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
}
