package serverSide.interfaces.Logger;

<<<<<<< HEAD
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILogger_PollingStation extends Remote {
    void voterAtDoor(int voterId) throws RemoteException;
    void voterEnteringQueue(int voterId) throws RemoteException;
    void validatingVoter(int voterId, int valid) throws RemoteException;
    void voterInBooth(int voterId, boolean voteA) throws RemoteException;
    void stationOpening() throws RemoteException;
    void stationClosing() throws RemoteException;
=======
public interface ILogger_PollingStation {
    void voterAtDoor(int voterId);
    void voterEnteringQueue(int voterId);
    void validatingVoter(int voterId, int valid);
    void voterInBooth(int voterId, boolean voteA);
    void stationOpening();
    void stationClosing();
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
}
