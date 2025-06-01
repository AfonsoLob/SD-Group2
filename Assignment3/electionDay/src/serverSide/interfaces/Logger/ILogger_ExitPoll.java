package serverSide.interfaces.Logger;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILogger_ExitPoll extends Remote {
    void exitPollVote(int voterId, String vote) throws RemoteException;
    
    // Configuration methods
    int getExitPollPercentage() throws RemoteException;
    
    // State logging methods
    void logVoterState(int voterId, String state, String message) throws RemoteException;
    void logPollsterState(String state, String message) throws RemoteException;
    void logGeneral(String message) throws RemoteException;
    void logResults(String pollType, int votesA, int votesB) throws RemoteException;

}
