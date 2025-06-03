package serverSide.interfaces.ExitPoll;

import java.rmi.RemoteException;

public interface IExitPoll_Clerk {
    void closeIn(int stillVotersInQueue) throws RemoteException;
}
