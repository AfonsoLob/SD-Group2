package clientSide.interfaces.GUI;

/**
 * Common GUI interface containing general methods needed across components
 */
public interface IGUI_Common {
    float getSimulationSpeed();
    void updateFromLogger(String voteCounts, int votersProcessed, boolean stationOpen);
    void updateQueueAndBoothInfo(int queueCount, String voterInBooth);
    void displayMessage(String message);
    void setSimulationRunning(boolean running);
}
