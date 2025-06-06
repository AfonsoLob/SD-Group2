package interfaces.PollingStation;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPollingStation_Clerk extends Remote {
    boolean callNextVoter() throws RemoteException;
    void openPollingStation() throws RemoteException;
    void closePollingStation() throws RemoteException;
    boolean isOpen() throws RemoteException;
    int numberVotersInQueue() throws RemoteException;
    void printFinalResults() throws RemoteException;
    int getMaxVotes() throws RemoteException;
}
