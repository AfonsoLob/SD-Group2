package Interfaces.GUI;

/**
 * GUI interface for statistics-related UI updates
 */
public interface IGUI_Statistics {
    void updateStats(int validationSuccess, int validationFail, 
                    int pollParticipants, int pollTotal,
                    int pollAccurate, int pollResponses,
                    long avgProcessingTime);
}
