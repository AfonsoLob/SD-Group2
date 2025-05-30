package interfaces.Logger;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILogger_PollingStation extends Remote {
    void voterAtDoor(int voterId) throws RemoteException;
    void voterEnteringQueue(int voterId) throws RemoteException;
    void validatingVoter(int voterId, int valid) throws RemoteException;
    void voterInBooth(int voterId, boolean voteA) throws RemoteException;
    void stationOpening() throws RemoteException;
    void stationClosing() throws RemoteException;
    
    int getPollingStationCapacity() throws RemoteException;
    void logClerkState(String state, String message) throws RemoteException;
    void logVoterState(int voterId, String state, String message) throws RemoteException;
    void logGeneral(String message) throws RemoteException;
    void logResults(String context, int a, int b) throws RemoteException;
    int getNumVoters() throws RemoteException;
}
