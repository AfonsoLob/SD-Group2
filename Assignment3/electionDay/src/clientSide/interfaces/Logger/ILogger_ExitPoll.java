package serverSide.interfaces.Logger;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILogger_ExitPoll extends Remote {
    void exitPollVote(int voterId, String vote) throws RemoteException;

}
