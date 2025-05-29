<<<<<<< HEAD
package serverSide.interfaces.ExitPoll;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IExitPoll_Clerk extends Remote {
    void closeIn(int stillVotersInQueue) throws RemoteException;
=======
package clientSide.interfaces.ExitPoll;

public interface IExitPoll_Clerk {
    void closeIn(int stillVotersInQueue);
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
}
