package clientSide.interfaces.ExitPoll;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IExitPoll_Pollster extends Remote {
    void inquire() throws RemoteException;
    boolean isOpen() throws RemoteException;
    void printExitPollResults() throws RemoteException;

}
