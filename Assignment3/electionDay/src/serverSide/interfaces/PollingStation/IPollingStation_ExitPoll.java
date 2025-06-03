package serverSide.interfaces.PollingStation;

import java.rmi.RemoteException;


public interface IPollingStation_ExitPoll {

    boolean isOpen() throws RemoteException;
}
