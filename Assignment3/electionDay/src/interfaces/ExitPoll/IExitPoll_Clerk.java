package interfaces.ExitPoll;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IExitPoll_Clerk extends Remote {
    void closeIn(int stillVotersInQueue) throws RemoteException;
    boolean isOpen() throws RemoteException;

}
