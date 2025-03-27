package Interfaces.Logger;

public interface ILogger_PollingStation {
    void voterAtDoor(int voterId);
    void voterEnteringQueue(int voterId);
    void validatingVoter(int voterId, boolean valid);
    void voterInBooth(int voterId, boolean voteA);
    void stationOpening();
    void stationClosing();
}
