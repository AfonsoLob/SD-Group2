package Interfaces;

public interface IPollingStation {
    boolean enterPollingStation(int voterId);
    int callNextVoter();
    // void exitPollingStation(int voterId);

    int waitIdValidation(int voterId);
    void sendSignal(boolean response);

    void openPollingStation();
    void closePollingStation();

    boolean isOpen();
    boolean stillVotersInQueue();
}