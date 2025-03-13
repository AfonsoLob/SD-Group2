package Interfaces;

public interface IPollingStation {
    boolean enterPollingStation(int voterId);
    int callNextVoter();
    void exitPollingStation(int voterId);

    boolean validateID(int voterId);
    void openPollingStation();
    void closePollingStation();

    boolean isOpen();
}