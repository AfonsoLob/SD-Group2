package serverSide.interfaces.Logger;


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

}
