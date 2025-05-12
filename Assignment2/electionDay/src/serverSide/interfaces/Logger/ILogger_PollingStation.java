package serverSide.interfaces.Logger;

public interface ILogger_PollingStation {
    void voterAtDoor(int voterId);
    void voterEnteringQueue(int voterId);
    void validatingVoter(int voterId, int valid);
    void voterInBooth(int voterId, boolean voteA);
    void stationOpening();
    void stationClosing();
}
