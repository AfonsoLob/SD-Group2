package serverSide.interfaces.Logger;

<<<<<<< HEAD
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILogger_ExitPoll extends Remote {
    void exitPollVote(int voterId, String vote) throws RemoteException;
=======
public interface ILogger_ExitPoll {
    void exitPollVote(int voterId, String vote);
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
}
