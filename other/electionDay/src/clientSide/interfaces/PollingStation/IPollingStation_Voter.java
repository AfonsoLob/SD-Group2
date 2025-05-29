package serverSide.interfaces.PollingStation;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPollingStation_Voter extends Remote {

    boolean enterPollingStation(int voterId) throws RemoteException;
    boolean waitIdValidation(int voterId) throws RemoteException;
    void voteA(int voterId) throws RemoteException;
    void voteB(int voterId) throws RemoteException;
    boolean isOpen() throws RemoteException; // Added isOpen if voters need to check this

    int getNumberOfVotersConfigured() throws RemoteException; // Added to get the number of voters configured
}
