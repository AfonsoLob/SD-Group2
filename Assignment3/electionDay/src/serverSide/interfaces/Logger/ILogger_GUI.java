package serverSide.interfaces.Logger;

/**
 * Logger interface containing methods needed by the GUI
 */
public interface ILogger_GUI {
    // Methods for getting state information needed by the GUI
    String getVoteCounts();
    int getVotersProcessed();
    boolean isStationOpen();
    String getCurrentVoterInBooth();
    int getCurrentQueueSize();
}
