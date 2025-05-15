package serverSide.interfaces.ExitPoll;

public interface IExitPoll_Voter {
    void exitPollingStation(int voterId, boolean myVote, boolean response);
    boolean isOpen();
}
