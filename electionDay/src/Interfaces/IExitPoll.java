package Interfaces;

public interface IExitPoll {
    void exitPollingStation(int voterId, boolean myVote);
    int inquire();
    boolean isOpen();
    void closeIn(int stillVotersInQueue);
    void tryClosingExitPoll();
}
