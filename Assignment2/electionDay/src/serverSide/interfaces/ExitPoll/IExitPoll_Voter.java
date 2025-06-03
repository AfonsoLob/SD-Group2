package serverSide.interfaces.ExitPoll;

import java.rmi.RemoteException;

public interface IExitPoll_Voter {
    void exitPollingStation(int voterId, boolean myVote, boolean response) throws RemoteException;
    boolean isOpen() throws RemoteException;
}
