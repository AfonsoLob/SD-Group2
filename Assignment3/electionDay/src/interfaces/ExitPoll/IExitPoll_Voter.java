package interfaces.ExitPoll;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IExitPoll_Voter extends Remote {
    void exitPollingStation(int voterId, boolean realVote, boolean response) throws RemoteException;
    boolean isOpen() throws RemoteException;

}
