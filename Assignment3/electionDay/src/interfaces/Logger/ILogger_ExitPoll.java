package interfaces.Logger;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILogger_ExitPoll extends Remote {
    void exitPollVote(int voterId, String vote) throws RemoteException;
    
    int getExitPollPercentage() throws RemoteException;
    void logVoterState(int voterId, String state, String message) throws RemoteException;
    void logPollsterState(String state, String message) throws RemoteException;
    void logGeneral(String message) throws RemoteException;
    void logResults(String context, int a, int b) throws RemoteException;
}
