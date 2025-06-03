package serverSide.interfaces.ExitPoll;

import java.rmi.RemoteException;

public interface IExitPoll_Pollster {
    void inquire() throws RemoteException;
    boolean isOpen();
    void printExitPollResults() throws RemoteException;
}
