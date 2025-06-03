package serverSide.interfaces.PollingStation;

import java.rmi.RemoteException;

/**
 * Interface for checking polling station status from the exit poll
 */
public interface IPollingStation_ExitPoll {
    /**
     * Check if the polling station is open
     * @return true if the polling station is open, false otherwise
     * @throws RemoteException if a remote communication error occurs
     */
    boolean isOpen() throws RemoteException;
}
