package Interfaces;

public interface IPollingStation {
    boolean enterPollingStation(int voterId);
    void exitPollingStation(int voterId);
    boolean validateID(int voterId);
    void openPollingStation();
    void closePollingStation();
    boolean isOpen();
}