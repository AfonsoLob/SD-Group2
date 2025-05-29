package serverSide.interfaces.ExitPoll;

<<<<<<< HEAD
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IExitPoll_Pollster extends Remote {
    void inquire() throws RemoteException;
    boolean isOpen() throws RemoteException;
    void printExitPollResults() throws RemoteException;
=======
public interface IExitPoll_Pollster {
    void inquire();
    boolean isOpen();
    void printExitPollResults();
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
}
