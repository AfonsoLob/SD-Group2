package Interfaces;

public interface IExitPoll {
    void exitPollingStation(int voterId, boolean myVote, boolean response);
    void inquire();
    boolean isOpen();
    void closeIn(int stillVotersInQueue);
    void printExitPollResults();
}
