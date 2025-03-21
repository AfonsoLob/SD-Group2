package Interfaces;

public interface IPollingStation {
    boolean enterPollingStation(int voterId);
    boolean callNextVoter();
    // void exitPollingStation(int voterId);

    boolean waitIdValidation(int voterId);
    void openPollingStation();
    void closePollingStation();

    boolean isOpen();
    // boolean stillVotersInQueue();
    void voteA(int voterId);
    void voteB(int voterId);
    void printFinalResults();

    int numberVotersInQueue();
}