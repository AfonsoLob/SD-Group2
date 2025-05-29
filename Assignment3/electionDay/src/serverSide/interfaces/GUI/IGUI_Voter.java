package serverSide.interfaces.GUI;

/**
 * GUI interface for voter-related UI updates
 */
public interface IGUI_Voter {
    void voterArrived(int voterId);
    void voterEnteringQueue(int voterId);
    void voterValidated(int voterId, int valid);
    void voterVoting(int voterId, boolean voteA);
    void voterExitPoll(int voterId, String vote);
    void voterReborn(int oldId, int newId);
}
