package serverSide.interfaces.ExitPoll;

<<<<<<< HEAD
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IExitPoll_Voter extends Remote {
    void exitPollingStation(int voterId, boolean realVote, boolean response) throws RemoteException;
    boolean isOpen() throws RemoteException;
=======
public interface IExitPoll_Voter {
    void exitPollingStation(int voterId, boolean myVote, boolean response);
    boolean isOpen();
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
}
