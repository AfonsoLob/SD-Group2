package serverSide.interfaces.Logger;

import java.rmi.RemoteException;

public interface ILogger_ExitPoll {
    void exitPollVote(int voterId, String vote) throws RemoteException;
}
