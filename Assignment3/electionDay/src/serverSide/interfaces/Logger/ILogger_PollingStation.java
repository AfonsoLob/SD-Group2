package serverSide.interfaces.Logger;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILogger_PollingStation extends Remote {
    void voterAtDoor(int voterId) throws RemoteException;
    void voterEnteringQueue(int voterId) throws RemoteException;
    void validatingVoter(int voterId, int valid) throws RemoteException;
    void voterInBooth(int voterId, boolean voteA) throws RemoteException;
    void stationOpening() throws RemoteException;
    void stationClosing() throws RemoteException;
    
    // Configuration methods
    int getPollingStationCapacity() throws RemoteException;
    int getNumberOfVotersConfigured() throws RemoteException;
    int getMaxVotes() throws RemoteException;
    int getTotalVotes() throws RemoteException;
    

}
